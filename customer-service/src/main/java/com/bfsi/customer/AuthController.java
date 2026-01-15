package com.bfsi.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public LoginResponse(String token) {
            this.token = token;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (!"admin".equals(req.username) || !"password".equals(req.password)) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // For now everyone gets USER role
        String token = jwtService.generateToken(req.username, List.of("ROLE_USER"));
        return ResponseEntity.ok(new LoginResponse(token));
    }
}