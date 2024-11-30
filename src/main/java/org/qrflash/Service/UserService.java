package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

//    Пошук користувача в базі
    public Optional<UserEntity> findByPhone(String userPhone) {
        return userRepository.findByPhoneNumber(userPhone);
    }

//    Переназначаємо метод UserDetailsService для пошуку користувача
    @Override
    public UserDetails loadUserByUsername(String userPhone) throws UsernameNotFoundException {
        UserEntity user = findByPhone(userPhone).orElseThrow(() -> new UsernameNotFoundException(
                String.format("Користувач %s не знайдений", userPhone)
        ));
        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                user.getPassword(),
                user.getIsActive(),
                true,
                true,
                true,
                List.of()
        );
    }

    public void createNewUser(UserEntity user) {
        userRepository.save(user);
    }

}
