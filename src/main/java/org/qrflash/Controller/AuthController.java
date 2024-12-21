package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.*;
import org.qrflash.Entity.EstablishmentsEntity;
import org.qrflash.Entity.UserEntity;
import org.qrflash.JWT.JwtUtil;
import org.qrflash.Repository.EstablishmentsRepository;
import org.qrflash.Service.Establishment.EstablishmentsService;
import org.qrflash.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "https://qrflash.online")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EstablishmentsRepository establishmentsRepository;
    private final EstablishmentsService establishmentsService;


    @PostMapping("/register")
    public ResponseEntity<?> createNewUser (@RequestBody RegistrationUserDTO registrationUserDTO) {
        //Перевірна, чи існує користувач із таким номером телефону
        if(userService.findByPhone(registrationUserDTO.getPhoneNumber()).isPresent()){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Користувач з таким іменем вже існує"),
                    HttpStatus.BAD_REQUEST);
        }
        //Створення нового користувача
        UserEntity user = new UserEntity();
        user.setPhoneNumber(registrationUserDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registrationUserDTO.getPassword()));
        userService.createNewUser(user);

        //Генерація токена
        String token = jwtUtil.generateToken(registrationUserDTO.getPhoneNumber());

        //Створення бази даних (закладу) для нового користувача
        String establishmentUuid;
        try{
            System.out.println(getCurrentTimestamp() + " - Створюється база данних для користувача...");
            EstablishmentsEntity establishment = establishmentsService.createEstablishmentForUser(user.getId());
            establishmentUuid = establishment.getUuid().toString();
            System.out.println(getCurrentTimestamp() +" - База успішно створення");
        }catch (Exception e){
            System.err.println(getCurrentTimestamp() +" - Помилка створення бази даних\n " + e);
            return new ResponseEntity<>(
                    new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Не вдалося створити базу даних для закладу"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        JwtResponse response = new JwtResponse(token, establishmentUuid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?>auth(@RequestBody JwtRequest request){
        try{
            System.out.println("Авторизація: " + request.getPhoneNumber());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword())
            );
            System.out.println("Авторизація успішна");
        } catch (BadCredentialsException e) {
            System.out.println("Помилка авторизації");
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Не правильний логін чи пароль"), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = userService.loadUserByUsername(request.getPhoneNumber());
        String token = jwtUtil.generateToken(userDetails.getUsername());

        String establishmentUuid = establishmentsService.getEstablishmentUuidForUser(request.getPhoneNumber());

        JwtResponse response = new JwtResponse(token, establishmentUuid);
        return ResponseEntity.ok(response);
    }

    //Метод для отримання поточного часу
    private String getCurrentTimestamp() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
