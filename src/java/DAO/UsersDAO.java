package DAO;

import DTO.Users;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import mylib.DBUtils;

public class UsersDAO {

    public boolean updatePasswordIfStaffOrManager(int userId, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE ID = ? AND Role IN ('Staff','Manager')";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Users checkLogin(String email, String password) {
        Users user = null;
        String query = "SELECT ID, FullName, Phone, Email, Password, Role, Status, Station_ID "
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
                            rs.getString("Status"), // lấy status
                            rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null
                    );
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
        String sql = "INSERT INTO Users(FullName, Phone, Email, Password, Role, Station_ID) VALUES (?, ?, ?, ?, ?, ?)";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getRole());
            ps.setNull(6, java.sql.Types.INTEGER);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Users getUserByEmail(String email) {
        Users user = null;
        String sql = "SELECT ID, FullName, Phone, Email, Password, Role, Status, Station_ID "
                + "FROM Users WHERE Email = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new Users();
                    user.setId(rs.getInt("ID"));                  // ✅ phải set ID
                    user.setFullName(rs.getString("FullName"));
                    user.setPhone(rs.getString("Phone"));
                    user.setEmail(rs.getString("Email"));
                    user.setPassword(rs.getString("Password"));
                    user.setRole(rs.getString("Role"));
                    user.setStatus(rs.getString("Status"));
                    Object st = rs.getObject("Station_ID");
                    if (st != null) {
                        user.setStationId(rs.getInt("Station_ID"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    // ✅ Lấy tên người dùng (FullName) theo ID
    public String getUsernameById(int userId) {
        String sql = "SELECT FullName FROM Users WHERE ID = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("FullName");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] getUsernameById: " + e.getMessage());
        }
        return null;
    }

    // Lấy Station_ID của user theo userId
    public int getStationIdByUserId(int userId) throws ClassNotFoundException, SQLException {
        String sql = "SELECT Station_ID FROM Users WHERE ID = ?"; // 👈 SỬA LẠI TÊN CỘT Ở ĐÂY
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }
    
    
}


