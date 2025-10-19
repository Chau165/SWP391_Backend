package mylib;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * EmailService - Gửi email OTP cho chức năng quên mật khẩu
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "evbatteryswap.system@gmail.com";
    
    // ✅ App Password đã được cập nhật (EV Battery Swap System)
    // Tạo ngày: 18/10/2025
    // Lưu ý: KHÔNG share mật khẩu này cho người khác!
    private static final String EMAIL_PASSWORD = "mzqbrzycduxhvbnr"; // Gmail App Password (16 ký tự, không dấu cách)

    /**
     * Gửi OTP qua email
     * @param toEmail Email người nhận
     * @param otp Mã OTP 6 chữ số
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public static boolean sendOtpEmail(String toEmail, String otp) {
        System.out.println("\n[EmailService] ========================================");
        System.out.println("[EmailService] === STARTING EMAIL SEND PROCESS ===");
        System.out.println("[EmailService] ========================================");
        System.out.println("[EmailService] To: " + toEmail);
        System.out.println("[EmailService] OTP: " + otp);
        System.out.println("[EmailService] From: " + EMAIL_FROM);
        
        // Cấu hình SMTP với đầy đủ options
        Properties props = new Properties();
        
        // Authentication
        props.put("mail.smtp.auth", "true");
        
        // TLS/SSL Settings
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Server settings
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        // Timeout settings (quan trọng!)
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
        props.put("mail.smtp.timeout", "10000"); // 10 seconds
        props.put("mail.smtp.writetimeout", "10000"); // 10 seconds
        
        // TLS settings - FIX cho Java 8 không hỗ trợ TLSv1.3
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Debug mode - bật để xem chi tiết lỗi
        props.put("mail.debug", "true");
        
        System.out.println("[EmailService] SMTP Host: " + SMTP_HOST);
        System.out.println("[EmailService] SMTP Port: " + SMTP_PORT);
        System.out.println("[EmailService] TLS Enabled: true");
        System.out.println("[EmailService] Authentication: true");

        try {
            // Tạo session với authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("[EmailService] 🔐 Authenticating with: " + EMAIL_FROM);
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            // Bật debug mode cho session
            session.setDebug(true);

            System.out.println("[EmailService] 📝 Creating email message...");
            
            // Tạo message với encoding UTF-8
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "EV Battery Swap System", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã OTP đặt lại mật khẩu - EV Battery Swap", "UTF-8");

            // Nội dung email với HTML và UTF-8
            String emailContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset=\"UTF-8\">"
                    + "</head>"
                    + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                    + "<h2 style=\"color: #4CAF50; text-align: center;\">🔐 Mã OTP Đặt Lại Mật Khẩu</h2>"
                    + "<p>Xin chào,</p>"
                    + "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản <strong>EV Battery Swap</strong>.</p>"
                    + "<div style=\"background-color: #f4f4f4; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;\">"
                    + "<h1 style=\"color: #4CAF50; margin: 0; font-size: 32px; letter-spacing: 5px;\">" + otp + "</h1>"
                    + "<p style=\"margin: 10px 0 0 0; color: #666;\">Mã OTP của bạn</p>"
                    + "</div>"
                    + "<p style=\"color: #d32f2f;\">⏰ <strong>Lưu ý:</strong> Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>"
                    + "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">"
                    + "<p style=\"text-align: center; color: #666; font-size: 12px;\">"
                    + "Trân trọng,<br>"
                    + "<strong>EV Battery Swap System</strong>"
                    + "</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            // Set content type là HTML với UTF-8
            message.setContent(emailContent, "text/html; charset=UTF-8");
            message.saveChanges();
            
            System.out.println("[EmailService] 📧 Message created successfully");
            System.out.println("[EmailService] 🚀 Sending email...");

            // Gửi email
            Transport.send(message);

            System.out.println("[EmailService] ========================================");
            System.out.println("[EmailService] ✅✅✅ EMAIL SENT SUCCESSFULLY! ✅✅✅");
            System.out.println("[EmailService] ========================================");
            System.out.println("[EmailService] Recipient: " + toEmail);
            System.out.println("[EmailService] OTP: " + otp);
            System.out.println("[EmailService] ========================================\n");
            
            return true;

        } catch (AuthenticationFailedException e) {
            System.err.println("\n[EmailService] ========================================");
            System.err.println("[EmailService] ❌ AUTHENTICATION FAILED!");
            System.err.println("[EmailService] ========================================");
            System.err.println("[EmailService] Error: " + e.getMessage());
            System.err.println("[EmailService] ");
            System.err.println("[EmailService] 🔧 HƯỚNG DẪN KHẮC PHỤC:");
            System.err.println("[EmailService] 1. Kiểm tra email và mật khẩu có đúng không");
            System.err.println("[EmailService] 2. Tạo App Password mới tại:");
            System.err.println("[EmailService]    https://myaccount.google.com/apppasswords");
            System.err.println("[EmailService] 3. Đảm bảo đã bật 2-Step Verification");
            System.err.println("[EmailService] 4. Cập nhật EMAIL_PASSWORD trong EmailService.java");
            System.err.println("[EmailService] ========================================\n");
            e.printStackTrace();
            return false;
            
        } catch (MessagingException e) {
            System.err.println("\n[EmailService] ========================================");
            System.err.println("[EmailService] ❌ MESSAGING ERROR!");
            System.err.println("[EmailService] ========================================");
            System.err.println("[EmailService] Error Type: " + e.getClass().getSimpleName());
            System.err.println("[EmailService] Error Message: " + e.getMessage());
            System.err.println("[EmailService] ");
            System.err.println("[EmailService] 🔧 HƯỚNG DẪN KHẮC PHỤC:");
            System.err.println("[EmailService] 1. Kiểm tra kết nối Internet");
            System.err.println("[EmailService] 2. Kiểm tra Firewall/Antivirus có block port 587 không");
            System.err.println("[EmailService] 3. Thử tắt VPN nếu đang dùng");
            System.err.println("[EmailService] 4. Kiểm tra email người nhận có hợp lệ không");
            System.err.println("[EmailService] ========================================\n");
            e.printStackTrace();
            return false;
            
        } catch (Exception e) {
            System.err.println("\n[EmailService] ========================================");
            System.err.println("[EmailService] ❌ UNEXPECTED ERROR!");
            System.err.println("[EmailService] ========================================");
            System.err.println("[EmailService] Error Type: " + e.getClass().getSimpleName());
            System.err.println("[EmailService] Error Message: " + e.getMessage());
            System.err.println("[EmailService] ========================================\n");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gửi OTP đăng ký tài khoản
     * @param toEmail Email người nhận
     * @param otp Mã OTP
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public boolean sendRegistrationOTP(String toEmail, String otp) {
        try {
            System.out.println("\n[EmailService] ========================================");
            System.out.println("[EmailService] 📧 SENDING REGISTRATION OTP EMAIL");
            System.out.println("[EmailService] ========================================");
            System.out.println("[EmailService] To: " + toEmail);
            System.out.println("[EmailService] OTP: " + otp);
            System.out.println("[EmailService] SMTP Server: " + SMTP_HOST + ":" + SMTP_PORT);
            System.out.println("[EmailService] ========================================\n");

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            
            // Fix TLS version for Java 8
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            System.out.println("[EmailService] 🔧 Creating session...");
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            System.out.println("[EmailService] ✅ Session created");
            System.out.println("[EmailService] 📝 Creating message...");

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "EV Battery Swap System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            
            // Fix: Jakarta Mail setSubject chỉ nhận 1 tham số
            MimeMessage mimeMsg = (MimeMessage) message;
            mimeMsg.setSubject("Xác thực đăng ký tài khoản - EV Battery Swap", "UTF-8");

            String emailContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "</head>"
                    + "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
                    + "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px;'>"
                    + "<h2 style='color: #2196F3; text-align: center;'>🔐 Xác thực đăng ký tài khoản</h2>"
                    + "<div style='background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                    + "<p style='font-size: 16px;'>Xin chào,</p>"
                    + "<p style='font-size: 16px;'>Bạn đã yêu cầu đăng ký tài khoản tại <strong>EV Battery Swap System</strong>.</p>"
                    + "<p style='font-size: 16px;'>Vui lòng sử dụng mã OTP dưới đây để xác thực email của bạn:</p>"
                    + "<div style='background-color: #e3f2fd; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; color: #1976D2; letter-spacing: 5px;'>" + otp + "</span>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #666;'>"
                    + "⏰ Mã OTP này có hiệu lực trong <strong>5 phút</strong>."
                    + "</p>"
                    + "<p style='font-size: 14px; color: #666;'>"
                    + "⚠️ Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này."
                    + "</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                    + "<p style='font-size: 12px; color: #999; text-align: center;'>"
                    + "© 2025 EV Battery Swap System. All rights reserved."
                    + "</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            message.setContent(emailContent, "text/html; charset=UTF-8");
            message.saveChanges();
            
            System.out.println("[EmailService] 📧 Message created successfully");
            System.out.println("[EmailService] 🚀 Sending email...");

            Transport.send(message);

            System.out.println("[EmailService] ========================================");
            System.out.println("[EmailService] ✅✅✅ REGISTRATION OTP EMAIL SENT! ✅✅✅");
            System.out.println("[EmailService] ========================================");
            System.out.println("[EmailService] Recipient: " + toEmail);
            System.out.println("[EmailService] OTP: " + otp);
            System.out.println("[EmailService] ========================================\n");
            
            return true;

        } catch (Exception e) {
            System.err.println("\n[EmailService] ========================================");
            System.err.println("[EmailService] ❌❌❌ EMAIL SENDING FAILED! ❌❌❌");
            System.err.println("[EmailService] ========================================");
            System.err.println("[EmailService] Error Type: " + e.getClass().getSimpleName());
            System.err.println("[EmailService] Error Message: " + e.getMessage());
            System.err.println("[EmailService] ========================================\n");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo mã OTP ngẫu nhiên 6 chữ số
     * @return OTP string (6 digits)
     */
    public static String generateOtp() {
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp);
    }
}
