package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.JwtResponse;
import org.qrflash.DTO.UserLoginRequest;
import org.qrflash.DTO.UserRegistrationRequest;
import org.qrflash.Service.AuthenticationService;
import org.qrflash.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody UserRegistrationRequest request){
        try{
            userService.registerUser(request.getPhoneNumber(), request.getPassword());
            return ResponseEntity.ok("Користувач успішно зареєстрований");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?>login(@RequestBody UserLoginRequest request){
        try{
            String token = authenticationService.authenticateUser(request.getPhoneNumber(), request.getPassword());
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Помилка авторизації: " + e.getMessage());
        }
    }
}
