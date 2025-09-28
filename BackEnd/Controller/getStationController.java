package controller;

import DAO.StationDAO;
import DTO.Station;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "getStationController", urlPatterns = {"/api/getstations"})
public class getStationController extends HttpServlet {

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

        try ( PrintWriter out = response.getWriter()) {
            List<Station> stations = stationDAO.getAllStation();

            if (stations.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.print("{\"status\":\"fail\",\"message\":\"No station found\"}");
            } else {
                String json = gson.toJson(stations);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(json);
            }
        }
    }

}
