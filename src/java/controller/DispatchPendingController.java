package controller;

import DAO.BatteryTypeDAO;
import DAO.DispatchLogDAO;
import DAO.StationDAO;
import DAO.UsersDAO;
import DTO.DispatchLog;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/secure/dispatchPending")
public class DispatchPendingController extends HttpServlet {
    private final DispatchLogDAO dispatchLogDAO = new DispatchLogDAO();
    private final StationDAO stationDAO = new StationDAO();
    private final BatteryTypeDAO batteryTypeDAO = new BatteryTypeDAO();
    private final UsersDAO userDAO = new UsersDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String role = (String) req.getAttribute("jwt_role");
        Integer userId = (Integer) req.getAttribute("jwt_id");

        if (!"Manager".equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");
            return;
        }

        int stationId = userDAO.getStationIdByUserId(userId);

        List<DispatchLog> pendingRequests = dispatchLogDAO.getPendingRequestsByStation(stationId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (DispatchLog log : pendingRequests) {
            Map<String, Object> map = new HashMap<>();
            map.put("requestId", log.getID());
            map.put("stationRequestName", stationDAO.getStationNameById(log.getStation_Request_ID()));
            map.put("batteryName", batteryTypeDAO.getBatteryTypeNameById(log.getBatteryType_Request_ID()));
            map.put("qtyGood", log.getQuantity_Type_Good());
            map.put("qtyAverage", log.getQuantity_Type_Average());
            map.put("qtyBad", log.getQuantity_Type_Bad());
            map.put("status", log.getStatus());
            map.put("requestTime", log.getRequest_Time().toString());
            result.add(map);
        }

        resp.getWriter().write(new Gson().toJson(result));
    }
}
