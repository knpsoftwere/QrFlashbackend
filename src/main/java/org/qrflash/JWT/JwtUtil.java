package org.qrflash.JWT;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String generateToken(String phoneNumber) {
        return Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                //Налаштування токена на 10 годин
                //.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                //Налаштування токена на 15 хв
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 3))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String getUserPhoneNumber(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }


    public boolean isValidToken(String token) {
        try {
            // Перевірка, чи токен є дійсним
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("Токен протерміновано: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("Невірний підпис токена: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Помилка під час перевірки токена: " + e.getMessage());
        }
        return false;
    }
}
