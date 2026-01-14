package com.mbclab.lablink.features.activitylog;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Listener untuk AuditEvent.
 * Berjalan async agar tidak menghambat proses utama.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final ActivityLogRepository activityLogRepository;

    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        try {
            ActivityLog activityLog = new ActivityLog();
            activityLog.setAction(event.getAction());
            activityLog.setTargetType(event.getTargetType());
            activityLog.setTargetId(event.getTargetId());
            activityLog.setTargetName(event.getTargetName());
            activityLog.setDescription(event.getDescription());
            
            // Get current user from SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                activityLog.setUserName(auth.getName());
                // Note: userId would come from UserDetails if we have custom implementation
            }
            
            // Get IP address from request
            String ipAddress = getClientIpAddress();
            activityLog.setIpAddress(ipAddress);
            
            activityLogRepository.save(activityLog);
            
            log.debug("Activity logged: {} {} {} by {}", 
                    event.getAction(), event.getTargetType(), event.getTargetId(), activityLog.getUserName());
                    
        } catch (Exception e) {
            // Log error but don't throw - we don't want logging to break the main flow
            log.error("Failed to save activity log: {}", e.getMessage());
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.debug("Could not get client IP: {}", e.getMessage());
        }
        return null;
    }
}
