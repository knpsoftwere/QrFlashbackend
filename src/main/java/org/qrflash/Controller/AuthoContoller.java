package org.qrflash.Controller;

import org.qrflash.DTO.JwtResponse;
import org.qrflash.DTO.UserLoginRequest;
import org.qrflash.DTO.UserRegistrationRequest;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Moduls.JwtUtil;
import org.qrflash.Service.TokenService;
import org.qrflash.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthoContoller {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request){
        try {
            UserEntity userEntity = userService.registerUser(request.getPhoneNumber(), request.getPassword());
            return ResponseEntity.ok("Користувач успішно зареєструвався");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request){
        try{
            UserEntity userEntity = userService.authenticateUser(request.getPhoneNumber(), request.getPassword());
            String token = jwtUtil.generateToken(userEntity.getId());
            String refreshToken = jwtUtil.generateRefreshToken(userEntity.getId()); // генеруємо refreshToken, якщо необхідно
            Date expiration = jwtUtil.getExpirationDate(token);

            // Зберігаємо токен в базу даних
            tokenService.saveToken(userEntity.getId(), token, refreshToken, expiration);
            return ResponseEntity.ok(new JwtResponse(token));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
