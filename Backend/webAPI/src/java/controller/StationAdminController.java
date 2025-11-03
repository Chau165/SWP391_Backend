package controller;

import DAO.StationDAO;
import DTO.Station;
import DTO.Users;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "StationAdminController", urlPatterns = {"/api/admin/station"})
public class StationAdminController extends HttpServlet {

    private final StationDAO dao = new StationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        Users u = (Users) req.getSession().getAttribute("User");
        if (u == null || u.getRole() == null || !u.getRole().trim().equalsIgnoreCase("admin")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"fail\",\"message\":\"Admin only\"}");
            }
            return;
        }

        // if id provided, return single station
        String idParam = req.getParameter("id");
        try (PrintWriter out = resp.getWriter()) {
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                Station s = dao.getStationById(id);
                if (s == null) { resp.setStatus(HttpServletResponse.SC_NOT_FOUND); out.print("{}"); }
                else { resp.setStatus(HttpServletResponse.SC_OK); out.print(gson.toJson(s)); }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(dao.getAllStation()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        Users u = (Users) req.getSession().getAttribute("User");
        if (u == null || u.getRole() == null || !u.getRole().trim().equalsIgnoreCase("admin")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) { out.print("{\"status\":\"fail\",\"message\":\"Admin only\"}"); }
            return;
        }

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            Station s = gson.fromJson(reader, Station.class);
            if (s == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print("{\"status\":\"fail\",\"message\":\"Missing body\"}"); return; }
            if (s.getStation_ID() == 0) {
                int newId = dao.insertStation(s);
                if (newId > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    java.util.HashMap<String, Object> m = new java.util.HashMap<>();
                    m.put("status", "success"); m.put("id", newId);
                    out.print(gson.toJson(m));
                }
                else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print("{\"status\":\"fail\"}"); }
            } else {
                boolean ok = dao.updateStation(s);
                if (ok) { resp.setStatus(HttpServletResponse.SC_OK); out.print("{\"status\":\"success\"}"); }
                else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print("{\"status\":\"fail\"}"); }
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        Users u = (Users) req.getSession().getAttribute("User");
        if (u == null || u.getRole() == null || !u.getRole().trim().equalsIgnoreCase("admin")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) { out.print("{\"status\":\"fail\",\"message\":\"Admin only\"}"); }
            return;
        }
        String idParam = req.getParameter("id");
        try (PrintWriter out = resp.getWriter()) {
            if (idParam == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print("{\"status\":\"fail\",\"message\":\"id required\"}"); return; }
            int id = Integer.parseInt(idParam);
            boolean ok = dao.deleteStation(id);
            if (ok) { resp.setStatus(HttpServletResponse.SC_OK); out.print("{\"status\":\"success\"}"); } else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print("{\"status\":\"fail\"}"); }
        }
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
