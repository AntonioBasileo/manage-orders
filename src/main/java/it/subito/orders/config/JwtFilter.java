package it.subito.orders.config;

import it.subito.orders.utility.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro per la validazione dei token JWT nelle richieste HTTP.
 * <p>
 * Questa classe intercetta ogni richiesta, verifica la presenza e la validità
 * del token JWT nell'header Authorization e, se valido, imposta l'autenticazione
 * nel contesto di sicurezza di Spring.
 * </p>
 *
 * <ul>
 *   <li>Estende {@link OncePerRequestFilter} per garantire l'esecuzione una sola volta per richiesta.</li>
 *   <li>Utilizza {@link JwtUtil} per la validazione e l'estrazione delle informazioni dal token.</li>
 *   <li>Imposta l'utente autenticato e i relativi ruoli nel {@link SecurityContextHolder}.</li>
 * </ul>
 *
 * @author antonio-basileo_Alten
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    /**
     * Intercetta la richiesta HTTP, valida il token JWT e imposta l'autenticazione.
     *
     * @param request  la richiesta HTTP
     * @param response la risposta HTTP
     * @param chain    la catena dei filtri
     * @throws ServletException in caso di errore nella servlet
     * @throws IOException      in caso di errore di I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);

            return;
        }

        try {
            String token = header.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                Set<String> roles = jwtUtil.extractRoles(token);

                var authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
