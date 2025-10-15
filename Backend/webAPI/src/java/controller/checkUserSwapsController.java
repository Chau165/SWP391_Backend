package controller;

import DAO.SwapTransactionDAO;
import DAO.SwapTransactionDAO.StationWithSwap;
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

@WebServlet(name = "checkUserSwapsController", urlPatterns = {"/api/checkUserSwaps"})
public class checkUserSwapsController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setContentType("application/json;charset=UTF-8");

        Users u = (Users) request.getSession().getAttribute("User");
        try (PrintWriter out = response.getWriter()) {
            if (u == null) {
                System.out.println("DEBUG checkUserSwapsController - No User in session");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson(new StationWithSwap[0]));
                return;
            }

            int userId = u.getId();
            String role = u.getRole();
            System.out.println("DEBUG checkUserSwapsController - User: ID=" + userId + ", Role=" + role);

            // Only drivers are allowed to comment. If the logged user is Staff, return 403 so frontend hides comment UI.
            if (role == null || !role.equalsIgnoreCase("driver")) {
                System.out.println("DEBUG checkUserSwapsController - User is not Driver, returning 403");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(new StationWithSwap[0]));
                return;
            }

            System.out.println("DEBUG checkUserSwapsController - Fetching stations for user...");
            List<StationWithSwap> list = SwapTransactionDAO.getStationsWithSwapIdsByUser(userId, role);
            System.out.println("DEBUG checkUserSwapsController - Stations found: " + (list != null ? list.size() : 0));
            
            if (list == null || list.isEmpty()) {
                System.out.println("DEBUG checkUserSwapsController - No completed swaps, returning 204");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.print(gson.toJson(new StationWithSwap[0]));
                return;
            }

            System.out.println("DEBUG checkUserSwapsController - Returning " + list.size() + " stations, status 200");
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(list));
        }
    }
}
