-- Test swap_transactions với Status = 'Completed'
-- Kiểm tra logic mới: chỉ cho comment khi có swap Completed

PRINT '========================================';
PRINT 'TEST SWAP STATUS - Comment Feature';
PRINT '========================================';
PRINT '';

-- Thay đổi user ID này để test
DECLARE @TestUserId INT = 1;

PRINT '=== Test 1: Kiểm tra user role ===';
SELECT 
    ID,
    FullName,
    Role,
    CASE 
        WHEN Role = 'Driver' THEN 'OK - Là Driver'
        ELSE 'FAIL - Không phải Driver: [' + ISNULL(Role, 'NULL') + ']'
    END AS RoleCheck,
    Status
FROM Users
WHERE ID = @TestUserId;

PRINT '';
PRINT '=== Test 2: Tổng số swap của user (tất cả status) ===';
SELECT 
    Status,
    COUNT(*) AS SoLuong
FROM swap_transactions
WHERE Driver_ID = @TestUserId
GROUP BY Status
ORDER BY Status;

PRINT '';
PRINT '=== Test 3: Kiểm tra swap COMPLETED (điều kiện cho comment) ===';
SELECT 
    COUNT(*) AS TotalCompletedSwaps,
    CASE 
        WHEN COUNT(*) > 0 THEN 'OK - Có swap Completed, được phép comment'
        ELSE 'FAIL - Không có swap Completed, KHÔNG được comment'
    END AS CommentAllowed
FROM swap_transactions
WHERE Driver_ID = @TestUserId 
  AND Status = 'Completed';

PRINT '';
PRINT '=== Test 4: Chi tiết các swap COMPLETED ===';
SELECT 
    ID AS SwapID,
    Driver_ID,
    Station_ID,
    Status,
    Swap_Time,
    CASE 
        WHEN Status = 'Completed' THEN 'OK'
        ELSE 'Skip - Status: ' + Status
    END AS StatusCheck
FROM swap_transactions
WHERE Driver_ID = @TestUserId
ORDER BY Swap_Time DESC;

PRINT '';
PRINT '=== Test 5: Stations từ swap COMPLETED (dùng cho select box) ===';
SELECT DISTINCT 
    s.ID AS SwapID,
    st.Station_ID,
    st.Name AS StationName,
    st.Address,
    s.Status,
    s.Swap_Time
FROM swap_transactions s
JOIN Station st ON s.Station_ID = st.Station_ID
WHERE s.Driver_ID = @TestUserId 
  AND s.Status = 'Completed'
ORDER BY s.Swap_Time DESC;

PRINT '';
PRINT '=== Test 6: Kiểm tra có Station nào bị thiếu không ===';
SELECT 
    s.ID AS SwapID,
    s.Station_ID,
    s.Status,
    CASE 
        WHEN st.Station_ID IS NULL THEN 'THIẾU - Cần INSERT vào bảng Station'
        ELSE 'OK - Station tồn tại: ' + st.Name
    END AS StationCheck
FROM swap_transactions s
LEFT JOIN Station st ON s.Station_ID = st.Station_ID
WHERE s.Driver_ID = @TestUserId 
  AND s.Status = 'Completed';

PRINT '';
PRINT '========================================';
PRINT 'TỔNG KẾT';
PRINT '========================================';

-- Tổng kết cuối cùng
SELECT 
    u.ID AS UserID,
    u.FullName,
    u.Role,
    (SELECT COUNT(*) FROM swap_transactions WHERE Driver_ID = @TestUserId AND Status = 'Completed') AS CompletedSwaps,
    (SELECT COUNT(DISTINCT st.Station_ID) 
     FROM swap_transactions s 
     JOIN Station st ON s.Station_ID = st.Station_ID 
     WHERE s.Driver_ID = @TestUserId AND s.Status = 'Completed') AS AvailableStations,
    CASE 
        WHEN u.Role <> 'Driver' THEN 'FAIL - Role không phải Driver'
        WHEN (SELECT COUNT(*) FROM swap_transactions WHERE Driver_ID = @TestUserId AND Status = 'Completed') = 0 
            THEN 'FAIL - Không có swap Completed'
        WHEN (SELECT COUNT(DISTINCT st.Station_ID) 
              FROM swap_transactions s 
              JOIN Station st ON s.Station_ID = st.Station_ID 
              WHERE s.Driver_ID = @TestUserId AND s.Status = 'Completed') = 0 
            THEN 'FAIL - Không có Station hợp lệ'
        ELSE 'OK - Được phép comment'
    END AS FinalResult
FROM Users u
WHERE u.ID = @TestUserId;

PRINT '';
PRINT 'Để test user khác, sửa @TestUserId ở đầu file';
