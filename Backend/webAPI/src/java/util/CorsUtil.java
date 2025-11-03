package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CorsUtil {
    // Allow multiple local dev origins; if request Origin header present and allowed, echo it back
    private static final String[] ALLOWED = new String[] {"http://localhost:5173", "http://localhost:3000"};

    public static void setCors(HttpServletResponse resp, HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        String allow = null;
        if (origin != null) {
            for (String a : ALLOWED) {
                if (a.equalsIgnoreCase(origin)) { allow = a; break; }
            }
        }
        if (allow == null) allow = ALLOWED[0];
        resp.setHeader("Access-Control-Allow-Origin", allow);
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Max-Age", "3600");
    }
}
