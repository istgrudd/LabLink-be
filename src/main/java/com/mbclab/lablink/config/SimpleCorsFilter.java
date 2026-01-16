package com.mbclab.lablink.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // PENTING: Filter ini jalan PALING AWAL
public class SimpleCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        // Ambil Origin dari request, atau default ke Vercel Anda jika null
        String origin = request.getHeader("Origin");
        
        // Set Header CORS secara manual (Paksa Browser Menerima)
        // Kita izinkan Origin yang meminta akses (supaya dinamis)
        response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "https://lablink-fe.vercel.app");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, X-Requested-With, remember-me");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // --- BAGIAN ANTI-CRASH ---
        // Jika browser kirim OPTIONS (Preflight), langsung jawab OK dan STOP di sini.
        // Jangan biarkan request masuk ke SecurityConfig atau Controller yang bikin crash.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}