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
import java.nio.charset.StandardCharsets;
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

    // ===== CORS =====
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp, req);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp, req);
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new LinkedHashMap<>();
        Gson gson = new Gson();

        try (PrintWriter out = resp.getWriter()) {
            // 1) Auth (JWT)
            Object idAttr = req.getAttribute("jwt_id");
            Object roleAttr = req.getAttribute("jwt_role");
            if (idAttr == null || roleAttr == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("status", "error");
                result.put("message", "Token không hợp lệ hoặc chưa đăng nhập!");
                out.print(gson.toJson(result)); return;
            }
            int userId = Integer.parseInt(idAttr.toString());
            String role = roleAttr.toString();
            if (!"Driver".equalsIgnoreCase(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                result.put("status", "error");
                result.put("message", "Chỉ tài xế (Driver) mới có quyền liên kết xe!");
                out.print(gson.toJson(result)); return;
            }

            // 2) Nhận file ảnh
            Part filePart = req.getPart("carDoc");
            if (filePart == null || filePart.getSize() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "error");
                result.put("message", "Vui lòng tải lên ảnh cà vẹt xe!");
                out.print(gson.toJson(result)); return;
            }

            // 3) Gọi OCR
            String ocrText = callOcrApi(filePart);
            System.out.println("🔹 [OCR] Full text:\n" + ocrText);
            if (ocrText == null || ocrText.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "error");
                result.put("message", "OCR thất bại hoặc không đọc được thông tin!");
                out.print(gson.toJson(result)); return;
            }

            // 4) Tiền xử lý riêng cho biển số (gỡ lỗi OCR ở nhãn)
            String plateReady = foldForPlate(ocrText);

            // 5) Extract
            String vin = extractVin(ocrText); // 17 ký tự
            String licensePlate = extractLicensePlate(plateReady, ocrText); // CHỈ trả chuẩn, không AUTO
            String owner = extractOwnerNoMark(ocrText); // tên không dấu
            String detectedModel = extractModel(ocrText);

            System.out.println("🔹 [EXTRACT] VIN: " + vin);
            System.out.println("🔹 [EXTRACT] License Plate: " + licensePlate);
            System.out.println("🔹 [EXTRACT] Owner (no mark): '" + owner + "'");
            System.out.println("🔹 [EXTRACT] Model: " + detectedModel);

            // 6) Validate bắt buộc
            List<String> missing = new ArrayList<>();
            if (!isValidVIN(vin)) missing.add("Số khung/VIN");
            if (!isValidPlate(licensePlate)) missing.add("Biển số");

            if (!missing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "error");
                result.put("message", "Thiếu/không hợp lệ: " + String.join(", ", missing));
                Map<String, String> dbg = new LinkedHashMap<>();
                dbg.put("vin", String.valueOf(vin));
                dbg.put("licensePlate", String.valueOf(licensePlate));
                dbg.put("owner", String.valueOf(owner));
                dbg.put("detectedModel", String.valueOf(detectedModel));
                result.put("debug", dbg);
                out.print(gson.toJson(result)); return;
            }

            // 7) So khớp tên chủ xe (bạn dùng không dấu)
            UsersDAO userDao = new UsersDAO();
            String usernameInDB = userDao.getUsernameById(userId); // ví dụ: "Bui Tri Duc"
            if (owner != null && owner.length() >= 2) {
                String o1 = toNoMarkNoSpace(owner);
                String o2 = toNoMarkNoSpace(usernameInDB);
                if (!o1.equalsIgnoreCase(o2)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    result.put("status", "error");
                    result.put("message", "Tên chủ xe trong giấy tờ không khớp với tài khoản đăng nhập (không dấu)!");
                    Map<String,String> dbg = new LinkedHashMap<>();
                    dbg.put("ownerFromOCR", owner);
                    dbg.put("usernameFromDB", usernameInDB);
                    dbg.put("normalizedOwner", o1);
                    dbg.put("normalizedUsername", o2);
                    result.put("debug", dbg);
                    out.print(gson.toJson(result)); return;
                }
            } else {
                System.out.println("⚠️ [OWNER] OCR không đọc được tên hoặc tên quá ngắn → bỏ qua kiểm tra");
            }

            // 8) Kiểm tra model (giữ logic cũ)
            boolean validModel = detectedModel != null &&
                    VALID_MODELS.stream().anyMatch(m -> m.equalsIgnoreCase(detectedModel));
            if (!validModel) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "error");
                result.put("message", "Model xe không hợp lệ! Hệ thống chỉ hỗ trợ xe Gogoro.");
                out.print(gson.toJson(result)); return;
            }

            VehicleDAO vehicleDao = new VehicleDAO();
            Integer modelId = vehicleDao.getModelIdByName(detectedModel);
            if (modelId == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "error");
                result.put("message", "Không tìm thấy model trong cơ sở dữ liệu!");
                out.print(gson.toJson(result)); return;
            }

            // 9) Lưu DB
            Vehicle vehicle = new Vehicle();
            vehicle.setUser_ID(userId);
            vehicle.setModel_ID(modelId);
            vehicle.setVin(vin);
            vehicle.setLicense_Plate(licensePlate);

            boolean success = vehicleDao.insertVehicle(vehicle);
            if (!success) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                result.put("status", "error");
                result.put("message", "Không thể lưu thông tin xe vào cơ sở dữ liệu!");
                out.print(gson.toJson(result)); return;
            }

            Vehicle inserted = vehicleDao.getVehicleByUserId(userId);
            result.put("status", "success");
            result.put("message", "Xe đã được đăng ký thành công!");
            result.put("data", inserted);
            result.put("owner", owner);
            result.put("detectedModel", detectedModel);

            out.print(gson.toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                result.put("status", "error");
                result.put("message", "Lỗi hệ thống: " + e.getMessage());
                out.print(new Gson().toJson(result));
            }
        }
    }

    // ===== CORS =====
    private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
        String origin = req.getHeader("Origin");
        boolean allowed = origin != null && (
                origin.equals("http://localhost:5173") ||
                origin.equals("http://127.0.0.1:5173") ||
                origin.contains("ngrok-free.app")
        );
        res.setHeader("Access-Control-Allow-Origin", allowed ? origin : "null");
        res.setHeader("Vary", "Origin");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, ngrok-skip-browser-warning, X-Requested-With, Accept");
        res.setHeader("Access-Control-Expose-Headers", "Authorization, Location");
        res.setHeader("Access-Control-Max-Age", "86400");
    }

    // ===== OCR =====
    private String callOcrApi(Part filePart) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(OCR_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", API_KEY);
        conn.setDoOutput(true);

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

            String fileName = filePart.getSubmittedFileName();
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
            writer.append("Content-Type: ").append(filePart.getContentType()).append("\r\n\r\n");
            writer.flush();

            try (InputStream inputStream = filePart.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            outputStream.flush();
            writer.append("\r\n--").append(boundary).append("--\r\n");
        }

        StringBuilder response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        JsonObject json = com.google.gson.JsonParser.parseString(response.toString()).getAsJsonObject();
        if (json.has("OCRExitCode") && json.get("OCRExitCode").getAsInt() == 1) {
            String parsed = json.getAsJsonArray("ParsedResults")
                    .get(0).getAsJsonObject()
                    .get("ParsedText").getAsString();
            return Normalizer.normalize(parsed, Normalizer.Form.NFC);
        }
        return null;
    }

    // ===== EXTRACTORS =====

    // VIN: yêu cầu 17 ký tự chuẩn
    private String extractVin(String raw) {
        // Ưu tiên nhãn
        Pattern p1 = Pattern.compile("(?im)^(?:S[oốo]\\s*khung|So\\s*khung|Frame\\s*/\\s*VIN|VIN)\\s*[:：]\\s*([A-HJ-NPR-Z0-9]{17})\\b");
        Matcher m1 = p1.matcher(raw);
        if (m1.find()) return m1.group(1).trim();

        // Fallback: 17 ký tự VIN
        Pattern p2 = Pattern.compile("\\b([A-HJ-NPR-Z0-9]{17})\\b");
        Matcher m2 = p2.matcher(raw);
        if (m2.find()) return m2.group(1).trim();

        return null;
    }

    // Tiền xử lý để bắt nhãn “Biển số” ổn định (bỏ dấu & sửa lỗi OCR nhẹ)
    private static String foldForPlate(String s) {
        if (s == null) return null;
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String noMarks = nfd.replaceAll("\\p{M}+", "");
        String x = noMarks;

        // Sửa lỗi OCR thường gặp: "Bien Sd" / "Bien S0" → "Bien so"
        x = x.replaceAll("(?i)Bien\\s*S[d0o]", "Bien so");
        x = x.replaceAll("(?i)Bien\\s*so", "Bien so");

        x = x.replace("\r\n", "\n").replaceAll("[\\t\\x0B\\f\\r]", " ");
        x = x.replaceAll(" {2,}", " ");
        return x;
    }

    // Biển số VN: chỉ trả về chuẩn, không AUTOxxxxx
    private String extractLicensePlate(String main, String raw) {
        // 1) Bắt ngay sau "Bien so:"
        Pattern p1 = Pattern.compile("(?im)^Bien\\s*so\\s*[:：]\\s*([0-9]{2}[A-Z]{1,2}[0-9]{1}[-\\s]?[0-9]{4,6})\\b");
        Matcher m1 = p1.matcher(main);
        if (m1.find()) {
            String plate = normalizePlate(m1.group(1));
            if (isValidPlate(plate)) return plate;
        }

        // 2) Quét toàn văn (main trước, rồi raw)
        Pattern p2 = Pattern.compile("\\b([0-9]{2}[A-Z]{1,2}[0-9]{1}[-\\s]?[0-9]{4,6})\\b");
        Matcher m2 = p2.matcher(main);
        if (m2.find()) {
            String plate = normalizePlate(m2.group(1));
            if (isValidPlate(plate)) return plate;
        }
        Matcher m3 = p2.matcher(raw);
        if (m3.find()) {
            String plate = normalizePlate(m3.group(1));
            if (isValidPlate(plate)) return plate;
        }
        return null; // không sinh AUTO nữa
    }

    // Chủ xe (không dấu): đọc sau "Chu xe:"; bạn dùng không dấu nên chỉ cần ASCII
    private String extractOwnerNoMark(String raw) {
        // Bỏ dấu toàn văn để bắt "Chu xe:"
        String nfd = Normalizer.normalize(raw, Normalizer.Form.NFD);
        String noMarks = nfd.replaceAll("\\p{M}+", "");
        String text = noMarks.replace("\r\n", "\n");

        Pattern p1 = Pattern.compile("(?im)^Chu\\s*xe\\s*[:：]\\s*([A-Za-z0-9\\s'.-]{2,60})$");
        Matcher m1 = p1.matcher(text);
        if (m1.find()) {
            String name = m1.group(1).trim()
                    .replaceAll("(?i)Dia\\s*chi.*$", "")
                    .replaceAll("[^A-Za-z0-9\\s'.-]", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim();
            if (name.length() >= 2) return name;
        }
        // Fallback: cùng dòng
        Pattern p2 = Pattern.compile("(?im)^Chu\\s*xe\\s*[:：]?\\s*([^\\n\\r]{2,80})");
        Matcher m2 = p2.matcher(text);
        if (m2.find()) {
            String name = m2.group(1).trim()
                    .replaceAll("(?i)Dia\\s*chi.*$", "")
                    .replaceAll("[^A-Za-z0-9\\s'.-]", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim();
            if (name.length() >= 2) return name;
        }
        return null; // cho phép null → bỏ qua kiểm tra tên nếu OCR quá xấu
    }

    // Model: giữ logic cũ
    private String extractModel(String text) {
        String normalizedText = text.toLowerCase(Locale.ROOT);
        Pattern modelPattern = Pattern.compile("(?i)Model[:\\s]+([^\\n\\r]{5,30})");
        Matcher m = modelPattern.matcher(text);
        if (m.find()) {
            String detectedModel = m.group(1).trim();
            for (String model : VALID_MODELS) {
                if (detectedModel.toLowerCase().contains(model.toLowerCase())) return model;
            }
        }
        for (String model : VALID_MODELS) {
            if (normalizedText.contains(model.toLowerCase(Locale.ROOT))) return model;
        }
        return null;
    }

    // ===== HELPERS =====
    private static boolean isValidVIN(String vin) {
        return vin != null && vin.trim().toUpperCase(Locale.ROOT).matches("^[A-HJ-NPR-Z0-9]{17}$");
    }

    private static boolean isValidPlate(String plate) {
        if (plate == null) return false;
        // Dạng phổ biến VN: 2 số tỉnh + 1–2 chữ + 1 số series + '-' + 4–6 số
        return plate.matches("^[0-9]{2}[A-Z]{1,2}[0-9]{1}-[0-9]{4,6}$");
    }

    private static String normalizePlate(String raw) {
        if (raw == null) return null;
        String p = raw.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        Matcher m = Pattern.compile("^([0-9]{2}[A-Z]{1,2}[0-9]{1})([0-9]{4,6})$").matcher(p);
        if (m.find()) p = m.group(1) + "-" + m.group(2); // 59X340351 -> 59X3-40351
        return p;
    }

    // so sánh tên không dấu
    private static String toNoMarkNoSpace(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String noMark = nfd.replaceAll("\\p{M}+", "");
        return noMark.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
