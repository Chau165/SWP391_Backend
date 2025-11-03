package test;

import mylib.EmailService;

/**
 * Test sending OTP email directly
 */
public class TestEmailSend {
    public static void main(String[] args) {
        System.out.println("=== Testing Email Service ===");
        
        String testEmail = "ahkhoinguyen169@gmail.com";
        String testOTP = "123456";
        
        System.out.println("Attempting to send OTP email to: " + testEmail);
        System.out.println("OTP: " + testOTP);
        
        boolean success = EmailService.sendOtpEmail(testEmail, testOTP);
        
        if (success) {
            System.out.println("✅ Email sent successfully!");
        } else {
            System.out.println("❌ Failed to send email!");
        }
    }
}
