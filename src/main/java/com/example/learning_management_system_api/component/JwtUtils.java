package com.example.learning_management_system_api.component;

import static io.jsonwebtoken.Jwts.parserBuilder;

import com.example.learning_management_system_api.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  @Value("${security.jwt.secret-key}")
  private String jwtSecret;

  @Value("${security.jwt.expiration-time}")
  private Long expirationTime;

  public String generateAccessToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("id", user.getId())
        .claim("role", user.getRole())
        .claim("fullname", user.getFullname())
        .setIssuedAt(new Date())
        .setExpiration(new Date((System.currentTimeMillis() + expirationTime)))
        .signWith(key())
        .compact();
  }

  public String generateRefreshToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("id", user.getId())
        .claim("role", user.getRole())
        .claim("fullname", user.getFullname())
        .setIssuedAt(new Date())
        .setExpiration(new Date((System.currentTimeMillis() + expirationTime * 30)))
        .signWith(key())
        .compact();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public String getUsernameFromToken(String token) {
    return parserBuilder()
        .setSigningKey(key())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException
        | UnsupportedJwtException
        | MalformedJwtException
        | IllegalArgumentException e) {
      throw new JwtException(e.getMessage());
    }
  }
}
