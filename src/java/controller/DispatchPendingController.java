package controller;

import DAO.BatteryTypeDAO;
import DAO.DispatchLogDAO;
import DAO.StationDAO;
import DAO.UsersDAO;
import DTO.DispatchLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/secure/dispatchPending")
public class DispatchPendingController extends HttpServlet {

    private final DispatchLogDAO dispatchLogDAO = new DispatchLogDAO();
    private final StationDAO stationDAO = new StationDAO();
    private final BatteryTypeDAO batteryTypeDAO = new BatteryTypeDAO();
    private final UsersDAO userDAO = new UsersDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("application/json; charset=UTF-8");
            
            String role = (String) req.getAttribute("jwt_role");
            Integer userId = (Integer) req.getAttribute("jwt_id");
            
            if (!"Manager".equals(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");
                return;
            }
            
            final int myStationId = userDAO.getStationIdByUserId(userId);
            
            // ✅ Lấy tất cả request mà trạm của Manager là requester HOẶC responder (mọi trạng thái)
            List<DispatchLog> related = dispatchLogDAO.getRelatedRequestsByStation(myStationId);
            
            // Chuẩn hoá output
            List<Map<String, Object>> result = new ArrayList<>();
            for (DispatchLog log : related) {
                Integer stationRequestId = log.getStation_Request_ID();
                Integer stationRespondId = log.getStation_Respond_ID();
                Integer batteryTypeId    = log.getBatteryType_Request_ID();
                
                String stationRequestName = stationRequestId != null ? stationDAO.getStationNameById(stationRequestId) : null;
                String stationRespondName = stationRespondId != null ? stationDAO.getStationNameById(stationRespondId) : null;
                String batteryName        = batteryTypeId    != null ? batteryTypeDAO.getBatteryTypeNameById(batteryTypeId) : null;
                
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("requestId", log.getID());
                m.put("stationRequestId", stationRequestId);
                m.put("stationRequestName", stationRequestName);
                m.put("stationRespondId", stationRespondId);
                m.put("stationRespondName", stationRespondName);
                m.put("batteryTypeId", batteryTypeId);
                m.put("batteryName", batteryName);
                m.put("qtyGood", log.getQuantity_Type_Good());
                m.put("qtyAverage", log.getQuantity_Type_Average());
                m.put("qtyBad", log.getQuantity_Type_Bad());
                m.put("requestTime", log.getRequest_Time() != null ? log.getRequest_Time().toString() : null);
                m.put("respondTime", log.getRespond_Time() != null ? log.getRespond_Time().toString() : null);
                m.put("status", log.getStatus());
                m.put("isMyStationRequest", Objects.equals(myStationId, stationRequestId));
                m.put("isMyStationRespond", Objects.equals(myStationId, stationRespondId));
                result.add(m);
            }
            
            Gson gson = new GsonBuilder().serializeNulls().create();
            resp.getWriter().write(gson.toJson(result));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DispatchPendingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DispatchPendingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}