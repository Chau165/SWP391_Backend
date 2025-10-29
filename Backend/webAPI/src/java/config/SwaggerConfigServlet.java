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
            out.println("    },");

            // ------------------ Guest (public) ------------------
            // Guest can view packages, stations, health
            out.println("    \"/api/health\": {\n" +
                        "      \"get\": {\n" +
                        "        \"summary\": \"Health check\",\n" +
                        "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n" +
                        "      }\n" +
                        "    },");

            // ------------------ Driver endpoints ------------------
            out.println("    \"/api/driver/swap\": {");
            out.println("      \"post\": {");
            out.println("        \"summary\": \"Yêu cầu đổi pin (Driver)\",");
            out.println("        \"description\": \"Driver gửi yêu cầu đổi pin tại một trạm (tự động hoặc theo stationId)\",");
            out.println("        \"requestBody\": {\n" +
                        "          \"required\": true,\n" +
                        "          \"content\": {\n" +
                        "            \"application/json\": {\n" +
                        "              \"schema\": {\n" +
                        "                \"type\": \"object\",\n" +
                        "                \"properties\": {\n" +
                        "                  \"driverId\": { \"type\": \"integer\" },\n" +
                        "                  \"stationId\": { \"type\": \"integer\" },\n" +
                        "                  \"packageId\": { \"type\": \"integer\" }\n" +
                        "                },\n" +
                        "                \"required\": [\"driverId\", \"packageId\"]\n" +
                        "              }\n" +
                        "            }\n" +
                        "          }\n" +
                        "        },");
            out.println("        \"responses\": { \"201\": { \"description\": \"Swap requested\" }, \"400\": { \"description\": \"Invalid request\" }, \"403\": { \"description\": \"Forbidden\" } }" );
            out.println("      }" );
            out.println("    },");

            out.println("    \"/api/driver/history\": {\n" +
                        "      \"get\": {\n" +
                        "        \"summary\": \"Lịch sử đổi pin của Driver\",\n" +
                        "        \"parameters\": [ { \"name\": \"driverId\", \"in\": \"query\", \"schema\": { \"type\": \"integer\" }, \"required\": true } ],\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Danh sách lịch sử\" }, \"204\": { \"description\": \"Không có dữ liệu\" } }\n" +
                        "      }\n" +
                        "    },");

            out.println("    \"/api/profile\": {\n" +
                        "      \"get\": {\n" +
                        "        \"summary\": \"Lấy profile người dùng\",\n" +
                        "        \"parameters\": [ { \"name\": \"userId\", \"in\": \"query\", \"schema\": { \"type\": \"integer\" }, \"required\": true } ],\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Thông tin profile\" }, \"404\": { \"description\": \"Không tìm thấy user\" } }\n" +
                        "      },\n" +
                        "      \"put\": {\n" +
                        "        \"summary\": \"Cập nhật profile (Driver/Staff/Admin)\",\n" +
                        "        \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"userId\": { \"type\": \"integer\" }, \"fullName\": { \"type\": \"string\" }, \"phone\": { \"type\": \"string\" }, \"avatarUrl\": { \"type\": \"string\" } }, \"required\": [\"userId\"] } } } },\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Updated\" }, \"400\": { \"description\": \"Invalid\" }, \"403\": { \"description\": \"Forbidden\" } }\n" +
                        "      }\n" +
                        "    },");

            // ------------------ Staff endpoints ------------------
            out.println("    \"/api/staff/assigned\": {\n" +
                        "      \"get\": {\n" +
                        "        \"summary\": \"Danh sách nhiệm vụ được giao cho Staff\",\n" +
                        "        \"parameters\": [ { \"name\": \"staffId\", \"in\": \"query\", \"schema\": { \"type\": \"integer\" }, \"required\": true } ],\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Danh sách nhiệm vụ\" }, \"204\": { \"description\": \"Không có nhiệm vụ\" } }\n" +
                        "      }\n" +
                        "    },");

            out.println("    \"/api/staff/complete-swap\": {\n" +
                        "      \"post\": {\n" +
                        "        \"summary\": \"Đánh dấu hoàn thành đổi pin (Staff)\",\n" +
                        "        \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"taskId\": { \"type\": \"integer\" }, \"staffId\": { \"type\": \"integer\" } }, \"required\": [\"taskId\",\"staffId\"] } } } },\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Completed\" }, \"400\": { \"description\": \"Invalid\" }, \"403\": { \"description\": \"Forbidden\" } }\n" +
                        "      }\n" +
                        "    },");

            out.println("    \"/api/staff/station-status\": {\n" +
                        "      \"get\": {\n" +
                        "        \"summary\": \"Trạng thái trạm (Staff)\",\n" +
                        "        \"parameters\": [ { \"name\": \"stationId\", \"in\": \"query\", \"schema\": { \"type\": \"integer\" } } ],\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Station status\" } }\n" +
                        "      }\n" +
                        "    },");

            // ------------------ Admin endpoints ------------------
            out.println("    \"/api/admin/users\": {\n" +
                        "      \"get\": {\n" +
                        "        \"summary\": \"Danh sách user (Admin)\",\n" +
                        "        \"responses\": { \"200\": { \"description\": \"List of users\" } }\n" +
                        "      },\n" +
                        "      \"post\": {\n" +
                        "        \"summary\": \"Tạo user mới (Admin)\",\n" +
                        "        \"requestBody\": { \"required\": true, \"content\": { \"multipart/form-data\": { \"schema\": { \"type\": \"object\", \"properties\": { \"fullName\": { \"type\": \"string\" }, \"email\": { \"type\": \"string\" }, \"role\": { \"type\": \"string\" }, \"avatarFile\": { \"type\": \"string\", \"format\": \"binary\" } }, \"required\": [\"fullName\",\"email\"] } } } },\n" +
                        "        \"responses\": { \"201\": { \"description\": \"Created\" }, \"400\": { \"description\": \"Invalid\" } }\n" +
                        "      },\n" +
                        "      \"put\": {\n" +
                        "        \"summary\": \"Cập nhật user (Admin)\",\n" +
                        "        \"requestBody\": { \"required\": true, \"content\": { \"application/json\": { \"schema\": { \"type\": \"object\", \"properties\": { \"id\": { \"type\": \"integer\" }, \"fullName\": { \"type\": \"string\" }, \"status\": { \"type\": \"string\" } }, \"required\": [\"id\"] } } } },\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Updated\" }, \"403\": { \"description\": \"Forbidden\" } }\n" +
                        "      },\n" +
                        "      \"delete\": {\n" +
                        "        \"summary\": \"Xóa user (Admin)\",\n" +
                        "        \"parameters\": [ { \"name\": \"id\", \"in\": \"query\", \"schema\": { \"type\": \"integer\" }, \"required\": true } ],\n" +
                        "        \"responses\": { \"200\": { \"description\": \"Deleted\" }, \"403\": { \"description\": \"Forbidden\" } }\n" +
                        "      }\n" +
                        "    },");

            out.println("    \"/api/admin/stations\": {\n" +
                        "      \"get\": { \"summary\": \"List stations (Admin)\", \"responses\": { \"200\": { \"description\": \"List\" } } },\n" +
                        "      \"post\": { \"summary\": \"Create station\", \"responses\": { \"201\": { \"description\": \"Created\" } } }\n" +
                        "    },");

            out.println("    \"/api/admin/packages\": {\n" +
                        "      \"get\": { \"summary\": \"List packages (Admin)\", \"responses\": { \"200\": { \"description\": \"List\" } } },\n" +
                        "      \"post\": { \"summary\": \"Create package\", \"responses\": { \"201\": { \"description\": \"Created\" } } }\n" +
                        "    }");

            // close paths and add basic components for security (optional)
            out.println("  },");
            out.println("  \"components\": {\n" +
                        "    \"securitySchemes\": {\n" +
                        "      \"BearerAuth\": { \"type\": \"http\", \"scheme\": \"bearer\", \"bearerFormat\": \"JWT\" }\n" +
                        "    }\n" +
                        "  },");
            out.println("  \"security\": [ { \"BearerAuth\": [] } ]");
            out.println("}");
        }
    }
}
