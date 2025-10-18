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
    private static final String EMAIL_PASSWORD = "skojqbzyujtymcyb"; // Gmail App Password (remove spaces)

    /**
     * Gửi OTP qua email
     * @param toEmail Email người nhận
     * @param otp Mã OTP 6 chữ số
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public static boolean sendOtpEmail(String toEmail, String otp) {
        System.out.println("[EmailService] === STARTING EMAIL SEND ===");
        System.out.println("[EmailService] To: " + toEmail);
        System.out.println("[EmailService] OTP: " + otp);
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        System.out.println("[EmailService] SMTP Config: " + SMTP_HOST + ":" + SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                System.out.println("[EmailService] Authenticating with: " + EMAIL_FROM);
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            System.out.println("[EmailService] Creating message...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã OTP đặt lại mật khẩu - EV Battery Swap");

            // SIMPLE TEXT VERSION FOR TESTING
            String simpleText = "Xin chào,\n\n"
                    + "Mã OTP của bạn là: " + otp + "\n\n"
                    + "Mã OTP có hiệu lực trong 5 phút.\n\n"
                    + "EV Battery Swap System";
            
            message.setText(simpleText);
            System.out.println("[EmailService] Message created. Sending...");

            Transport.send(message);

            System.out.println("[EmailService] ✅ OTP sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("[EmailService] ❌ FAILED to send OTP email!");
            System.err.println("[EmailService] Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("[EmailService] ❌ UNEXPECTED ERROR!");
            System.err.println("[EmailService] Error: " + e.getMessage());
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
