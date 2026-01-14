package com.mbclab.lablink.config;

import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class DataSeeder {

    @Bean
    CommandLineRunner initData(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            var existingAdmin = memberRepository.findByUsername("admin");
            
            if (existingAdmin.isEmpty()) {
                // Create new admin
                ResearchAssistant admin = new ResearchAssistant();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Administrator");
                admin.setRole("ADMIN");
                admin.setExpertDivision("CROSS_DIVISION");
                admin.setDepartment("INTERNAL");
                admin.setActive(true);
                admin.setPasswordChanged(true);
                
                memberRepository.save(admin);
                log.info("✅ Admin user created: username=admin, password=admin123");
            } else {
                // Update existing admin password
                ResearchAssistant admin = existingAdmin.get();
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                memberRepository.save(admin);
                log.info("✅ Admin password updated to: admin123");
            }
        };
    }
}
