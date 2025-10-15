-- Quick diagnostic: Check role values in Users table
-- This will show us the EXACT role strings in the database

PRINT '=== Kiểm tra Role của các user (10 user đầu) ===';
SELECT TOP 10
    ID,
    FullName,
    Email,
    Role,
    CASE 
        WHEN Role = 'Driver' THEN 'OK - Driver (chính xác)'
        WHEN Role = 'driver' THEN 'Lỗi - driver (chữ thường)'
        WHEN Role = 'DRIVER' THEN 'Lỗi - DRIVER (chữ hoa)'
        WHEN Role = 'Staff' THEN 'OK - Staff'
        WHEN Role = 'Admin' THEN 'OK - Admin'
        ELSE 'CẢNH BÁO - Role không đúng định dạng: [' + Role + ']'
    END AS RoleCheck,
    LEN(Role) AS RoleLength
FROM Users
ORDER BY ID;

PRINT '';
PRINT '=== Đếm số lượng user theo từng role ===';
SELECT 
    Role,
    COUNT(*) AS SoLuong,
    CASE 
        WHEN Role = 'Driver' THEN 'Đúng format'
        ELSE 'SAI format hoặc không phải Driver'
    END AS TrangThai
FROM Users
GROUP BY Role;

PRINT '';
PRINT '=== Kiểm tra user ID 1 cụ thể ===';
SELECT 
    ID,
    FullName,
    Role,
    CASE WHEN Role = 'Driver' THEN 'PASS' ELSE 'FAIL: Role = [' + Role + ']' END AS DriverCheck,
    Status
FROM Users
WHERE ID = 1;

PRINT '';
PRINT '=== Kiểm tra swap của user ID 1 ===';
SELECT 
    COUNT(*) AS TotalSwaps,
    CASE WHEN COUNT(*) > 0 THEN 'Có swap' ELSE 'Không có swap' END AS SwapStatus
FROM swap_transactions
WHERE Driver_ID = 1;
