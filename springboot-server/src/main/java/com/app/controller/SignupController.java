package com.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.SignupRequest;
import com.app.model.User;
import com.app.service.SignupService;

@RestController
@RequestMapping("/signup")
@CrossOrigin(origins = "*")
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    private SignupService signupService;

    @PostMapping
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            // Check if user already exists
            if (signupService.userExists(signupRequest.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Create new user
            User user = new User();
            user.setName(signupRequest.getName());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(signupRequest.getPassword());
            user.setPhonenumber(signupRequest.getPhonenumber());

            User savedUser = signupService.registerUser(user);
            
            // Clear password before sending response
            savedUser.setPassword("");

            return ResponseEntity.status(HttpStatus.OK).body(savedUser);

        } catch (Exception e) {
            logger.error("Signup error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = signupService.getAllUsers();
            
            // Clear passwords before sending response
            users.forEach(user -> user.setPassword(""));

            return ResponseEntity.status(HttpStatus.OK).body(users);

        } catch (Exception e) {
            logger.error("Get users error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
