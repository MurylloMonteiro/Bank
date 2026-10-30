package git.MurylloMonteiro.Ratbank.Service.Jwt;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${ratbank.jwt.secret:ratbank-dev-only-change-me-secret-key-32}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String generateToken(UUID username) {
        return Jwts.builder()
                .issuer("Ratbank")
                .subject(username.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 240_000_000))
                .signWith(key)
                .compact();
    }

    public String validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
