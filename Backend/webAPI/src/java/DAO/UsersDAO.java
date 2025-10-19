package DAO;

import DTO.Users;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mylib.DBUtils;

public class UsersDAO {

    public Users checkLogin(String email, String password) {
        Users user = null;
        String query = "SELECT ID, FullName, Phone, Email, Password, Role, Station_ID "
                + "FROM Users WHERE Email = ? AND Password = ?";

        try ( Connection connect = DBUtils.getConnection();  PreparedStatement ps = connect.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new Users(
                            rs.getInt("ID"),
                            rs.getString("FullName"),
                            rs.getString("Phone"),
                            rs.getString("Email"),
                            rs.getString("Password"),
                            rs.getString("Role"),
                            rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null
                    );
                    System.out.println("[DEBUG UsersDAO] Login successful: ID=" + user.getId() + ", Role=" + user.getRole() + ", FullName=" + user.getFullName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT ID FROM Users WHERE Email=?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertUser(Users u) {
        // SQL Server specific: Use OUTPUT clause to get generated ID
        String sql = "INSERT INTO Users(FullName, Phone, Email, Password, Role, Station_ID) OUTPUT INSERTED.ID VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            
            System.out.println("[UsersDAO] insertUser - START");
            System.out.println("[UsersDAO] Email: " + u.getEmail());
            System.out.println("[UsersDAO] Role: " + u.getRole());
            
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getRole());
            
            // SQL Server: Station_ID có thể cần xử lý khác
            if (u.getStationId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, u.getStationId());
            }

            System.out.println("[UsersDAO] Executing SQL with OUTPUT clause");
            
            // Execute and get result set with ID directly
            rs = ps.executeQuery();
            
            if (rs.next()) {
                int newId = rs.getInt(1);
                System.out.println("[UsersDAO] ✅ Insert successful! New user ID: " + newId);
                return newId;
            } else {
                System.err.println("[UsersDAO] ❌ Insert executed but no ID returned!");
                return -1;
            }
        } catch (Exception e) {
            System.err.println("[UsersDAO] ❌ INSERT FAILED - Exception: " + e.getClass().getName());
            System.err.println("[UsersDAO] Message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                System.err.println("[UsersDAO] Error closing resources: " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * Cập nhật mật khẩu cho user
     * @param email Email của user
     * @param newPassword Mật khẩu mới
     * @return true nếu cập nhật thành công
     */
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, email);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println("[UsersDAO] Password updated for email: " + email);
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.err.println("[UsersDAO] Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra email đã tồn tại trong hệ thống chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    public boolean checkEmailExists(String email) {
        return existsByEmail(email);
    }

    /**
     * Tạo tài khoản người dùng mới
     * @param fullName Họ tên
     * @param email Email
     * @param phone Số điện thoại
     * @param password Mật khẩu (đã mã hóa)
     * @param role Vai trò (Customer, Admin, Staff, etc.)
     * @return true nếu tạo thành công
     */
    public boolean createUser(String fullName, String email, String phone, String password, String role) {
        Users newUser = new Users();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setPassword(password);
        newUser.setRole(role);
        newUser.setStationId(null); // Station_ID mặc định null cho user mới
        
        int userId = insertUser(newUser);
        return userId > 0;
    }
}

