package org.qrflash.Moduls;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component()
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Long userId) {
        return Jwts.builder()
//                Унікальний ID токена
                .setId(UUID.randomUUID().toString())
//                Айді користувача
                .setSubject(String.valueOf(userId))
//               Дата створення токена
                .setIssuedAt(new Date())
//               Термін дії токена (10 днів)
                .setExpiration(new Date(System.currentTimeMillis() + 864_000_000))
//               Алгоритс підпису HS512 і підписується секретним ключем
                .signWith(SignatureAlgorithm.HS512, secret + generateSalt())
                .compact();
    }
//    Отримуємо дату закінчення терміну токена
    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret + generateSalt())
//               Розбиває отриманий токен підтягнутим ключем
                .parseClaimsJws(token)
//                Отримує дату закінчення токену
                .getBody();
        return claims.getExpiration();
    }

//    Аналогічна генерація токену але вже на 30 днів
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 днів
                .signWith(SignatureAlgorithm.HS512, secret + generateSalt())
                .compact();
    }

    private String generateSalt() {
//        Генерація salt, простий варіант
        return UUID.randomUUID().toString().replace("-","");
    }


}
