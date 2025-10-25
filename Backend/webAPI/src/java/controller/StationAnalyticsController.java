package controller;

import DAO.StationDAO;
import DTO.Users;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "StationAnalyticsController", urlPatterns = {"/api/admin/stationAnalytics"})
public class StationAnalyticsController extends HttpServlet {

    private final StationDAO dao = new StationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp); resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");
        Users u = (Users) req.getSession().getAttribute("User");
        if (u == null || u.getRole() == null || !u.getRole().trim().equalsIgnoreCase("admin")) { resp.setStatus(HttpServletResponse.SC_FORBIDDEN); try (PrintWriter out = resp.getWriter()) { out.print("{\"status\":\"fail\",\"message\":\"Admin only\"}"); } return; }

        try (PrintWriter out = resp.getWriter()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(dao.getAnalytics()));
        }
    }

    private void setCors(HttpServletResponse resp) { resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS"); resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization"); resp.setHeader("Access-Control-Allow-Credentials", "true"); }
}
