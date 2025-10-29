package controller;

import DAO.UsersDAO;
import DTO.Users;
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

@WebServlet(name = "StaffController", urlPatterns = {"/api/admin/staff"})
public class StaffController extends HttpServlet {
    private final UsersDAO usersDAO = new UsersDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        List<Users> staff = usersDAO.getUsersByRole("Staff");
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(staff));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            Users u = gson.fromJson(reader, Users.class);
            boolean ok = usersDAO.createStaff(u);
            JsonObject res = new JsonObject();
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Staff created"); response.setStatus(HttpServletResponse.SC_CREATED); }
            else { res.addProperty("status","fail"); res.addProperty("message","Create failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = request.getReader(); PrintWriter out = response.getWriter()) {
            Users u = gson.fromJson(reader, Users.class);
            boolean ok = usersDAO.updateStaff(u);
            JsonObject res = new JsonObject();
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Staff updated"); response.setStatus(HttpServletResponse.SC_OK); }
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
            boolean ok = usersDAO.deleteUser(id);
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Staff deleted"); response.setStatus(HttpServletResponse.SC_OK); }
            else { res.addProperty("status","fail"); res.addProperty("message","Delete failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        }
    }
}
