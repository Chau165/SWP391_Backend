package controller;

import DAO.DispatchDAO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ProcessDispatchController", urlPatterns = {"/api/dispatch/process"})
public class ProcessDispatchController extends HttpServlet {

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
            java.util.Map body = gson.fromJson(reader, java.util.Map.class);
            if (body == null || !body.containsKey("id") || !body.containsKey("action")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","Invalid payload"); }}));
                return;
            }
            int id = ((Number) body.get("id")).intValue();
            String action = (String) body.get("action"); // "approve" | "reject" | "assign"
            Integer assignedFrom = body.containsKey("fromStationId") && body.get("fromStationId") != null ? ((Number) body.get("fromStationId")).intValue() : null;
            Integer processedBy = body.containsKey("processedBy") && body.get("processedBy") != null ? ((Number) body.get("processedBy")).intValue() : null;

            if ("approve".equalsIgnoreCase(action)) {
                // Try to decrement from assignedFrom if provided, otherwise try to decrement from same station
                boolean ok = false;
                if (assignedFrom != null) {
                    ok = dao.decrementStationBattery(assignedFrom, ((Number) body.getOrDefault("quantity", 0)).intValue());
                }
                if (!ok) {
                    // decrement from requested station
                    ok = dao.decrementStationBattery(((Number) body.getOrDefault("stationId", 0)).intValue(), ((Number) body.getOrDefault("quantity", 0)).intValue());
                }
                if (!ok) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","Not enough battery available"); }}));
                    return;
                }
                dao.updateDispatchStatus(id, "Approved", processedBy, assignedFrom);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","ok"); }}));
                return;
            } else if ("reject".equalsIgnoreCase(action)) {
                dao.updateDispatchStatus(id, "Rejected", processedBy, null);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","ok"); }}));
                return;
            } else if ("assign".equalsIgnoreCase(action)) {
                if (assignedFrom == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","fromStationId required for assign"); }}));
                    return;
                }
                boolean ok = dao.decrementStationBattery(assignedFrom, ((Number) body.getOrDefault("quantity", 0)).intValue());
                if (!ok) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","Not enough battery available in source station"); }}));
                    return;
                }
                dao.updateDispatchStatus(id, "Assigned", processedBy, assignedFrom);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","ok"); }}));
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","fail"); put("message","Unknown action"); }}));
                return;
            }

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(new java.util.HashMap<String, Object>() {{ put("status","error"); put("message", ex.getMessage()); }}));
        }
    }
}
