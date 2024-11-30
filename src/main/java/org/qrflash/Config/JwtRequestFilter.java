package org.qrflash.Config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qrflash.JWT.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String UserNumberPhone = null;
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                UserNumberPhone = jwtUtil.getUserPhoneNumber(jwtToken);
            } catch (ExpiredJwtException e) {
                log.debug("Час токену завершився.");
            }catch (SignatureException e) {
                log.debug("Підпис не правильний");
            }
        }

        if (UserNumberPhone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(UserNumberPhone, null, null);
            SecurityContextHolder.getContext().setAuthentication(token);
        }
        filterChain.doFilter(request, response);
    }
}
