package controller;

import DAO.DispatchLogDAO;
import DAO.StationDAO;
import DTO.DispatchLog;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/api/secure/dispatchApprove")
public class DispatchApproveController extends HttpServlet {

    private final DispatchLogDAO dispatchLogDAO = new DispatchLogDAO();
    private final StationDAO stationDAO = new StationDAO(); // ✅ thêm DAO để tra ID theo tên trạm

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String role = (String) req.getAttribute("jwt_role");
        if (role == null || !"Admin".equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");
            return;
        }

        try {
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            String action = req.getParameter("action"); // "approve" hoặc "cancel"
            String stationRespondName = req.getParameter("stationRespondName"); // ✅ tên trạm được chọn từ FE

            DispatchLog log = dispatchLogDAO.getById(requestId);
            if (log == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\":false,\"message\":\"Request not found\"}");
                return;
            }

            // === Hủy yêu cầu ===
            if ("cancel".equalsIgnoreCase(action)) {
                log.setStatus("Cancelled");
                log.setRespond_Time(LocalDate.now());
                dispatchLogDAO.update(log);
                resp.getWriter().write("{\"success\":true,\"message\":\"Request cancelled successfully\"}");
                return;
            }

            // === Duyệt yêu cầu (approve) ===
            // === Duyệt yêu cầu (approve) ===
            if ("approve".equalsIgnoreCase(action)) {
                if (stationRespondName == null || stationRespondName.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"success\":false,\"message\":\"Missing stationRespondName\"}");
                    return;
                }

                Integer stationRespondId = stationDAO.getStationIdByName(stationRespondName);
                if (stationRespondId == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"success\":false,\"message\":\"Station not found\"}");
                    return;
                }

                // ✅ Cập nhật sang PREPARING (không chuyển pin tại đây)
                log.setRespond_Time(LocalDate.now());     // mốc đã được admin phân công
                log.setStatus("Preparing");               // <— thay vì "Approved"
                log.setStation_Respond_ID(stationRespondId);

                dispatchLogDAO.update(log);

                resp.getWriter().write("{\"success\":true,\"message\":\"Request set to PREPARING\"}");
                return;
            }

            // Nếu action không hợp lệ
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid action\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid requestId\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"Server error: " + e.getMessage() + "\"}");
        }
    }
}
