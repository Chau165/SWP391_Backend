package controller;

import DAO.DispatchLogDAO;
import DAO.StationDAO;
import DAO.BatteryDAO;
import DAO.UsersDAO;
import DTO.DispatchLog;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/api/secure/dispatchConfirm")
public class dispatchConfirmController extends HttpServlet {

    private final DispatchLogDAO dispatchLogDAO = new DispatchLogDAO();
    private final StationDAO stationDAO = new StationDAO();
    private final BatteryDAO batteryDAO = new BatteryDAO();
    private final UsersDAO userDAO = new UsersDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String role = (String) req.getAttribute("jwt_role");
        Integer userId = (Integer) req.getAttribute("jwt_id");

        if (!"Manager".equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");
            return;
        }

        try {
            int dispatchId = Integer.parseInt(req.getParameter("dispatchId"));
            DispatchLog log = dispatchLogDAO.getById(dispatchId);
            if (log == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\":false,\"message\":\"Dispatch not found\"}");
                return;
            }

            int myStationId = userDAO.getStationIdByUserId(userId);
            if (log.getStation_Request_ID() != myStationId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"success\":false,\"message\":\"Only request station can confirm\"}");
                return;
            }

            if (!"Preparing".equalsIgnoreCase(log.getStatus())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"Cannot confirm: status must be Preparing\"}");
                return;
            }

            // --- Gọi DAO mới ---
            BatteryDAO.TransferResult result = batteryDAO.transferBatteriesUpdateOnly(
                    log.getStation_Respond_ID(),
                    log.getStation_Request_ID(),
                    log.getBatteryType_Request_ID(),
                    log.getQuantity_Type_Good(),
                    log.getQuantity_Type_Average(),
                    log.getQuantity_Type_Bad()
            );

            if (!result.success) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String safeMsg = result.message != null ? result.message.replace("\"","\\\"") : "Battery transfer failed";
                resp.getWriter().write("{\"success\":false,\"message\":\"" + safeMsg + "\"}");
                return;
            }

            // ✅ Thành công (có thể cảnh báo thiếu pin)
            log.setStatus("Complete");
            log.setRespond_Time(LocalDate.now());
            dispatchLogDAO.update(log);

            String warn = (result.warning != null)
                    ? (", \"warning\":\"" + result.warning.replace("\"","\\\"") + "\"")
                    : "";

            String json = "{"
                    + "\"success\":true,"
                    + "\"message\":\"Dispatch confirmed (COMPLETE)\","
                    + "\"movedGood\":" + result.movedGood + ","
                    + "\"movedAverage\":" + result.movedAvg + ","
                    + "\"movedBad\":" + result.movedBad
                    + warn + "}";

            resp.getWriter().write(json);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid dispatchId\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"Server error: " + e.getMessage() + "\"}");
        }
    }
}
