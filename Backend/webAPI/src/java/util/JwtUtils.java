package util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class JwtUtils {
    // Simple HMAC-SHA256 JWT implementation (no external libs)
    private static final String HMAC_ALGO = "HmacSHA256";
    // NOTE: In production, keep this secret outside source (env variable / vault)
    private static final String SECRET = "change_this_to_a_strong_secret";

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static String generateToken(String email, String role, int id) {
        long now = System.currentTimeMillis() / 1000L;
        long exp = now + 7 * 24 * 3600; // 7 days

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format("{\"sub\":\"%s\",\"role\":\"%s\",\"id\":%d,\"iat\":%d,\"exp\":%d}",
                escape(email), escape(role), id, now, exp);

        try {
            String headerB = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
            String payloadB = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
            String signingInput = headerB + "." + payloadB;
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] sig = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            String sigB = base64UrlEncode(sig);
            return signingInput + "." + sigB;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
