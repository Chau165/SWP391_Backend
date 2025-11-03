package DAO;

import DTO.Users;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mylib.DBUtils;

public class UsersDAO {

    public Users checkLogin(String email, String password) {
        Users user = null;
    String query = "SELECT ID, FullName, Phone, Email, Password, Role, Station_ID, Status, Avatar_URL "
        + "FROM Users WHERE Email = ?";

        try (Connection connect = DBUtils.getConnection(); PreparedStatement ps = connect.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPass = rs.getString("Password");
                    String role = rs.getString("Role"); if (role != null) role = role.trim();

                    // compute hashed incoming password
                    String hashedIncoming = null;
                    try { hashedIncoming = util.PasswordUtil.hashSHA256(password); } catch (Throwable t) { hashedIncoming = null; }

                    boolean match = false;
                    if (storedPass != null) {
                        // direct match (DB contains plaintext)
                        if (storedPass.equals(password)) match = true;
                        // hashed match (DB stores SHA-256 hex)
                        else if (hashedIncoming != null && storedPass.equalsIgnoreCase(hashedIncoming)) match = true;
                    }

                    if (match) {
                        user = new Users(
                                rs.getInt("ID"),
                                rs.getString("FullName"),
                                rs.getString("Phone"),
                                rs.getString("Email"),
                                rs.getString("Password"),
                                role,
                                rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null
                        );
                        // populate optional fields Status and Avatar_URL so callers (login/filter) can use them
                        try { user.setStatus(rs.getString("Status")); } catch (Exception ex) { /* ignore */ }
                        try { user.setAvatarUrl(rs.getString("Avatar_URL")); } catch (Exception ex) { /* ignore */ }
                        System.out.println("[DEBUG UsersDAO] Login successful: ID=" + user.getId() + ", Role=" + user.getRole() + ", FullName=" + user.getFullName() + ", Status=" + user.getStatus());
                    }
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

    /** Get user by email (full fields) */
    public Users getUserByEmail(String email) {
        String sql = "SELECT ID, FullName, Phone, Email, Password, Role, Station_ID, Status, Avatar_URL FROM Users WHERE Email = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String r = rs.getString("Role"); if (r != null) r = r.trim();
                    Users u = new Users(
                            rs.getInt("ID"),
                            rs.getString("FullName"),
                            rs.getString("Phone"),
                            rs.getString("Email"),
                            rs.getString("Password"),
                            r,
                            rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null
                    );
                    try { u.setStatus(rs.getString("Status")); } catch (Exception ex) {}
                    try { u.setAvatarUrl(rs.getString("Avatar_URL")); } catch (Exception ex) {}
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Update user's status by email */
    public boolean updateStatusByEmail(String email, String status) {
        String sql = "UPDATE Users SET Status = ? WHERE Email = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status == null) ps.setNull(1, java.sql.Types.VARCHAR); else ps.setString(1, status);
            ps.setString(2, email);
            int row = ps.executeUpdate();
            System.out.println("[UsersDAO] updateStatusByEmail: email=" + email + ", status=" + status + ", rows=" + row);
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Check whether a phone number already exists in Users table */
    public boolean existsByPhone(String phone) {
        if (phone == null) return false;
        String sql = "SELECT ID FROM Users WHERE Phone = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertUser(Users u) {
        // SQL Server specific: Use OUTPUT clause to get generated ID
        // Include Status and Avatar_URL to satisfy DB NOT NULL constraints and store avatar
        String sql = "INSERT INTO Users(FullName, Phone, Email, Password, Role, Station_ID, Status, Avatar_URL) OUTPUT INSERTED.ID VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            
            System.out.println("[UsersDAO] insertUser - START");
            System.out.println("[UsersDAO] Email: " + u.getEmail());
            String roleToInsert = u.getRole();
            if (roleToInsert != null) roleToInsert = roleToInsert.trim();
            System.out.println("[UsersDAO] Role: " + roleToInsert);
            
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, roleToInsert);
            // SQL Server: Station_ID có thể cần xử lý khác
            if (u.getStationId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, u.getStationId());
            }
            // Status
            if (u.getStatus() == null) ps.setNull(7, java.sql.Types.VARCHAR); else ps.setString(7, u.getStatus());
            // Avatar URL
            if (u.getAvatarUrl() == null) ps.setNull(8, java.sql.Types.VARCHAR); else ps.setString(8, u.getAvatarUrl());

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

    /** Create user from Users object (preserves Status and Avatar_URL) */
    public boolean createUser(Users u) {
        if (u == null) return false;
        if (u.getRole() == null || u.getRole().trim().isEmpty()) u.setRole("User");
        int userId = insertUser(u);
        return userId > 0;
    }

    // --- Staff/Admin helpers used by StaffController ---
    public java.util.List<Users> getUsersByRole(String role) {
        java.util.List<Users> list = new java.util.ArrayList<>();
        String sql = "SELECT ID, FullName, Phone, Email, Password, Role, Station_ID, Status, Avatar_URL FROM Users WHERE Role = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String r = rs.getString("Role"); if (r != null) r = r.trim();
                    Users u = new Users(
                            rs.getInt("ID"),
                            rs.getString("FullName"),
                            rs.getString("Phone"),
                            rs.getString("Email"),
                            rs.getString("Password"),
                            r,
                            rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null
                    );
                    // set optional fields if present
                    try { u.setStatus(rs.getString("Status")); } catch(Exception ex) {}
                    try { u.setAvatarUrl(rs.getString("Avatar_URL")); } catch(Exception ex) {}
                    list.add(u);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createStaff(Users u) {
        // set role to Staff defensively
        if (u.getRole() == null || u.getRole().trim().isEmpty()) u.setRole("Staff"); else u.setRole(u.getRole().trim());
        int id = insertUser(u);
        return id > 0;
    }

    public boolean updateStaff(Users u) {
        // Only update Password when a non-empty password is provided; otherwise keep existing Password value
        String sql = "UPDATE Users SET FullName = ?, Phone = ?, Email = ?, Password = CASE WHEN ? IS NULL OR ? = '' THEN Password ELSE ? END, Station_ID = ?, Role = ?, Status = ?, Avatar_URL = ? WHERE ID = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getEmail());
            // for CASE WHEN checks and assignment (duplicate password param)
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getPassword());
            ps.setString(6, u.getPassword());
            if (u.getStationId() == null) ps.setNull(7, java.sql.Types.INTEGER); else ps.setInt(7, u.getStationId());
            ps.setString(8, u.getRole());
            ps.setString(9, u.getStatus());
            ps.setString(10, u.getAvatarUrl());
            ps.setInt(11, u.getId());
            int row = ps.executeUpdate();
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM Users WHERE ID = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int row = ps.executeUpdate();
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get user by ID with full fields
     */
    public Users getUserById(int id) {
        String sql = "SELECT ID, FullName, Phone, Email, Password, Role, Station_ID, Status, Avatar_URL FROM Users WHERE ID = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String r = rs.getString("Role"); if (r != null) r = r.trim();
                    Users u = new Users(
                            rs.getInt("ID"),
                            rs.getString("FullName"),
                            rs.getString("Phone"),
                            rs.getString("Email"),
                            rs.getString("Password"),
                            r,
                            rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null
                    );
                    try { u.setStatus(rs.getString("Status")); } catch (Exception ex) {}
                    try { u.setAvatarUrl(rs.getString("Avatar_URL")); } catch (Exception ex) {}
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Count number of admins currently in Users table */
    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM Users WHERE Role = 'Admin'";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Assign a user to a station (set Station_ID) - used by admin */
    public boolean assignUserToStation(int userId, Integer stationId) {
        String sql = "UPDATE Users SET Station_ID = ? WHERE ID = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            if (stationId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, stationId);
            ps.setInt(2, userId);
            int row = ps.executeUpdate();
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Update password by user id (safe for reset flows) */
    public boolean updatePasswordById(int id, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE ID = ?";
        try (java.sql.Connection conn = DBUtils.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            int row = ps.executeUpdate();
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

