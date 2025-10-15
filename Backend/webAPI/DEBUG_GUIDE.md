# 🔍 DEBUG GUIDE - Tìm lỗi tại sao User ID 1 không comment được

## Bước 1: Kiểm tra Database trực tiếp

### A. Chạy script SQL debug
Trong SQL Server Management Studio hoặc sqlcmd:

```powershell
cd Backend\webAPI
sqlcmd -S "YOUR_SERVER" -U "YOUR_USER" -P "YOUR_PASSWORD" -d "YOUR_DB" -i "debug_status_check.sql" -o "debug_result.txt"
```

Sau đó mở file `debug_result.txt` và tìm:
- **Check 2**: Xem chính xác giá trị của cột Status (có thể có khoảng trắng)
- **Check 4**: Xem tất cả các giá trị Status trong database

### B. Test query đơn giản
```sql
-- Kiểm tra User ID 1 có swap không
SELECT * FROM swap_transactions WHERE Driver_ID = 1;

-- Kiểm tra có swap Completed không (exact match)
SELECT * FROM swap_transactions WHERE Driver_ID = 1 AND Status = 'Completed';

-- Kiểm tra với TRIM và UPPER
SELECT * FROM swap_transactions 
WHERE Driver_ID = 1 AND UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED';
```

**KẾT QUẢ MONG ĐỢI**:
- Query 1 phải trả về ít nhất 1 row (Swap ID 20 từ screenshot)
- Query 2 hoặc 3 phải trả về row đó nếu Status thực sự là "Completed"

---

## Bước 2: Kiểm tra NetBeans Console Log

Sau khi tôi thêm debug log, khi bạn chạy NetBeans và test:

### A. Login với User ID 1
```
Email: nguyenvana@email.com
Password: pass123
```

### B. Xem NetBeans Output/Console, tìm các dòng DEBUG:

**Login thành công sẽ thấy**:
```
(không có DEBUG từ login controller vì chưa thêm, nhưng session sẽ được tạo)
```

**Khi load trang hoặc gọi /api/checkUserSwaps**:
```
DEBUG checkUserSwapsController - User: ID=1, Role=Driver
DEBUG checkUserSwapsController - Fetching stations for user...
DEBUG getStationsWithSwapIdsByUser - userId: 1, role: Driver
DEBUG - Found station: [Tên Station] (ID: [Station_ID])
DEBUG - Total stations found: [số lượng]
DEBUG checkUserSwapsController - Stations found: [số lượng]
DEBUG checkUserSwapsController - Returning [số lượng] stations, status 200
```

**Nếu KHÔNG có stations**:
```
DEBUG checkUserSwapsController - User: ID=1, Role=Driver
DEBUG checkUserSwapsController - Fetching stations for user...
DEBUG getStationsWithSwapIdsByUser - userId: 1, role: Driver
DEBUG - Total stations found: 0
DEBUG checkUserSwapsController - No completed swaps, returning 204
```

**Khi gửi comment (POST /api/comment)**:
```
DEBUG commentController - User logged in: ID=1, Role=Driver
DEBUG commentController - Checking if user has completed swaps...
DEBUG userHasSwapTransactions - userId: 1, role: Driver
DEBUG userHasSwapTransactions - Result: true (hoặc false)
DEBUG commentController - Has completed swaps: true (hoặc false)
```

---

## Bước 3: Phân tích kết quả

### Case 1: Nếu DEBUG log cho thấy Result: false
**Nguyên nhân**: SQL query không match được Status trong database

**Giải pháp đã áp dụng**: Tôi đã sửa query dùng:
```sql
UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED'
```

Nhưng nếu vẫn false, có thể:
1. Cột Status có giá trị khác hoàn toàn (ví dụ: "Complete", "Done", v.v.)
2. Encoding issue (Unicode characters)
3. Driver_ID không phải 1 (kiểm tra lại login)

### Case 2: Nếu DEBUG không hiện gì
**Nguyên nhân**: Code không chạy vào đó, có thể:
1. Session không tồn tại (login failed)
2. Request không đến controller (URL sai, servlet mapping sai)
3. NetBeans không deploy code mới

**Giải pháp**:
- Clean & Build project trong NetBeans
- Restart server (Stop, Start lại)
- F5 reload browser (clear cache: Ctrl+Shift+R)

### Case 3: Nếu Role không phải "Driver"
**Nguyên nhân**: Database Users table có giá trị Role khác

**Kiểm tra**:
```sql
SELECT ID, FullName, Email, Role, Status 
FROM Users 
WHERE ID = 1;
```

Nếu Role = "driver" (lowercase) hoặc " Driver " (có space), code vẫn work vì dùng `.equalsIgnoreCase()`, nhưng nếu Role = NULL hoặc khác (ví dụ "Customer"), sẽ bị block.

