package com.mbclab.lablink.features.auth;

import com.mbclab.lablink.features.auth.dto.ChangePasswordRequest;
import com.mbclab.lablink.features.auth.dto.LoginRequest;
import com.mbclab.lablink.features.auth.dto.LoginResponse;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;
import com.mbclab.lablink.features.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse.UserInfo> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token tidak valid");
        }
        
        String token = authHeader.substring(7);
        var user = authService.validateToken(token);
        
        return ResponseEntity.ok(LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isPasswordChanged(user.isPasswordChanged())
                .build());
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token tidak valid");
        }
        
        String token = authHeader.substring(7);
        var user = authService.validateToken(token);
        
        authService.changePassword(user.getUsername(), request);
        
        return ResponseEntity.ok(java.util.Map.of("message", "Password berhasil diubah"));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateMemberRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token tidak valid");
        }
        
        String token = authHeader.substring(7);
        var user = authService.validateToken(token);
        
        // User can only update their own profile
        memberService.updateMember(user.getId(), request);
        
        return ResponseEntity.ok(java.util.Map.of("message", "Profile berhasil diperbarui"));
    }
}
