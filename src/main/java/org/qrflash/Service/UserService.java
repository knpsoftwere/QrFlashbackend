package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Qualifier("securityPasswordEncoder")
    private final BCryptPasswordEncoder passwordEncoder;

    public void registerUser(String phoneNumber, String password) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Користувач за таким номером вже існує");
        }

        UserEntity user = new UserEntity();
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password)); // Використовуємо passwordHash
        userRepository.save(user);
    }


    public UserEntity findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Користувач не знайдений"));
    }
}
