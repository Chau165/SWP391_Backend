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
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

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
            out.println("    \"description\": \"API cho hệ thống đổi pin xe điện — chia rõ theo vai trò Guest (khách), Driver (tài xế), Staff (nhân viên trạm) và Admin.\",");
            out.println("    \"version\": \"1.0.0\"");
            out.println("  },");

            // === Tags ===
            out.println("  \"tags\": [");
            out.println("    { \"name\": \"Guest\", \"description\": \"Các API dành cho người dùng chưa đăng nhập: đăng nhập, đăng ký, xem gói pin, xem trạm.\" },");
            out.println("    { \"name\": \"Driver\", \"description\": \"Các API dành cho tài xế đã đăng nhập: liên kết xe, thanh toán, mua gói pin.\" },");
            out.println("    { \"name\": \"Staff\", \"description\": \"Các API dành cho nhân viên trạm: quản lý pin, check-in/check-out.\" },");
            out.println("    { \"name\": \"Admin\", \"description\": \"Các API dành cho quản trị viên hệ thống: xem báo cáo, thống kê.\" }");
            out.println("  ],");

            // === Servers ===
            out.println("  \"servers\": [");
            out.println("    { \"url\": \"" + baseUrl + "\", \"description\": \"Dynamic server\" }");
            out.println("  ],");

            // === Security Scheme (JWT) ===
            out.println("  \"components\": {");
            out.println("    \"securitySchemes\": {");
            out.println("      \"bearerAuth\": {");
            out.println("        \"type\": \"http\",");
            out.println("        \"scheme\": \"bearer\",");
            out.println("        \"bearerFormat\": \"JWT\"");
            out.println("      }");
            out.println("    }");
            out.println("  },");

            out.println("  \"paths\": {");

            // ==== [GUEST] Login ====
            out.println("    \"/api/login\": {");
            out.println("      \"post\": {");
            out.println("        \"tags\": [\"Guest\"],");
            out.println("        \"summary\": \"[Guest] Đăng nhập hệ thống\",");
            out.println("        \"description\": \"Nhập email và mật khẩu để đăng nhập.\",");
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
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Đăng nhập thành công\" },");
            out.println("          \"401\": { \"description\": \"Sai email hoặc mật khẩu\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== [GUEST] Register ====
            out.println("    \"/api/register\": {");
            out.println("      \"post\": {");
            out.println("        \"tags\": [\"Guest\"],");
            out.println("        \"summary\": \"[Guest] Đăng ký tài khoản mới\",");
            out.println("        \"description\": \"Tạo mới tài khoản người dùng (vai trò mặc định là Driver).\",");
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
            out.println("                }");
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

            // ==== [GUEST] Get Packages ====
            out.println("    \"/api/getpackages\": {");
            out.println("      \"get\": {");
            out.println("        \"tags\": [\"Guest\"],");
            out.println("        \"summary\": \"[Guest] Lấy danh sách gói pin\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Danh sách gói pin\" },");
            out.println("          \"204\": { \"description\": \"Không có gói pin nào\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== [GUEST] Get Stations ====
            out.println("    \"/api/getstations\": {");
            out.println("      \"get\": {");
            out.println("        \"tags\": [\"Guest\"],");
            out.println("        \"summary\": \"[Guest] Lấy danh sách trạm đổi pin\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Danh sách trạm\" },");
            out.println("          \"204\": { \"description\": \"Không có trạm nào\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== [GUEST] Get Station Battery Report ====
            out.println("    \"/api/getStationBatteryReportGuest\": {");
            out.println("      \"get\": {");
            out.println("        \"tags\": [\"Guest\"],");
            out.println("        \"summary\": \"[Guest] Báo cáo tổng hợp pin tại các trạm\",");
            out.println("        \"description\": \"Trả về danh sách báo cáo tổng hợp tình trạng pin tại tất cả các trạm.\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Danh sách báo cáo trạm\" },");
            out.println("          \"500\": { \"description\": \"Lỗi xử lý nội bộ\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

            // ==== [DRIVER] Link Vehicle ====
            out.println("    \"/api/linkVehicleController\": {");
            out.println("      \"post\": {");
            out.println("        \"tags\": [\"Driver\"],");
            out.println("        \"summary\": \"[Driver] Liên kết xe với tài khoản\",");
            out.println("        \"description\": \"Người dùng upload ảnh cà vẹt xe để hệ thống nhận dạng và lưu vào DB.\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"multipart/form-data\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"carDoc\": { \"type\": \"string\", \"format\": \"binary\" },");
            out.println("                  \"model\": { \"type\": \"string\" },");
            out.println("                  \"batteryType\": { \"type\": \"string\" }");
            out.println("                }");
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

            // ==== [STAFF] View Battery Slot Status ====
            out.println("    \"/api/secure/viewBatterySlotStatus\": {");
            out.println("      \"get\": {");
            out.println("        \"tags\": [\"Staff\"],");
            out.println("        \"summary\": \"[Staff] Xem trạng thái pin trong các slot của trạm\",");
            out.println("        \"description\": \"API cho phép nhân viên trạm xem danh sách các khe pin trong trạm của họ.\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": { \"description\": \"Danh sách slot tại trạm\" },");
            out.println("          \"401\": { \"description\": \"Thiếu token hoặc token không hợp lệ\" },");
            out.println("          \"403\": { \"description\": \"Không có quyền truy cập (chỉ dành cho Staff)\" },");
            out.println("          \"500\": { \"description\": \"Lỗi xử lý nội bộ server\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");


// ==== API Add Package ====
            out.println("    \"/api/package\": {");
            out.println("      \"post\": {");
            out.println("        \"summary\": \"Thêm gói pin mới\",");
            out.println("        \"description\": \"Tạo mới một gói pin trong hệ thống\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"application/json\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"packageName\": { \"type\": \"string\", \"description\": \"Tên gói pin\" },");
            out.println("                  \"description\": { \"type\": \"string\", \"description\": \"Mô tả gói pin\" },");
            out.println("                  \"price\": { \"type\": \"number\", \"description\": \"Giá gói pin\" },");
            out.println("                  \"duration\": { \"type\": \"integer\", \"description\": \"Thời hạn gói (ngày)\" },");
            out.println("                  \"batteryType\": { \"type\": \"string\", \"description\": \"Loại pin\" },");
            out.println("                  \"capacity\": { \"type\": \"number\", \"description\": \"Dung lượng pin (kWh)\" }");
            out.println("                },");
            out.println("                \"required\": [\"packageName\", \"price\", \"duration\"]");
            out.println("              },");
            out.println("              \"example\": {");
            out.println("                \"packageName\": \"Gói Basic\",");
            out.println("                \"description\": \"Gói pin cơ bản cho xe máy điện\",");
            out.println("                \"price\": 200000,");
            out.println("                \"duration\": 30,");
            out.println("                \"batteryType\": \"Lithium-ion\",");
            out.println("                \"capacity\": 2.5");
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": {");
            out.println("            \"description\": \"Thêm gói pin thành công\",");
            out.println("            \"content\": {");
            out.println("              \"application/json\": {");
            out.println("                \"schema\": {");
            out.println("                  \"type\": \"object\",");
            out.println("                  \"properties\": {");
            out.println("                    \"status\": { \"type\": \"string\", \"example\": \"success\" },");
            out.println("                    \"package\": { \"type\": \"object\" }");
            out.println("                  }");
            out.println("                }");
            out.println("              }");
            out.println("            }");
            out.println("          },");
            out.println("          \"401\": { \"description\": \"Không có quyền thêm gói pin\" },");
            out.println("          \"500\": { \"description\": \"Lỗi server\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");
             // ==== API Package Revenue Statistics ====
            out.println("    \"/api/package-revenue-statistics\": {");
            out.println("      \"get\": {");
            out.println("        \"summary\": \"Thống kê doanh thu gói pin\",");
            out.println("        \"description\": \"Lấy thống kê doanh thu theo từng gói pin trong tháng hiện tại\",");
            out.println("        \"responses\": {");
            out.println("          \"200\": {");
            out.println("            \"description\": \"Lấy thống kê thành công\",");
            out.println("            \"content\": {");
            out.println("              \"application/json\": {");
            out.println("                \"schema\": {");
            out.println("                  \"type\": \"object\",");
            out.println("                  \"properties\": {");
            out.println("                    \"status\": { \"type\": \"string\", \"example\": \"success\" },");
            out.println("                    \"data\": {");
            out.println("                      \"type\": \"array\",");
            out.println("                      \"items\": {");
            out.println("                        \"type\": \"array\",");
            out.println("                        \"items\": {");
            out.println("                          \"oneOf\": [");
            out.println("                            { \"type\": \"integer\" },");
            out.println("                            { \"type\": \"string\" },");
            out.println("                            { \"type\": \"number\" }");
            out.println("                          ]");
            out.println("                        },");
            out.println("                        \"description\": \"[Package_ID, Name, SoLuongMua, TongDoanhThu]\"");
            out.println("                      }");
            out.println("                    }");
            out.println("                  }");
            out.println("                },");
            out.println("                \"example\": {");
            out.println("                  \"status\": \"success\",");
            out.println("                  \"data\": [");
            out.println("                    [1, \"Gói Basic\", 15, 3000000.0],");
            out.println("                    [2, \"Gói Premium\", 8, 4000000.0]");
            out.println("                  ]");
            out.println("                }");
            out.println("              }");
            out.println("            }");
            out.println("          },");
            out.println("          \"204\": { \"description\": \"Không có dữ liệu doanh thu\" },");
            out.println("          \"500\": { \"description\": \"Lỗi server\" }");
            out.println("        }");
            out.println("      }");
            out.println("    },");

  // ==== API Delete Package ====
            out.println("    \"/api/package/delete\": {");
            out.println("      \"post\": {");
            out.println("        \"summary\": \"Xóa gói pin (soft delete)\",");
            out.println("        \"description\": \"Đánh dấu gói pin là inactive thay vì xóa vật lý\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"application/json\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"packageId\": { \"type\": \"integer\", \"description\": \"ID của gói pin cần xóa\" }");
            out.println("                },");
            out.println("                \"required\": [\"packageId\"]");
            out.println("              },");
            out.println("              \"example\": {");
            out.println("                \"packageId\": 1");
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": {");
            out.println("            \"description\": \"Xóa gói pin thành công\",");
            out.println("            \"content\": {");
            out.println("              \"application/json\": {");
            out.println("                \"schema\": {");
            out.println("                  \"type\": \"object\",");
            out.println("                  \"properties\": {");
            out.println("                    \"status\": { \"type\": \"string\", \"example\": \"success\" },");
            out.println("                    \"message\": { \"type\": \"string\", \"example\": \"Package deleted successfully\" },");
            out.println("                    \"packageId\": { \"type\": \"integer\" }");
            out.println("                  }");
            out.println("                }");
            out.println("              }");
            out.println("            }");
            out.println("          },");
            out.println("          \"400\": { \"description\": \"ID gói pin không hợp lệ\" },");
            out.println("          \"404\": { \"description\": \"Không tìm thấy gói pin hoặc đã bị xóa\" },");
            out.println("          \"500\": { \"description\": \"Lỗi server\" }");
            out.println("        }");
            out.println("      },");
            out.println("      \"delete\": {");
            out.println("        \"summary\": \"Xóa gói pin (soft delete) - DELETE method\",");
            out.println("        \"description\": \"Đánh dấu gói pin là inactive thay vì xóa vật lý (sử dụng DELETE method)\",");
            out.println("        \"requestBody\": {");
            out.println("          \"required\": true,");
            out.println("          \"content\": {");
            out.println("            \"application/json\": {");
            out.println("              \"schema\": {");
            out.println("                \"type\": \"object\",");
            out.println("                \"properties\": {");
            out.println("                  \"packageId\": { \"type\": \"integer\", \"description\": \"ID của gói pin cần xóa\" }");
            out.println("                },");
            out.println("                \"required\": [\"packageId\"]");
            out.println("              },");
            out.println("              \"example\": {");
            out.println("                \"packageId\": 1");
            out.println("              }");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        \"responses\": {");
            out.println("          \"200\": {");
            out.println("            \"description\": \"Xóa gói pin thành công\",");
            out.println("            \"content\": {");
            out.println("              \"application/json\": {");
            out.println("                \"schema\": {");
            out.println("                  \"type\": \"object\",");
            out.println("                  \"properties\": {");
            out.println("                    \"status\": { \"type\": \"string\", \"example\": \"success\" },");
            out.println("                    \"message\": { \"type\": \"string\", \"example\": \"Package deleted successfully\" },");
            out.println("                    \"packageId\": { \"type\": \"integer\" }");
            out.println("                  }");
            out.println("                }");
            out.println("              }");
            out.println("            }");
            out.println("          },");
            out.println("          \"400\": { \"description\": \"ID gói pin không hợp lệ\" },");
            out.println("          \"404\": { \"description\": \"Không tìm thấy gói pin hoặc đã bị xóa\" },");
            out.println("          \"500\": { \"description\": \"Lỗi server\" }");
            out.println("        }");
            out.println("      }");
            out.println("    }");

            out.println("  }");
            out.println("}");
        }
    }
}
