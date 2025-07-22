package com.datashift.datashift_v2.controller.users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datashift.datashift_v2.dto.users.UserDTO;
import com.datashift.datashift_v2.jwtutil.JwtUtil;
import com.datashift.datashift_v2.entity.users.UserEntity;
import com.datashift.datashift_v2.service.users.UserService;
import com.datashift.datashift_v2.io.AuthRequest;
import com.datashift.datashift_v2.io.AuthResponse;
import com.datashift.datashift_v2.io.RegisterRequest;
import com.datashift.datashift_v2.service.users.TokenBlacklistService;

import org.springframework.web.bind.annotation.RequestBody;
import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final ModelMapper modelMapper;

    private final AuthenticationManager authenticationManager;

    private final TokenBlacklistService tokenBlacklistService;

    // @CrossOrigin(origins =
    // "http://todoapp-front-end.s3-website-us-east-1.amazonaws.com")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public AuthResponse registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("API /register is called for user: {}", registerRequest.getName());
        UserDTO userDTO = modelMapper.map(registerRequest, UserDTO.class);
        UserEntity registeredUser = userService.createUser(userDTO);
        log.info("User created successfully: {}", registeredUser.getEmail());

        final String token = jwtUtil.generateToken(registeredUser);

        return AuthResponse.builder()
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .role(registeredUser.getRole())
                .token(token)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody AuthRequest authRequest) {
        log.info("API /login is called for user: {}", authRequest.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: Invalid credentials", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserEntity authenticatedUser = userService.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        final String token = jwtUtil.generateToken(authenticatedUser);

        AuthResponse authResponse = AuthResponse.builder()
                .id(authenticatedUser.getId())
                .name(authenticatedUser.getName())
                .role(authenticatedUser.getRole())
                .token(token)
                .build();
        log.info("User {} logged in successfully.", authRequest.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        log.info("API /logout is called");
        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            if (jwtToken != null && !tokenBlacklistService.isTokenBlacklisted(jwtToken)) {
                tokenBlacklistService.addTokenToBlacklist(jwtToken);
                log.info("Token blacklisted successfully for logout.");
                return ResponseEntity.ok("Logged out successfully.");
            }
        }
        log.warn("Logout attempt with no valid token or token already blacklisted/invalid.");
        return ResponseEntity.badRequest().body("Logout failed: No valid token provided or token already invalidated.");
    }
}
