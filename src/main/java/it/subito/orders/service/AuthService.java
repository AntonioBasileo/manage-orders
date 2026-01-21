package it.subito.orders.service;

import it.subito.orders.entity.AppUser;
import it.subito.orders.repository.UserRepository;
import it.subito.orders.utility.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dell'autenticazione e della registrazione utenti.
 * <p>
 * Questa classe fornisce metodi per generare token JWT, registrare nuovi utenti
 * e ottenere l'utente autenticato dal contesto di sicurezza.
 * </p>
 *
 * <ul>
 *   <li>Utilizza {@link AuthenticationManager} per autenticare le credenziali.</li>
 *   <li>Codifica le password tramite {@link PasswordEncoder}.</li>
 *   <li>Gestisce la persistenza degli utenti tramite {@link UserRepository}.</li>
 *   <li>Genera token JWT tramite {@link JwtUtil}.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    /**
     * Genera un token JWT a partire dalle credenziali fornite.
     *
     * @param credentials mappa contenente username e password
     * @return mappa con il token JWT generato
     */
    public Map<String, String> generateToken(Map<String, String> credentials) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.get("username"), credentials.get("password"))
        );

        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Map.of("token", jwtUtil.generateToken(auth.getName(), roles));
    }

    /**
     * Registra un nuovo utente con ruolo di default.
     *
     * @param userData mappa contenente username e password
     * @return messaggio di conferma della registrazione
     * @throws IllegalArgumentException se lo username è già esistente
     */
    public String registerUser(Map<String, String> userData) {
        String username = userData.get("username");
        String rawPassword = userData.get("password");

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username già esistente");
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRoles(Set.of("ROLE_USER"));
        userRepository.save(newUser);

        return "Registrazione avvenuta con successo";
    }

    /**
     * Restituisce l'utente autenticato dal contesto di sicurezza.
     *
     * @return l'utente autenticato
     * @throws UsernameNotFoundException se l'utente non viene trovato
     */
    public AppUser getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return null;

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Utente con username %s non trovato", username)));
    }
}
