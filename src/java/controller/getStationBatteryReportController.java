package controller;

import DAO.AnalyticsDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * API [Guest] Báo cáo tổng hợp pin tại các trạm Swagger:
 * /api/getStationBatteryReportGuest Không yêu cầu JWT
 */
@WebServlet("/api/getStationBatteryReportGuest")
public class getStationBatteryReportController extends HttpServlet {

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp, req);
        resp.setContentType("application/json;charset=UTF-8");

        String idStr = req.getParameter("stationId"); // <-- lấy param

        try ( PrintWriter out = resp.getWriter()) {

            Integer stationId = null;
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    stationId = Integer.parseInt(idStr.trim());
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"status\":\"fail\",\"message\":\"stationId must be an integer\"}");
                    return;
                }
            }

            // ✅ Gọi DAO (null = tất cả; != null = 1 trạm)
            List<JsonObject> stationReports = analyticsDAO.getStationBatterySummariesJson(stationId);

            resp.setStatus(HttpServletResponse.SC_OK);
            if (stationReports == null || stationReports.isEmpty()) {
                out.print("{\"status\":\"success\",\"data\":[]}");
            } else {
                out.print("{\"status\":\"success\",\"data\":" + gson.toJson(stationReports) + "}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"status\":\"error\",\"message\":\"Server error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Cấu hình CORS headers cho phép gọi từ front-end React hoặc ngrok.
     */
    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        boolean allowed
                = origin != null && (origin.equals("http://localhost:5173")
                || origin.equals("http://127.0.0.1:5173")
                || origin.contains("ngrok-free.app"));

        if (allowed) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            res.setHeader("Access-Control-Allow-Origin", "null");
        }

        res.setHeader("Vary", "Origin");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, ngrok-skip-browser-warning");
        res.setHeader("Access-Control-Expose-Headers", "Authorization");
        res.setHeader("Access-Control-Max-Age", "86400");
    }
}
