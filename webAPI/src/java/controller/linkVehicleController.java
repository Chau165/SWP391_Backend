package controller;

import DAO.VehicleDAO;
import DTO.Vehicle;
import com.google.gson.*;
import DTO.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/linkVehicleController")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class linkVehicleController extends HttpServlet {

    private static final String API_KEY = "K87030538488957"; // OCR.space API key
    private static final String OCR_URL = "https://api.ocr.space/parse/image";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new Gson();

        try {
            // ==== Check login ====
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("User") == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("status", "error");
                result.put("message", "Bạn cần đăng nhập trước!");
                out.print(gson.toJson(result));
                return;
            }
            Users user = (Users) session.getAttribute("User");
            int userId = user.getId();

            // ==== Lấy file upload ====
            Part filePart = req.getPart("carDoc");
            if (filePart == null || filePart.getSize() == 0) {
                result.put("status", "error");
                result.put("message", "Vui lòng chọn file ảnh cà vẹt xe!");
                out.print(gson.toJson(result));
                return;
            }

            // ==== Gọi OCR.space ====
            String ocrText = callOcrApi(filePart);

            if (ocrText == null || ocrText.trim().isEmpty()) {
                result.put("status", "error");
                result.put("message", "OCR thất bại hoặc không tìm thấy text!");
                out.print(gson.toJson(result));
                return;
            }

            // ==== Regex tách VIN & Biển số ====
            // ==== Regex tách VIN & Biển số ====
            Pattern vinPattern = Pattern.compile("\\b[A-HJ-NPR-Z0-9]{6,17}\\b");
            Matcher vinMatcher = vinPattern.matcher(ocrText);
            String vin = vinMatcher.find() ? vinMatcher.group() : "VIN" + System.currentTimeMillis();

            Pattern platePattern = Pattern.compile("\\b\\d{2}[A-Z]{1,2}[-\\s]?\\d{4,5}\\b");
            Matcher plateMatcher = platePattern.matcher(ocrText);
            String licensePlate = plateMatcher.find()
                    ? plateMatcher.group().replaceAll("\\s+", "")
                    : "AUTO" + ((int) (Math.random() * 100000));

            // Model & battery type có thể lấy từ form input kèm theo
            String model = req.getParameter("model");
            String batteryType = req.getParameter("batteryType");

            // ==== Lưu vào DB ====
            Vehicle v = new Vehicle();
            v.setUser_ID(userId);
            v.setModel(model);
            v.setBattery_Type_Current(batteryType);
            v.setVin(vin);
            v.setLicense_Plate(licensePlate);

            VehicleDAO dao = new VehicleDAO();
            boolean success = dao.insertVehicle(v);

            if (success) {
                result.put("status", "success");
                result.put("message", "Xe đã được thêm vào DB!");
                result.put("data", v);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("status", "error");
                result.put("message", "Không thể lưu thông tin xe!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("status", "error");
            result.put("message", "Lỗi server: " + e.getMessage());
        }

        out.print(gson.toJson(result));
        out.flush();
        out.close();
    }

    // Hàm gọi OCR.space API
    private String callOcrApi(Part filePart) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(OCR_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", API_KEY);
        conn.setDoOutput(true);

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream outputStream = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

        // Gửi file
        String fileName = filePart.getSubmittedFileName();
        writer.append("--" + boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
        writer.append("Content-Type: " + filePart.getContentType() + "\r\n\r\n");
        writer.flush();

        InputStream inputStream = filePart.getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append("\r\n").flush();

        // Ngôn ngữ OCR
        writer.append("--" + boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
        writer.append("eng").append("\r\n").flush();

        // Kết thúc request
        writer.append("--" + boundary + "--").append("\r\n");
        writer.close();

        // Đọc response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder apiResponse = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            apiResponse.append(line);
        }
        in.close();

        JsonObject json = JsonParser.parseString(apiResponse.toString()).getAsJsonObject();
        if (json.has("OCRExitCode") && json.get("OCRExitCode").getAsInt() == 1) {
            return json.getAsJsonArray("ParsedResults")
                    .get(0).getAsJsonObject()
                    .get("ParsedText").getAsString();
        }
        return null;
    }
}
