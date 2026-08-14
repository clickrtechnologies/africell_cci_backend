package com.africell.cci_backend.Service;

import com.africell.cci_backend.dto.LoginRequest;
import com.africell.cci_backend.dto.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    public LoginResponse login(LoginRequest request) {
        if (USERNAME.equals(request.getUsername()) && PASSWORD.equals(request.getPassword())) {
            return new LoginResponse("Login successful");
        }

        return new LoginResponse("Invalid username or password");
    }

}
