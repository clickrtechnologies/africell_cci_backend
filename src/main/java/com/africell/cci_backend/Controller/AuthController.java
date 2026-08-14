package com.africell.cci_backend.Controller;

import com.africell.cci_backend.dto.LoginRequest;
import com.africell.cci_backend.dto.LoginResponse;
import com.africell.cci_backend.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authservice) {
        this.authService = authservice;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        if ("Invalid username or password".equals(response.getMessage())) {
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(response);
    }

}
