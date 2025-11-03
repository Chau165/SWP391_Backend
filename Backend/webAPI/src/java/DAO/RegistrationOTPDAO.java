package DAO;

import java.sql.*;
import mylib.DBUtils;

public class RegistrationOtpDAO {
    public boolean saveOtp(String email, String otp) {
        // ✅ KHÔNG xóa tất cả OTP cũ
        // Chỉ cleanup expired/used OTPs (tùy chọn)
        // cleanupExpiredOtpsForEmail(email);
        
        // ✅ LUÔN INSERT record MỚI (giống Password_Reset)
        String sql = "INSERT INTO Registration_OTP (Email, OTP, Expired_At) VALUES (?, ?, DATEADD(MINUTE, 5, GETDATE()))";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[RegistrationOtpDAO] New OTP saved for email: " + email + " (new record created)");
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error saving OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyOtp(String email, String otp) {
        String sql = "SELECT TOP 1 ID FROM Registration_OTP WHERE Email = ? AND OTP = ? AND Expired_At > GETDATE() AND Is_Used = 0 ORDER BY Created_At DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            ResultSet rs = ps.executeQuery();
            boolean isValid = rs.next();
            if (isValid) {
                System.out.println("[RegistrationOtpDAO] OTP verified successfully for email: " + email);
            } else {
                System.out.println("[RegistrationOtpDAO] OTP verification failed for: " + email);
            }
            return isValid;
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error verifying OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void markOtpAsUsed(String email, String otp) {
        String sql = "UPDATE Registration_OTP SET Is_Used = 1 WHERE Email = ? AND OTP = ? AND Is_Used = 0";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            ps.executeUpdate();
            System.out.println("[RegistrationOtpDAO] OTP marked as used for email: " + email);
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error marking OTP as used: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Optional: Cleanup expired/used OTPs for a specific email
     * This is optional - you can call it before saving new OTP
     * or run it periodically
     */
    private void cleanupExpiredOtpsForEmail(String email) {
        String sql = "DELETE FROM Registration_OTP WHERE Email = ? AND (Expired_At < GETDATE() OR Is_Used = 1)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("[RegistrationOtpDAO] Cleaned up " + deleted + " expired/used OTP(s) for email: " + email);
            }
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error cleaning up old OTPs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanupExpiredOtps() {
        String sql = "DELETE FROM Registration_OTP WHERE Expired_At < GETDATE() OR Is_Used = 1";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            System.out.println("[RegistrationOtpDAO] Cleaned up " + deleted + " expired registration OTPs");
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error cleaning up OTPs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean hasValidOtp(String email) {
        String sql = "SELECT ID FROM Registration_OTP WHERE Email = ? AND Expired_At > GETDATE() AND Is_Used = 0";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error checking valid OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the Created_At timestamp of the most recent OTP record for the given email,
     * or null if none exists.
     */
    public Timestamp getLastOtpCreatedAt(String email) {
        String sql = "SELECT TOP 1 Created_At FROM Registration_OTP WHERE Email = ? ORDER BY Created_At DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("Created_At");
            }
            return null;
        } catch (Exception e) {
            System.err.println("[RegistrationOtpDAO] Error fetching last OTP time: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
