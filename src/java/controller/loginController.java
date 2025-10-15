package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;
import utils.JwtUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/api/login")
public class loginController extends HttpServlet {

    private final UsersDAO usersDAO = new UsersDAO();
    private final Gson gson = new Gson();

    // ==================== OPTIONS ====================
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response, request);
        // Không body cho preflight
        response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
    }

    // ==================== POST ====================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response, request);
        response.setContentType("application/json;charset=UTF-8");

        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {

            LoginRequest loginReq = gson.fromJson(reader, LoginRequest.class);
            if (loginReq == null || isBlank(loginReq.email) || isBlank(loginReq.password)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonFail("Thiếu email hoặc mật khẩu", "AUTH_MISSING_FIELD"));
                out.flush();
                return;
            }

            Users user = usersDAO.checkLogin(loginReq.email.trim(), loginReq.password);

            if (user != null) {
                if ("Blocked".equalsIgnoreCase(user.getStatus())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(jsonFail("Tài khoản bị khóa", "AUTH_BLOCKED"));
                    out.flush();
                    return;
                }

                String token = JwtUtils.generateToken(user.getEmail(), user.getRole(), user.getId());
                System.out.println("Token User: "+token);
                        
                response.setStatus(HttpServletResponse.SC_OK);
                LoginResponse payload = new LoginResponse("success", token, user);
                out.print(gson.toJson(payload));
                out.flush();

            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(jsonFail("Email hoặc mật khẩu không hợp lệ", "AUTH_INVALID"));
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print(jsonError("Lỗi server: " + e.getMessage()));
                out.flush();
            }
        }
    }

    // ==================== CORS (áp dụng cho mọi request) ====================
    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");

        // ✅ Chỉ cho phép FE chạy ở localhost:5173 (Vite mặc định) & 127.0.0.1:5173
        boolean allowed =
                origin != null && (
                        origin.equals("http://localhost:5173") ||
                        origin.equals("http://127.0.0.1:5173")
                );

        if (allowed) {
            // Echo đúng origin (bắt buộc nếu bật credentials)
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            // Nếu không khớp, không set origin (tránh rủi ro bảo mật/CORS lỏng)
            // Hoặc có thể set "null" nếu bạn muốn rõ ràng:
            res.setHeader("Access-Control-Allow-Origin", "null");
        }

        res.setHeader("Vary", "Origin");

        // Phương thức được phép
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        // ✅ Thêm toàn bộ header FE thực sự gửi (bao gồm ngrok-skip-browser-warning)
        res.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, ngrok-skip-browser-warning");

        // FE nếu cần đọc Authorization từ response thì expose:
        res.setHeader("Access-Control-Expose-Headers", "Authorization");

        // Cache preflight 1 ngày
        res.setHeader("Access-Control-Max-Age", "86400");
    }

    // ==================== Helpers ====================
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String jsonFail(String message, String code) {
        return "{\"status\":\"fail\",\"code\":\"" + escape(message == null ? "" : code) +
                "\",\"message\":\"" + escape(message) + "\"}";
    }

    private String jsonError(String message) {
        return "{\"status\":\"error\",\"message\":\"" + escape(message) + "\"}";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ==================== DTOs ====================
    private static class LoginRequest {
        String email;
        String password;
    }

    private static class LoginResponse {
        String status;
        String token;
        Users user;

        public LoginResponse(String status, String token, Users user) {
            this.status = status;
            this.token = token;
            this.user = user;
        }
    }
}
