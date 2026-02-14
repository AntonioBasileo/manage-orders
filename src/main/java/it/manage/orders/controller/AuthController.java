package it.manage.orders.controller;

import it.auth.security.starter.service.AuthService;
import it.auth.security.core.dto.LoginRequestDTO;
import it.auth.security.core.dto.RegisterRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Contract;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    @Contract(value = "null -> fail; !null -> !null")
    @GetMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequestDTO credentials) {
        return ResponseEntity.ok(authService.generateToken(credentials));
    }

    @Contract(value = "null -> fail; !null -> !null")
    @PostMapping(value = "/register-user", consumes = "application/json", produces = "application/text")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO userData) {
        return ResponseEntity.ok(authService.registerUser(userData));
    }
}
