package filter;

import DAO.UsersDAO;
import DTO.Users;
import util.JwtUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Global filter that prevents users whose Status != Active from performing API actions.
 * - Allows public endpoints like /api/login and /api/register.
 * - Checks HttpSession attribute "User" and Bearer JWT tokens.
 */
@WebFilter(urlPatterns = {"/api/*"})
public class StatusFilter implements Filter {

    private UsersDAO usersDAO = new UsersDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        // allow unauthenticated public endpoints
        if (path == null) path = "";
        String lower = path.toLowerCase();
        if (lower.endsWith("/api/login") || lower.endsWith("/api/register") || lower.contains("/api/public/")) {
            chain.doFilter(request, response);
            return;
        }

        // OPTIONS preflight should be allowed
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 1) check session user
        try {
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object o = session.getAttribute("User");
                if (o instanceof Users) {
                    Users u = (Users) o;
                    String status = u.getStatus();
                    if (status == null || !"Active".equalsIgnoreCase(status.trim())) {
                        sendBlocked(res, status);
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            // ignore and continue to JWT check
        }

        // 2) if Authorization: Bearer token present, try to parse and enforce status via DB
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
            String token = auth.substring(7).trim();
            Map<String,Object> claims = JwtUtils.parseToken(token);
            if (claims != null && claims.containsKey("id")) {
                try {
                    int id = ((Number)claims.get("id")).intValue();
                    Users u = usersDAO.getUserById(id);
                    if (u != null) {
                        String status = u.getStatus();
                        if (status == null || !"Active".equalsIgnoreCase(status.trim())) {
                            sendBlocked(res, status);
                            return;
                        }
                    }
                } catch (Exception ex) {
                    // fallthrough
                }
            }
        }

        chain.doFilter(request, response);
    }

    private void sendBlocked(HttpServletResponse res, String status) throws IOException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType("application/json;charset=UTF-8");
        String msg = "Your account cannot access the system";
        if ("Blocked".equalsIgnoreCase(status)) msg = "Your account is blocked";
        else msg = "Your account is pending activation/verification";
        try (PrintWriter pw = res.getWriter()) {
            pw.print("{\"status\":\"fail\",\"message\":\"" + msg.replace("\"","'") + "\"}");
        }
    }

    @Override
    public void destroy() { }
}
