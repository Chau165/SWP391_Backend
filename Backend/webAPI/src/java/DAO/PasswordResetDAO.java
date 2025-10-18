package DAO;

import java.sql.*;
import mylib.DBUtils;

/**
 * PasswordResetDAO - Quản lý OTP trong database (SQL Server)
 * Tương thích với bảng Password_Reset (User_ID thay vì Email)
 */
public class PasswordResetDAO {

    /**
     * Lấy User_ID từ email
     * @param email Email người dùng
     * @return User_ID hoặc -1 nếu không tìm thấy
     */
    private int getUserIdByEmail(String email) {
        String sql = "SELECT ID FROM Users WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (Exception e) {
            System.err.println("[PasswordResetDAO] Error getting User_ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Lưu OTP mới vào database
     * @param email Email người dùng
     * @param otp Mã OTP
     * @return true nếu thành công
     */
    public boolean saveOtp(String email, String otp) {
        // Lấy User_ID từ email
        int userId = getUserIdByEmail(email);
        if (userId == -1) {
            System.err.println("[PasswordResetDAO] User not found for email: " + email);
            return false;
        }
        
        // SQL Server: DATEADD thay vì DATE_ADD
        String sql = "INSERT INTO Password_Reset (User_ID, OTP, Expired_At) VALUES (?, ?, DATEADD(MINUTE, 5, GETDATE()))";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, otp);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println("[PasswordResetDAO] OTP saved for User_ID: " + userId + " (Email: " + email + ")");
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.err.println("[PasswordResetDAO] Error saving OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xác thực OTP
     * @param email Email người dùng
     * @param otp Mã OTP cần kiểm tra
     * @return true nếu OTP hợp lệ và chưa hết hạn
     */
    public boolean verifyOtp(String email, String otp) {
        // Lấy User_ID từ email
        int userId = getUserIdByEmail(email);
        if (userId == -1) {
            return false;
        }
        
        // SQL Server: GETDATE() thay vì NOW(), TOP 1 thay vì LIMIT 1, Is_Used = 0 thay vì FALSE
        String sql = "SELECT TOP 1 ID FROM Password_Reset WHERE User_ID = ? AND OTP = ? AND Expired_At > GETDATE() AND Is_Used = 0 ORDER BY Created_At DESC";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, otp);
            
            ResultSet rs = ps.executeQuery();
            boolean isValid = rs.next();
            
            if (isValid) {
                System.out.println("[PasswordResetDAO] OTP verified successfully for User_ID: " + userId + " (Email: " + email + ")");
            } else {
                System.out.println("[PasswordResetDAO] OTP verification failed for: " + email);
            }
            
            return isValid;
            
        } catch (Exception e) {
            System.err.println("[PasswordResetDAO] Error verifying OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đánh dấu OTP đã được sử dụng
     * @param email Email người dùng
     * @param otp Mã OTP
     */
    public void markOtpAsUsed(String email, String otp) {
        // Lấy User_ID từ email
        int userId = getUserIdByEmail(email);
        if (userId == -1) {
            return;
        }
        
        // SQL Server: Is_Used = 1 thay vì TRUE
        String sql = "UPDATE Password_Reset SET Is_Used = 1 WHERE User_ID = ? AND OTP = ? AND Is_Used = 0";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, otp);
            
            ps.executeUpdate();
            System.out.println("[PasswordResetDAO] OTP marked as used for User_ID: " + userId + " (Email: " + email + ")");
            
        } catch (Exception e) {
            System.err.println("[PasswordResetDAO] Error marking OTP as used: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xóa các OTP cũ đã hết hạn (cleanup)
     */
    public void cleanupExpiredOtps() {
        // SQL Server: GETDATE() thay vì NOW(), Is_Used = 1 thay vì TRUE
        String sql = "DELETE FROM Password_Reset WHERE Expired_At < GETDATE() OR Is_Used = 1";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int deleted = ps.executeUpdate();
            System.out.println("[PasswordResetDAO] Cleaned up " + deleted + " expired OTPs");
            
        } catch (Exception e) {
            System.err.println("[PasswordResetDAO] Error cleaning up OTPs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra xem email có tồn tại OTP chưa hết hạn không
     * @param email Email người dùng
     * @return true nếu có OTP chưa hết hạn
     */
    public boolean hasValidOtp(String email) {
        // Lấy User_ID từ email
        int userId = getUserIdByEmail(email);
        if (userId == -1) {
            return false;
        }
        
        // SQL Server: GETDATE() thay vì NOW(), Is_Used = 0 thay vì FALSE
        String sql = "SELECT ID FROM Password_Reset WHERE User_ID = ? AND Expired_At > GETDATE() AND Is_Used = 0";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
            
        } catch (Exception e) {
            System.err.println("[PasswordResetDAO] Error checking valid OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
