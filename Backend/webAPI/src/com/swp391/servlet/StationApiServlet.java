package com.swp391.servlet;

import com.swp391.dao.StationDAO;
import com.swp391.model.Station;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "StationApiServlet", urlPatterns = {"/admin/api/stations"})
public class StationApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String search = req.getParameter("search");
        try {
            StationDAO dao = new StationDAO();
            List<Station> list = dao.getAll(search);
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (Station s : list) {
                if (!first) sb.append(','); first = false;
                String name = s.getName() == null ? "" : s.getName().replace("\"", "\\\"");
                String addr = s.getAddress() == null ? "" : s.getAddress().replace("\"", "\\\"");
                sb.append('{')
                        .append("\"Station_ID\":").append(s.getStationId())
                        .append(",\"Name\":\"").append(name).append('\"')
                        .append(",\"Address\":\"").append(addr).append('\"')
                        .append(",\"TotalSlots\":").append(s.getTotalSlots())
                        .append(",\"TotalCapacity\":").append(s.getTotalCapacity())
                        .append(",\"TotalPower\":").append(s.getTotalPower())
                        .append('}');
            }
            sb.append("]");
            resp.getWriter().print(sb.toString());
        } catch (Exception e) {
            resp.setStatus(500);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "";
            resp.getWriter().print("{\"error\":\"" + msg + "\"}");
        }
    }
}
