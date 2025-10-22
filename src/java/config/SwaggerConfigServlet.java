package config;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/openapi.json")
public class SwaggerConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Build dynamic base URL
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int port = req.getServerPort();
        String contextPath = req.getContextPath();

        String baseUrl = scheme + "://" + serverName + ((port == 80 || port == 443) ? "" : ":" + port) + contextPath;
        if (serverName.contains("ngrok-free.app")) {
            baseUrl = "https://" + serverName + contextPath;
        }

        try (PrintWriter out = resp.getWriter()) {
            out.println("{");
            out.println("  \"openapi\": \"3.0.1\",");
            out.println("  \"info\": { \"title\": \"Battery Swap API\", \"description\": \"API cho hệ thống đổi pin xe điện — chia rõ theo vai trò Guest, Driver, Staff và Admin.\", \"version\": \"1.0.0\" },");

            out.println("  \"tags\": [ { \"name\": \"Guest\" }, { \"name\": \"Driver\" }, { \"name\": \"Staff\" }, { \"name\": \"Admin\" } ],");
            out.println("  \"servers\": [ { \"url\": \"" + baseUrl + "\" } ],");
            out.println("  \"components\": { \"securitySchemes\": { \"bearerAuth\": { \"type\": \"http\", \"scheme\": \"bearer\", \"bearerFormat\": \"JWT\" } } },");

            out.println("  \"paths\": {");
            out.println("    \"/api/login\": { \"post\": { \"tags\": [\"Guest\"], \"summary\": \"Đăng nhập\", \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"email\": { \"type\": \"string\" }, \"password\": { \"type\": \"string\" } }, \"required\": [\"email\",\"password\"] } } } }, \"responses\": { \"200\": { \"description\": \"Đăng nhập thành công\" }, \"401\": { \"description\": \"Sai email hoặc mật khẩu\" } } } },");
            out.println("    \"/api/register\": { \"post\": { \"tags\": [\"Guest\"], \"summary\": \"Đăng ký\", \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"fullName\": { \"type\": \"string\" }, \"phone\": { \"type\": \"string\" }, \"email\": { \"type\": \"string\" }, \"password\": { \"type\": \"string\" } } } } } }, \"responses\": { \"200\": { \"description\": \"Đăng ký thành công\" }, \"400\": { \"description\": \"Dữ liệu không hợp lệ\" }, \"409\": { \"description\": \"Email đã tồn tại\" } } } },");
            out.println("    \"/api/getpackages\": { \"get\": { \"tags\": [\"Guest\"], \"summary\": \"Lấy danh sách gói pin\", \"responses\": { \"200\": { \"description\": \"Danh sách gói pin\" }, \"204\": { \"description\": \"Không có gói pin nào\" } } } },");
            out.println("    \"/api/getstations\": { \"get\": { \"tags\": [\"Guest\"], \"summary\": \"Lấy danh sách trạm\", \"responses\": { \"200\": { \"description\": \"Danh sách trạm\" }, \"204\": { \"description\": \"Không có trạm nào\" } } } },");
            out.println("    \"/api/getStationBatteryReportGuest\": { \"get\": { \"tags\": [\"Guest\"], \"summary\": \"Báo cáo tổng hợp pin\", \"responses\": { \"200\": { \"description\": \"Danh sách báo cáo trạm\" }, \"500\": { \"description\": \"Lỗi xử lý nội bộ\" } } } },");
            out.println("    \"/api/linkVehicleController\": { \"post\": { \"tags\": [\"Driver\"], \"summary\": \"Liên kết xe\", \"requestBody\": { \"required\": true, \"content\": { \"multipart/form-data\": { \"schema\": { \"type\": \"object\", \"properties\": { \"carDoc\": { \"type\": \"string\", \"format\": \"binary\" }, \"model\": { \"type\": \"string\" }, \"batteryType\": { \"type\": \"string\" } } } } } }, \"responses\": { \"200\": { \"description\": \"Xe đã được thêm vào DB\" }, \"400\": { \"description\": \"Thiếu dữ liệu hoặc file không hợp lệ\" }, \"500\": { \"description\": \"Lỗi xử lý hoặc OCR thất bại\" } } } },");
            out.println("    \"/api/secure/viewBatterySlotStatus\": { \"get\": { \"tags\": [\"Staff\"], \"summary\": \"Xem trạng thái pin\", \"responses\": { \"200\": { \"description\": \"Danh sách slot\" }, \"401\": { \"description\": \"Thiếu token\" }, \"403\": { \"description\": \"Không có quyền truy cập\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");
            out.println("    \"/api/package\": { \"post\": { \"summary\": \"Thêm gói pin mới\", \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"packageName\": { \"type\": \"string\" }, \"description\": { \"type\": \"string\" }, \"price\": { \"type\": \"number\" }, \"duration\": { \"type\": \"integer\" }, \"batteryType\": { \"type\": \"string\" }, \"capacity\": { \"type\": \"number\" } }, \"required\": [\"packageName\",\"price\",\"duration\"] } } } }, \"responses\": { \"200\": { \"description\": \"Thêm gói pin thành công\" }, \"401\": { \"description\": \"Không có quyền\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");
            out.println("    \"/api/package-revenue-statistics\": { \"get\": { \"summary\": \"Thống kê doanh thu\", \"responses\": { \"200\": { \"description\": \"Lấy thống kê thành công\" }, \"204\": { \"description\": \"Không có dữ liệu\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");
            out.println("    \"/api/package/delete\": { \"post\": { \"summary\": \"Xóa gói pin (soft)\", \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"packageId\": { \"type\": \"integer\" } }, \"required\": [\"packageId\"] } } } }, \"responses\": { \"200\": { \"description\": \"Xóa thành công\" }, \"400\": { \"description\": \"ID không hợp lệ\" } } }, \"delete\": { \"summary\": \"Xóa gói pin (soft) - DELETE\", \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"packageId\": { \"type\": \"integer\" } }, \"required\": [\"packageId\"] } } } }, \"responses\": { \"200\": { \"description\": \"Xóa thành công\" } } } },");
            out.println("    \"/api/payment\": { \"get\": { \"summary\": \"Tạo yêu cầu thanh toán VNPay\", \"parameters\": [ { \"name\": \"userId\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" } }, { \"name\": \"amount\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" } }, { \"name\": \"orderType\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" } }, { \"name\": \"packageId\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" } } ], \"responses\": { \"302\": { \"description\": \"Chuyển hướng sang VNPay\" }, \"400\": { \"description\": \"Dữ liệu không hợp lệ\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");
            out.println("    \"/api/buyPackage\": { \"get\": { \"summary\": \"Callback VNPay\", \"responses\": { \"200\": { \"description\": \"Thanh toán thành công\" }, \"400\": { \"description\": \"Chữ ký không hợp lệ\" }, \"500\": { \"description\": \"Lỗi server\" } } } }");

            out.println("  }\n}");
        }
    }
}
