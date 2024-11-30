package org.qrflash.Service;

import lombok.Data;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Data
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByPhoneNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача з номером " + username + " не знайдено!"));

        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                user.getPassword(),
                user.getIsActive(),
                true,
                true,
                true,
                new ArrayList<>()
        );
    }
}
