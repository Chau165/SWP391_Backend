package com.swp391.admin.servlet;

import com.swp391.admin.dao.ChargingStationDAO;
import com.swp391.admin.model.ChargingStation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

// Servlet to manage charging station CRUD
public class ChargingStationServlet extends HttpServlet {
    private ChargingStationDAO dao = new ChargingStationDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("save".equals(action)) {
                String id = req.getParameter("chargingStationId");
                int stationId = Integer.parseInt(req.getParameter("stationId"));
                String name = req.getParameter("name");
                int capacity = Integer.parseInt(req.getParameter("slotCapacity"));
                String slotType = req.getParameter("slotType");
                String power = req.getParameter("powerRating");
                if (id == null || id.isEmpty()) {
                    ChargingStation cs = new ChargingStation();
                    cs.setStationId(stationId); cs.setName(name); cs.setSlotCapacity(capacity); cs.setSlotType(slotType); cs.setPowerRating(power);
                    dao.insert(cs);
                } else {
                    ChargingStation cs = new ChargingStation(Integer.parseInt(id), stationId, name, capacity, slotType, power);
                    dao.update(cs);
                }
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                dao.delete(id);
            }
            resp.sendRedirect(req.getContextPath()+"/stations?action=detail&id="+req.getParameter("stationId"));
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
