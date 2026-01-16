package com.mbclab.lablink.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter implements Filter {

    public SimpleCorsFilter() {
        System.out.println("🔥 SimpleCorsFilter Loaded! Siap menjaga gerbang.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // DEBUG LOG: Cek apakah request masuk?
        System.out.println("👉 Request masuk: " + request.getMethod() + " ke " + request.getRequestURI());

        try {
            String origin = request.getHeader("Origin");
            
            // Set Headers (Paksa Boleh Masuk)
            response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "*"); // Izinkan SEMUA Header biar ga rewel
            response.setHeader("Access-Control-Allow-Credentials", "true");

            // JIKA OPTIONS (Preflight/Cek Ombak), langsung OK dan STOP.
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                System.out.println("✅ Menangani OPTIONS (Preflight) secara manual. Status: 200 OK");
                response.setStatus(HttpServletResponse.SC_OK);
                return; // PENTING: Jangan teruskan ke rantai berikutnya!
            }
            
            chain.doFilter(req, res);
            
        } catch (Exception e) {
            // Tangkap error biar ga jadi 500 Internal Server Error
            System.err.println("❌ Error di SimpleCorsFilter: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_OK); // Tetap jawab OK biar browser ga panik
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}