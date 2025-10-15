# Hướng dẫn Debug - Vấn đề Comment không hiển thị dù có swap

## Vấn đề
User đã đăng nhập, có giao dịch trong bảng `swap_transactions`, nhưng vẫn thấy thông báo "Bạn chưa thực hiện giao dịch nào. Không thể gửi nhận xét."

## Các bước debug (thực hiện theo thứ tự)

### Bước 1: Kiểm tra Role trong Database
Chạy file SQL để xem role thực tế:
```powershell
# Trong SQL Server Management Studio, mở và chạy:
c:\AK\HOCKI5\SWP391\Code\webAPI1\check_role_issue.sql
```

**Kết quả mong đợi:**
- Role phải là `"Driver"` (chữ D hoa, không có khoảng trắng)
- Nếu thấy `"driver"`, `" Driver "`, hoặc khác → cần UPDATE database

**Cách fix nếu role sai:**
```sql
-- Fix role cho user ID 1
UPDATE Users SET Role = 'Driver' WHERE ID = 1;

-- Fix tất cả users có role là 'driver' (chữ thường)
UPDATE Users SET Role = 'Driver' WHERE Role = 'driver';

-- Xóa khoảng trắng thừa
UPDATE Users SET Role = RTRIM(LTRIM(Role));
```

### Bước 2: Kiểm tra Swap Transactions
Chạy query để xác nhận user có swap:
```sql
-- Thay 1 bằng ID của user bạn đang test
SELECT COUNT(*) AS Total FROM swap_transactions WHERE Driver_ID = 1;
```

**Kết quả mong đợi:** Total > 0

### Bước 3: Kiểm tra JOIN với Station
```sql
-- Đây là query chính xác mà code đang chạy
SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address, st.Total_Battery
FROM swap_transactions s 
JOIN Station st ON s.Station_ID = st.Station_ID
WHERE s.Driver_ID = 1;
```

**Nếu query này trả về 0 hàng:**
- Kiểm tra bảng Station có dữ liệu không:
  ```sql
  SELECT TOP 5 * FROM Station;
  ```
- Kiểm tra Station_ID trong swap_transactions có tồn tại trong Station không:
  ```sql
  SELECT DISTINCT s.Station_ID, 
         CASE WHEN st.Station_ID IS NULL THEN 'THIẾU' ELSE 'OK' END AS StationExists
  FROM swap_transactions s
  LEFT JOIN Station st ON s.Station_ID = st.Station_ID
  WHERE s.Driver_ID = 1;
  ```

**Cách fix nếu thiếu Station:**
```sql
-- Thêm Station thiếu (ví dụ Station_ID = 1)
INSERT INTO Station (Station_ID, Name, Address, Total_Battery)
VALUES (1, 'Trạm 1', '123 ABC Street', 50);
```

### Bước 4: Chạy NetBeans và xem Console Log

1. Build và Run project trong NetBeans (F6)
2. Đăng nhập bằng user ID 1
3. Mở tab **Output** trong NetBeans
4. Tìm các dòng log:
   ```
   [checkUserSwaps] Session user: userId=...
   [checkUserSwaps] After trim: role=...
   [SwapTransactionDAO] Getting stations for userId=...
   [SwapTransactionDAO] Total rows fetched: ...
   ```

**Các trường hợp có thể:**

#### Trường hợp A: Role check fail
```
[checkUserSwaps] Session user: userId=1, role=[Staff]
[checkUserSwaps] User is not Driver (role check failed), returning 403
```
→ **FIX:** UPDATE role trong database thành 'Driver'

#### Trường hợp B: Query trả về 0 rows
```
[checkUserSwaps] User IS a Driver, proceeding to get stations...
[SwapTransactionDAO] Query executed successfully
[SwapTransactionDAO] Total rows fetched: 0
[checkUserSwaps] No stations found, returning 204
```
→ **FIX:** Kiểm tra JOIN với Station (bước 3 ở trên)

#### Trường hợp C: SQL Error
```
[SwapTransactionDAO] ERROR getting stations: Invalid object name 'Station'
```
→ **FIX:** Kiểm tra tên bảng Station trong database (có thể là `station` chữ thường)

### Bước 5: Test trực tiếp trong Browser

1. Đăng nhập vào hệ thống
2. Mở DevTools (F12) → tab Console
3. Chạy lệnh:
```javascript
fetch('/webAPI/api/checkUserSwaps', { credentials: 'include' })
  .then(r => { 
    console.log('Status:', r.status); 
    return r.json().catch(() => 'empty'); 
  })
  .then(data => console.log('Data:', data));
```

**Kết quả mong đợi:**
- Status: 200
- Data: [{ swapId: 20, stationId: 1, name: "Trạm ABC", ... }]

**Nếu thấy:**
- Status: 403 → Role không phải Driver
- Status: 204 → Không có stations (JOIN fail hoặc không có swap)
- Status: 401 → Chưa đăng nhập

### Bước 6: Kiểm tra Session

Chạy trong Console của browser:
```javascript
fetch('/webAPI/api/checkUserSwaps', { credentials: 'include' })
  .then(r => r.text())
  .then(text => console.log('Response:', text));
```

Nếu trả về `[]` (empty array), session có thể bị mất. Thử:
1. Logout
2. Login lại
3. Thử lại

## Tổng kết các lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách fix |
|-------------|-------------|----------|
| 403 Forbidden | Role không phải "Driver" | UPDATE Users SET Role = 'Driver' |
| 204 No Content | JOIN fail hoặc không có swap | Kiểm tra bảng Station, thêm dữ liệu |
| 401 Unauthorized | Session mất | Login lại |
| Empty stations | Query sai hoặc DB thiếu data | Chạy SQL test ở bước 3 |

## Sau khi fix

1. Restart NetBeans server
2. Hard refresh browser (Ctrl + Shift + R)
3. Login lại
4. Kiểm tra thông báo hiển thị: "Bạn có thực hiện giao dịch..."
5. Kiểm tra select box có hiển thị tên trạm

## Cần hỗ trợ thêm?

Paste vào chat:
1. Kết quả của `check_role_issue.sql`
2. Console log từ NetBeans (toàn bộ phần có [checkUserSwaps] và [SwapTransactionDAO])
3. Response từ browser DevTools
