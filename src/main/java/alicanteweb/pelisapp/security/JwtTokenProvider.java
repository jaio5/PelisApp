package alicanteweb.pelisapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessValidityMs;
    private final long refreshValidityMs;

    public JwtTokenProvider(@Value("${app.jwt.secret:changeit123456789012345678901234}") String secret,
                            @Value("${app.jwt.access-expiration-ms:86400000}") long accessValidityMs,
                            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
    }

    public String createAccessToken(String username, Set<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessValidityMs);

        String rolesClaim = String.join(",", roles);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesClaim)
                .claim("typ", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValidityMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("typ", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public Set<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Object roles = claims.get("roles");
        if (roles == null) return new HashSet<>();
        return Arrays.stream(roles.toString().split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    public boolean isRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Object typ = claims.get("typ");
        return "refresh".equals(typ);
    }

    public long getExpiryMillis(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Date exp = claims.getExpiration();
        return exp == null ? 0L : exp.getTime();
    }

    /**
     * Crea un token de confirmación de email
     */
    public String createConfirmationToken(String username, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .claim("typ", "confirmation")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
