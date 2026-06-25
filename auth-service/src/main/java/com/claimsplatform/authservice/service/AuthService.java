package com.claimsplatform.authservice.service;

import com.claimsplatform.authservice.entity.User;
import com.claimsplatform.authservice.repository.UserRepository;
import com.claimsplatform.common.dto.AuthRequest;
import com.claimsplatform.common.dto.AuthResponse;
import com.claimsplatform.common.dto.RegisterRequest;
import com.claimsplatform.common.enums.UserRole;
import com.claimsplatform.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserRole role = request.getRole() != null
                ? UserRole.valueOf(request.getRole().toUpperCase())
                : UserRole.POLICYHOLDER;

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
        );

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), claims);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400)
                .role(user.getRole().name())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .build();
    }
}
