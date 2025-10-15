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

        // Cho phép CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        // === Build base URL động ===
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int port = req.getServerPort();
        String contextPath = req.getContextPath();

        String baseUrl = scheme + "://" + serverName
                + ((port == 80 || port == 443) ? "" : ":" + port)
                + contextPath;

        if (serverName.contains("ngrok-free.app")) {
            baseUrl = "https://" + serverName + contextPath;
        }

        try (PrintWriter out = resp.getWriter()) {
            out.println("{");
            out.println("  \"openapi\": \"3.0.1\",");
            out.println("  \"info\": {");
            out.println("    \"title\": \"Battery Swap API\",");
            out.println("    \"description\": \"API cho hệ thống đổi pin xe điện\",");
            out.println("    \"version\": \"1.0.0\"");
            out.println("  },");
            out.println("  \"servers\": [");
            out.println("    { \"url\": \"" + baseUrl + "\", \"description\": \"dynamic server\" }");
            out.println("  ],");
            out.println("  \"paths\": {");

            // ==== API Login ====
            out.println("    \"/api/login\": {");
            out.println("      \"post\": {");
            out.println("        \"summary\": \"Đăng nhập hệ thống\",");
            out.println("        \"description\": \"Nhập email/password để đăng nhập\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"application/json\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"email\": { \"type\": \"string\" },");
            out.println("                  \"password\": { \"type\": \"string\" }");
            out.println("                },");
            out.println("                \"required\": [\"email\", \"password\"]");
            out.println("              },");
            out.println("              \"example\": {");
            out.println("                \"email\": \"nguyenvana@email.com\",");
            out.println("                \"password\": \"pass123\"");
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Đăng nhập thành công\" },");
            out.println("          \"401\": { \"description\": \"Sai email hoặc password\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== API Register ====
            out.println("    \"/api/register\": {");
            out.println("      \"post\": {");
            out.println("        \"summary\": \"Đăng ký tài khoản mới\",");
            out.println("        \"description\": \"Tạo mới user với vai trò mặc định là Driver\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"application/json\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"fullName\": { \"type\": \"string\" },");
            out.println("                  \"phone\": { \"type\": \"string\" },");
            out.println("                  \"email\": { \"type\": \"string\" },");
            out.println("                  \"password\": { \"type\": \"string\" }");
            out.println("                },");
            out.println("                \"required\": [\"fullName\", \"phone\", \"email\", \"password\"]");
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Đăng ký thành công\" },");
            out.println("          \"400\": { \"description\": \"Dữ liệu không hợp lệ\" },");
            out.println("          \"409\": { \"description\": \"Email đã tồn tại\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== API Get Packages ====
            out.println("    \"/api/getpackages\": {");
            out.println("      \"get\": {");
            out.println("        \"summary\": \"Lấy danh sách gói pin\",");
            out.println("        \"description\": \"Trả về toàn bộ danh sách các gói pin khả dụng\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Danh sách gói pin\" },");
            out.println("          \"204\": { \"description\": \"Không có gói pin nào\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== API Get Stations ====
            out.println("    \"/api/getstations\": {");
            out.println("      \"get\": {");
            out.println("        \"summary\": \"Lấy danh sách trạm đổi pin\",");
            out.println("        \"description\": \"Trả về toàn bộ danh sách các trạm khả dụng\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Danh sách trạm\" },");
            out.println("          \"204\": { \"description\": \"Không có trạm nào\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== API Link Vehicle ====
            out.println("    \"/api/linkVehicleController\": {");
            out.println("      \"post\": {");
            out.println("        \"summary\": \"Liên kết xe với tài khoản người dùng\",");
            out.println("        \"description\": \"Upload ảnh cà vẹt xe để hệ thống nhận dạng (OCR) và lưu thông tin xe vào DB.\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"multipart/form-data\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"carDoc\": { \"type\": \"string\", \"format\": \"binary\", \"description\": \"Ảnh cà vẹt xe\" },");
            out.println("                  \"model\": { \"type\": \"string\" },");
            out.println("                  \"batteryType\": { \"type\": \"string\" }");
            out.println("                },");
            out.println("                \"required\": [\"carDoc\"]");
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Xe đã được thêm vào DB\" },");
            out.println("          \"400\": { \"description\": \"Thiếu dữ liệu hoặc file không hợp lệ\" },");
            out.println("          \"500\": { \"description\": \"Lỗi xử lý hoặc OCR thất bại\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== API Payment (Tạo thanh toán VNPay) ====
            out.println("    \"/api/payment\": {");
            out.println("      \"get\": {");
            out.println("        \"summary\": \"Tạo yêu cầu thanh toán VNPay\",");
            out.println("        \"description\": \"API này tạo URL thanh toán VNPay cho người dùng để mua gói pin.\",");
            out.println("        \"parameters\": [");
            out.println("          { \"name\": \"userId\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" }, \"description\": \"ID người dùng\" },");
            out.println("          { \"name\": \"amount\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" }, \"description\": \"Số tiền thanh toán\" },");
            out.println("          { \"name\": \"orderType\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" }, \"description\": \"Loại giao dịch, ví dụ: buyPackage\" },");
            out.println("          { \"name\": \"packageId\", \"in\": \"query\", \"required\": true, \"schema\": { \"type\": \"string\" }, \"description\": \"ID gói pin cần mua\" }");
            out.println("        ],");
            out.println("        \"responses\": {");
            out.println("          \"302\": { \"description\": \"Chuyển hướng sang trang thanh toán VNPay\" },");
            out.println("          \"400\": { \"description\": \"Dữ liệu không hợp lệ\" },");
            out.println("          \"500\": { \"description\": \"Lỗi xử lý nội bộ\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== API Buy Package (Callback sau thanh toán) ====
            out.println("    \"/api/buyPackage\": {");
            out.println("      \"get\": {");
            out.println("        \"summary\": \"Xử lý callback từ VNPay sau thanh toán\",");
            out.println("        \"description\": \"API xử lý phản hồi từ VNPay, kiểm tra chữ ký, xác thực giao dịch và cập nhật gói pin cho người dùng.\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Thanh toán thành công và cập nhật gói pin\" },");
            out.println("          \"400\": { \"description\": \"Chữ ký không hợp lệ hoặc mã lỗi VNPay\" },");
            out.println("          \"500\": { \"description\": \"Lỗi xử lý nội bộ\" }");
            out.println("        }");
            out.println("      }");
            out.println("    }");

            out.println("  }");
            out.println("}");
        }
    }
}

