package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javax.imageio.ImageIO; 
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.crypto.SecretKey;

public class QRCodeUtil {

    /**
     * Generate QR Code as Base64 string
     * @param text Nội dung muốn encode vào QR code
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Base64 encoded image string (PNG)
     * @throws Exception
     */
    public static String generateQRCodeBase64(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        } catch (WriterException e) {
            throw new Exception("Error generating QR code: " + e.getMessage());
        }

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        byte[] pngData = baos.toByteArray();

        return Base64.getEncoder().encodeToString(pngData);
    }
}
