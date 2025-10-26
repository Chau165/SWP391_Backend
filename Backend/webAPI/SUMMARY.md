# API Station CRUD - Battery Swap Admin Dashboard

## 1. Station CRUD API

### Endpoint
- Base: `http://localhost:8080/webAPI3/api/station`

### 1.1. Tạo mới trạm (Create)
- **Method:** POST
- **URL:** `/api/station`
- **Body (JSON):**
```json
{
  "Name": "Station A",
  "Address": "123 Main St",
  "Total_Battery": 10
}
```
- **Response:**
```json
{"status":"success","message":"Station created"}
```

### 1.2. Sửa trạm (Update)
- **Method:** PUT
- **URL:** `/api/station`
- **Body (JSON):**
```json
{
  "Station_ID": 1,
  "Name": "Station A Updated",
  "Address": "123 Main St",
  "Total_Battery": 12
}
```
- **Response:**
```json
{"status":"success","message":"Station updated"}
```

### 1.3. Xóa trạm (Delete)
- **Method:** DELETE
- **URL:** `/api/station?id=1`
- **Response:**
```json
{"status":"success","message":"Station deleted"}
```

### 1.4. Lấy danh sách trạm (List)
- **Method:** GET
- **URL:** `/api/getstations`
- **Response:**
```json
[
  {"Station_ID":1,"Name":"Station A","Address":"123 Main St","Total_Battery":10},
  ...
]
```

## 2. Hướng dẫn test
- Dùng Postman hoặc curl để gửi request tới các endpoint trên.
- Đảm bảo đã add gson.jar và servlet-api.jar vào project (NetBeans: chuột phải project > Properties > Libraries > Add JAR/Folder).
- Nếu lỗi import, kiểm tra lại cấu hình thư viện.

## 3. Ghi chú
- Các API trả về JSON, hỗ trợ CORS.
- Nếu cần thêm API hoặc chức năng khác, báo lại để bổ sung.

## 4. Cleanup performed (2025-10-26)

- Deleted helper and deployment scripts under `Backend/webAPI/scripts`:
  - `deploy_copy_to_webapi3.ps1`
  - `find_and_deploy_webapi3.ps1`
  - `smoke_webapi3.ps1`
  - `inspect_war.ps1`

- Deleted helper README under `Backend/webAPI/web/swagger-ui/README.md`.

- Deleted legacy `src/com` Java sources (these were legacy duplicates / placeholders not referenced by current `src/java` code):
  - `src/com/example/admin/dao/ChargingStationDAO.java`
  - `src/com/example/admin/dao/StationDAO.java`
  - `src/com/example/admin/model/ChargingStation.java`
  - `src/com/example/admin/model/Station.java`
  - `src/com/example/admin/servlet/ChargingStationServlet.java`
  - `src/com/example/admin/servlet/StationServlet.java`
  - `src/com/example/admin/util/DBConnection.java`
  - `src/com/swp391/servlet/StationApiServlet.java` (placeholder)
  - `src/com/swp391/servlet/ChargingStationApiServlet.java` (placeholder)

Commit: chore(cleanup): remove generated docs and legacy com sources (pushed to branch `Admin-CRUD-Station-ChargingStation`).

If you'd like, I can also remove the now-empty `src/com` directory itself — let me know and I will delete it and push another commit.