---

## Bước 4: Test từng bước riêng biệt

### Test A: Direct SQL
```sql
-- Phải return 1 row
SELECT TOP 1 ID 
FROM swap_transactions 
WHERE Driver_ID = 1 AND UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED';
```

Nếu query này **KHÔNG** return row:
→ **VẤN ĐỀ Ở DATABASE**: Status không phải "Completed" hoặc Driver_ID không phải 1

Nếu query này **CÓ** return row:
→ **VẤN ĐỀ Ở CODE**: Java không chạy đúng query hoặc session sai

### Test B: API trực tiếp (Postman hoặc curl)

1. **Login trước**:
```powershell
# PowerShell
$body = '{"email":"nguyenvana@email.com","password":"pass123"}'
$response = Invoke-WebRequest -Uri "http://localhost:8080/webAPI/api/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -SessionVariable session

Write-Host $response.Content
```

2. **Check swaps**:
```powershell
$swaps = Invoke-WebRequest -Uri "http://localhost:8080/webAPI/api/checkUserSwaps" `
    -Method GET `
    -WebSession $session

Write-Host "Status Code: " $swaps.StatusCode
Write-Host $swaps.Content
```

**Kết quả mong đợi**:
- Status Code: 200
- Content: JSON array với ít nhất 1 station

**Nếu Status Code: 204**:
→ User không có swap Completed → Kiểm tra database

**Nếu Status Code: 403**:
→ User không phải Driver → Kiểm tra Role trong Users table

**Nếu Status Code: 401**:
→ Session không tồn tại → Login failed hoặc session expired

---

## Bước 5: Fix các vấn đề thường gặp

### Fix 1: Status có khoảng trắng hoặc case khác
✅ **ĐÃ FIX** - Query hiện dùng `UPPER(LTRIM(RTRIM(Status)))`

### Fix 2: Driver_ID trong database không khớp với Users.ID
**Kiểm tra**:
```sql
SELECT u.ID, u.Email, st.ID AS SwapID, st.Driver_ID, st.Status
FROM Users u
LEFT JOIN swap_transactions st ON u.ID = st.Driver_ID
WHERE u.Email = 'nguyenvana@email.com';
```

Nếu Driver_ID = NULL hoặc khác u.ID:
→ **DATABASE SAI** - Phải sửa foreign key hoặc data

### Fix 3: Session không lưu User object
**Thêm debug vào loginController.java**:
```java
// Sau khi login thành công
session.setAttribute("User", user);
System.out.println("DEBUG login - Set session User: ID=" + user.getId() + ", Role=" + user.getRole());
```

### Fix 4: NetBeans không deploy code mới
**Solution**:
```
1. Right-click project → Clean and Build
2. Right-click project → Run (hoặc Debug)
3. Wait for "BUILD SUCCESSFUL"
4. Trong browser: Ctrl+Shift+R (hard reload)
```

---

## Bước 6: Chạy lại sau khi sửa

1. **Clean & Build**:
```
NetBeans → Right-click project → Clean and Build
```

2. **Run**:
```
NetBeans → Right-click project → Run
```

3. **Kiểm tra Output tab** trong NetBeans, phải thấy:
```
Building jar: C:\...\build\web\WEB-INF\lib\...
BUILD SUCCESSFUL (total time: X seconds)
Deploying on Apache Tomcat or TomEE
...
```

4. **Test trên browser**:
- Mở http://localhost:8080/webAPI/index.html
- Login: nguyenvana@email.com / pass123
- Mở F12 Developer Tools → Console tab
- Xem có errors không

5. **Xem NetBeans Output tab** (trong khi test):
- Phải thấy các dòng DEBUG tôi thêm ở trên
- Copy toàn bộ log paste cho tôi nếu vẫn lỗi

---

## Câu hỏi debug cho tôi (trả lời để tôi fix tiếp)

1. **Kết quả SQL**:
   - Chạy query `SELECT * FROM swap_transactions WHERE Driver_ID = 1;` → Có row không?
   - Giá trị chính xác của cột Status là gì? (copy paste y nguyên)

2. **Kết quả NetBeans Console**:
   - Có thấy dòng "DEBUG checkUserSwapsController" không?
   - Có thấy dòng "DEBUG userHasSwapTransactions" không?
   - Result là true hay false?

3. **Kết quả Browser**:
   - F12 Console tab có error gì không?
   - Network tab → request /api/checkUserSwaps → Status code là bao nhiêu?
   - Response body là gì?

4. **User Role**:
   - Chạy `SELECT ID, Email, Role FROM Users WHERE ID = 1;` → Role là gì?

Cho tôi 4 thông tin này, tôi sẽ fix chính xác ngay!
