package controller;

import DAO.SwapTransactionDAO;
import DTO.PeakHourStatistics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Controller để xử lý thống kê giờ cao điểm của swap transactions
 * API endpoints:
 * - GET /api/secure/analytics/peak-hours - Thống kê tất cả khung giờ
 * - GET /api/secure/analytics/peak-hours/top - Lấy top N khung giờ cao điểm
 * - GET /api/secure/analytics/peak-hours/station/{stationId} - Thống kê theo trạm
 */
@WebServlet(urlPatterns = {
        "/api/secure/analytics/peak-hours",
        "/api/secure/analytics/peak-hours/top",
        "/api/secure/analytics/peak-hours/station"
})
public class PeakHourStatisticsController extends HttpServlet {

    private final SwapTransactionDAO swapTransactionDAO = new SwapTransactionDAO();
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

        // Chỉ Admin và Staff xem được thống kê
        if (!"Admin".equalsIgnoreCase(role) && !"Staff".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied: Admin or Staff only\"}");
            return;
        }

        String path = req.getServletPath();

        try {
            // Lấy parameters cho filter theo ngày
            String startDateStr = req.getParameter("startDate"); // format: yyyy-MM-dd
            String endDateStr = req.getParameter("endDate");     // format: yyyy-MM-dd
            
            Date startDate = null;
            Date endDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = new Date(sdf.parse(startDateStr).getTime());
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = new Date(sdf.parse(endDateStr).getTime());
            }

            if ("/api/secure/analytics/peak-hours".equals(path)) {
                // Thống kê tất cả khung giờ
                handleGetAllPeakHours(req, resp, startDate, endDate);
                
            } else if ("/api/secure/analytics/peak-hours/top".equals(path)) {
                // Lấy top N khung giờ cao điểm
                handleGetTopPeakHours(req, resp, startDate, endDate);
                
            } else if ("/api/secure/analytics/peak-hours/station".equals(path)) {
                // Thống kê theo trạm cụ thể
                handleGetPeakHoursByStation(req, resp, startDate, endDate);
                
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\":false,\"message\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() == null ? "Server error" : e.getMessage().replace("\"", "\\\"");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
        }
    }

    /**
     * Xử lý GET /api/secure/analytics/peak-hours
     * Trả về thống kê tất cả 24 khung giờ trong ngày
     */
    private void handleGetAllPeakHours(HttpServletRequest req, HttpServletResponse resp, 
                                      Date startDate, Date endDate) throws IOException {
        try {
            List<PeakHourStatistics> stats = swapTransactionDAO.getPeakHourStatistics(startDate, endDate);

            JsonArray arr = new JsonArray();
            for (PeakHourStatistics stat : stats) {
                JsonObject obj = new JsonObject();
                obj.addProperty("timeSlot", stat.getTimeSlot());
                obj.addProperty("swapCount", stat.getSwapCount());
                obj.addProperty("totalRevenue", stat.getTotalRevenue());
                obj.addProperty("averageFee", stat.getAverageFee());
                arr.add(obj);
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("totalSlots", stats.size());
            res.add("peakHours", arr);

            // Tìm khung giờ có lượt swap cao nhất
            if (!stats.isEmpty()) {
                PeakHourStatistics maxStat = stats.stream()
                    .max((a, b) -> Integer.compare(a.getSwapCount(), b.getSwapCount()))
                    .orElse(null);
                if (maxStat != null) {
                    res.addProperty("peakHour", maxStat.getTimeSlot());
                    res.addProperty("peakHourSwapCount", maxStat.getSwapCount());
                }
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(res));

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error retrieving peak hour statistics", e);
        }
    }

    /**
     * Xử lý GET /api/secure/analytics/peak-hours/top?limit=5
     * Trả về top N khung giờ có nhiều giao dịch nhất
     */
    private void handleGetTopPeakHours(HttpServletRequest req, HttpServletResponse resp,
                                      Date startDate, Date endDate) throws IOException {
        try {
            // Lấy parameter limit (mặc định là 5)
            String limitStr = req.getParameter("limit");
            int limit = 5;
            if (limitStr != null && !limitStr.isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr);
                    if (limit < 1) limit = 5;
                    if (limit > 24) limit = 24;
                } catch (NumberFormatException e) {
                    limit = 5;
                }
            }

            List<PeakHourStatistics> stats = swapTransactionDAO.getTopPeakHours(limit, startDate, endDate);

            JsonArray arr = new JsonArray();
            for (PeakHourStatistics stat : stats) {
                JsonObject obj = new JsonObject();
                obj.addProperty("timeSlot", stat.getTimeSlot());
                obj.addProperty("swapCount", stat.getSwapCount());
                obj.addProperty("totalRevenue", stat.getTotalRevenue());
                obj.addProperty("averageFee", stat.getAverageFee());
                arr.add(obj);
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("limit", limit);
            res.add("topPeakHours", arr);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(res));

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error retrieving top peak hours", e);
        }
    }

    /**
     * Xử lý GET /api/secure/analytics/peak-hours/station?stationId=1
     * Trả về thống kê giờ cao điểm cho một trạm cụ thể
     */
    private void handleGetPeakHoursByStation(HttpServletRequest req, HttpServletResponse resp,
                                            Date startDate, Date endDate) throws IOException {
        try {
            // Lấy parameter stationId (bắt buộc)
            String stationIdStr = req.getParameter("stationId");
            if (stationIdStr == null || stationIdStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"Missing stationId parameter\"}");
                return;
            }

            int stationId;
            try {
                stationId = Integer.parseInt(stationIdStr);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"Invalid stationId format\"}");
                return;
            }

            List<PeakHourStatistics> stats = swapTransactionDAO.getPeakHourStatisticsByStation(
                stationId, startDate, endDate
            );

            JsonArray arr = new JsonArray();
            for (PeakHourStatistics stat : stats) {
                JsonObject obj = new JsonObject();
                obj.addProperty("timeSlot", stat.getTimeSlot());
                obj.addProperty("swapCount", stat.getSwapCount());
                obj.addProperty("totalRevenue", stat.getTotalRevenue());
                obj.addProperty("averageFee", stat.getAverageFee());
                arr.add(obj);
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("stationId", stationId);
            res.addProperty("totalSlots", stats.size());
            res.add("peakHours", arr);

            // Tìm khung giờ có lượt swap cao nhất cho trạm này
            if (!stats.isEmpty()) {
                PeakHourStatistics maxStat = stats.stream()
                    .max((a, b) -> Integer.compare(a.getSwapCount(), b.getSwapCount()))
                    .orElse(null);
                if (maxStat != null) {
                    res.addProperty("peakHour", maxStat.getTimeSlot());
                    res.addProperty("peakHourSwapCount", maxStat.getSwapCount());
                }
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(res));

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error retrieving station peak hour statistics", e);
        }
    }

    // ==================== CORS ====================
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
        res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With, Accept");
        res.setHeader("Access-Control-Max-Age", "3600");
    }
}
