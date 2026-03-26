package com.app.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.model.User;
import com.app.security.JwtUtil;
import com.app.service.AuthService;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Optional<User> user = authService.findUserByEmail(loginRequest.getEmail());

            if (user.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User foundUser = user.get();
            if (!authService.verifyPassword(loginRequest.getPassword(), foundUser.getPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(foundUser.getId(), foundUser.getEmail());

            // Clear password before sending response
            foundUser.setPassword("");

            LoginResponse loginResponse = new LoginResponse(foundUser, token);
            return ResponseEntity.status(HttpStatus.OK).body(loginResponse);

        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> profile() {
        try {
            String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getDetails();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Protected profile");
            response.put("userId", userId);
            response.put("email", email);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            logger.error("Profile error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
