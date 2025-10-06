package config;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        String scheme = req.getScheme(); // http hoặc https
        String serverName = req.getServerName(); // ví dụ: localhost hoặc 03dafbc27102.ngrok-free.app
        int port = req.getServerPort();
        String contextPath = req.getContextPath();

        String baseUrl = scheme + "://" + serverName
                + ((port == 80 || port == 443) ? "" : ":" + port)
                + contextPath;

        // Nếu chạy ngrok -> luôn force https
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
            out.println("          \"201\": { \"description\": \"Đăng ký thành công\" },");
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
            out.println("    }");

            out.println("  }");
            out.println("}");
        }
    }
}
