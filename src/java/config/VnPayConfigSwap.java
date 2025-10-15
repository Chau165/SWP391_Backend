package config;

public class VnPayConfigSwap {

    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String vnp_TmnCode = "KX9UWJAB"; // mã cho checkin/swap (nếu khác, thay đúng mã)
    public static final String vnp_HashSecret = "CM39K9J6WJ9N2DMR6JFBFV801JFONL8L";
    public static final String vnp_ReturnUrl = "http://localhost:8084/webAPI/api/checkin";
}
