package ru.bookstore.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import ru.bookstore.exceptions.JwtAuthException;

public class JwtUtils {
  private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
      "my-very-secure-key-256-bits-long-1234567890".getBytes()
  );
  private static final MacAlgorithm ALGORITHM = Jwts.SIG.HS256;
  private static final long EXPIRATION_TIME_MS = 1000*60*60*24;

  public static String generateToken(String username, String role) {
    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusMillis(EXPIRATION_TIME_MS)))
        .signWith(SECRET_KEY, ALGORITHM)
        .compact();
  }

  public static boolean validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(SECRET_KEY)
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtAuthException(e.getMessage());
    }
  }

  public static String getUsernameFromToken(String token) {
    return Jwts.parser()
        .verifyWith(SECRET_KEY)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
