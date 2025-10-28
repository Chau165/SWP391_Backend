-- ========================================
-- SQL Script để Test Thống Kê Giờ Cao Điểm
-- ========================================

-- 1. Thống kê tất cả khung giờ (toàn bộ dữ liệu)
SELECT 
    DATEPART(HOUR, Swap_Time) AS HourOfDay,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY HourOfDay;

-- 2. Thống kê với khung giờ format đẹp
SELECT 
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY DATEPART(HOUR, Swap_Time);

-- 3. Top 5 khung giờ có nhiều giao dịch nhất
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

-- 4. Thống kê theo Station_ID = 1
SELECT 
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE Status = 'Completed' 
    AND Station_ID = 1
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY DATEPART(HOUR, Swap_Time);

-- 5. Thống kê theo khoảng thời gian (ví dụ: tháng 10/2025)
DECLARE @StartDate DATE = '2025-10-01';
DECLARE @EndDate DATE = '2025-10-31';

SELECT 
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
    AND Swap_Time >= @StartDate 
    AND Swap_Time < DATEADD(DAY, 1, @EndDate)
GROUP BY DATEPART(HOUR, Swap_Time)
ORDER BY DATEPART(HOUR, Swap_Time);

-- 6. Thống kê chi tiết theo ngày trong tuần và giờ
SELECT 
    DATENAME(WEEKDAY, Swap_Time) AS DayOfWeek,
    DATEPART(HOUR, Swap_Time) AS HourOfDay,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
GROUP BY DATENAME(WEEKDAY, Swap_Time), DATEPART(HOUR, Swap_Time)
ORDER BY 
    CASE DATENAME(WEEKDAY, Swap_Time)
        WHEN 'Monday' THEN 1
        WHEN 'Tuesday' THEN 2
        WHEN 'Wednesday' THEN 3
        WHEN 'Thursday' THEN 4
        WHEN 'Friday' THEN 5
        WHEN 'Saturday' THEN 6
        WHEN 'Sunday' THEN 7
    END,
    DATEPART(HOUR, Swap_Time);

-- 7. So sánh giờ cao điểm giữa các trạm
SELECT 
    Station_ID,
    FORMAT(DATEPART(HOUR, Swap_Time), '00') + ':00-' + 
    FORMAT((DATEPART(HOUR, Swap_Time) + 1) % 24, '00') + ':00' AS TimeSlot,
    COUNT(*) AS SwapCount
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
GROUP BY Station_ID, DATEPART(HOUR, Swap_Time)
ORDER BY Station_ID, DATEPART(HOUR, Swap_Time);

-- 8. Tìm giờ cao điểm nhất của mỗi trạm
WITH StationPeakHours AS (
    SELECT 
        Station_ID,
        DATEPART(HOUR, Swap_Time) AS HourOfDay,
        COUNT(*) AS SwapCount,
        ROW_NUMBER() OVER (PARTITION BY Station_ID ORDER BY COUNT(*) DESC) AS RankNum
    FROM dbo.SwapTransaction
    WHERE Status = 'Completed'
    GROUP BY Station_ID, DATEPART(HOUR, Swap_Time)
)
SELECT 
    Station_ID,
    FORMAT(HourOfDay, '00') + ':00-' + FORMAT((HourOfDay + 1) % 24, '00') + ':00' AS PeakHourSlot,
    SwapCount
FROM StationPeakHours
WHERE RankNum = 1
ORDER BY Station_ID;

-- 9. Phân tích theo khung thời gian trong ngày
SELECT 
    CASE 
        WHEN DATEPART(HOUR, Swap_Time) BETWEEN 0 AND 5 THEN 'Đêm khuya (00:00-06:00)'
        WHEN DATEPART(HOUR, Swap_Time) BETWEEN 6 AND 11 THEN 'Buổi sáng (06:00-12:00)'
        WHEN DATEPART(HOUR, Swap_Time) BETWEEN 12 AND 17 THEN 'Buổi chiều (12:00-18:00)'
        ELSE 'Buổi tối (18:00-00:00)'
    END AS TimeOfDay,
    COUNT(*) AS SwapCount,
    ISNULL(SUM(Fee), 0) AS TotalRevenue,
    ISNULL(AVG(Fee), 0) AS AverageFee
FROM dbo.SwapTransaction
WHERE Status = 'Completed'
GROUP BY 
    CASE 
        WHEN DATEPART(HOUR, Swap_Time) BETWEEN 0 AND 5 THEN 'Đêm khuya (00:00-06:00)'
        WHEN DATEPART(HOUR, Swap_Time) BETWEEN 6 AND 11 THEN 'Buổi sáng (06:00-12:00)'
        WHEN DATEPART(HOUR, Swap_Time) BETWEEN 12 AND 17 THEN 'Buổi chiều (12:00-18:00)'
        ELSE 'Buổi tối (18:00-00:00)'
    END
ORDER BY SwapCount DESC;

-- 10. Kiểm tra dữ liệu mẫu
SELECT TOP 10
    ID,
    Station_ID,
    Swap_Time,
    DATEPART(HOUR, Swap_Time) AS HourOfDay,
    Fee,
    Status
FROM dbo.SwapTransaction
ORDER BY Swap_Time DESC;
