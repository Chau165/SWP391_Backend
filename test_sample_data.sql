-- Test Query cho dữ liệu mẫu hiện tại (28/10/2025)
-- Dựa trên 2 records: ID 16 (09:45) và ID 17 (15:30)

-- 1. Xem tất cả giao dịch hôm nay
SELECT 
    ID,
    Driver_ID,
    Station_ID,
    Swap_Time,
    DATEPART(HOUR, Swap_Time) AS HourOfDay,
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    Fee,
    Status
FROM dbo.SwapTransaction
WHERE CAST(Swap_Time AS DATE) = '2025-10-28'
    AND Status = 'Completed'
ORDER BY Swap_Time;

-- Kết quả mong đợi:
-- ID 16: 09:00-10:00, Fee: 15000
-- ID 17: 15:00-16:00, Fee: 18000


-- 2. Thống kê theo khung giờ hôm nay
SELECT 
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE CAST(Swap_Time AS DATE) = '2025-10-28'
    AND Status = 'Completed'
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY DATEPART(HOUR, Swap_Time);

-- Kết quả mong đợi:
-- 09:00-10:00 | 1 | 15000.0 | 15000.0
-- 15:00-16:00 | 1 | 18000.0 | 18000.0


-- 3. Top khung giờ cao điểm
SELECT TOP 5
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY SwapCount DESC;


-- 4. Thống kê theo Station_ID = 1 (cả 2 records đều thuộc station 1)
SELECT 
    Station_ID,
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
    AND Station_ID = 1
GROUP BY Station_ID, DATEPART(HOUR, Swap_Time)
ORDER BY DATEPART(HOUR, Swap_Time);


-- 5. Kiểm tra tổng quan tất cả dữ liệu
SELECT 
    COUNT(*) AS TotalSwaps,
    COUNT(DISTINCT Station_ID) AS TotalStations,
    COUNT(DISTINCT CAST(Swap_Time AS DATE)) AS TotalDays,
    MIN(Swap_Time) AS FirstSwap,
    MAX(Swap_Time) AS LastSwap,
    SUM(Fee) AS TotalRevenue
FROM dbo.SwapTransaction
WHERE Status = 'Completed';
