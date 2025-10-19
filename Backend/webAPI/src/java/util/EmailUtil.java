package util;

import mylib.EmailService;

/**
 * EmailUtil - Wrapper để gọi EmailService
 */
public class EmailUtil {
    
    /**
     * Gửi OTP qua email
     * @param email Email người nhận
     * @param otp Mã OTP 6 chữ số
     * @return true nếu thành công, false nếu thất bại
     */
    public static boolean sendOtp(String email, String otp) {
        System.out.println("[EmailUtil] Sending OTP to: " + email);
        System.out.println("[EmailUtil] OTP: " + otp);
        
        boolean result = EmailService.sendOtpEmail(email, otp);
        
        if (result) {
            System.out.println("[EmailUtil] ✅ Email sent successfully");
        } else {
            System.err.println("[EmailUtil] ❌ Email failed to send");
        }
        
        return result;
    }
    
    /**
     * Gửi OTP đăng ký qua email
     * @param email Email người đăng ký
     * @param otp Mã OTP 6 chữ số
     * @return true nếu thành công, false nếu thất bại
     */
    public static boolean sendRegistrationOtp(String email, String otp) {
        System.out.println("[EmailUtil] Sending Registration OTP to: " + email);
        System.out.println("[EmailUtil] OTP: " + otp);
        
        boolean result = EmailService.sendRegistrationOtpEmail(email, otp);
        
        if (result) {
            System.out.println("[EmailUtil] ✅ Registration email sent successfully");
        } else {
            System.err.println("[EmailUtil] ❌ Registration email failed to send");
        }
        
        return result;
    }
}
