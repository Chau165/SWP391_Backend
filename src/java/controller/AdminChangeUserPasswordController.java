package controller;

import DAO.UsersDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = "/api/secure/admin/users/change-password")
public class AdminChangeUserPasswordController extends HttpServlet {

    private final UsersDAO usersDAO = new UsersDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        String role = (String) req.getAttribute("jwt_role");
        Integer adminId = (Integer) req.getAttribute("jwt_id");
        if (role == null || adminId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
            return;
        }
        if (!"Admin".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied: Admin only\"}");
            return;
        }

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        JsonObject input = gson.fromJson(body.toString(), JsonObject.class);
        if (input == null || !input.has("userId") || !input.has("newPassword")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Missing userId or newPassword\"}");
            return;
        }

        int userId = input.get("userId").getAsInt();
        String newPassword = input.get("newPassword").getAsString();
        if (newPassword == null || newPassword.trim().length() < 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Password must be at least 6 characters\"}");
            return;
        }

        boolean ok = usersDAO.updatePasswordIfStaffOrManager(userId, newPassword);
        JsonObject res = new JsonObject();
        res.addProperty("success", ok);
        if (ok) {
            res.addProperty("message", "Password updated (only Staff/Manager allowed)");
        } else {
            res.addProperty("message", "User not found or role is not Staff/Manager");
        }
        resp.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write(gson.toJson(res));
    }

    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        boolean allowed = origin != null && (
                origin.equals("http://localhost:5173") ||
                origin.equals("http://127.0.0.1:5173")
        );
        if (allowed) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            res.setHeader("Access-Control-Allow-Origin", "null");
        }
        res.setHeader("Vary", "Origin");
        res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        res.setHeader("Access-Control-Max-Age", "3600");
    }
}
