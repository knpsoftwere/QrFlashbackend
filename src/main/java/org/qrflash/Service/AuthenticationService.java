package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.UserLoginRequest;
import org.qrflash.DTO.UserRegistrationRequest;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Moduls.JwtUtil;
import org.qrflash.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Lombok анотація для автоматичного створення конструктора
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public void registerUser(UserRegistrationRequest request) {
        userService.registerUser(request.getPhoneNumber(), request.getPassword());
    }

    public String authenticateUser(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword())
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(user.getUsername());
    }
}

