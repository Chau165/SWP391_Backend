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
     * Gửi OTP qua email cho đăng ký tài khoản
     * @param toEmail Email người nhận
     * @param otp Mã OTP 6 chữ số
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public static boolean sendRegistrationOtpEmail(String toEmail, String otp) {
        System.out.println("\n[EmailService] ========================================");
        System.out.println("[EmailService] === STARTING REGISTRATION OTP EMAIL SEND ===");
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

            System.out.println("[EmailService] 📝 Creating registration email message...");
            
            // Tạo message với encoding UTF-8
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "EV Battery Swap System", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Xác thực Email Đăng Ký - EV Battery Swap", "UTF-8");

            // Nội dung email với HTML và UTF-8
            String emailContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset=\"UTF-8\">"
                    + "</head>"
                    + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                    + "<h2 style=\"color: #2196F3; text-align: center;\">✉️ Xác Thực Email Đăng Ký</h2>"
                    + "<p>Xin chào,</p>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản <strong>EV Battery Swap</strong>.</p>"
                    + "<p>Để hoàn tất quá trình đăng ký, vui lòng nhập mã OTP bên dưới:</p>"
                    + "<div style=\"background-color: #e3f2fd; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;\">"
                    + "<h1 style=\"color: #2196F3; margin: 0; font-size: 32px; letter-spacing: 5px;\">" + otp + "</h1>"
                    + "<p style=\"margin: 10px 0 0 0; color: #666;\">Mã OTP xác thực của bạn</p>"
                    + "</div>"
                    + "<p style=\"color: #d32f2f;\">⏰ <strong>Lưu ý:</strong> Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>"
                    + "<p>Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
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
            
            System.out.println("[EmailService] 📧 Registration email message created successfully");
            System.out.println("[EmailService] 🚀 Sending registration OTP email...");

            // Gửi email
            Transport.send(message);

            System.out.println("[EmailService] ========================================");
            System.out.println("[EmailService] ✅✅✅ REGISTRATION OTP EMAIL SENT SUCCESSFULLY! ✅✅✅");
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
     * Tạo mã OTP ngẫu nhiên 6 chữ số
     * @return OTP string (6 digits)
     */
    public static String generateOtp() {
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp);
    }
}
