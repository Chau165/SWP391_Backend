# API Thống Kê Giờ Cao Điểm (Peak Hour Statistics)

## Mô tả
API này cung cấp các endpoint để thống kê giờ cao điểm của các giao dịch đổi pin (swap transactions) dựa trên cột `Swap_Time` trong bảng `SwapTransaction`.

## Authentication
- Tất cả các endpoint đều yêu cầu JWT token trong header `Authorization`
- Chỉ role **Admin** và **Staff** mới có quyền truy cập các API này

---

## 1. Lấy Thống Kê Tất Cả Khung Giờ

### Endpoint
```
GET /api/secure/analytics/peak-hours
```

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| startDate | string | No | Ngày bắt đầu lọc (format: `yyyy-MM-dd`) |
| endDate | string | No | Ngày kết thúc lọc (format: `yyyy-MM-dd`) |

### Response Success (200)
```json
{
  "success": true,
  "totalSlots": 15,
  "peakHour": "08:00-09:00",
  "peakHourSwapCount": 45,
  "peakHours": [
    {
      "timeSlot": "00:00-01:00",
      "swapCount": 5,
      "totalRevenue": 250000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "06:00-07:00",
      "swapCount": 12,
      "totalRevenue": 600000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "08:00-09:00",
      "swapCount": 45,
      "totalRevenue": 2250000.0,
      "averageFee": 50000.0
    }
  ]
}
```

### Ví dụ
```bash
# Lấy thống kê tất cả thời gian
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Lọc theo khoảng thời gian
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours?startDate=2025-10-01&endDate=2025-10-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 2. Lấy Top N Khung Giờ Cao Điểm

### Endpoint
```
GET /api/secure/analytics/peak-hours/top
```

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| limit | integer | No | Số lượng khung giờ muốn lấy (default: 5, max: 24) |
| startDate | string | No | Ngày bắt đầu lọc (format: `yyyy-MM-dd`) |
| endDate | string | No | Ngày kết thúc lọc (format: `yyyy-MM-dd`) |

### Response Success (200)
```json
{
  "success": true,
  "limit": 5,
  "topPeakHours": [
    {
      "timeSlot": "08:00-09:00",
      "swapCount": 45,
      "totalRevenue": 2250000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "17:00-18:00",
      "swapCount": 38,
      "totalRevenue": 1900000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "07:00-08:00",
      "swapCount": 32,
      "totalRevenue": 1600000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "18:00-19:00",
      "swapCount": 28,
      "totalRevenue": 1400000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "12:00-13:00",
      "swapCount": 25,
      "totalRevenue": 1250000.0,
      "averageFee": 50000.0
    }
  ]
}
```

### Ví dụ
```bash
# Lấy top 5 khung giờ cao điểm (mặc định)
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours/top" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Lấy top 10 khung giờ cao điểm trong tháng 10
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours/top?limit=10&startDate=2025-10-01&endDate=2025-10-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 3. Thống Kê Giờ Cao Điểm Theo Trạm

### Endpoint
```
GET /api/secure/analytics/peak-hours/station
```

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | integer | **Yes** | ID của trạm cần thống kê |
| startDate | string | No | Ngày bắt đầu lọc (format: `yyyy-MM-dd`) |
| endDate | string | No | Ngày kết thúc lọc (format: `yyyy-MM-dd`) |

### Response Success (200)
```json
{
  "success": true,
  "stationId": 1,
  "totalSlots": 12,
  "peakHour": "08:00-09:00",
  "peakHourSwapCount": 25,
  "peakHours": [
    {
      "timeSlot": "06:00-07:00",
      "swapCount": 8,
      "totalRevenue": 400000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "08:00-09:00",
      "swapCount": 25,
      "totalRevenue": 1250000.0,
      "averageFee": 50000.0
    },
    {
      "timeSlot": "17:00-18:00",
      "swapCount": 20,
      "totalRevenue": 1000000.0,
      "averageFee": 50000.0
    }
  ]
}
```

### Response Error
```json
{
  "success": false,
  "message": "Missing stationId parameter"
}
```

### Ví dụ
```bash
# Thống kê giờ cao điểm cho trạm ID = 1
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours/station?stationId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Thống kê cho trạm trong khoảng thời gian cụ thể
curl -X GET "http://localhost:8080/SWP391_Backend/api/secure/analytics/peak-hours/station?stationId=1&startDate=2025-10-01&endDate=2025-10-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Giải Thích Dữ Liệu Trả Về

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| success | boolean | Trạng thái thành công của request |
| totalSlots | integer | Tổng số khung giờ có dữ liệu |
| peakHour | string | Khung giờ có nhiều giao dịch nhất |
| peakHourSwapCount | integer | Số lượng giao dịch ở khung giờ cao điểm |
| timeSlot | string | Khung giờ (format: "HH:00-HH:00") |
| swapCount | integer | Số lượng giao dịch đổi pin trong khung giờ |
| totalRevenue | double | Tổng doanh thu trong khung giờ (VNĐ) |
| averageFee | double | Phí trung bình mỗi giao dịch (VNĐ) |

---

## Error Responses

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied: Admin or Staff only"
}
```

### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid stationId format"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Server error"
}
```

---

## Use Cases

### 1. Phân Tích Giờ Cao Điểm Toàn Hệ Thống
Admin có thể xem tổng quan các khung giờ có lượng giao dịch cao để:
- Lên kế hoạch staffing
- Chuẩn bị inventory pin
- Tối ưu hóa chiến dịch marketing

### 2. So Sánh Hiệu Suất Trạm
Admin có thể so sánh giờ cao điểm giữa các trạm khác nhau để:
- Phân bổ tài nguyên hợp lý
- Xác định trạm hoạt động hiệu quả
- Điều chỉnh giờ làm việc của staff

### 3. Dashboard Realtime
Frontend có thể sử dụng API này để hiển thị:
- Biểu đồ cột thể hiện số lượng giao dịch theo giờ
- Biểu đồ line chart cho doanh thu theo giờ
- Heatmap cho các khung giờ cao điểm trong tuần/tháng

### 4. Báo Cáo Định Kỳ
Tự động generate báo cáo hàng tuần/tháng về:
- Các khung giờ có performance tốt nhất
- Xu hướng thay đổi qua thời gian
- So sánh với chu kỳ trước

---

## Notes

1. **Chỉ tính giao dịch Completed**: API chỉ thống kê các giao dịch có `Status = 'Completed'`
2. **Múi giờ**: Thời gian dựa trên timezone của server database
3. **Format ngày**: Sử dụng format `yyyy-MM-dd` (ví dụ: `2025-10-28`)
4. **Khung giờ**: Chia thành 24 khung giờ từ 00:00 đến 23:00

---

## Testing

### Postman Collection
Import file `peak-hour-statistics.postman_collection.json` để test các endpoint.

### Sample Data
Đảm bảo database có dữ liệu trong bảng `SwapTransaction` với:
- Cột `Swap_Time` có giá trị
- `Status = 'Completed'`
- Có giao dịch ở nhiều khung giờ khác nhau

---

## Changelog

### Version 1.0.0 (2025-10-28)
- ✅ Thêm endpoint thống kê tất cả khung giờ
- ✅ Thêm endpoint lấy top N khung giờ cao điểm
- ✅ Thêm endpoint thống kê theo trạm
- ✅ Hỗ trợ filter theo khoảng thời gian
- ✅ Chỉ role Admin và Staff có quyền truy cập
