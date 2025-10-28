package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
public class PasswordUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateTempPassword(int length) {
        if (length <= 0) length = 12;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = secureRandom.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }

    public static String hashSHA256(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // return hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Compatibility wrapper used by existing code: hashPassword (uses SHA-256)
     */
    public static String hashPassword(String input) {
        if (input == null) return null;
        return hashSHA256(input);
    }
}
