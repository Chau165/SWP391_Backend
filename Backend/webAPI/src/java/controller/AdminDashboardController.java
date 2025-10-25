package controller;

import DTO.Users;
import com.google.gson.Gson;
import util.CorsUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AdminDashboardController", urlPatterns = {"/api/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CorsUtil.setCors(resp, req);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    CorsUtil.setCors(resp, req);
        resp.setContentType("application/json;charset=UTF-8");

        Users u = (Users) req.getSession().getAttribute("User");
        if (u == null || u.getRole() == null || !u.getRole().trim().equalsIgnoreCase("admin")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) {
                java.util.HashMap<String,String> m = new java.util.HashMap<>();
                m.put("status","fail"); m.put("message","Admin only");
                out.print(gson.toJson(m));
            }
            return;
        }

        // Return metadata describing available admin APIs (frontend will use these)
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("status", "ok");
        payload.put("features", new String[]{
            "/api/admin/station (GET/POST/DELETE)",
            "/api/admin/updateBattery (POST)",
            "/api/admin/assignStaff (POST)",
            "/api/admin/stationAnalytics (GET)",
            "/api/admin/users?role=Staff (GET)"
        });

        try (PrintWriter out = resp.getWriter()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(payload));
        }
    }

    // CORS handled by util.CorsUtil
}
