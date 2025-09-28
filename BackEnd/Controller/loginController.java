package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/login")
public class loginController extends HttpServlet {

    private final UsersDAO usersDAO = new UsersDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try ( PrintWriter out = response.getWriter();  BufferedReader reader = request.getReader()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            LoginRequest loginReq = gson.fromJson(sb.toString(), LoginRequest.class);

            if (loginReq == null || loginReq.email == null || loginReq.password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Missing email or password\"}");
                return;
            }

            Users user = usersDAO.checkLogin(loginReq.email, loginReq.password);

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("User", user);
                String json = gson.toJson(user);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"user\":" + json + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"Invalid email or password\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try ( PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: "
                        + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    //private void setCorsHeaders(HttpServletResponse response) {
    //    response.setHeader("Access-Control-Allow-Origin", "*");
    //   response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    //    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    //}
  private void setCorsHeaders(HttpServletResponse response) {
    response.setHeader("Access-Control-Allow-Origin", "*"); // cho mọi origin
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    // bỏ Access-Control-Allow-Credentials khi dùng *
}

    private static class LoginRequest {

        String email;
        String password;
    }
}
