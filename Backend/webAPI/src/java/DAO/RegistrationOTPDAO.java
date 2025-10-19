package DAO;

import DTO.RegistrationOTPData;
import java.sql.*;
import java.time.LocalDateTime;
import mylib.DBUtils;

public class RegistrationOTPDAO {
    
    /**
     * Lưu thông tin đăng ký tạm thời và OTP
     */
    public boolean saveRegistrationOTP(String email, String otp, String fullName, 
                                       String phone, String password, String role) {
        String sql = "INSERT INTO Registration_OTP (Email, OTP, Full_Name, Phone, Password, Role, Expires_At) "
                   + "VALUES (?, ?, ?, ?, ?, ?, DATEADD(MINUTE, 5, GETDATE()))";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ps.setString(2, otp);
            ps.setString(3, fullName);
            ps.setString(4, phone);
            ps.setString(5, password);
            ps.setString(6, role);
            
            int result = ps.executeUpdate();
            System.out.println("[RegistrationOTPDAO] OTP saved for email: " + email);
            return result > 0;
            
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("[RegistrationOTPDAO] Error saving OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xác thực OTP và lấy thông tin đăng ký
     */
    public RegistrationOTPData verifyOTPAndGetRegistrationData(String email, String otp) {
        String sql = "SELECT Full_Name, Phone, Password, Role, Expires_At, Is_Verified "
                   + "FROM Registration_OTP "
                   + "WHERE Email = ? AND OTP = ? "
                   + "ORDER BY Created_At DESC";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ps.setString(2, otp);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Timestamp expiresAt = rs.getTimestamp("Expires_At");
                boolean isVerified = rs.getBoolean("Is_Verified");
                
                // Kiểm tra OTP đã được sử dụng chưa
                if (isVerified) {
                    System.out.println("[RegistrationOTPDAO] OTP already used for: " + email);
                    return null;
                }
                
                // Kiểm tra OTP hết hạn chưa
                if (expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
                    System.out.println("[RegistrationOTPDAO] OTP expired for: " + email);
                    return null;
                }
                
                // OTP hợp lệ - tạo và trả về RegistrationOTPData
                RegistrationOTPData data = new RegistrationOTPData();
                data.setEmail(email);
                data.setFullName(rs.getString("Full_Name"));
                data.setPhone(rs.getString("Phone"));
                data.setPassword(rs.getString("Password"));
                data.setRole(rs.getString("Role"));
                
                System.out.println("[RegistrationOTPDAO] OTP verified successfully for: " + email);
                return data;
                
            } else {
                System.out.println("[RegistrationOTPDAO] Invalid OTP for: " + email);
                return null;
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("[RegistrationOTPDAO] Error verifying OTP: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Đánh dấu OTP đã được sử dụng
     */
    public boolean markOTPAsUsed(String email, String otp) {
        String sql = "UPDATE Registration_OTP SET Is_Verified = 1 WHERE Email = ? AND OTP = ?";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ps.setString(2, otp);
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("[RegistrationOTPDAO] Error marking OTP as used: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa OTP cũ của email (trước khi tạo OTP mới)
     */
    public void deleteOldOTP(String email) {
        String sql = "DELETE FROM Registration_OTP WHERE Email = ?";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ps.executeUpdate();
            System.out.println("[RegistrationOTPDAO] Deleted old OTP for: " + email);
            
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("[RegistrationOTPDAO] Error deleting old OTP: " + e.getMessage());
        }
    }
}
