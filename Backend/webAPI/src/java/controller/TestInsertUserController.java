package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Test controller để debug insert user vào database
 * URL: /api/test-insert-user
 */
@WebServlet("/api/test-insert-user")
public class TestInsertUserController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            Gson gson = new Gson();
            
            System.out.println("========================================");
            System.out.println("[TestInsertUser] Starting test insert...");
            
            // Tạo user test
            Users testUser = new Users();
            testUser.setFullName("Test User Debug");
            testUser.setPhone("0999888777");
            testUser.setEmail("debug_" + System.currentTimeMillis() + "@test.com"); // Email unique
            testUser.setPassword("test123");
            testUser.setRole("Driver");
            testUser.setStationId(null);
            
            System.out.println("[TestInsertUser] Test user created:");
            System.out.println("  - Email: " + testUser.getEmail());
            System.out.println("  - Role: " + testUser.getRole());
            
            // Thử insert
            UsersDAO dao = new UsersDAO();
            int newId = dao.insertUser(testUser);
            
            System.out.println("[TestInsertUser] Insert result: " + newId);
            System.out.println("========================================");
            
            // Trả về kết quả
            if (newId > 0) {
                resp.setStatus(200);
                out.print(gson.toJson(new TestResult(
                    true, 
                    "Insert successful!", 
                    newId, 
                    testUser.getEmail()
                )));
            } else {
                resp.setStatus(500);
                out.print(gson.toJson(new TestResult(
                    false, 
                    "Insert failed - returned ID: " + newId, 
                    newId, 
                    testUser.getEmail()
                )));
            }
            
        } catch (Exception e) {
            System.err.println("[TestInsertUser] ❌ Exception: " + e.getMessage());
            e.printStackTrace();
            
            resp.setStatus(500);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"success\":false,\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    // Inner class cho JSON response
    private static class TestResult {
        boolean success;
        String message;
        int userId;
        String email;
        
        public TestResult(boolean success, String message, int userId, String email) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.email = email;
        }
    }
}
