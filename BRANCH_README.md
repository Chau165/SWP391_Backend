# Branch: feature/driver-comment-schema-aligned

## 📋 Mô tả Branch

Branch này chứa **Comment Feature** hoàn chỉnh cho hệ thống Battery Swap với schema đã được align đầy đủ với database thực tế.

## ✅ Các tính năng đã implement

### 1. Comment Feature hoàn chỉnh (Driver-only)
- ✅ Chỉ Driver role mới được phép comment
- ✅ Staff và Admin không được comment (403 Forbidden)
- ✅ Comment gắn liền với swap transaction cụ thể (Swap_ID)
- ✅ Hiển thị station name để user chọn nhưng backend lưu Swap_ID

### 2. Schema đã align với database thực tế
- ✅ Đã fix tất cả table names và column names theo BatterySwapDBVer2
- ✅ Không còn mismatch giữa code và database
- ✅ Sử dụng N prefix cho nvarchar comparison (`Status = N'Completed'`)

### 3. SwapTransaction table name fix
- ✅ Đổi từ `swap_transactions` → `SwapTransaction` (không có dấu `_`)
- ✅ Tất cả queries đã update
- ✅ SwapTransactionDAO.java đã được refactor hoàn toàn

### 4. Swap_ID column implementation
- ✅ Comment table sử dụng `Swap_ID` thay vì `Station_ID`
- ✅ Logic: Comment references swap transaction (business event), không phải station (location)
- ✅ Cho phép track được comment thuộc về giao dịch nào
- ✅ Frontend gửi swapId, backend lưu vào Comment.Swap_ID

### 5. Role-based access control
- ✅ Session-based authentication với HttpSession
- ✅ Check role từ session User object
- ✅ commentController: chỉ accept role='Driver'
- ✅ checkUserSwapsController: return 403 nếu không phải Driver
- ✅ Admin có endpoint riêng để xem tất cả comments

### 6. Status='Completed' filtering
- ✅ Chỉ swap transactions với `Status='Completed'` mới cho phép comment
- ✅ Query: `WHERE Driver_ID = ? AND Status = N'Completed'`
- ✅ Frontend hiển thị message rõ ràng nếu user chưa có completed swaps
- ✅ Validation ở cả backend và frontend

### 7. Frontend UI với Vietnamese messages
- ✅ Message: "Bạn có lịch sử giao dịch hoàn thành. Vui lòng chọn trạm để gửi nhận xét."
- ✅ Message: "Bạn chưa có giao dịch hoàn thành (Completed). Không thể gửi nhận xét."
- ✅ Message: "Bạn không có quyền gửi nhận xét (chỉ dành cho Driver)."
- ✅ Message: "Vui lòng đăng nhập để gửi nhận xét."
- ✅ Form validation với error messages rõ ràng
- ✅ Auto-populate station select từ user's completed swaps
- ✅ Preselect station nếu user chỉ có 1 completed swap

### 8. Debug tools và documentation
- ✅ `FINAL_FIX_README.md` - Hướng dẫn test và debug
- ✅ `COMMENT_FEATURE_README.md` - Chi tiết về comment feature
- ✅ `DEBUG_GUIDE.md` - Hướng dẫn debug với log messages
- ✅ `debug_status_check.sql` - SQL script để check Status values
- ✅ `emergency_debug.sql` - SQL script để verify database state
- ✅ `quick_test_user1.ps1` - PowerShell script để test User ID 1
- ✅ `test_completed_swaps.sql` - Verify completed swaps query
- ✅ Debug logging trong tất cả DAO và Controller methods

## 📂 Files đã thêm mới

### Backend - DTO Layer
- `src/java/DTO/Comment.java` - Comment entity với Swap_ID

### Backend - DAO Layer
- `src/java/DAO/CommentDAO.java` - Insert và get all comments
- `src/java/DAO/SwapTransactionDAO.java` - Query swap transactions với schema-aligned

### Backend - Controller Layer
- `src/java/controller/commentController.java` - POST (create) và GET (list) comments
- `src/java/controller/checkUserSwapsController.java` - GET user's completed swaps với station info

### Frontend
- Updated `web/index.html` và `build/web/index.html` - Comment UI section

### Documentation
- `FINAL_FIX_README.md` - Main guide
- `COMMENT_FEATURE_README.md` - Feature documentation
- `DEBUG_GUIDE.md` - Debug instructions

### Debug Scripts
- `debug_status_check.sql`
- `emergency_debug.sql`
- `quick_test_user1.ps1`
- `run_sql_debug.ps1`
- `test_comment_api.ps1`
- `test_completed_swaps.sql`
- `verify_user_id_1.sql`
- `quick_check.ps1`
- `quick_check_en.ps1`

## 📊 Thống kê

- **71 files** đã thay đổi
- **3054 dòng code** mới thêm
- **8 files DTO/DAO/Controller** mới
- **10+ debug scripts** và documentation files

## 🔄 API Endpoints mới

### 1. POST /api/comment
**Purpose:** Driver gửi comment cho completed swap transaction

**Request:**
```json
{
  "swapId": 20,
  "content": "Dịch vụ tốt, nhân viên nhiệt tình!"
}
```

**Response:**
- 201: Comment created successfully
- 401: User not logged in
- 403: User is not Driver role
- 403: User has no completed swaps
- 400: Missing swapId or content

