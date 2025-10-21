package controller;

import DAO.PackageDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/package-revenue-statistics")
public class getPackageRevenueStatisticsController extends HttpServlet {

    private final PackageDAO packageDAO = new PackageDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            List<Object[]> statistics = packageDAO.getPackageRevenueStatistics();

            if (statistics != null && !statistics.isEmpty()) {
                // Convert List<Object[]> to JSON
                String json = gson.toJson(statistics);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"data\":" + json + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.print("{\"status\":\"success\",\"message\":\"No revenue data found\",\"data\":[]}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: "
                        + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
