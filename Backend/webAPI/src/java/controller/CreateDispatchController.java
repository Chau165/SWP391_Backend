package controller;

import DAO.DispatchDAO;
import DTO.DispatchLog;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "CreateDispatchController", urlPatterns = {"/api/dispatch/create"})
public class CreateDispatchController extends HttpServlet {

    private final DispatchDAO dao = new DispatchDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setContentType("application/json;charset=UTF-8");

        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            DispatchLog d = gson.fromJson(reader, DispatchLog.class);
            if (d == null || d.getStation_ID() == 0 || d.getQuantity() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","Invalid payload"); }}));
                return;
            }

            d.setStatus("Pending");
            int id = dao.createDispatchRequest(d);
            if (id > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","ok"); put("id", id); }}));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","Could not create dispatch request"); }}));
            }
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","error"); put("message", ex.getMessage()); }}));
        }
    }
}
