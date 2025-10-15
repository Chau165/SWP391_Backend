# Comment Feature - Status='Completed' Requirement

## Tóm tắt thay đổi (Summary)

Hệ thống comment hiện chỉ cho phép **Driver** có ít nhất **1 giao dịch hoàn thành** (Status='Completed') mới được gửi nhận xét.

### Quy tắc kiểm tra (Validation Rules)

1. **Vai trò (Role)**: Chỉ `Driver` được comment. `Staff` và `Admin` không được comment.
2. **Trạng thái giao dịch (Status)**: Phải có ít nhất 1 hàng trong `swap_transactions` với:
   - `Driver_ID = Users.ID` (user đăng nhập)
   - `Status = 'Completed'`
3. **Kết nối bảng**: `Users.ID` ↔ `swap_transactions.Driver_ID`

---

## Chi tiết thay đổi mã nguồn (Code Changes)

### 1. Backend - DAO Layer
**File**: `src/java/DAO/SwapTransactionDAO.java`

**Thay đổi**:
- `userHasSwapTransactions()`: Thêm điều kiện `Status = 'Completed'`
  ```sql
  SELECT TOP 1 ID 
  FROM swap_transactions 
  WHERE Driver_ID = ? AND Status = 'Completed'
  ```

- `getStationsWithSwapIdsByUser()`: Thêm điều kiện `Status = 'Completed'`
  ```sql
  SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address, st.Total_Battery
  FROM swap_transactions s 
  JOIN Station st ON s.Station_ID = st.Station_ID
  WHERE s.Driver_ID = ? AND s.Status = 'Completed'
  ```

### 2. Backend - Controller Layer
**File**: `src/java/controller/commentController.java`

**Thay đổi**:
- Kiểm tra role trước: chỉ `Driver` được phép
- Message tiếng Việt: `"Bạn chưa có giao dịch hoàn thành (Status='Completed') nên không thể gửi nhận xét."`

**File**: `src/java/controller/checkUserSwapsController.java`

**Thay đổi**:
- Trả về HTTP 403 nếu role không phải `Driver`
- Trả về HTTP 204 nếu không có swap `Completed`
- Trả về HTTP 200 + danh sách station nếu có swap `Completed`

### 3. Frontend
**File**: `web/index.html`

**Thay đổi**:
- Ẩn form comment nếu user không phải Driver (403)
- Hiển thị message: `"Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét."` nếu không có swap Completed (204)
- Hiển thị message: `"Bạn có lịch sử giao dịch hoàn thành. Vui lòng chọn trạm để gửi nhận xét."` nếu có swap Completed (200)

---

## Cách kiểm tra (Testing)

### A. Kiểm tra bằng SQL
Chạy file `test_completed_swaps.sql` trong SQL Server Management Studio:

```powershell
# Mở SQL Server Management Studio hoặc dùng sqlcmd:
sqlcmd -S "SERVER_NAME" -U "username" -P "password" -d "DB_NAME" -i "Backend\webAPI\test_completed_swaps.sql"
```

**Kết quả mong đợi**:
- Liệt kê users có swap Status='Completed'
- Từ screenshots bạn gửi, users sau **có thể** comment:
  - User ID: 1, 4, 6, 8, 11, 12, 13, 15, 16 (có swap Completed)

### B. Kiểm tra bằng API (PowerShell)
Chạy script `test_comment_api.ps1`:

```powershell
cd Backend\webAPI
.\test_comment_api.ps1
```

**Lưu ý**: Sửa email/password trong script cho khớp với database của bạn.

### C. Kiểm tra trên UI (index.html)

#### Test Case 1: Driver với swap Completed
1. Mở `http://localhost:8080/webAPI/index.html`
2. Đăng nhập bằng user có Role='Driver' và có swap Status='Completed' (ví dụ: `nguyenvana@email.com`)
3. **Kết quả mong đợi**:
   - Form comment hiển thị
   - Dropdown station hiện các trạm user đã dùng
   - Message: `"Bạn có lịch sử giao dịch hoàn thành. Vui lòng chọn trạm để gửi nhận xét."`
   - Gửi comment thành công

#### Test Case 2: Driver KHÔNG có swap Completed
1. Đăng nhập bằng user có Role='Driver' nhưng swap_transactions toàn Status='Processing'
2. **Kết quả mong đợi**:
   - Form comment hiển thị nhưng disabled
   - Message: `"Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét."`

#### Test Case 3: Staff user
1. Đăng nhập bằng user có Role='Staff' (ví dụ: `staff2@email.com`)
2. **Kết quả mong đợi**:
   - Form comment **bị ẩn hoàn toàn** (display: none)
   - Message: `"Chỉ tài xế (Driver) được gửi nhận xét. Bạn không có quyền."`

#### Test Case 4: Admin view comments
1. Đăng nhập bằng user có Role='Admin'
2. Click button "Lấy nhận xét" trong admin section
3. **Kết quả mong đợi**:
   - Hiển thị tất cả comments từ các Driver đã gửi
   - JSON format với các field: Comment_ID, User_ID, Station_ID, Content, Time_Post

---

## Dữ liệu test từ screenshots

Dựa trên screenshots bạn gửi:

### Users có swap Completed (CÓ THỂ comment):
| User ID | Full Name      | Email (ví dụ)           | Swap IDs với Status='Completed' |
|---------|----------------|-------------------------|----------------------------------|
| 1       | Nguyen Van A   | nguyenvana@email.com    | 20                               |
| 4       | (từ data)      | ...                     | 21                               |
| 6       | (từ data)      | ...                     | 22                               |
| 8       | (từ data)      | ...                     | 25, 26                           |
| 11      | (từ data)      | ...                     | 26                               |
| 12      | (từ data)      | ...                     | 27                               |
| 13      | (từ data)      | ...                     | 28                               |
| 15      | (từ data)      | ...                     | 29                               |
| 16      | (từ data)      | ...                     | 30                               |

