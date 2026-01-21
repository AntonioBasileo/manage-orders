package it.subito.orders.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility per la gestione dei token JWT nell'applicazione.
 * <p>
 * Questa classe fornisce metodi per generare, validare e estrarre informazioni dai token JWT,
 * utilizzando una chiave segreta e una scadenza predefinita.
 * </p>
 *
 * <ul>
 *   <li>Genera token JWT con username e ruoli.</li>
 *   <li>Estrae username e ruoli da un token JWT.</li>
 *   <li>Valida la firma e la scadenza di un token JWT.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Component
@Qualifier("jwtUtil")
public class JwtUtil {

    private static final String SECRET = "una_chiave_segreta_molto_lunga_e_sicura_da_almeno_32_caratteri";
    private static final long EXPIRATION = 86400000; // 1 giorno


    /**
     * Genera un token JWT contenente username e ruoli.
     *
     * @param username nome utente
     * @param roles insieme di ruoli associati all'utente
     * @return token JWT generato
     */
    public String generateToken(String username, Set<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Estrae lo username dal token JWT.
     *
     * @param token il token JWT
     * @return lo username estratto
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Estrae i ruoli dal token JWT.
     *
     * @param token il token JWT
     * @return insieme di ruoli estratti
     */
    public Set<String> extractRoles(String token) {
        Object rolesObject = extractAllClaims(token).get("roles");

        if (rolesObject instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toUnmodifiableSet());
        }

        return Set.of();
    }

    /**
     * Verifica la validità del token JWT (firma e scadenza).
     *
     * @param token il token JWT da validare
     * @return true se il token è valido, false altrimenti
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Estrae tutti i claims dal token JWT.
     *
     * @param token il token JWT
     * @return claims estratti dal token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Restituisce la chiave segreta per la firma dei token JWT.
     *
     * @return chiave segreta
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
