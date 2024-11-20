package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.JwtResponse;
import org.qrflash.DTO.UserLoginRequest;
import org.qrflash.DTO.UserRegistrationRequest;
import org.qrflash.Service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "https://qrflash.online", allowCredentials = "true")
@RequestMapping("/auth")
@RequiredArgsConstructor // Lombok анотація для автоматичного створення конструктора
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            authenticationService.registerUser(request);
            return ResponseEntity.ok("Користувач успішно зареєстрований");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            String token = authenticationService.authenticateUser(request);
            System.out.println("Generate token" + token);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

