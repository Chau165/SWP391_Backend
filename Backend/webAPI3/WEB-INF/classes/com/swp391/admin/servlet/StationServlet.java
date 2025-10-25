package com.swp391.admin.servlet;

import com.swp391.admin.dao.StationDAO;
import com.swp391.admin.dao.ChargingStationDAO;
import com.swp391.admin.model.Station;
import com.swp391.admin.model.ChargingStation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Servlet handling Station CRUD and showing detail page
public class StationServlet extends HttpServlet {
    private StationDAO stationDAO = new StationDAO();
    private ChargingStationDAO chargingDAO = new ChargingStationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("new".equals(action)) {
                req.getRequestDispatcher("/WEB-INF/jsp/stationForm.jsp").forward(req, resp);
                return;
            } else if ("edit".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                Station s = stationDAO.getById(id);
                req.setAttribute("station", s);
                req.getRequestDispatcher("/WEB-INF/jsp/stationForm.jsp").forward(req, resp);
                return;
            } else if ("detail".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                Station s = stationDAO.getById(id);
                List<ChargingStation> slots = chargingDAO.getByStationId(id);
                req.setAttribute("station", s);
                req.setAttribute("slots", slots);
                req.getRequestDispatcher("/WEB-INF/jsp/stationDetail.jsp").forward(req, resp);
                return;
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                stationDAO.delete(id);
            }
            // default: list
            List<Station> stations = stationDAO.getAll();
            req.setAttribute("stations", stations);
            req.getRequestDispatcher("/WEB-INF/jsp/stations.jsp").forward(req, resp);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("save".equals(action)) {
                String id = req.getParameter("stationId");
                String name = req.getParameter("name");
                String address = req.getParameter("address");
                if (id == null || id.isEmpty()) {
                    Station s = new Station(); s.setName(name); s.setAddress(address);
                    stationDAO.insert(s);
                } else {
                    Station s = new Station(Integer.parseInt(id), name, address);
                    stationDAO.update(s);
                }
            }
            resp.sendRedirect(req.getContextPath()+"/stations");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
