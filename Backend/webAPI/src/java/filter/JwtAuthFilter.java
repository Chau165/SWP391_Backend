package filter;

import util.JwtUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Simple JWT authentication filter used for /api/secure/* endpoints.
 * Expects Authorization: Bearer <token>. Uses util.JwtUtils to parse and validate.
 */
public class JwtAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // allow OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.toLowerCase().startsWith("bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            try (PrintWriter pw = res.getWriter()) {
                pw.print("{\"status\":\"fail\",\"message\":\"Missing or invalid Authorization header\"}");
            }
            return;
        }

        String token = auth.substring(7).trim();
        Map<String, Object> claims = JwtUtils.parseToken(token);
        if (claims == null || !claims.containsKey("id")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            try (PrintWriter pw = res.getWriter()) {
                pw.print("{\"status\":\"fail\",\"message\":\"Invalid or expired token\"}");
            }
            return;
        }

        // attach claims for downstream code
        req.setAttribute("jwtClaims", claims);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
