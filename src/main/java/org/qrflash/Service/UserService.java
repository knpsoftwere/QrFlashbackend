package org.qrflash.Service;

import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity registerUser(String phoneNumber, String password) {
        Optional<UserEntity> existingUser = userRepository.findByPhoneNumber(phoneNumber);
        if(existingUser.isPresent()) {
            throw new RuntimeException("Користувач за цим номером вже існує");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setPhoneNumber(phoneNumber);

        // Генеруємо salt і зберігаємо його окремо
        String salt = passwordEncoder.encode(phoneNumber + System.currentTimeMillis()); // унікальний salt
        userEntity.setSalt(salt);

        // Генеруємо хеш пароля з доданим salt
        String passwordHash = passwordEncoder.encode(password + salt);
        userEntity.setPasswordHash(passwordHash);

        System.out.println("Збережений salt: " + salt);
        System.out.println("Збережений хеш пароля: " + passwordHash);

        return userRepository.save(userEntity);
    }

    public UserEntity authenticateUser(String phoneNumber, String password) {
        UserEntity userEntity = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        // Використовуємо збережений salt для хешування введеного пароля
        String hashedPassword = passwordEncoder.encode(password + userEntity.getSalt());

        // Порівнюємо збережений хеш із хешем введеного пароля
        if (!passwordEncoder.matches(password + userEntity.getSalt(), userEntity.getPasswordHash())) {
            throw new RuntimeException("Не правильний пароль");
        }


        System.out.println("Salt користувача: " + userEntity.getSalt());
        System.out.println("Хеш для перевірки логіну: " + hashedPassword);

        return userEntity;
    }

}
