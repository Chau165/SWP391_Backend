package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@WebServlet(name = "StaffController", urlPatterns = {"/api/admin/staff"})
@MultipartConfig(fileSizeThreshold = 1024 * 50, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
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
        try (PrintWriter out = response.getWriter()) {
            // Detect multipart vs JSON
            String ct = request.getContentType();
            Users u = null;
            if (ct != null && ct.toLowerCase().startsWith("multipart/")) {
                // parse form fields
                String fullName = request.getParameter("fullName");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String password = request.getParameter("password");
                String role = request.getParameter("role");
                String status = request.getParameter("status");
                String avatarUrl = request.getParameter("avatarUrl");

                u = new Users();
                u.setFullName(fullName);
                u.setEmail(email);
                u.setPhone(phone);
                if (password != null && !password.trim().isEmpty()) {
                    u.setPassword(util.PasswordUtil.hashPassword(password));
                }
                u.setRole(role);
                u.setStatus(status);

                Part avatarPart = null;
                try { avatarPart = request.getPart("avatarFile"); } catch(Exception ex) { avatarPart = null; }
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String uploadsPath = request.getServletContext().getRealPath("/resources/images/uploads");
                    File dir = new File(uploadsPath);
                    if (!dir.exists()) dir.mkdirs();
                    String submitted = avatarPart.getSubmittedFileName();
                    String ext = "";
                    int dot = submitted != null ? submitted.lastIndexOf('.') : -1;
                    if (dot >= 0) ext = submitted.substring(dot);
                    String filename = "avatar_staff_" + System.currentTimeMillis() + ext;
                    File outFile = new File(dir, filename);
                    try (InputStream in = avatarPart.getInputStream()) { Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING); }
                    avatarUrl = request.getContextPath() + "/resources/images/uploads/" + filename;
                    u.setAvatarUrl(avatarUrl);
                } else if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    u.setAvatarUrl(avatarUrl);
                }
            } else {
                // JSON
                try (BufferedReader reader = request.getReader()) {
                    u = gson.fromJson(reader, Users.class);
                }
            }

            JsonObject res = new JsonObject();
            if (u == null) { res.addProperty("status","fail"); res.addProperty("message","Missing body"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(res)); return; }
            boolean ok = usersDAO.createStaff(u);
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Staff created"); response.setStatus(HttpServletResponse.SC_CREATED); }
            else { res.addProperty("status","fail"); res.addProperty("message","Create failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(java.util.Collections.singletonMap("error", e.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String ct = request.getContentType();
            Users u = null;
            if (ct != null && ct.toLowerCase().startsWith("multipart/")) {
                String idStr = request.getParameter("id");
                if (idStr == null) { response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(java.util.Collections.singletonMap("error","id required"))); return; }
                int id = Integer.parseInt(idStr);
                u = new Users(); u.setId(id);
                u.setFullName(request.getParameter("fullName"));
                u.setEmail(request.getParameter("email"));
                u.setPhone(request.getParameter("phone"));
                u.setRole(request.getParameter("role"));
                u.setStatus(request.getParameter("status"));
                String password = request.getParameter("password");
                if (password != null && !password.trim().isEmpty()) u.setPassword(util.PasswordUtil.hashPassword(password));

                String avatarUrl = request.getParameter("avatarUrl");
                Part avatarPart = null;
                try { avatarPart = request.getPart("avatarFile"); } catch(Exception ex) { avatarPart = null; }
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String uploadsPath = request.getServletContext().getRealPath("/resources/images/uploads");
                    File dir = new File(uploadsPath); if (!dir.exists()) dir.mkdirs();
                    String submitted = avatarPart.getSubmittedFileName(); String ext = ""; int dot = submitted != null ? submitted.lastIndexOf('.') : -1; if (dot >= 0) ext = submitted.substring(dot);
                    String filename = "avatar_staff_" + id + "_" + System.currentTimeMillis() + ext;
                    File outFile = new File(dir, filename);
                    try (InputStream in = avatarPart.getInputStream()) { Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING); }
                    avatarUrl = request.getContextPath() + "/resources/images/uploads/" + filename;
                }
                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) u.setAvatarUrl(avatarUrl);
            } else {
                try (BufferedReader reader = request.getReader()) { u = gson.fromJson(reader, Users.class); }
            }
            JsonObject res = new JsonObject();
            if (u == null) { res.addProperty("status","fail"); res.addProperty("message","Missing body"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(res)); return; }
            boolean ok = usersDAO.updateStaff(u);
            if (ok) { res.addProperty("status","success"); res.addProperty("message","Staff updated"); response.setStatus(HttpServletResponse.SC_OK); }
            else { res.addProperty("status","fail"); res.addProperty("message","Update failed"); response.setStatus(HttpServletResponse.SC_BAD_REQUEST); }
            out.print(gson.toJson(res));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(java.util.Collections.singletonMap("error", e.getMessage())));
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
