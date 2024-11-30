package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.*;
import org.qrflash.Entity.UserEntity;
import org.qrflash.JWT.JwtUtil;
import org.qrflash.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<?> createNewUser (@RequestBody RegistrationUserDTO registrationUserDTO) {
        if(userService.findByPhone(registrationUserDTO.getPhoneNumber()).isPresent()){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Користувач з таким іменем вже існує"), HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();

        user.setPhoneNumber(registrationUserDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registrationUserDTO.getPassword()));
        userService.createNewUser(user);
        String token = jwtUtil.generateToken(registrationUserDTO.getPhoneNumber());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?>create(@RequestBody JwtRequest request){
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
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
