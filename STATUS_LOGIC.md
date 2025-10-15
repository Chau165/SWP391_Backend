# Logic Comment với Status = 'Completed'

## Quy tắc mới (Updated)

Người dùng được phép comment KHI VÀ CHỈ KHI:
1. ✅ Role = "Driver" (không phân biệt hoa/thường)
2. ✅ Có ít nhất 1 swap trong bảng `swap_transactions` với:
   - `Driver_ID` = user's ID
   - **`Status` = 'Completed'** (chữ C hoa)
3. ✅ Station từ swap đó phải tồn tại trong bảng `Station`

## Các trường hợp

### ✅ Được comment
```
User: ID=1, Role='Driver'
Swap: ID=20, Driver_ID=1, Status='Completed', Station_ID=1
Station: Station_ID=1, Name='Trạm A'
→ Hiển thị: "Bạn có thực hiện giao dịch. Bạn có 1 trạm..."
→ Cho phép chọn "Trạm A" và gửi comment
```

### ❌ KHÔNG được comment - Swap chưa hoàn thành
```
User: ID=2, Role='Driver'
Swap: ID=32, Driver_ID=2, Status='Processing', Station_ID=1
→ Hiển thị: "Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét."
```

### ❌ KHÔNG được comment - Không phải Driver
```
User: ID=17, Role='Staff'
Swap: ID=26, Driver_ID=17, Status='Completed', Station_ID=3
→ Hiển thị: "Chỉ tài xế (Driver) được gửi nhận xét. Bạn không có quyền."
```

### ❌ KHÔNG được comment - Có swap nhưng không có Completed
```
User: ID=4, Role='Driver'
Swap 1: ID=32, Driver_ID=4, Status='Processing'
Swap 2: ID=33, Driver_ID=4, Status='Pending'
→ Hiển thị: "Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét."
```

## SQL Queries sử dụng

### 1. Kiểm tra có swap Completed không
```sql
SELECT TOP 1 ID 
FROM swap_transactions 
WHERE Driver_ID = ? 
  AND Status = 'Completed'
```

### 2. Lấy danh sách station từ swap Completed
```sql
SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address, st.Total_Battery
FROM swap_transactions s 
JOIN Station st ON s.Station_ID = st.Station_ID
WHERE s.Driver_ID = ? 
  AND s.Status = 'Completed'
```

## Test nhanh

### Test trong SQL Server:
```powershell
# Mở SSMS và chạy:
c:\AK\HOCKI5\SWP391\Code\webAPI1\test_completed_status.sql
```

### Test case theo ảnh bạn gửi:

Từ ảnh swap_transactions:
- Rows 1-11: Status = "Completed" ✅ → được comment
- Rows 12-20: Status = "Processing" ❌ → KHÔNG được comment

**User ID 1:**
- Swap ID 20: Status='Completed' → OK
- Swap ID 31: Status='Processing' → Skip
→ Kết quả: User 1 được comment vì có ít nhất 1 swap Completed

**User ID 4:**
- Swap ID 32: Status='Processing' → Skip
→ Kết quả: User 4 KHÔNG được comment

**User ID 8:**
- Swap ID 25: Status='Completed' → OK
- Swap ID 33: Status='Processing' → Skip
→ Kết quả: User 8 được comment (vì có swap 25)

## Cách fix nếu user không thấy comment dù có swap

### 1. Check Status value
```sql
SELECT ID, Driver_ID, Status, 
       CASE 
           WHEN Status = 'Completed' THEN 'OK'
           WHEN Status = 'completed' THEN 'SAI - chữ thường'
           WHEN Status = 'COMPLETED' THEN 'SAI - chữ hoa toàn bộ'
           ELSE 'SAI - Status = [' + Status + ']'
       END AS StatusCheck
FROM swap_transactions
WHERE Driver_ID = 1;
```

### 2. Fix Status nếu sai
```sql
-- Fix chữ thường thành Completed
UPDATE swap_transactions 
SET Status = 'Completed' 
WHERE Status = 'completed';

-- Fix chữ hoa
UPDATE swap_transactions 
SET Status = 'Completed' 
WHERE Status = 'COMPLETED';

-- Trim khoảng trắng
UPDATE swap_transactions 
SET Status = RTRIM(LTRIM(Status));
```

### 3. Verify lại
```sql
SELECT Driver_ID, Status, COUNT(*) AS Total
FROM swap_transactions
GROUP BY Driver_ID, Status
ORDER BY Driver_ID, Status;
```

## Debug trong NetBeans

Console sẽ hiển thị:
```
[SwapTransactionDAO] Checking COMPLETED swaps for userId=1, role=Driver
[SwapTransactionDAO] SQL: SELECT TOP 1 ID FROM swap_transactions WHERE Driver_ID = ? AND Status = 'Completed'
[SwapTransactionDAO] Result: hasCompletedSwap=true
[SwapTransactionDAO] Found completed swap ID: 20
```

Nếu thấy `hasCompletedSwap=false` → không có swap Completed → chạy query fix Status ở trên.

## API Response

### Có swap Completed:
```json
HTTP 200 OK
[
  {
    "swapId": 20,
    "stationId": 1,
    "name": "Trạm A",
    "address": "123 ABC",
    "totalBattery": 50
  }
]
```

### Không có swap Completed:
```json
HTTP 204 No Content
[]
```

Frontend sẽ hiển thị: "Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét."
