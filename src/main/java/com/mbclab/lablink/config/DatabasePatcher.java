package com.mbclab.lablink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabasePatcher implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabasePatcher.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking database schema patches...");
        
        try {
            // Fix missing approval_status in events table
            logger.info("Patching 'events' table: ensuring 'approval_status' column exists...");
            jdbcTemplate.execute("ALTER TABLE events ADD COLUMN IF NOT EXISTS approval_status VARCHAR(255) DEFAULT 'PENDING'");
            jdbcTemplate.execute("UPDATE events SET approval_status = 'PENDING' WHERE approval_status IS NULL");
            logger.info("Patch 'events' table applied successfully.");
            
        } catch (Exception e) {
            logger.error("Failed to apply database patches", e);
        }
    }
}
