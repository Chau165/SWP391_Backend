-- ========================================
-- DEBUG Script: Kiểm tra chính xác giá trị Status trong database
-- Chạy script này TRƯỚC để xem Status thực tế là gì
-- ========================================

-- 1. Kiểm tra User ID 1 có swap không (bất kể status)
SELECT 'Check 1: User ID 1 có swap không?' AS Test;
SELECT ID, Driver_ID, Staff_ID, Station_ID, Status, Swap_Time
FROM swap_transactions
WHERE Driver_ID = 1;

-- 2. Kiểm tra giá trị CHÍNH XÁC của Status (có thể có khoảng trắng, case khác)
SELECT 'Check 2: Giá trị Status thực tế là gì?' AS Test;
SELECT 
    Status,
    LEN(Status) AS Length,
    DATALENGTH(Status) AS DataLength,
    '[' + Status + ']' AS StatusWithBrackets,
    CASE WHEN Status = 'Completed' THEN 'MATCH exact'
         WHEN Status LIKE '%Completed%' THEN 'MATCH with spaces'
         ELSE 'NO MATCH - khác hoàn toàn'
    END AS ComparisonResult
FROM swap_transactions
WHERE Driver_ID = 1;

-- 3. Thử các cách match khác nhau
SELECT 'Check 3: Test các cách so sánh Status' AS Test;
SELECT 
    ID,
    Driver_ID,
    Status,
    CASE WHEN Status = 'Completed' THEN 'Match =' ELSE 'No Match =' END AS Test_Equal,
    CASE WHEN LTRIM(RTRIM(Status)) = 'Completed' THEN 'Match TRIM' ELSE 'No Match TRIM' END AS Test_Trim,
    CASE WHEN UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED' THEN 'Match UPPER' ELSE 'No Match UPPER' END AS Test_Upper,
    CASE WHEN Status LIKE '%Completed%' THEN 'Match LIKE' ELSE 'No Match LIKE' END AS Test_Like
FROM swap_transactions
WHERE Driver_ID = 1;

-- 4. Đếm các status khác nhau trong toàn bộ bảng
SELECT 'Check 4: Tất cả các giá trị Status trong bảng' AS Test;
SELECT 
    Status,
    '[' + Status + ']' AS StatusWithBrackets,
    COUNT(*) AS Count,
    LEN(Status) AS Length
FROM swap_transactions
GROUP BY Status
ORDER BY Count DESC;

-- 5. Test query thực tế mà code đang dùng
SELECT 'Check 5: Test query chính xác như trong code' AS Test;
SELECT TOP 1 ID 
FROM swap_transactions 
WHERE Driver_ID = 1 AND Status = 'Completed';

-- Nếu query trên KHÔNG trả về kết quả, thử với TRIM:
SELECT 'Check 6: Test query với TRIM' AS Test;
SELECT TOP 1 ID 
FROM swap_transactions 
WHERE Driver_ID = 1 AND LTRIM(RTRIM(Status)) = 'Completed';

-- 6. Kiểm tra collation (case sensitivity)
SELECT 'Check 7: Thông tin về cột Status' AS Test;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'swap_transactions' AND COLUMN_NAME = 'Status';

-- ========================================
-- KẾT QUẢ MONG ĐỢI:
-- - Check 1: Phải có ít nhất 1 row với Driver_ID = 1
-- - Check 2: Xem chính xác giá trị Status (có thể "Completed ", "completed", v.v.)
-- - Check 3: Xem cách nào match được
-- - Check 4: Xem tất cả các giá trị Status có trong bảng
-- - Check 5 hoặc 6: Phải trả về ít nhất 1 ID
-- ========================================

-- Sau khi chạy script này, cho tôi biết kết quả của Check 2 và Check 4
-- để tôi biết chính xác cách fix code
