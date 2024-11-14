package org.qrflash.Service;

import org.qrflash.Entity.TokenEntity;
import org.qrflash.Repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    public void saveToken(Long userId, String token, String refreshToken, Date expiration) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setExpirationToken(expiration);
        tokenRepository.save(tokenEntity);
    }
}
