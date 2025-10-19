package controller;

import DAO.BatterySlotDAO;
import DAO.UsersDAO;
import DTO.BatterySlot;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "viewBatterySlotStatusController", urlPatterns = {"/api/secure/viewBatterySlotStatus"})
public class viewBatterySlotStatusController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        Gson gson = new Gson();

        try {
            // ✅ Lấy thông tin từ JWT
            String role = (String) req.getAttribute("jwt_role");
            Integer userId = (Integer) req.getAttribute("jwt_id");

            if (role == null || !role.equalsIgnoreCase("staff")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Access denied. Staff only.\"}");
                return;
            }

            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\":\"Missing user id in token.\"}");
                return;
            }

            // ✅ Lấy Station_ID của staff từ DB
            UsersDAO userDao = new UsersDAO();
            Integer stationId = userDao.getStationIdByUserId(userId);

            if (stationId == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Staff has no assigned station.\"}");
                return;
            }

            // ✅ Gọi DAO lấy danh sách slot theo trạm
            BatterySlotDAO slotDao = new BatterySlotDAO();
            List<BatterySlot> slots = slotDao.getSlotsByStationId(stationId);

            // ✅ Trả kết quả JSON
            String json = gson.toJson(slots);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
        }
    }
}
