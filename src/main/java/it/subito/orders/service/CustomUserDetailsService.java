package it.subito.orders.service;

import it.subito.orders.entity.AppUser;
import it.subito.orders.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Servizio personalizzato per il recupero dei dettagli utente in fase di autenticazione.
 * <p>
 * Implementa {@link UserDetailsService} per caricare le informazioni dell'utente
 * dal repository e convertirle in un oggetto {@link UserDetails} compatibile con Spring Security.
 * </p>
 *
 * <ul>
 *   <li>Recupera l'utente tramite {@link UserRepository} usando lo username.</li>
 *   <li>Converte i ruoli dell'utente in {@link SimpleGrantedAuthority} per la gestione delle autorizzazioni.</li>
 *   <li>Lancia {@link UsernameNotFoundException} se l'utente non viene trovato.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    /**
     * Carica i dettagli dell'utente dato lo username.
     *
     * @param username lo username dell'utente da cercare
     * @return i dettagli dell'utente per Spring Security
     * @throws UsernameNotFoundException se l'utente non viene trovato
     */
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Utente con username %s non trovato", username)));

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .authorities(appUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_".concat(role)))
                        .collect(Collectors.toList()))
                .build();
    }
}
