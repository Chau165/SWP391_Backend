-- ========================================
-- EMERGENCY DEBUG: Kiểm tra chính xác Status của User ID 1
-- ========================================

-- 1. Xem TẤT CẢ swaps của User ID 1 (không filter gì cả)
SELECT 
    ID AS SwapID,
    Driver_ID,
    Staff_ID,
    Station_ID,
    Status,
    '[' + Status + ']' AS StatusWithBrackets,
    LEN(Status) AS StatusLength,
    ASCII(SUBSTRING(Status, 1, 1)) AS FirstCharASCII,
    Swap_Time
FROM swap_transactions
WHERE Driver_ID = 1
ORDER BY ID;

-- 2. So sánh Status với nhiều cách khác nhau
SELECT 
    ID,
    Status,
    CASE WHEN Status = 'Completed' THEN '1-Match =' ELSE '0-No' END AS Test_Equal,
    CASE WHEN Status = N'Completed' THEN '1-Match N' ELSE '0-No' END AS Test_NVarchar,
    CASE WHEN LTRIM(RTRIM(Status)) = 'Completed' THEN '1-Match TRIM' ELSE '0-No' END AS Test_Trim,
    CASE WHEN UPPER(Status) = 'COMPLETED' THEN '1-Match UPPER' ELSE '0-No' END AS Test_Upper,
    CASE WHEN UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED' THEN '1-Match UPPER+TRIM' ELSE '0-No' END AS Test_UpperTrim,
    CASE WHEN Status LIKE '%Completed%' THEN '1-Match LIKE' ELSE '0-No' END AS Test_Like,
    CASE WHEN Status LIKE '%Complete%' THEN '1-Match LIKE Complete' ELSE '0-No' END AS Test_Like2
FROM swap_transactions
WHERE Driver_ID = 1;

-- 3. Test query chính xác như code Java đang dùng
DECLARE @UserID INT = 1;

-- Query hiện tại (với UPPER + TRIM)
SELECT 'Query với UPPER+TRIM:' AS TestType, COUNT(*) AS RowCount
FROM swap_transactions
WHERE Driver_ID = @UserID AND UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED';

-- Query cũ (exact match)
SELECT 'Query với exact match:' AS TestType, COUNT(*) AS RowCount
FROM swap_transactions
WHERE Driver_ID = @UserID AND Status = 'Completed';

-- Query với N prefix (nvarchar)
SELECT 'Query với N prefix:' AS TestType, COUNT(*) AS RowCount
FROM swap_transactions
WHERE Driver_ID = @UserID AND Status = N'Completed';

-- 4. Liệt kê TẤT CẢ giá trị Status khác nhau trong toàn bộ bảng
SELECT DISTINCT
    Status,
    '[' + Status + ']' AS WithBrackets,
    LEN(Status) AS Length,
    DATALENGTH(Status) AS ByteLength,
    COUNT(*) AS Count
FROM swap_transactions
GROUP BY Status
ORDER BY Count DESC;

-- 5. Kiểm tra kiểu dữ liệu của cột Status
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'swap_transactions' 
  AND COLUMN_NAME = 'Status';

-- ========================================
-- QUAN TRỌNG: Chạy script này và gửi cho tôi KẾT QUẢ của:
-- - Query 1: Xem giá trị Status thực tế (trong ngoặc vuông)
-- - Query 2: Xem cách nào match (Test_Equal, Test_Upper, v.v.)
-- - Query 3: Xem query nào return > 0 rows
-- - Query 4: Xem tất cả giá trị Status có trong database
-- ========================================
