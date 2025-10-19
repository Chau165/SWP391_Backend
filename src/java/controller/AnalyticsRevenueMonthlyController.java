package controller;

import DAO.AnalyticsDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(urlPatterns = {
        "/api/secure/analytics/monthly" // trả danh sách doanh thu/lượt đổi pin từng trạm (tháng hiện tại)
})
public class AnalyticsRevenueMonthlyController extends HttpServlet {

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final Gson gson = new Gson();

    // ==================== OPTIONS (preflight) ====================
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
    }

    // ==================== GET ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // CORS như loginController
        setCorsHeaders(resp, req);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        // Lấy thông tin từ token (JwtAuthFilter đã parse sẵn)
        String role = (String) req.getAttribute("jwt_role");
        Integer userId = (Integer) req.getAttribute("jwt_id");

        if (role == null || userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
            return;
        }

        // Chỉ Admin xem được toàn bộ trạm
        if (!"Admin".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied: Admin only\"}");
            return;
        }

        try {
            // Lấy thống kê tất cả trạm trong THÁNG HIỆN TẠI
            List<JsonObject> stats = analyticsDAO.getCurrentMonthSwapStatsAllStations();

            JsonArray arr = new JsonArray();
            for (JsonObject row : stats) arr.add(row);

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("stations", arr);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(res));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"Class not found\"}");
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() == null ? "Server error" : e.getMessage().replace("\"", "\\\"");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
        }
    }

    // ==================== CORS (giống loginController) ====================
    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");

        boolean allowed = origin != null && (
                origin.equals("http://localhost:5173") ||
                origin.equals("http://127.0.0.1:5173")
        );

        if (allowed) {
            // Echo đúng origin nếu dùng credentials
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            // Không cho origin lạ
            res.setHeader("Access-Control-Allow-Origin", "null");
        }

        res.setHeader("Vary", "Origin");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        // Thêm các header FE thực sự gửi (kể cả ngrok)
        res.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, ngrok-skip-browser-warning");

        // Nếu FE cần đọc Authorization từ response:
        res.setHeader("Access-Control-Expose-Headers", "Authorization");

        // Cache preflight 1 ngày
        res.setHeader("Access-Control-Max-Age", "86400");
    }
}
