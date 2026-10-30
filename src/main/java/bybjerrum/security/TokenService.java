
package bybjerrum.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TokenService {

    private final Algorithm algorithm;

    public TokenService() {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is missing"
            );
        }
        algorithm = Algorithm.HMAC256(secret);
    }

    public TokenService(String secret) {
        algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();

        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(now)
                .withExpiresAt(
                        now.plus(2, ChronoUnit.HOURS)
                )
                .sign(algorithm);
    }

    public Long verifyToken(String token) {

        DecodedJWT decoded = JWT.require(algorithm)
                .build()
                .verify(token);

        return Long.valueOf(decoded.getSubject());
    }

    public void validateToken(String token) {
        verifyToken(token);
    }
}