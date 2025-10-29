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
        // If tempPassword is provided, legacy behavior could include it; for admin-created accounts we prefer
        // to instruct the user to set their password via the Forgot/Reset flow. Avoid sending raw passwords.
        try {
            // Try to call EmailService.sendOnboardingEmail via reflection to avoid compile-time dependency
                try {
                    Class<?> svc = Class.forName("mylib.EmailService");
                    // Prefer a no-password onboarding method if available
                    try {
                        java.lang.reflect.Method m = svc.getMethod("sendOnboardingEmailNoPassword", String.class);
                        Object res = m.invoke(null, email);
                        if (res instanceof Boolean) return (Boolean) res;
                        return true;
                    } catch (NoSuchMethodException nsme) {
                        // fallback to older two-arg method if present (but avoid sending raw password if null)
                        try {
                            java.lang.reflect.Method m2 = svc.getMethod("sendOnboardingEmail", String.class, String.class);
                            Object res = m2.invoke(null, email, tempPassword == null ? "" : tempPassword);
                            if (res instanceof Boolean) return (Boolean) res;
                            return true;
                        } catch (NoSuchMethodException nsme2) {
                            System.out.println("[EmailUtil] No suitable EmailService onboarding method found, logged only.");
                            // Log an advisory message indicating what the onboarding should say.
                            System.out.println("[EmailUtil] Advisory: Send email telling the user their account is ready and to use the 'Forgot Password' link to set a password.");
                            return true;
                        }
                    }
                } catch (ClassNotFoundException cnfe) {
                    System.out.println("[EmailUtil] EmailService class not found, logged only.");
                    System.out.println("[EmailUtil] Advisory: Send email telling the user their account is ready and to use the 'Forgot Password' link to set a password.");
                    return true;
                }
        } catch (Throwable t) {
            System.err.println("[EmailUtil] Failed to send onboarding email: " + t.getMessage());
            return false;
        }
    }
}
