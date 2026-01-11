package com.mbclab.lablink.features.auth;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // Wajib: agar id/createdAt dari BaseEntity ikut dicek
@MappedSuperclass
public abstract class AppUser extends BaseEntity {

    // --- 1. LOGIN CREDENTIALS ---
    
    @Column(nullable = false, unique = true)
    protected String username;
    
    @Column(nullable = false)
    protected String password;
    
    @Column(nullable = false)
    protected String role;
    
    // --- 2. IDENTITY ---
    
    @Column(nullable = false)
    protected String fullName;
    
    // --- 3. SECURITY LOGIC ---
    
    @Column(nullable = false)
    protected boolean isPasswordChanged = false;
    // False = User baru (Password == NIM), Wajib Redirect ke Ganti Password
    // True  = User aktif, Boleh masuk Dashboard
}