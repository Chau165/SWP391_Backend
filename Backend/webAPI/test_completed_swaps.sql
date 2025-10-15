-- ========================================
-- SQL Test Script: Check Users with Completed Swaps
-- Purpose: Verify which users (Driver_ID) have completed swap_transactions
--          and are allowed to comment
-- ========================================

-- 1. Count users with at least one completed swap
SELECT COUNT(DISTINCT Driver_ID) AS UsersWithCompletedSwaps
FROM swap_transactions
WHERE Status = 'Completed';

-- 2. List all users with completed swaps and their details
SELECT DISTINCT 
    u.ID AS UserID,
    u.FullName,
    u.Email,
    u.Role,
    u.Status AS UserStatus
FROM Users u
INNER JOIN swap_transactions st ON u.ID = st.Driver_ID
WHERE st.Status = 'Completed'
ORDER BY u.ID;

-- 3. Check specific user (replace 1 with the user ID you want to test)
DECLARE @TestUserID INT = 1;

SELECT 
    'User ' + CAST(@TestUserID AS VARCHAR) + ' có giao dịch Completed: ' +
    CASE 
        WHEN EXISTS(SELECT 1 FROM swap_transactions WHERE Driver_ID = @TestUserID AND Status = 'Completed')
        THEN 'CÓ - Được phép comment'
        ELSE 'KHÔNG - Không được comment'
    END AS Result;

-- 4. List completed swaps for a specific user (replace 1 with user ID)
SELECT 
    st.ID AS SwapID,
    st.Driver_ID,
    st.Station_ID,
    s.Name AS StationName,
    st.Swap_Time,
    st.Status
FROM swap_transactions st
LEFT JOIN Station s ON st.Station_ID = s.Station_ID
WHERE st.Driver_ID = 1 AND st.Status = 'Completed'
ORDER BY st.Swap_Time DESC;

-- 5. Get stations available for commenting for a specific user (what API returns)
SELECT DISTINCT 
    st.Station_ID,
    s.Name AS StationName,
    s.Address
FROM swap_transactions st
INNER JOIN Station s ON st.Station_ID = s.Station_ID
WHERE st.Driver_ID = 1 AND st.Status = 'Completed'
ORDER BY s.Name;

-- 6. Summary: All users and their completed swap count
SELECT 
    u.ID AS UserID,
    u.FullName,
    u.Email,
    u.Role,
    COUNT(st.ID) AS CompletedSwapsCount,
    CASE 
        WHEN COUNT(st.ID) > 0 THEN 'Được phép comment'
        ELSE 'Không được comment'
    END AS CommentPermission
FROM Users u
LEFT JOIN swap_transactions st ON u.ID = st.Driver_ID AND st.Status = 'Completed'
WHERE u.Role = 'Driver'
GROUP BY u.ID, u.FullName, u.Email, u.Role
ORDER BY CompletedSwapsCount DESC, u.FullName;

-- 7. Check users with swaps but NONE completed (cannot comment)
SELECT 
    u.ID AS UserID,
    u.FullName,
    u.Email,
    COUNT(st.ID) AS TotalSwaps,
    SUM(CASE WHEN st.Status = 'Completed' THEN 1 ELSE 0 END) AS CompletedSwaps,
    'Không được comment vì chưa có swap Completed' AS Note
FROM Users u
INNER JOIN swap_transactions st ON u.ID = st.Driver_ID
WHERE u.Role = 'Driver'
GROUP BY u.ID, u.FullName, u.Email
HAVING SUM(CASE WHEN st.Status = 'Completed' THEN 1 ELSE 0 END) = 0
ORDER BY u.FullName;

-- ========================================
-- Quick test with sample data from screenshots:
-- Based on your screenshots, these users have completed swaps:
-- User IDs with Status='Completed': 1, 4, 6, 8, 11, 12, 13, 15, 16
-- ========================================

-- Verify against screenshot data (rows 1-11 from swap_transactions):
SELECT 
    st.ID AS SwapRowID,
    st.Driver_ID,
    u.FullName,
    st.Station_ID,
    st.Status,
    CASE 
        WHEN st.Status = 'Completed' THEN '✓ Được comment'
        ELSE '✗ Không được comment'
    END AS CommentAllowed
FROM swap_transactions st
LEFT JOIN Users u ON st.Driver_ID = u.ID
WHERE st.ID IN (20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30)
ORDER BY st.ID;
