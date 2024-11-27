package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(String phoneNumber, String password) {
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Користувач із таким номером вже існує");
        }

        UserEntity newUser = new UserEntity();
        newUser.setPhoneNumber(phoneNumber);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setIsActive(true);

        userRepository.save(newUser);
    }
}
