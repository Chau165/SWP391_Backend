package controller;

import com.google.gson.Gson;
import util.EmailUtil;
import util.PasswordResetManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/request-reset")
public class RequestResetController extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            System.out.println("[RequestResetController] Received request: " + sb.toString());
            
            // Parse JSON
            Req r = gson.fromJson(sb.toString(), Req.class);
            if (r == null || r.email == null || r.email.isEmpty()) {
                System.err.println("[RequestResetController] Missing email in request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(new Resp("error", "Missing email")));
                out.flush();
                return;
            }
            
            System.out.println("[RequestResetController] Processing reset request for: " + r.email);
            
            // Generate OTP
            String otp = PasswordResetManager.generateOtp(r.email);
            System.out.println("[RequestResetController] Generated OTP: " + otp);
            
            // Send email
            boolean emailSent = EmailUtil.sendOtp(r.email, otp);
            
            if (emailSent) {
                System.out.println("[RequestResetController] ✅ Email sent successfully to: " + r.email);
                out.print(gson.toJson(new Resp("ok", "OTP sent")));
            } else {
                System.err.println("[RequestResetController] ❌ Failed to send email to: " + r.email);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(new Resp("error", "Failed to send email")));
            }
            out.flush();
            
        } catch (Exception e) {
            System.err.println("[RequestResetController] ❌ Exception: " + e.getClass().getName());
            System.err.println("[RequestResetController] Message: " + e.getMessage());
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(new Resp("error", "Server error: " + e.getMessage())));
            out.flush();
        }
    }

    private static class Req { String email; }
    private static class Resp { String status; String message; Resp(String s, String m){status=s;message=m;} }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
