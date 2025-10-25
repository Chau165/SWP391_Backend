package com.example.admin.servlet;

import com.example.admin.dao.ChargingStationDAO;
import com.example.admin.model.ChargingStation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "ChargingStationServlet", urlPatterns = {"/admin/charging"})
public class ChargingStationServlet extends HttpServlet {
    private ChargingStationDAO dao = new ChargingStationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "list";
        try {
            switch (action) {
                case "create":
                    req.getRequestDispatcher("/admin/charge-form.jsp").forward(req, resp);
                    break;
                case "edit":
                    int id = Integer.parseInt(req.getParameter("id"));
                    ChargingStation c = dao.getById(id);
                    req.setAttribute("charge", c);
                    req.getRequestDispatcher("/admin/charge-form.jsp").forward(req, resp);
                    break;
                case "list":
                default:
                    int stationId = Integer.parseInt(req.getParameter("stationId"));
                    List<ChargingStation> list = dao.getByStationId(stationId);
                    req.setAttribute("charges", list);
                    req.setAttribute("stationId", stationId);
                    req.getRequestDispatcher("/admin/charge-list.jsp").forward(req, resp);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "create";
        try {
            if ("create".equals(action)) {
                ChargingStation c = new ChargingStation();
                c.setStationId(Integer.parseInt(req.getParameter("stationId")));
                c.setName(req.getParameter("name"));
                c.setSlotCapacity(Integer.parseInt(req.getParameter("slotCapacity")));
                c.setSlotType(req.getParameter("slotType"));
                c.setPowerRating(Double.parseDouble(req.getParameter("powerRating")));
                boolean ok = dao.insert(c);
                req.getSession().setAttribute("message", ok ? "Charging slot created" : "Failed to create");
                resp.sendRedirect(req.getContextPath() + "/admin/charging?stationId=" + c.getStationId());
            } else if ("update".equals(action)) {
                ChargingStation c = new ChargingStation();
                c.setChargingStationId(Integer.parseInt(req.getParameter("chargingStationId")));
                c.setStationId(Integer.parseInt(req.getParameter("stationId")));
                c.setName(req.getParameter("name"));
                c.setSlotCapacity(Integer.parseInt(req.getParameter("slotCapacity")));
                c.setSlotType(req.getParameter("slotType"));
                c.setPowerRating(Double.parseDouble(req.getParameter("powerRating")));
                boolean ok = dao.update(c);
                req.getSession().setAttribute("message", ok ? "Charging slot updated" : "Failed to update");
                resp.sendRedirect(req.getContextPath() + "/admin/charging?stationId=" + c.getStationId());
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                int stationId = Integer.parseInt(req.getParameter("stationId"));
                boolean ok = dao.delete(id);
                req.getSession().setAttribute("message", ok ? "Deleted" : "Delete failed");
                resp.sendRedirect(req.getContextPath() + "/admin/charging?stationId=" + stationId);
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
