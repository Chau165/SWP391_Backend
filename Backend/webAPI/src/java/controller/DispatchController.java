package controller;

import DAO.DispatchRequestDAO;
import DTO.DispatchRequest;
import DTO.Users;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "DispatchController", urlPatterns = {"/api/dispatch/*"})
public class DispatchController extends HttpServlet {
    private final DispatchRequestDAO dao = new DispatchRequestDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // station creates a dispatch request
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            DispatchRequest req = gson.fromJson(reader, DispatchRequest.class);
            int id = dao.createRequest(req);
            JsonObject res = new JsonObject();
            if (id > 0) { res.addProperty("status","success"); res.addProperty("id", id); response.setStatus(HttpServletResponse.SC_CREATED); }
            else { res.addProperty("status","fail"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // admin lists requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        List<DispatchRequest> list = dao.getAllRequests();
        try (PrintWriter out = response.getWriter()) { out.print(gson.toJson(list)); }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // admin approve/reject
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo(); // /{id}/approve or /{id}/reject
        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            JsonObject res = new JsonObject();
            if (path == null) { res.addProperty("status","fail"); res.addProperty("message","Missing path"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(res)); return; }
            String[] parts = path.split("/");
            if (parts.length < 2) { res.addProperty("status","fail"); res.addProperty("message","Invalid path"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(res)); return; }
            int id = Integer.parseInt(parts[1]);
            String action = parts.length >=3 ? parts[2] : "";
            HttpSession session = request.getSession(false);
            Users admin = session != null ? (Users) session.getAttribute("User") : null;
            int adminId = admin != null ? admin.getId() : -1;

            if ("approve".equalsIgnoreCase(action)) {
                boolean ok = dao.approveRequest(id, adminId);
                if (ok) { res.addProperty("status","success"); response.setStatus(HttpServletResponse.SC_OK); }
                else { res.addProperty("status","fail"); res.addProperty("message","Approve failed or insufficient supply"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
                out.print(gson.toJson(res));
                return;
            } else if ("reject".equalsIgnoreCase(action)) {
                // read reason
                JsonObject body = gson.fromJson(reader, JsonObject.class);
                String reason = body.has("reason") ? body.get("reason").getAsString() : null;
                boolean ok = dao.rejectRequest(id, adminId, reason);
                if (ok) { res.addProperty("status","success"); response.setStatus(HttpServletResponse.SC_OK); }
                else { res.addProperty("status","fail"); res.addProperty("message","Reject failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
                out.print(gson.toJson(res));
                return;
            } else {
                res.addProperty("status","fail"); res.addProperty("message","Unknown action"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(res)); return;
            }
        }
    }
}
