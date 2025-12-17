package com.tcs.controller;

import com.tcs.model.AuthRequest;
import com.tcs.model.AuthResponse;
import com.tcs.util.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

	
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {

        // 🔥 THIS LINE DOES REAL AUTHENTICATION
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        // ✅ Token ONLY if auth succeeds
        String token = jwtUtil.generateToken(authentication.getName());

        return new AuthResponse(token);
    }
}
