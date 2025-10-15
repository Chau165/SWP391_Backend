package config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

public class VnPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            int comma = ip.indexOf(',');
            return (comma > -1) ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = req.getHeader("X-Real-IP");
        return (ip != null && !ip.isEmpty()) ? ip : req.getRemoteAddr();
    }

    // ✅ Tạo URL thanh toán cho CHECK-IN/SWAP
    // orderInfo ĐÃ được encode Base64 từ CheckInController → an toàn 100%
    public static String createSwapPaymentUrl(HttpServletRequest req,
            String txnRef,
            long amountVnd,
            String orderInfo) throws Exception {

        String vnp_TmnCode = VnPayConfigSwap.vnp_TmnCode;
        String vnp_HashSecret = VnPayConfigSwap.vnp_HashSecret;
        String vnp_Url = VnPayConfigSwap.vnp_PayUrl;
        String vnp_ReturnUrl = VnPayConfigSwap.vnp_ReturnUrl;

        String vnp_Amount = String.valueOf(amountVnd * 100L);

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        String createDate = df.format(new Date());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        cal.add(Calendar.MINUTE, 15);
        String expireDate = df.format(cal.getTime());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo); // ✅ ĐÃ LÀ BASE64 từ CheckInController
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_CreateDate", createDate);
        vnp_Params.put("vnp_ExpireDate", expireDate);
        vnp_Params.put("vnp_IpAddr", getClientIp(req));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String key = itr.next();
            String value = vnp_Params.get(key);
            if (value != null && !value.isEmpty()) {
                String encValue = URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
                hashData.append(key).append('=').append(encValue);
                query.append(key).append('=').append(encValue);
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return vnp_Url + "?" + query;
    }
}