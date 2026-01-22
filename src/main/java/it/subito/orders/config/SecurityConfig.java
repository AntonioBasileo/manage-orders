package it.subito.orders.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configurazione della sicurezza per l'applicazione Spring Boot.
 * <p>
 * Questa classe definisce la catena dei filtri di sicurezza, la gestione CORS,
 * l'encoder delle password e il manager di autenticazione.
 * </p>
 *
 * <ul>
 *   <li>Configura le regole di accesso alle API e gestisce le eccezioni di autenticazione.</li>
 *   <li>Applica il filtro JWT per la validazione dei token nelle richieste.</li>
 *   <li>Imposta le policy CORS per endpoint pubblici e autenticati.</li>
 *   <li>Espone i bean per l'encoder delle password e il manager di autenticazione.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;


    /**
     * Configura la catena dei filtri di sicurezza HTTP.
     *
     * @param http configurazione di sicurezza HTTP
     * @return la catena dei filtri configurata
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("text/plain; charset=UTF-8");
                            response.getWriter().write("Errore Autenticazione: " + authException.getMessage());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("text/plain; charset=UTF-8");
                            response.getWriter().write("Accesso negato: permessi insufficienti");
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Rimuove il prefisso di default "ROLE_" usato da Spring quando valuta i ruoli
     * (es. @RolesAllowed, hasRole, ecc.). In questo modo @RolesAllowed("ROLE_USER")
     * controllerà esattamente l'authority "ROLE_USER" e non "ROLE_ROLE_USER".
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    /**
     * Configura le policy CORS per gli endpoint pubblici e autenticati.
     *
     * @return la sorgente di configurazione CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration publicConfiguration = new CorsConfiguration();
        publicConfiguration.setAllowedOrigins(List.of("*"));
        publicConfiguration.setAllowedHeaders(List.of("*"));
        publicConfiguration.setAllowedMethods(List.of("POST"));

        CorsConfiguration authenticatedConfiguration = new CorsConfiguration();
        publicConfiguration.setAllowedOrigins(List.of("*"));
        publicConfiguration.setAllowedHeaders(List.of("*"));
        publicConfiguration.setExposedHeaders(List.of("Authorization")); // Importante per JWT
        publicConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/auth/**", publicConfiguration);
        source.registerCorsConfiguration("/api/**", authenticatedConfiguration);

        return source;
    }

    /**
     * Espone il manager di autenticazione.
     *
     * @param config configurazione di autenticazione
     * @return il manager di autenticazione
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
