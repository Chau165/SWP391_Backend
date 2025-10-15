# Quick Check Script - Kiểm tra nhanh vấn đề comment
# Chạy script này để kiểm tra các điều kiện cần thiết

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "QUICK CHECK - Comment Feature Debug" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Thông tin kết nối (THAY ĐỔI CHO PHÙ HỢP)
$ServerName = "localhost"  # hoặc tên server của bạn
$DatabaseName = "YourDatabaseName"  # tên database
$UserId = 1  # User ID cần kiểm tra

Write-Host "Thông tin kiểm tra:" -ForegroundColor Yellow
Write-Host "  - Server: $ServerName" -ForegroundColor Gray
Write-Host "  - Database: $DatabaseName" -ForegroundColor Gray
Write-Host "  - User ID: $UserId" -ForegroundColor Gray
Write-Host ""

# Test 1: Check Role
Write-Host "[Test 1] Kiểm tra Role của user..." -ForegroundColor Yellow
$checkRoleSQL = "SELECT ID, FullName, Role, Status FROM Users WHERE ID = $UserId"
Write-Host "  SQL: $checkRoleSQL" -ForegroundColor Gray
Write-Host "  → Chạy query này trong SSMS hoặc sqlcmd" -ForegroundColor Green
Write-Host ""

# Test 2: Check Swaps
Write-Host "[Test 2] Kiểm tra Swap Transactions..." -ForegroundColor Yellow
$checkSwapSQL = "SELECT COUNT(*) AS Total FROM swap_transactions WHERE Driver_ID = $UserId"
Write-Host "  SQL: $checkSwapSQL" -ForegroundColor Gray
Write-Host "  → Kết quả phải > 0" -ForegroundColor Green
Write-Host ""

# Test 3: Check Stations
Write-Host "[Test 3] Kiểm tra JOIN với Station..." -ForegroundColor Yellow
$checkStationSQL = @"
SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address, st.Total_Battery
FROM swap_transactions s 
JOIN Station st ON s.Station_ID = st.Station_ID
WHERE s.Driver_ID = $UserId
"@
Write-Host "  SQL: $checkStationSQL" -ForegroundColor Gray
Write-Host "  → Phải trả về ít nhất 1 hàng" -ForegroundColor Green
Write-Host ""

# Hướng dẫn chạy
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CÁCH CHẠY KIỂM TRA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Option 1: Sử dụng SQL Server Management Studio (SSMS)" -ForegroundColor Yellow
Write-Host "  1. Mở SSMS và kết nối database" -ForegroundColor Gray
Write-Host "  2. Mở file: c:\AK\HOCKI5\SWP391\Code\webAPI1\check_role_issue.sql" -ForegroundColor Gray
Write-Host "  3. Execute (F5)" -ForegroundColor Gray
Write-Host ""

Write-Host "Option 2: Sử dụng sqlcmd (Command Line)" -ForegroundColor Yellow
Write-Host "  Chạy lệnh sau (thay đổi thông tin kết nối):" -ForegroundColor Gray
Write-Host '  sqlcmd -S "' + $ServerName + '" -d "' + $DatabaseName + '" -i "c:\AK\HOCKI5\SWP391\Code\webAPI1\check_role_issue.sql"' -ForegroundColor Green
Write-Host ""

Write-Host "Option 3: Test trong NetBeans" -ForegroundColor Yellow
Write-Host "  1. Build & Run project (F6)" -ForegroundColor Gray
Write-Host "  2. Login bằng user ID $UserId" -ForegroundColor Gray
Write-Host "  3. Xem tab Output → tìm dòng [checkUserSwaps] và [SwapTransactionDAO]" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CÁC VẤN ĐỀ THƯỜNG GẶP" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Role không đúng (không phải 'Driver')" -ForegroundColor Red
Write-Host "   Fix: UPDATE Users SET Role = 'Driver' WHERE ID = $UserId;" -ForegroundColor Green
Write-Host ""
Write-Host "2. Không có swap trong bảng swap_transactions" -ForegroundColor Red
Write-Host "   Fix: Thêm swap test hoặc kiểm tra Driver_ID" -ForegroundColor Green
Write-Host ""
Write-Host "3. JOIN với Station fail (Station_ID không tồn tại)" -ForegroundColor Red
Write-Host "   Fix: INSERT vào bảng Station hoặc UPDATE Station_ID trong swap_transactions" -ForegroundColor Green
Write-Host ""
Write-Host "4. Session mất sau khi login" -ForegroundColor Red
Write-Host "   Fix: Logout + Login lại + Hard refresh (Ctrl+Shift+R)" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Đọc thêm: c:\AK\HOCKI5\SWP391\Code\webAPI1\DEBUG_GUIDE.md" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
