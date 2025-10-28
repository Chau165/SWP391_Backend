# 📊 Thống Kê Giờ Cao Điểm - Peak Hour Statistics

## 🎯 Mục Đích

Chức năng này cho phép thống kê các giao dịch đổi pin theo khung giờ dựa trên cột `Swap_Time` trong bảng `SwapTransaction`, giúp:

- Xác định giờ cao điểm trong ngày
- Phân tích xu hướng giao dịch theo thời gian
- Tối ưu hóa vận hành trạm đổi pin
- Lên kế hoạch staffing và quản lý tài nguyên

## 📁 Các File Đã Tạo

### 1. DTO (Data Transfer Object)

**File:** `src/java/DTO/PeakHourStatistics.java`

- Chứa dữ liệu thống kê: khung giờ, số lượng swap, doanh thu, phí trung bình

### 2. DAO (Data Access Object)

**File:** `src/java/DAO/SwapTransactionDAO.java` (đã cập nhật)

- Thêm 3 methods mới:
  - `getPeakHourStatistics()` - Thống kê tất cả 24 khung giờ
  - `getPeakHourStatisticsByStation()` - Thống kê theo trạm cụ thể
  - `getTopPeakHours()` - Lấy top N khung giờ cao điểm

### 3. Controller

**File:** `src/java/controller/PeakHourStatisticsController.java`

- 3 endpoints API:
  - `GET /api/secure/analytics/peak-hours` - Tất cả khung giờ
  - `GET /api/secure/analytics/peak-hours/top` - Top N khung giờ cao điểm
  - `GET /api/secure/analytics/peak-hours/station` - Thống kê theo trạm

### 4. Documentation

**File:** `docs/PEAK_HOUR_STATISTICS_API.md`

- Hướng dẫn chi tiết về API
- Ví dụ request/response
- Use cases và testing guide

### 5. SQL Queries

**File:** `docs/peak_hour_statistics_queries.sql`

- 10+ SQL queries mẫu để test và phân tích
- Các query tối ưu cho dashboard

## 🚀 Cách Sử Dụng

### 1. Build & Deploy

```bash
# Build project (nếu dùng NetBeans)
# File -> Build Project

# Hoặc deploy trực tiếp lên Tomcat
# Run -> Run Project
```

### 2. Test API

#### Lấy tất cả khung giờ

```bash
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Lấy top 5 giờ cao điểm

```bash
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours/top?limit=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Thống kê theo trạm

```bash
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours/station?stationId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Lọc theo thời gian

```bash
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours?startDate=2025-10-01&endDate=2025-10-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📊 Dữ Liệu Trả Về

```json
{
  "success": true,
  "totalSlots": 15,
  "peakHour": "08:00-09:00",
  "peakHourSwapCount": 45,
  "peakHours": [
    {
      "timeSlot": "08:00-09:00",
      "swapCount": 45,
      "totalRevenue": 2250000.0,
      "averageFee": 50000.0
    }
  ]
}
```

## 🔐 Authentication & Authorization

- **Required:** JWT Token in Authorization header
- **Allowed Roles:** Admin, Staff
- **Token Format:** `Bearer <token>`

## 💡 Use Cases

### Dashboard Analytics

```javascript
// Frontend có thể fetch dữ liệu và hiển thị:
// 1. Bar chart - Số lượng giao dịch theo giờ
// 2. Line chart - Doanh thu theo giờ
// 3. Heatmap - Giờ cao điểm trong tuần
```

### Báo Cáo Quản Lý

```sql
-- Admin có thể:
-- - Xem giờ cao điểm của từng trạm
-- - So sánh hiệu suất giữa các trạm
-- - Lập kế hoạch staffing dựa trên dữ liệu
```

## 🔧 Technical Details

### Database Query

```sql
SELECT
    DATEPART(HOUR, Swap_Time) AS HourOfDay,
    COUNT(*) AS SwapCount,
    SUM(Fee) AS TotalRevenue,
    AVG(Fee) AS AverageFee
FROM SwapTransaction
WHERE Status = 'Completed'
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY HourOfDay
```

### Filter Logic

- **Chỉ tính giao dịch:** `Status = 'Completed'`
- **Chia 24 khung giờ:** 00:00-01:00, 01:00-02:00, ..., 23:00-00:00
- **Hỗ trợ filter:** startDate, endDate, stationId

## 📝 Notes

1. **Performance:** Query được tối ưu với index trên `Swap_Time` và `Status`
2. **Timezone:** Sử dụng timezone của SQL Server
3. **NULL Handling:** Sử dụng `ISNULL()` cho Fee
4. **Date Range:** Inclusive startDate, exclusive endDate + 1 day

## 🐛 Troubleshooting

### Error: Cannot find symbol (VS Code)

- Đây là warning của VS Code do chưa configure classpath
- Code vẫn sẽ compile và chạy bình thường trong NetBeans/Tomcat
- Có thể bỏ qua hoặc configure `.classpath` file

### Error: Unauthorized

- Kiểm tra JWT token có hợp lệ không
- Verify role là Admin hoặc Staff

### No Data Returned

- Kiểm tra có dữ liệu trong bảng SwapTransaction không
- Verify Status = 'Completed'
- Thử bỏ filter startDate/endDate

## 📚 Related Documentation

- [API Documentation](./PEAK_HOUR_STATISTICS_API.md)
- [SQL Queries](./peak_hour_statistics_queries.sql)
- [Database Schema](./DATABASE_SCHEMA.md) _(nếu có)_

## 👨‍💻 Author

- Created: 2025-10-28
- Version: 1.0.0

## ✅ Checklist Deploy

- [x] Tạo DTO: PeakHourStatistics.java
- [x] Cập nhật DAO: SwapTransactionDAO.java
- [x] Tạo Controller: PeakHourStatisticsController.java
- [x] Viết documentation: PEAK_HOUR_STATISTICS_API.md
- [x] Tạo SQL queries: peak_hour_statistics_queries.sql
- [ ] Test API endpoints
- [ ] Verify với frontend team
- [ ] Deploy lên production

---

**Happy Coding! 🎉**
