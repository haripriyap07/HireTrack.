package com.hiretrack.hiretrack.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hiretrack.hiretrack.dto.AuthResponse;
import com.hiretrack.hiretrack.dto.LoginRequest;
import com.hiretrack.hiretrack.entity.User;
import com.hiretrack.hiretrack.repository.UserRepository;
import com.hiretrack.hiretrack.security.JwtService;
import com.hiretrack.hiretrack.util.UserMapper;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
        private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

            String token = jwtService.generateToken(user);
            return new AuthResponse(token, UserMapper.toResponse(user));
        } catch (Exception ex) {
            logger.error("Login failed for {}: {}", request != null ? request.getEmail() : "<null>", ex.getMessage(), ex);
            throw ex;
        }
    }
}
