package controller;

import DAO.BatteryTypeDAO;
import DAO.DispatchLogDAO;
import DAO.StationDAO;
import DTO.DispatchLog;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/api/secure/dispatchRequest")
public class DispatchRequestController extends HttpServlet {
    private final StationDAO stationDAO = new StationDAO();
    private final BatteryTypeDAO batteryTypeDAO = new BatteryTypeDAO();
    private final DispatchLogDAO dispatchLogDAO = new DispatchLogDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        // Lấy claim từ JWT Filter
        String role = (String) req.getAttribute("jwt_role");
        Integer userId = (Integer) req.getAttribute("jwt_id");

        try (PrintWriter out = resp.getWriter()) {

            // 1) Quyền hạn
            if (role == null || !"Manager".equals(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(json(false, "Access denied"));
                return;
            }

            // 2) Lấy & validate tham số
            String stationName = trimOrNull(req.getParameter("stationName"));
            String batteryName  = trimOrNull(req.getParameter("batteryName"));
            String sGood        = trimOrNull(req.getParameter("qtyGood"));
            String sAvg         = trimOrNull(req.getParameter("qtyAverage"));
            String sBad         = trimOrNull(req.getParameter("qtyBad"));

            if (isBlank(stationName) || isBlank(batteryName) ||
                isBlank(sGood) || isBlank(sAvg) || isBlank(sBad)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(json(false, "Missing required fields"));
                return;
            }

            int qtyGood, qtyAverage, qtyBad;
            try {
                qtyGood    = Integer.parseInt(sGood);
                qtyAverage = Integer.parseInt(sAvg);
                qtyBad     = Integer.parseInt(sBad);
            } catch (NumberFormatException nfe) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(json(false, "Quantity must be integer"));
                return;
            }

            if (qtyGood < 0 || qtyAverage < 0 || qtyBad < 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(json(false, "Quantity cannot be negative"));
                return;
            }
            if (qtyGood + qtyAverage + qtyBad == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(json(false, "At least one quantity must be > 0"));
                return;
            }

            // 3) Tra ID theo tên (đã chuẩn hóa trong DAO: trim + collate CI)
            int stationId = stationDAO.getStationIdByName(stationName);
            if (stationId <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(json(false, "Station not found: " + stationName));
                return;
            }

            int batteryId = batteryTypeDAO.getBatteryTypeIdByName(batteryName);
            if (batteryId <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(json(false, "Battery type not found: " + batteryName));
                return;
            }

            // (Tùy chọn khuyến nghị) kiểm tra stationId có thuộc quyền Manager này không
            // if (!stationDAO.isStationOwnedByManager(stationId, userId)) { ... }

            // 4) Tạo log & insert
            DispatchLog log = new DispatchLog();
            log.setStation_Request_ID(stationId);
            log.setBatteryType_Request_ID(batteryId);
            log.setQuantity_Type_Good(qtyGood);
            log.setQuantity_Type_Average(qtyAverage);
            log.setQuantity_Type_Bad(qtyBad);
            log.setRequest_Time(LocalDate.now());
            log.setStatus("Pending");

            int requestId = dispatchLogDAO.insert(log); // nên dùng OUTPUT INSERTED.ID trong DAO
            if (requestId <= 0) {
                // Insert fail (FK/NOT NULL/logic…). DAO cần log chi tiết SQLException.
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(json(false, "Insert failed"));
                return;
            }

            // 5) Thành công
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"success\":true,\"requestId\":" + requestId + "}");
        } catch (Exception ex) {
            // 6) Bắt mọi lỗi không lường trước
            ex.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(json(false, "Unexpected error"));
        }
    }

    private static String trimOrNull(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }

    private static String json(boolean success, String message) {
        // escape tối thiểu cho dấu ngoặc kép
        String safe = message == null ? "" : message.replace("\"","\\\"");
        return "{\"success\":" + success + ",\"message\":\"" + safe + "\"}";
    }
}
