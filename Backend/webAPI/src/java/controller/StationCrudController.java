package controller;

import DAO.StationDAO;
import DTO.Station;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "StationCrudController", urlPatterns = {"/api/station"})
/**
 * API CRUD cho Station (trạm):
 * - POST   /api/station    : tạo mới trạm
 * - PUT    /api/station    : cập nhật trạm
 * - DELETE /api/station?id= : xóa trạm theo id
 *
 * Body request/response đều là JSON.
 * Đảm bảo đã add gson.jar và servlet-api.jar vào project.
 */
public class StationCrudController extends HttpServlet {
    private final StationDAO stationDAO = new StationDAO();
    private final Gson gson = new Gson();

    // Cho phép CORS và các method RESTful
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Tạo mới trạm
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            BufferedReader reader = request.getReader();
            Station station = gson.fromJson(reader, Station.class);
            boolean success = stationDAO.createStation(station);
            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"status\":\"success\",\"message\":\"Station created\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Create failed\"}");
            }
        }
    }

    // Cập nhật trạm
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            BufferedReader reader = request.getReader();
            Station station = gson.fromJson(reader, Station.class);
            boolean success = stationDAO.updateStation(station);
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Station updated\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Update failed\"}");
            }
        }
    }

    // Xóa trạm
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String idParam = request.getParameter("id");
            boolean success = false;
            if (idParam != null) {
                try {
                    int id = Integer.parseInt(idParam);
                    success = stationDAO.deleteStation(id);
                } catch (NumberFormatException e) {
                    // ignore, sẽ trả về fail
                }
            }
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Station deleted\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Delete failed\"}");
            }
        }
    }
}
