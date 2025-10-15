package filter;

import utils.JwtUtils;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns = {"/api/secure/*"})
public class JwtAuthFilter implements Filter {

    private void setCors(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Vary", "Origin");
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Expose-Headers", "Authorization");
        resp.setContentType("application/json;charset=UTF-8");
    }

    @Override
    public void doFilter(ServletRequest r, ServletResponse s, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) r;
        HttpServletResponse resp = (HttpServletResponse) s;

        System.out.println("🔹 [JWT FILTER] Request: " + req.getRequestURI());
        
        setCors(req, resp);
        
        // ✅ Cho phép OPTIONS request đi qua
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String auth = req.getHeader("Authorization");
        System.out.println("🔹 Authorization header: " + auth);
        
        if (auth == null || !auth.startsWith("Bearer ")) {
            System.out.println("❌ Missing or invalid Authorization header");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Missing token\"}");
            resp.getWriter().flush();
            return;
        }

        String token = auth.substring(7).trim();
        System.out.println("🔹 Token: " + token.substring(0, Math.min(20, token.length())) + "...");

        if (!JwtUtils.validateToken(token)) {
            System.out.println("❌ Token validation failed");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Invalid or expired token\"}");
            resp.getWriter().flush();
            return;
        }

        try {
            String email = JwtUtils.getEmailFromToken(token);
            String role = JwtUtils.getRoleFromToken(token);
            Integer id = JwtUtils.getIdFromToken(token);

            System.out.println("✅ Token parsed: email=" + email + ", role=" + role + ", id=" + id);

            if (email == null || role == null || id == null) {
                throw new Exception("Missing claims in token");
            }

            req.setAttribute("jwt_email", email);
            req.setAttribute("jwt_role", role);
            req.setAttribute("jwt_id", id);

            chain.doFilter(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Error parsing token claims: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Invalid token claims\"}");
            resp.getWriter().flush();
        }
    }
}