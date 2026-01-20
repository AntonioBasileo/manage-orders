package it.subito.orders.controller;

import it.subito.orders.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(authService.generateToken(credentials));
    }

    @PostMapping("/register-user")
    public ResponseEntity<String> register(@RequestBody Map<String, String> userData) {
        return ResponseEntity.ok(authService.registerUser(userData));
    }
}
