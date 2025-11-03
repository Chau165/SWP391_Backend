package DAO;

import DTO.UserProfile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mylib.DBUtils;

/**
 * DAO để quản lý User Profile
 */
public class UserProfileDAO {

    /**
     * Lấy profile của user theo User ID
     * Sử dụng bảng Users trực tiếp, JOIN với DriverPackage và Package
     */
    public UserProfile getProfileByUserId(int userId) {
        UserProfile profile = null;
        String sql = "SELECT " +
                     "    u.ID, " +
                     "    u.FullName, " +
                     "    u.Email, " +
                     "    u.Phone, " +
                     "    u.Role, " +
                     "    u.Avatar_URL, " +
                     "    dp.Package_ID, " +
                     "    p.Name AS Package_Name, " +
                     "    dp.Start_date, " +
                     "    dp.End_date " +
                     "FROM Users u " +
                     "LEFT JOIN DriverPackage dp ON u.ID = dp.User_ID AND dp.End_date >= GETDATE() " +
                     "LEFT JOIN Package p ON dp.Package_ID = p.Package_ID " +
                     "WHERE u.ID = ?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                profile = new UserProfile();
                profile.setId(rs.getInt("ID"));
                profile.setUserId(rs.getInt("ID")); // Users.ID is the userId
                profile.setFullName(rs.getString("FullName"));
                profile.setEmail(rs.getString("Email"));
                profile.setPhone(rs.getString("Phone"));
                profile.setRole(rs.getString("Role"));
                profile.setAvatarUrl(rs.getString("Avatar_URL"));
                
                // Package info (chỉ có cho Driver có package active)
                if (rs.getObject("Package_ID") != null) {
                    profile.setCurrentPackageId(rs.getInt("Package_ID"));
                    profile.setPackageName(rs.getString("Package_Name"));
                    profile.setPackageStartDate(rs.getDate("Start_date"));
                    profile.setPackageEndDate(rs.getDate("End_date"));
                }
                
                // Users table không có Created_At/Updated_At, set null
                profile.setCreatedAt(null);
                profile.setUpdatedAt(null);
                
                System.out.println("[DEBUG UserProfileDAO] Found profile for userId=" + userId);
            } else {
                System.out.println("[DEBUG UserProfileDAO] No user found for userId=" + userId);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR UserProfileDAO] Error getting profile: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR UserProfileDAO] Database driver not found: " + e.getMessage());
            e.printStackTrace();
        }

        return profile;
    }

    /**
     * Tạo hoặc cập nhật profile khi user đăng ký/đăng nhập
     * Không cần làm gì vì Users table đã tự động có data khi register/login
     * Method này giữ lại để backward compatibility
     */
    public boolean createOrUpdateProfile(int userId, String fullName, String email, String phone, String role) {
        // Users table đã có thông tin từ register/login
        // Không cần insert/update gì thêm
        System.out.println("[DEBUG UserProfileDAO] Profile already exists in Users table for userId=" + userId);
        return true;
    }

    /**
     * Cập nhật thông tin profile
     * Sử dụng bảng Users trực tiếp
     */
    public boolean updateProfile(int userId, String fullName, String phone, String avatarUrl) {
        String sql = "UPDATE Users SET FullName = ?, Phone = ?, Avatar_URL = ? WHERE ID = ?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, avatarUrl);
            ps.setInt(4, userId);

            int rowsAffected = ps.executeUpdate();
            System.out.println("[DEBUG UserProfileDAO] Profile updated in Users table for userId=" + userId + ", rows=" + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR UserProfileDAO] Error updating profile: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR UserProfileDAO] Database driver not found: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật package cho user (dành cho Driver khi mua gói)
     * Package info được lưu trong DriverPackage table, không phải Users
     * Method này giữ lại để backward compatibility
     */
    public boolean updatePackageInfo(int userId, int packageId, java.sql.Date startDate, java.sql.Date endDate) {
        // Package info được manage bởi DriverPackage table
        // Không cần update Users table
        System.out.println("[DEBUG UserProfileDAO] Package info is managed by DriverPackage table for userId=" + userId);
        return true;
    }
}