### Users có swap Processing (KHÔNG THỂ comment):
| User ID | Status      | Note                         |
|---------|-------------|------------------------------|
| 1       | Processing  | Swap ID 31 - status chưa xong|
| 4       | Processing  | Swap ID 32                   |
| 8       | Processing  | Swap ID 33                   |
| 10      | Processing  | Swap ID 34, 38               |
...

---

## SQL Queries hữu ích

### 1. Kiểm tra 1 user cụ thể có được comment không
```sql
-- Thay @UserID bằng ID cần kiểm tra
DECLARE @UserID INT = 1;

SELECT 
    CASE 
        WHEN EXISTS(
            SELECT 1 
            FROM swap_transactions 
            WHERE Driver_ID = @UserID AND Status = 'Completed'
        )
        THEN 'CÓ - Được phép comment'
        ELSE 'KHÔNG - Không được comment'
    END AS Result;
```

### 2. Lấy danh sách stations user có thể comment
```sql
-- Thay @UserID bằng ID cần kiểm tra
DECLARE @UserID INT = 1;

SELECT DISTINCT 
    st.Station_ID,
    s.Name AS StationName,
    s.Address
FROM swap_transactions st
INNER JOIN Station s ON st.Station_ID = s.Station_ID
WHERE st.Driver_ID = @UserID 
  AND st.Status = 'Completed'
ORDER BY s.Name;
```

### 3. Thống kê tất cả Drivers và số swap Completed
```sql
SELECT 
    u.ID,
    u.FullName,
    u.Email,
    COUNT(st.ID) AS CompletedSwapsCount,
    CASE 
        WHEN COUNT(st.ID) > 0 THEN '✓ Được comment'
        ELSE '✗ Không được comment'
    END AS Permission
FROM Users u
LEFT JOIN swap_transactions st 
    ON u.ID = st.Driver_ID 
    AND st.Status = 'Completed'
WHERE u.Role = 'Driver'
GROUP BY u.ID, u.FullName, u.Email
ORDER BY CompletedSwapsCount DESC;
```

---

## Flow diagram

```
User đăng nhập
    ↓
Session lưu Users object (ID, Role, ...)
    ↓
Frontend gọi GET /api/checkUserSwaps (với session cookie)
    ↓
Server check:
    1. Session có User? → Không → HTTP 401
    2. Role = "Driver"? → Không → HTTP 403
    3. Có swap WHERE Driver_ID=User.ID AND Status='Completed'? → Không → HTTP 204
    4. Có → HTTP 200 + JSON array stations
    ↓
Frontend xử lý:
    - 401 → Hiện "Vui lòng đăng nhập", ẩn form
    - 403 → Hiện "Chỉ Driver được comment", ẩn form
    - 204 → Hiện "Chưa có giao dịch Completed", disable form
    - 200 → Populate select với station names, enable form
    ↓
User chọn station, nhập content, click "Gửi nhận xét"
    ↓
Frontend gọi POST /api/comment { stationId, content }
    ↓
Server check lại:
    1. Session có User?
    2. Role = "Driver"?
    3. Có swap Completed?
    4. Nếu pass → Insert vào bảng Comment → HTTP 200
    5. Nếu fail → HTTP 403 với message tiếng Việt
```

---

## Các file đã thay đổi (Changed Files)

1. `src/java/DAO/SwapTransactionDAO.java` - Thêm Status='Completed' filter
2. `src/java/controller/commentController.java` - Check role + completed status, messages tiếng Việt
3. `src/java/controller/checkUserSwapsController.java` - Block Staff (403), check completed
4. `web/index.html` - UI logic: ẩn/hiện form, messages tiếng Việt

## Các file test mới (New Test Files)

1. `test_completed_swaps.sql` - SQL queries để verify logic
2. `test_comment_api.ps1` - PowerShell script test API endpoints
3. `COMMENT_FEATURE_README.md` - File này (documentation)

---

## Troubleshooting

### Lỗi: "Unresolved imports jakarta.servlet / com.google.gson"
**Nguyên nhân**: Classpath thiếu servlet-api.jar và gson.jar

**Giải pháp**:
1. Chạy trong IDE (NetBeans/Eclipse/IntelliJ) - IDE sẽ tự thêm libs
2. Hoặc thêm vào `lib/` folder: `servlet-api.jar`, `gson-x.x.x.jar`
3. Hoặc deploy lên Tomcat/GlassFish - server có sẵn servlet API

### Lỗi: "Cannot connect to database"
**Kiểm tra**: `src/java/mylib/DBUtils.java` - connection string, username, password

### Lỗi: Frontend không hiện stations
**Kiểm tra**:
1. F12 Developer Tools → Network tab → xem response của `/api/checkUserSwaps`
2. Console tab → xem có lỗi JS không
3. Verify session cookie được gửi kèm request (credentials: 'include')

---

## Next Steps (Bước tiếp theo)

1. ✅ Code đã sửa xong (Status='Completed' check implemented)
2. ⏳ Build project (run Ant hoặc IDE)
3. ⏳ Run SQL script `test_completed_swaps.sql` để verify users
4. ⏳ Deploy và test trên browser với các test cases trên
5. ⏳ Optional: thêm unit tests cho `SwapTransactionDAO` và controllers

---

**Ngày cập nhật**: October 15, 2025
**Version**: 2.0 - Added Status='Completed' requirement
