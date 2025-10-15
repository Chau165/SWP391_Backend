package controller;

import DAO.VehicleDAO;
import DAO.UsersDAO;
import DTO.Vehicle;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/secure/linkVehicle")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class linkVehicleController extends HttpServlet {

    private static final String API_KEY = "K87030538488957";
    private static final String OCR_URL = "https://api.ocr.space/parse/image";

    private static final List<String> VALID_MODELS = Arrays.asList(
            "Gogoro SuperSport",
            "Gogoro 2 Delight",
            "Gogoro Viva Mix",
            "Gogoro CrossOver S",
            "Gogoro S2 ABS"
    );

    // ✅ Thêm preflight cho OPTIONS request
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ✅ Gọi setCorsHeaders để cho phép FE 5173 / ngrok
        setCorsHeaders(resp, req);

        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            Gson gson = new Gson();
            Map<String, Object> result = new HashMap<>();
            try {
                // === 1️⃣ Lấy thông tin người dùng từ JWT filter ===
                Object idAttr = req.getAttribute("jwt_id");
                Object roleAttr = req.getAttribute("jwt_role");

                if (idAttr == null || roleAttr == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    result.put("status", "error");
                    result.put("message", "Token không hợp lệ hoặc chưa đăng nhập!");
                    out.print(gson.toJson(result));
                    return;
                }

                int userId = Integer.parseInt(idAttr.toString());
                String role = roleAttr.toString();

                if (!role.equalsIgnoreCase("Driver")) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    result.put("status", "error");
                    result.put("message", "Chỉ tài xế (Driver) mới có quyền liên kết xe!");
                    out.print(gson.toJson(result));
                    return;
                }

                // === 2️⃣ Lấy file ảnh upload ===
                Part filePart = req.getPart("carDoc");
                if (filePart == null || filePart.getSize() == 0) {
                    result.put("status", "error");
                    result.put("message", "Vui lòng tải lên ảnh cà vẹt xe!");
                    out.print(gson.toJson(result));
                    return;
                }

                // === 3️⃣ Gọi API OCR.space để đọc text từ ảnh ===
                String ocrText = callOcrApi(filePart);
                System.out.println("🔹 [OCR] Full text:\n" + ocrText);
                
                if (ocrText == null || ocrText.trim().isEmpty()) {
                    result.put("status", "error");
                    result.put("message", "OCR thất bại hoặc không đọc được thông tin!");
                    out.print(gson.toJson(result));
                    return;
                }

                // === 4️⃣ Trích xuất thông tin cần thiết từ OCR text ===
                String vin = extractVin(ocrText);
                String licensePlate = extractLicensePlate(ocrText);
                String owner = extractOwner(ocrText);
                String detectedModel = extractModel(ocrText);

                System.out.println("🔹 [EXTRACT] VIN: " + vin);
                System.out.println("🔹 [EXTRACT] License Plate: " + licensePlate);
                System.out.println("🔹 [EXTRACT] Owner from OCR: '" + owner + "'");
                System.out.println("🔹 [EXTRACT] Model: " + detectedModel);

                VehicleDAO vehicleDao = new VehicleDAO();
                UsersDAO userDao = new UsersDAO();

                // === 5️⃣ Kiểm tra chủ xe trùng tên tài khoản đăng nhập ===
                String usernameInDB = userDao.getUsernameById(userId);
                System.out.println("🔹 [DB] Username from DB: '" + usernameInDB + "'");
                
                if (owner != null && !owner.equals("Không xác định")) {
                    String normalizedOwner = normalizeVietnamese(owner);
                    String normalizedUsername = normalizeVietnamese(usernameInDB);
                    
                    System.out.println("🔹 [COMPARE] Normalized Owner: '" + normalizedOwner + "'");
                    System.out.println("🔹 [COMPARE] Normalized Username: '" + normalizedUsername + "'");
                    
                    if (!normalizedOwner.equalsIgnoreCase(normalizedUsername)) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        result.put("status", "error");
                        result.put("message", "Tên chủ xe trong giấy tờ không khớp với người dùng đăng nhập!");
                        
                        Map<String, String> debugInfo = new HashMap<>();
                        debugInfo.put("ownerFromOCR", owner);
                        debugInfo.put("usernameFromDB", usernameInDB);
                        debugInfo.put("normalizedOwner", normalizedOwner);
                        debugInfo.put("normalizedUsername", normalizedUsername);
                        result.put("debug", debugInfo);
                        
                        out.print(gson.toJson(result));
                        return;
                    }
                } else {
                    System.out.println("⚠️ [OWNER] OCR không đọc được tên, bỏ qua kiểm tra tên chủ xe");
                }

                // === 6️⃣ Kiểm tra model xe có hợp lệ trong hệ thống không ===
                boolean validModel = VALID_MODELS.stream()
                        .anyMatch(m -> m.equalsIgnoreCase(detectedModel));
                if (!validModel) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("status", "error");
                    result.put("message", "Model xe không hợp lệ! Hệ thống chỉ hỗ trợ xe Gogoro.");
                    out.print(gson.toJson(result));
                    return;
                }

                // === 7️⃣ Lấy model_id từ DB ===
                Integer modelId = vehicleDao.getModelIdByName(detectedModel);
                if (modelId == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("status", "error");
                    result.put("message", "Không tìm thấy model trong cơ sở dữ liệu!");
                    out.print(gson.toJson(result));
                    return;
                }

                // === 8️⃣ Tạo đối tượng Vehicle và lưu DB ===
                Vehicle vehicle = new Vehicle();
                vehicle.setUser_ID(userId);
                vehicle.setModel_ID(modelId);
                vehicle.setVin(vin);
                vehicle.setLicense_Plate(licensePlate);

                boolean success = vehicleDao.insertVehicle(vehicle);

                if (success) {
                    Vehicle inserted = vehicleDao.getVehicleByUserId(userId);
                    result.put("status", "success");
                    result.put("message", "Xe đã được đăng ký thành công!");
                    result.put("data", inserted);
                    result.put("owner", owner);
                    result.put("detectedModel", detectedModel);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    result.put("status", "error");
                    result.put("message", "Không thể lưu thông tin xe vào cơ sở dữ liệu!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("status", "error");
                result.put("message", "Lỗi hệ thống: " + e.getMessage());
            }

            out.print(gson.toJson(result));
            out.flush();
        }
    }

    // ✅ Thêm hàm setCorsHeaders
    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");

        boolean allowed = origin != null && (
                origin.equals("http://localhost:5173")
                || origin.equals("http://127.0.0.1:5173")
                || origin.contains("ngrok-free.app") // hỗ trợ khi FE deploy tạm qua ngrok
        );

        if (allowed) {
            res.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            res.setHeader("Access-Control-Allow-Origin", "null");
        }

        res.setHeader("Vary", "Origin");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, ngrok-skip-browser-warning, X-Requested-With, Accept");
        res.setHeader("Access-Control-Expose-Headers", "Authorization, Location");
        res.setHeader("Access-Control-Max-Age", "86400"); // cache preflight 24h
    }

    // === 🧹 CHUẨN HÓA TÊN TIẾNG VIỆT ===
    private String normalizeVietnamese(String text) {
        if (text == null) return "";
        text = text.replaceAll("[\\s\\u00A0\\u1680\\u2000-\\u200B\\u202F\\u205F\\u3000]+", " ");
        text = text.trim();
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[^a-z\\s]", "");
        normalized = normalized.trim().replaceAll("\\s+", "");
        return normalized.toLowerCase();
    }

    // === 🧠 Gọi OCR API ===
    private String callOcrApi(Part filePart) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(OCR_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", API_KEY);
        conn.setDoOutput(true);

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream outputStream = conn.getOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {
            String fileName = filePart.getSubmittedFileName();
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
            writer.append("Content-Type: " + filePart.getContentType() + "\r\n\r\n");
            writer.flush();
            try (InputStream inputStream = filePart.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            writer.append("\r\n").flush();
            writer.append("--" + boundary + "--").append("\r\n");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder apiResponse = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            apiResponse.append(line);
        }
        in.close();

        JsonObject json = com.google.gson.JsonParser.parseString(apiResponse.toString()).getAsJsonObject();
        if (json.has("OCRExitCode") && json.get("OCRExitCode").getAsInt() == 1) {
            return json.getAsJsonArray("ParsedResults")
                    .get(0).getAsJsonObject()
                    .get("ParsedText").getAsString();
        }
        return null;
    }

    // === 🔍 Hàm tách thông tin từ OCR text ===
    private String extractVin(String text) {
        Pattern vinPattern1 = Pattern.compile("(?i)(Frame/VIN|S[o6] khung)[:\\s]+([A-HJ-NPR-Z0-9]{10,17})");
        Matcher m1 = vinPattern1.matcher(text);
        if (m1.find()) {
            return m1.group(2).trim();
        }
        Pattern vinPattern2 = Pattern.compile("\\b[A-HJ-NPR-Z0-9]{17}\\b");
        Matcher m2 = vinPattern2.matcher(text);
        if (m2.find()) {
            return m2.group();
        }
        return "VIN" + System.currentTimeMillis();
    }

    private String extractLicensePlate(String text) {
        Pattern platePattern1 = Pattern.compile("(?i)(Bien.*?so|License Plate)[:\\s]+([0-9]{2}[A-Z]{1,2}[\\-\\s]?[0-9]{5,6})");
        Matcher m1 = platePattern1.matcher(text);
        if (m1.find()) {
            return m1.group(2).replaceAll("[\\s\\-]", "");
        }
        Matcher m2 = Pattern.compile("\\b([0-9]{2}[A-Z]{1,2}[\\-]?[0-9]{5,6})\\b").matcher(text);
        if (m2.find()) return m2.group(1).replaceAll("-", "");
        return "AUTO" + ((int) (Math.random() * 100000));
    }

    private String extractOwner(String text) {
        Pattern p = Pattern.compile("(?i)(Chu xe|Owner)[:\\s]+([A-ZÀ-Ỹ][A-ZÀ-Ỹa-zà-ỹ\\s]{2,50})");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(2).trim();
        return "Không xác định";
    }

    private String extractModel(String text) {
        for (String model : VALID_MODELS) {
            if (text.toLowerCase().contains(model.toLowerCase())) return model;
        }
        return null;
    }
}
