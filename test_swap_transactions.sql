-- Test script to verify swap_transactions data and Driver_ID mapping
-- Run this in SQL Server Management Studio or sqlcmd

-- Test 1: Check if user ID 1 has any swaps as Driver
PRINT '=== Test 1: Check swaps for user ID 1 ===';
SELECT COUNT(*) AS TotalSwaps
FROM swap_transactions
WHERE Driver_ID = 1;

-- Test 2: List all swaps for user ID 1
PRINT '=== Test 2: List all swaps for user ID 1 ===';
SELECT ID AS SwapID, Driver_ID, Staff_ID, Station_ID, Swap_Time, Status
FROM swap_transactions
WHERE Driver_ID = 1
ORDER BY Swap_Time DESC;

-- Test 3: Get stations used by user ID 1 (same query as DAO)
PRINT '=== Test 3: Get stations with names for user ID 1 ===';
SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address, st.Total_Battery
FROM swap_transactions s 
JOIN Station st ON s.Station_ID = st.Station_ID
WHERE s.Driver_ID = 1;

-- Test 4: Check if Station table has data
PRINT '=== Test 4: Check Station table ===';
SELECT TOP 5 Station_ID, Name, Address, Total_Battery
FROM Station;

-- Test 5: Verify user ID 1 exists and check role
PRINT '=== Test 5: Check user ID 1 details ===';
SELECT ID, FullName, Email, Role, Status
FROM Users
WHERE ID = 1;

-- Test 6: Check for any case-sensitivity or data type issues
PRINT '=== Test 6: Raw data check ===';
SELECT TOP 1 
    ID, 
    Driver_ID, 
    CASE WHEN Driver_ID = 1 THEN 'MATCH' ELSE 'NO MATCH' END AS DriverCheck,
    Station_ID
FROM swap_transactions
WHERE Driver_ID = 1;
