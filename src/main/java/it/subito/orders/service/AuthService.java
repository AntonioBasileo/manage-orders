package it.subito.orders.service;

import it.subito.orders.entity.AppUser;
import it.subito.orders.repository.UserRepository;
import it.subito.orders.utility.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    public Map<String, String> generateToken(Map<String, String> credentials) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.get("username"), credentials.get("password"))
        );

        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Map.of("token", jwtUtil.generateToken(auth.getName(), roles));
    }

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
}
