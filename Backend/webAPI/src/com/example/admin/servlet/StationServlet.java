package com.example.admin.servlet;

import com.example.admin.dao.StationDAO;
import com.example.admin.model.Station;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "StationServlet", urlPatterns = {"/admin/stations"})
public class StationServlet extends HttpServlet {
    private StationDAO dao = new StationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "list";
        try {
            switch (action) {
                case "create":
                    req.getRequestDispatcher("/admin/station-form.jsp").forward(req, resp);
                    break;
                case "edit":
                    int id = Integer.parseInt(req.getParameter("id"));
                    Station s = dao.getById(id);
                    req.setAttribute("station", s);
                    req.getRequestDispatcher("/admin/station-form.jsp").forward(req, resp);
                    break;
                case "list":
                default:
                    String q = req.getParameter("q");
                    List<Station> list = dao.getAll(q);
                    req.setAttribute("stations", list);
                    req.getRequestDispatcher("/admin/stations-list.jsp").forward(req, resp);
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
                String name = req.getParameter("name");
                String address = req.getParameter("address");
                Station s = new Station();
                s.setName(name);
                s.setAddress(address);
                boolean ok = dao.insert(s);
                req.getSession().setAttribute("message", ok ? "Station created" : "Failed to create station");
                resp.sendRedirect(req.getContextPath() + "/admin/stations");
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("stationId"));
                String name = req.getParameter("name");
                String address = req.getParameter("address");
                Station s = new Station(id, name, address);
                boolean ok = dao.update(s);
                req.getSession().setAttribute("message", ok ? "Station updated" : "Failed to update station");
                resp.sendRedirect(req.getContextPath() + "/admin/stations");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean ok = dao.delete(id);
                req.getSession().setAttribute("message", ok ? "Station deleted" : "Failed to delete station");
                resp.sendRedirect(req.getContextPath() + "/admin/stations");
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
