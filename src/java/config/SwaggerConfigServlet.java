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
            out.println("    \"/api/buyPackage\": { \"get\": { \"summary\": \"Callback VNPay\", \"responses\": { \"200\": { \"description\": \"Thanh toán thành công\" }, \"400\": { \"description\": \"Chữ ký không hợp lệ\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");

            // Peak Hour Statistics APIs
            out.println("    \"/api/secure/analytics/peak-hours\": { \"get\": { \"tags\": [\"Admin\"], \"summary\": \"Thống kê giờ cao điểm - Tất cả khung giờ\", \"description\": \"Lấy thống kê số lượt swap và doanh thu theo 24 khung giờ trong ngày. Chỉ tính giao dịch có Status = Completed.\", \"security\": [{ \"bearerAuth\": [] }], \"parameters\": [ { \"name\": \"startDate\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"string\", \"format\": \"date\", \"example\": \"2025-10-01\" }, \"description\": \"Ngày bắt đầu (yyyy-MM-dd)\" }, { \"name\": \"endDate\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"string\", \"format\": \"date\", \"example\": \"2025-10-31\" }, \"description\": \"Ngày kết thúc (yyyy-MM-dd)\" } ], \"responses\": { \"200\": { \"description\": \"Thống kê thành công\", \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"success\": { \"type\": \"boolean\" }, \"totalSlots\": { \"type\": \"integer\" }, \"peakHour\": { \"type\": \"string\", \"example\": \"08:00-09:00\" }, \"peakHourSwapCount\": { \"type\": \"integer\" }, \"peakHours\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"timeSlot\": { \"type\": \"string\" }, \"swapCount\": { \"type\": \"integer\" }, \"totalRevenue\": { \"type\": \"number\" }, \"averageFee\": { \"type\": \"number\" } } } } } } } } }, \"401\": { \"description\": \"Chưa xác thực\" }, \"403\": { \"description\": \"Không có quyền truy cập (chỉ Admin/Staff)\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");
            
            out.println("    \"/api/secure/analytics/peak-hours/top\": { \"get\": { \"tags\": [\"Admin\"], \"summary\": \"Thống kê giờ cao điểm - Top N khung giờ\", \"description\": \"Lấy top N khung giờ có nhiều giao dịch nhất, sắp xếp theo số lượng swap giảm dần.\", \"security\": [{ \"bearerAuth\": [] }], \"parameters\": [ { \"name\": \"limit\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"integer\", \"default\": 5, \"minimum\": 1, \"maximum\": 24 }, \"description\": \"Số lượng khung giờ muốn lấy (1-24)\" }, { \"name\": \"startDate\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"string\", \"format\": \"date\" }, \"description\": \"Ngày bắt đầu (yyyy-MM-dd)\" }, { \"name\": \"endDate\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"string\", \"format\": \"date\" }, \"description\": \"Ngày kết thúc (yyyy-MM-dd)\" } ], \"responses\": { \"200\": { \"description\": \"Lấy top khung giờ thành công\", \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"success\": { \"type\": \"boolean\" }, \"limit\": { \"type\": \"integer\" }, \"topPeakHours\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"timeSlot\": { \"type\": \"string\" }, \"swapCount\": { \"type\": \"integer\" }, \"totalRevenue\": { \"type\": \"number\" }, \"averageFee\": { \"type\": \"number\" } } } } } } } } }, \"401\": { \"description\": \"Chưa xác thực\" }, \"403\": { \"description\": \"Không có quyền truy cập\" }, \"500\": { \"description\": \"Lỗi server\" } } } },");
            
            out.println("    \"/api/secure/analytics/peak-hours/station\": { \"get\": { \"tags\": [\"Admin\"], \"summary\": \"Thống kê giờ cao điểm theo trạm\", \"description\": \"Lấy thống kê giờ cao điểm cho một trạm cụ thể theo Station_ID.\", \"security\": [{ \"bearerAuth\": [] }], \"parameters\": [ { \"name\": \"stationId\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"integer\" }, \"description\": \"ID của trạm cần thống kê\" }, { \"name\": \"startDate\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"string\", \"format\": \"date\" }, \"description\": \"Ngày bắt đầu (yyyy-MM-dd)\" }, { \"name\": \"endDate\", \"in\": \"query\", \"required\": false, \"schema\": { \"type\": \"string\", \"format\": \"date\" }, \"description\": \"Ngày kết thúc (yyyy-MM-dd)\" } ], \"responses\": { \"200\": { \"description\": \"Thống kê theo trạm thành công\", \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"success\": { \"type\": \"boolean\" }, \"stationId\": { \"type\": \"integer\" }, \"totalSlots\": { \"type\": \"integer\" }, \"peakHour\": { \"type\": \"string\" }, \"peakHourSwapCount\": { \"type\": \"integer\" }, \"peakHours\": { \"type\": \"array\", \"items\": { \"type\": \"object\", \"properties\": { \"timeSlot\": { \"type\": \"string\" }, \"swapCount\": { \"type\": \"integer\" }, \"totalRevenue\": { \"type\": \"number\" }, \"averageFee\": { \"type\": \"number\" } } } } } } } } }, \"400\": { \"description\": \"Thiếu stationId hoặc format không hợp lệ\" }, \"401\": { \"description\": \"Chưa xác thực\" }, \"403\": { \"description\": \"Không có quyền truy cập\" }, \"500\": { \"description\": \"Lỗi server\" } } } }");

            out.println("  }\n}");
        }
    }
}
