package controller;

import DAO.UsersDAO;
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

@WebServlet(name = "AssignStaffController", urlPatterns = {"/api/admin/assignStaff"})
public class AssignStaffController extends HttpServlet {

    private final UsersDAO usersDAO = new UsersDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        DTO.Users u = (DTO.Users) req.getSession().getAttribute("User");
        if (u == null || u.getRole() == null || !u.getRole().trim().equalsIgnoreCase("admin")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) { out.print("{\"status\":\"fail\",\"message\":\"Admin only\"}"); }
            return;
        }

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            java.util.Map body = gson.fromJson(reader, java.util.Map.class);
            Integer userId = body.get("userId") == null ? null : ((Number) body.get("userId")).intValue();
            Integer stationId = body.get("stationId") == null ? null : ((Number) body.get("stationId")).intValue();
            if (userId == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print("{\"status\":\"fail\",\"message\":\"userId required\"}"); return; }
            boolean ok = usersDAO.assignUserToStation(userId, stationId);
            if (ok) { resp.setStatus(HttpServletResponse.SC_OK); out.print("{\"status\":\"success\"}"); } else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print("{\"status\":\"fail\"}"); }
        }
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
