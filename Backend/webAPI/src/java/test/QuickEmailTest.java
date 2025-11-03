package test;

import mylib.EmailService;

/**
 * Test nhanh gửi email OTP
 * Chạy file này để kiểm tra email service có hoạt động không
 */
public class QuickEmailTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   QUICK EMAIL OTP TEST                     ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        // Thay email của bạn vào đây
        String testEmail = "ahkhoinguyen169@gmail.com";
        String testOTP = EmailService.generateOtp();
        
        System.out.println("📧 Test Email: " + testEmail);
        System.out.println("🔢 Test OTP: " + testOTP);
        System.out.println("\n⏳ Đang gửi email...\n");
        
        // Gửi email
        boolean success = EmailService.sendOtpEmail(testEmail, testOTP);
        
        System.out.println("\n╔════════════════════════════════════════════╗");
        if (success) {
            System.out.println("║   ✅ EMAIL GỬI THÀNH CÔNG!                 ║");
            System.out.println("║   Kiểm tra hộp thư của bạn                 ║");
        } else {
            System.out.println("║   ❌ GỬI EMAIL THẤT BẠI!                   ║");
            System.out.println("║   Xem logs phía trên để biết lý do         ║");
            System.out.println("║   Đọc file HUONG_DAN_SUA_LOI_EMAIL.md      ║");
        }
        System.out.println("╚════════════════════════════════════════════╝");
    }
}
