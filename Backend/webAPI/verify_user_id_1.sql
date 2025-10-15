-- ========================================
-- Kiểm tra cụ thể User ID = 1 (nguyenvana@email.com)
-- ========================================

PRINT '========================================';
PRINT 'KIỂM TRA USER ID = 1 (Nguyen Van A)';
PRINT '========================================';
PRINT '';

-- 1. Thông tin user
PRINT '1. THÔNG TIN USER:';
SELECT 
    ID,
    FullName,
    Email,
    Role,
    Status
FROM Users
WHERE ID = 1;

PRINT '';
PRINT '2. TẤT CẢ GIAO DỊCH CỦA USER ID = 1:';
-- 2. Tất cả swap transactions của user này
SELECT 
    ID AS SwapID,
    Driver_ID,
    Staff_ID,
    Station_ID,
    Status,
    Swap_Time,
    CASE 
        WHEN Status = 'Completed' THEN '✓ Cho phép comment'
        ELSE '✗ Không cho comment'
    END AS CommentPermission
FROM swap_transactions
WHERE Driver_ID = 1
ORDER BY Swap_Time DESC;

PRINT '';
PRINT '3. KIỂM TRA: USER CÓ ÍT NHẤT 1 SWAP COMPLETED?';
-- 3. Check điều kiện: có ít nhất 1 swap Completed?
SELECT 
    CASE 
        WHEN EXISTS(
            SELECT 1 
            FROM swap_transactions 
            WHERE Driver_ID = 1 AND Status = 'Completed'
        )
        THEN '✓✓✓ CÓ - User ID 1 ĐƯỢC PHÉP COMMENT ✓✓✓'
        ELSE '✗✗✗ KHÔNG - User ID 1 KHÔNG ĐƯỢC COMMENT ✗✗✗'
    END AS Result;

PRINT '';
PRINT '4. CÁC TRẠM USER ID = 1 CÓ THỂ COMMENT (từ swap Completed):';
-- 4. Danh sách stations mà user có thể comment (từ swap completed)
SELECT DISTINCT 
    st.Station_ID,
    s.Name AS StationName,
    s.Address,
    COUNT(st.ID) AS NumberOfCompletedSwaps
FROM swap_transactions st
INNER JOIN Station s ON st.Station_ID = s.Station_ID
WHERE st.Driver_ID = 1 
  AND st.Status = 'Completed'
GROUP BY st.Station_ID, s.Name, s.Address
ORDER BY s.Name;

PRINT '';
PRINT '5. THỐNG KÊ CHI TIẾT:';
-- 5. Thống kê chi tiết
SELECT 
    COUNT(*) AS TotalSwaps,
    SUM(CASE WHEN Status = 'Completed' THEN 1 ELSE 0 END) AS CompletedSwaps,
    SUM(CASE WHEN Status = 'Processing' THEN 1 ELSE 0 END) AS ProcessingSwaps,
    SUM(CASE WHEN Status = 'Pending' THEN 1 ELSE 0 END) AS PendingSwaps,
    CASE 
        WHEN SUM(CASE WHEN Status = 'Completed' THEN 1 ELSE 0 END) > 0 
        THEN 'User ĐƯỢC PHÉP comment'
        ELSE 'User KHÔNG ĐƯỢC comment'
    END AS FinalDecision
FROM swap_transactions
WHERE Driver_ID = 1;

PRINT '';
PRINT '========================================';
PRINT 'KẾT LUẬN DỰA TRÊN SCREENSHOT:';
PRINT 'Từ ảnh bạn gửi:';
PRINT '- Swap ID 20: Driver_ID=1, Status=Completed → ✓';
PRINT '- Swap ID 31: Driver_ID=1, Status=Processing';
PRINT '';
PRINT '→ User ID 1 CÓ swap Completed';
PRINT '→ ĐƯỢC PHÉP COMMENT!';
PRINT '========================================';
