# 🔄 Cách Restart và Xem API trên Swagger UI

## Bước 1: Clean và Build Project

1. Trong NetBeans:
   - Click chuột phải vào project "SWP391_Backend"
   - Chọn **Clean and Build**
2. Hoặc trong terminal:
   ```bash
   # Nếu có ant
   ant clean build
   ```

## Bước 2: Restart Tomcat Server

1. Trong NetBeans:
   - Vào tab "Services"
   - Tìm "Servers" > "Apache Tomcat"
   - Click chuột phải > **Restart**
2. Hoặc stop và start lại:
   - Stop Server
   - Start Server

## Bước 3: Deploy lại Project

1. Click chuột phải vào project
2. Chọn **Run** hoặc **Deploy**

## Bước 4: Mở Swagger UI

1. Mở trình duyệt và truy cập:

   ```
   http://localhost:8080/SWP391_Backend/swagger-ui/index.html
   ```

2. Hoặc:
   ```
   http://localhost:8080/SWP391_Backend/swagger-ui/
   ```

## Bước 5: Kiểm Tra API Mới

Trong Swagger UI, bạn sẽ thấy 3 endpoint mới trong tag **Admin**:

✅ **GET /api/secure/analytics/peak-hours**

- Thống kê tất cả khung giờ
- Parameters: startDate, endDate (optional)

✅ **GET /api/secure/analytics/peak-hours/top**

- Top N khung giờ cao điểm
- Parameters: limit, startDate, endDate

✅ **GET /api/secure/analytics/peak-hours/station**

- Thống kê theo trạm
- Parameters: stationId (required), startDate, endDate

## Bước 6: Test API trên Swagger

1. Click vào endpoint muốn test
2. Click nút **"Try it out"**
3. Nhập JWT token vào Authorization (click nút 🔒 ở góc phải trên)
4. Nhập parameters (nếu có)
5. Click **"Execute"**
6. Xem kết quả trong Response body

## ⚠️ Lưu Ý

- Đảm bảo server đã start hoàn toàn trước khi test
- JWT token phải hợp lệ và có role Admin hoặc Staff
- Kiểm tra database có dữ liệu trong bảng SwapTransaction

## 🐛 Troubleshooting

### Không thấy API mới?

- Clear browser cache (Ctrl + Shift + R)
- Restart Tomcat server
- Kiểm tra file SwaggerConfigServlet.java đã save chưa

### Error 404?

- Kiểm tra URL có đúng không
- Verify project đã deploy thành công

### Error 401 Unauthorized?

- Kiểm tra JWT token có hợp lệ không
- Click nút 🔒 và nhập token với format: `Bearer <your_token>`

---

**Chúc bạn thành công! 🎉**
