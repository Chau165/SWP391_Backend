package util;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordResetManager {
    private static final Map<String, Entry> store = new ConcurrentHashMap<>();
    private static final Random rng = new Random();

    public static String generateOtp(String email) {
        String otp = String.format("%06d", rng.nextInt(1_000_000));
        store.put(email.toLowerCase(), new Entry(otp, Instant.now().plusSeconds(5 * 60))); // 5 minutes
        return otp;
    }

    public static boolean verifyOtp(String email, String otp) {
        Entry e = store.get(email.toLowerCase());
        if (e == null) return false;
        if (Instant.now().isAfter(e.expiry)) {
            store.remove(email.toLowerCase());
            return false;
        }
        boolean ok = e.otp.equals(otp);
        if (ok) store.remove(email.toLowerCase());
        return ok;
    }

    private static class Entry {
        final String otp;
        final Instant expiry;
        Entry(String otp, Instant expiry) { this.otp = otp; this.expiry = expiry; }
    }
}
