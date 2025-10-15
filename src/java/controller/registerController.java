package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;
import mylib.ValidationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/register")
public class registerController extends HttpServlet {

    private final Gson gson = new Gson();

    // ==================== OPTIONS ====================
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
    }

    // ==================== POST ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp, req);
        resp.setContentType("application/json;charset=UTF-8");

        try (BufferedReader reader = req.getReader();
             PrintWriter out = resp.getWriter()) {

            Users input = gson.fromJson(reader, Users.class);

            if (input == null) {
                resp.setStatus(400);
                out.print("{\"error\":\"Invalid input\"}");
                return;
            }

            // ✅ Validate dữ liệu
            if (!ValidationUtil.isValidFullName(input.getFullName())) {
                resp.setStatus(400);
                out.print("{\"error\":\"Full name is invalid\"}");
                return;
            }

            if (!ValidationUtil.isValidVNPhone(input.getPhone())) {
                resp.setStatus(400);
                out.print("{\"error\":\"Phone number is not a valid VN mobile number\"}");
                return;
            }

            if (!ValidationUtil.isValidEmail(input.getEmail())) {
                resp.setStatus(400);
                out.print("{\"error\":\"Email is invalid\"}");
                return;
            }

            if (!ValidationUtil.isValidPassword(input.getPassword())) {
                resp.setStatus(400);
                out.print("{\"error\":\"Password must be at least 6 characters, include letters and digits\"}");
                return;
            }

            UsersDAO dao = new UsersDAO();

            // ✅ Check email tồn tại
            if (dao.existsByEmail(input.getEmail())) {
                resp.setStatus(409);
                out.print("{\"error\":\"Email already exists\"}");
                return;
            }

            // ✅ Gán mặc định role & station
            input.setRole("Driver");
            input.setStationId(null);

            int newId = dao.insertUser(input);

            if (newId <= 0) {
                resp.setStatus(400);
                out.print("{\"error\":\"Failed to create user\"}");
                return;
            }

            // ✅ Thành công
            resp.setStatus(200);
            out.print("{\"status\":\"success\",\"userId\":" + newId + ",\"role\":\"Driver\"}");
        }
    }

    // ==================== CORS (giống loginController) ====================
    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");

        // ✅ Cho phép FE chạy ở localhost hoặc ngrok
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
            res.setHeader("Access-Control-Allow-Origin", "null");
        }

        res.setHeader("Vary", "Origin");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, ngrok-skip-browser-warning");
        res.setHeader("Access-Control-Expose-Headers", "Authorization");
        res.setHeader("Access-Control-Max-Age", "86400"); // cache preflight 24h
    }
}
