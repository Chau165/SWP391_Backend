package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

@WebServlet("/api/register")
public class registerController extends HttpServlet {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (BufferedReader reader = req.getReader();
             PrintWriter out = resp.getWriter()) {

            Gson gson = new Gson();
            Users input = gson.fromJson(reader, Users.class);

            // validate phone
            if (!PHONE_PATTERN.matcher(input.getPhone()).matches()) {
                resp.setStatus(400);
                out.print("{\"error\":\"Số điện thoại phải đúng 10 số\"}");
                return;
            }

            // validate email
            if (!EMAIL_PATTERN.matcher(input.getEmail()).matches()) {
                resp.setStatus(400);
                out.print("{\"error\":\"Email không hợp lệ\"}");
                return;
            }

            UsersDAO dao = new UsersDAO();
            if (dao.existsByEmail(input.getEmail())) {
                resp.setStatus(409);
                out.print("{\"error\":\"Email đã tồn tại\"}");
                return;
            }

            // gán role & station mặc định
            input.setRole("Driver");
            input.setStationId(null);

            int newId = dao.insertUser(input);

            resp.setStatus(201);
            out.print("{\"status\":\"success\",\"userId\":" + newId + ",\"role\":\"Driver\"}");
        }
    }
}
