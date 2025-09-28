package config;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet trả về spec OpenAPI JSON cho Swagger UI
 */
@WebServlet("/api/openapi.json")
public class SwaggerConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        // Lấy base URL động
        String baseUrl = req.getScheme() + "://" + req.getServerName()
                       + ":" + req.getServerPort()
                       + req.getContextPath();

        try (PrintWriter out = resp.getWriter()) {
            out.print("{\n"
                    + "  \"openapi\": \"3.0.1\",\n"
                    + "  \"info\": {\n"
                    + "    \"title\": \"Battery Swap API\",\n"
                    + "    \"description\": \"API cho hệ thống đổi pin xe điện\",\n"
                    + "    \"version\": \"1.0.0\"\n"
                    + "  },\n"
                    + "  \"servers\": [\n"
                    + "    { \"url\": \"" + baseUrl + "\" }\n"
                    + "  ],\n"
                    + "  \"paths\": {\n"
                    // ==== API Login ====
                    + "    \"/api/login\": {\n"
                    + "      \"post\": {\n"
                    + "        \"summary\": \"Đăng nhập hệ thống\",\n"
                    + "        \"description\": \"Nhập email/password để đăng nhập\",\n"
                    + "        \"requestBody\": {\n"
                    + "          \"required\": true,\n"
                    + "          \"content\": {\n"
                    + "            \"application/json\": {\n"
                    + "              \"schema\": {\n"
                    + "                \"type\": \"object\",\n"
                    + "                \"properties\": {\n"
                    + "                  \"email\": { \"type\": \"string\" },\n"
                    + "                  \"password\": { \"type\": \"string\" }\n"
                    + "                },\n"
                    + "                \"required\": [\"email\", \"password\"]\n"
                    + "              },\n"
                    + "              \"example\": {\n"
                    + "                \"email\": \"nguyenvana@email.com\",\n"
                    + "                \"password\": \"pass123\"\n"
                    + "              }\n"
                    + "            }\n"
                    + "          }\n"
                    + "        },\n"
                    + "        \"responses\": {\n"
                    + "          \"200\": {\n"
                    + "            \"description\": \"Đăng nhập thành công\",\n"
                    + "            \"content\": {\n"
                    + "              \"application/json\": {\n"
                    + "                \"example\": {\n"
                    + "                  \"status\": \"success\",\n"
                    + "                  \"user\": {\n"
                    + "                    \"id\": 1,\n"
                    + "                    \"username\": \"nguyenvana@email.com\",\n"
                    + "                    \"name\": \"Nguyen Van A\",\n"
                    + "                    \"phone\": \"0123456789\",\n"
                    + "                    \"role\": \"Driver\",\n"
                    + "                    \"station_id\": null\n"
                    + "                  }\n"
                    + "                }\n"
                    + "              }\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"401\": { \"description\": \"Sai email hoặc password\" }\n"
                    + "        }\n"
                    + "      }\n"
                    + "    },\n"

                    // ==== API Register =====
                    + "    \"/api/register\": {\n"
                    + "      \"post\": {\n"
                    + "        \"summary\": \"Đăng ký tài khoản mới\",\n"
                    + "        \"description\": \"Tạo mới user với vai trò mặc định là Driver\",\n"
                    + "        \"requestBody\": {\n"
                    + "          \"required\": true,\n"
                    + "          \"content\": {\n"
                    + "            \"application/json\": {\n"
                    + "              \"schema\": {\n"
                    + "                \"type\": \"object\",\n"
                    + "                \"properties\": {\n"
                    + "                  \"fullName\": { \"type\": \"string\" },\n"
                    + "                  \"phone\": { \"type\": \"string\" },\n"
                    + "                  \"email\": { \"type\": \"string\", \"format\": \"email\" },\n"
                    + "                  \"password\": { \"type\": \"string\" }\n"
                    + "                },\n"
                    + "                \"required\": [\"fullName\", \"phone\", \"email\", \"password\"]\n"
                    + "              }\n"
                    + "            }\n"
                    + "          }\n"
                    + "        },\n"
                    + "        \"responses\": {\n"
                    + "          \"201\": { \"description\": \"Đăng ký thành công\" },\n"
                    + "          \"400\": { \"description\": \"Dữ liệu không hợp lệ (phone/email sai)\" },\n"
                    + "          \"409\": { \"description\": \"Email đã tồn tại\" }\n"
                    + "        }\n"
                    + "      }\n"
                    + "    },\n"

                    // ==== API Get Packages ====
                    + "    \"/api/getpackages\": {\n"
                    + "      \"get\": {\n"
                    + "        \"summary\": \"Lấy danh sách gói pin\",\n"
                    + "        \"description\": \"Trả về toàn bộ danh sách các gói pin khả dụng\",\n"
                    + "        \"responses\": {\n"
                    + "          \"200\": { \"description\": \"Danh sách gói pin\" },\n"
                    + "          \"204\": { \"description\": \"Không có gói pin nào\" }\n"
                    + "        }\n"
                    + "      }\n"
                    + "    },\n"

                    // ==== API Get Stations ====
                    + "    \"/api/getstations\": {\n"
                    + "      \"get\": {\n"
                    + "        \"summary\": \"Lấy danh sách trạm đổi pin\",\n"
                    + "        \"description\": \"Trả về toàn bộ danh sách các trạm khả dụng\",\n"
                    + "        \"responses\": {\n"
                    + "          \"200\": { \"description\": \"Danh sách trạm\" },\n"
                    + "          \"204\": { \"description\": \"Không có trạm nào\" }\n"
                    + "        }\n"
                    + "      }\n"
                    + "    }\n"

                    + "  }\n"
                    + "}");
        }
    }
}
