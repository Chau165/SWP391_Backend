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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "getUserStationsController", urlPatterns = {"/api/getUserStations"})
public class getUserStationsController extends HttpServlet {

    private final StationDAO stationDAO = new StationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setContentType("application/json;charset=UTF-8");

        Users u = (Users) request.getSession().getAttribute("User");
        if (u == null) {
            // not logged in, return 401 so frontend can fallback
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try (PrintWriter out = response.getWriter()) {
                out.print("[]");
            }
            return;
        }

        int userId = u.getId();
        List<Station> stations = StationDAO.getStationsByUserId(userId);

        try (PrintWriter out = response.getWriter()) {
            String json = gson.toJson(stations);
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(json);
        }
    }

}
