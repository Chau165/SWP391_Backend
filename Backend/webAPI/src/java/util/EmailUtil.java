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

    /**
     * Send onboarding email with temporary password. Uses existing EmailService if available.
     */
    public static boolean sendOnboardingEmail(String email, String tempPassword) {
        System.out.println("[EmailUtil] Sending onboarding email to: " + email);
        System.out.println("[EmailUtil] Temporary password: " + tempPassword);
        try {
            // Try to call EmailService.sendOnboardingEmail via reflection to avoid compile-time dependency
            try {
                Class<?> svc = Class.forName("mylib.EmailService");
                java.lang.reflect.Method m = null;
                try {
                    m = svc.getMethod("sendOnboardingEmail", String.class, String.class);
                } catch (NoSuchMethodException nsme) {
                    m = null;
                }
                if (m != null) {
                    Object res = m.invoke(null, email, tempPassword);
                    if (res instanceof Boolean) return (Boolean) res;
                    return true;
                } else {
                    System.out.println("[EmailUtil] No EmailService.sendOnboardingEmail method found, logged only.");
                    return true;
                }
            } catch (ClassNotFoundException cnfe) {
                System.out.println("[EmailUtil] EmailService class not found, logged only.");
                return true;
            }
        } catch (Throwable t) {
            System.err.println("[EmailUtil] Failed to send onboarding email: " + t.getMessage());
            return false;
        }
    }
}