### 2. GET /api/comment
**Purpose:** Admin lấy tất cả comments

**Response:**
```json
[
  {
    "commentId": 1,
    "userId": 1,
    "swapId": 20,
    "content": "Dịch vụ tốt!",
    "timePost": "2025-10-15T10:30:00"
  }
]
```

**Access:** Admin only (403 for other roles)

### 3. GET /api/checkUserSwaps
**Purpose:** Get danh sách stations từ user's completed swaps

**Response:**
```json
[
  {
    "swapId": 20,
    "stationId": 1,
    "name": "Trạm ABC",
    "address": "123 Đường XYZ"
  }
]
```

**Codes:**
- 200: Success với JSON array
- 204: No completed swaps
- 401: Not logged in
- 403: Not Driver role

## 🗄️ Database Schema

### Comment Table
```sql
CREATE TABLE Comment (
    Comment_ID INT IDENTITY(1,1) PRIMARY KEY,
    User_ID INT NOT NULL,
    Swap_ID INT NOT NULL,  -- ← References SwapTransaction.ID
    Content NVARCHAR(500) NOT NULL,
    Time_Post DATETIME NOT NULL,
    FOREIGN KEY (User_ID) REFERENCES Users(ID),
    FOREIGN KEY (Swap_ID) REFERENCES SwapTransaction(ID)
)
```

### SwapTransaction Table (correct name)
```sql
CREATE TABLE SwapTransaction (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Driver_ID INT NOT NULL,
    Station_ID INT NOT NULL,
    Status NVARCHAR(50) NOT NULL,  -- 'Completed', 'Processing', etc.
    Swap_Time DATETIME NOT NULL,
    -- ... other columns
)
```

## 🧪 Testing Instructions

### 1. Build Project
```
Right-click project → Clean and Build
Wait for "BUILD SUCCESSFUL"
```

### 2. Run Project
```
Right-click project → Run
Wait for server startup
```

### 3. Test Login
```
URL: http://localhost:8080/webAPI/index.html
Email: nguyenvana@email.com
Password: pass123
```

### 4. Test Comment UI
```
1. Scroll to "Gửi nhận xét (Chỉ dành cho Driver)" section
2. Should see dropdown with station names
3. Select station, enter comment, click "Gửi nhận xét"
4. Should see success message
```

### 5. Verify in Database
```sql
SELECT * FROM Comment ORDER BY Comment_ID DESC;
-- Should show new comment with Swap_ID populated
```

### 6. Check Debug Logs
```
NetBeans Output tab should show:
DEBUG checkUserSwapsController - User: ID=1, Role=Driver
DEBUG - Found station: [name] (ID: [id], SwapID: [swapId])
DEBUG - Total stations found: [count]
```

## 🐛 Troubleshooting

### If comment form still disabled:

1. **Check NetBeans Output** for DEBUG logs
2. **Run SQL query:**
   ```sql
   SELECT ID, Driver_ID, Status 
   FROM SwapTransaction 
   WHERE Driver_ID = 1;
   ```
3. **Verify Status value** (must be exactly 'Completed')
4. **Check session** - ensure User object has role='Driver'

### Common Issues:

- **"Bạn chưa có giao dịch hoàn thành"** → No rows with Status='Completed' in SwapTransaction
- **"Không có quyền gửi nhận xét"** → Session user is not Driver role
- **"Vui lòng đăng nhập"** → Session expired or no User in session
- **Dropdown empty** → getStationsWithSwapIdsByUser returns empty array

## 📝 Commit Message

```
feat: Add Driver comment feature with schema-aligned implementation

- Implemented comment feature for Drivers to leave reviews after completed swap transactions
- Fixed schema alignment: SwapTransaction table name, Swap_ID column in Comment table
- Added role-based access control: only Driver role can submit comments
- Added status filtering: only completed swaps (Status='Completed') allow comments
- Frontend UI with Vietnamese messages and session integration
- Backend: CommentDAO, SwapTransactionDAO, commentController, checkUserSwapsController
- Debug logging and test scripts included for troubleshooting
```

## 🔗 Related Links

- **Repository:** https://github.com/Chau165/SWP391_Group5
- **Branch:** `feature/driver-comment-schema-aligned`
- **Create PR:** https://github.com/Chau165/SWP391_Group5/pull/new/feature/driver-comment-schema-aligned

## 👥 Author

Branch created by: GitHub Copilot Agent
Date: October 15, 2025
Project: SWP391 Group 5 - Battery Swap System

---

## 📌 Notes for Reviewers

**Critical Changes:**
1. Table name `swap_transactions` → `SwapTransaction` (no underscore)
2. Comment references `Swap_ID` not `Station_ID` (business logic improvement)
3. N prefix added for nvarchar comparison: `Status = N'Completed'`
4. Driver-only policy enforced at controller level with 403 responses
5. Frontend sends swapId but displays station name for UX

**Breaking Changes:**
- None (this is a new feature)

**Dependencies:**
- Existing Users table with Role column
- Existing SwapTransaction table with Status='Completed' rows
- Servlet API (already in project)
- Gson library (already in project)

**Migration Notes:**
- Comment table must be created in database (see schema above)
- No changes to existing tables required
- No data migration needed

---

**Ready for Review and Testing!** ✅
