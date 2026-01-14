package com.mbclab.lablink.features.auth;

import com.mbclab.lablink.features.auth.dto.LoginRequest;
import com.mbclab.lablink.features.auth.dto.LoginResponse;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // 1. Find user by username
        ResearchAssistant user = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username atau password salah"));

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Username atau password salah");
        }

        // 3. Generate JWT token
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole(),
                user.getId()
        );

        // 4. Build response
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .isPasswordChanged(user.isPasswordChanged())
                        .build())
                .build();
    }

    public ResearchAssistant validateToken(String token) {
        String username = jwtService.extractUsername(token);
        ResearchAssistant user = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        
        if (!jwtService.isTokenValid(token, username)) {
            throw new RuntimeException("Token tidak valid");
        }
        
        return user;
    }
}
