package controller;

import DAO.BatteryDAO;
import DTO.Battery;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "BatteryController", urlPatterns = {"/api/admin/battery"})
public class BatteryController extends HttpServlet {
    private final BatteryDAO dao = new BatteryDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        List<Battery> list = dao.getAll();
        try (PrintWriter out = response.getWriter()) { out.print(gson.toJson(list)); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            Battery b = gson.fromJson(reader, Battery.class);
            boolean ok = dao.create(b);
            JsonObject res = new JsonObject();
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Battery created"); response.setStatus(HttpServletResponse.SC_CREATED); }
            else { res.addProperty("status","fail"); res.addProperty("message","Create failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            Battery b = gson.fromJson(reader, Battery.class);
            boolean ok = dao.update(b);
            JsonObject res = new JsonObject();
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Battery updated"); response.setStatus(HttpServletResponse.SC_OK); }
            else { res.addProperty("status","fail"); res.addProperty("message","Update failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        String idStr = request.getParameter("id");
        try (PrintWriter out = response.getWriter()) {
            JsonObject res = new JsonObject();
            if (idStr == null) { res.addProperty("status","fail"); res.addProperty("message","Missing id"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(res)); return; }
            int id = Integer.parseInt(idStr);
            boolean ok = dao.delete(id);
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Battery deleted"); response.setStatus(HttpServletResponse.SC_OK); }
            else { res.addProperty("status","fail"); res.addProperty("message","Delete failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        }
    }
}
