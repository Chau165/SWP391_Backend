# Quick Check Script - Debug Comment Feature
# Run this script for quick diagnosis

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "QUICK CHECK - Comment Feature Debug" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$UserId = 1

Write-Host "[Test 1] Check User Role" -ForegroundColor Yellow
Write-Host "  SQL: SELECT ID, FullName, Role FROM Users WHERE ID = $UserId" -ForegroundColor Gray
Write-Host ""

Write-Host "[Test 2] Check Swap Transactions" -ForegroundColor Yellow
Write-Host "  SQL: SELECT COUNT(*) FROM swap_transactions WHERE Driver_ID = $UserId" -ForegroundColor Gray
Write-Host ""

Write-Host "[Test 3] Check Station JOIN" -ForegroundColor Yellow
Write-Host "  SQL: SELECT s.ID, st.Name FROM swap_transactions s JOIN Station st ON s.Station_ID = st.Station_ID WHERE s.Driver_ID = $UserId" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "HOW TO RUN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Option 1: SQL Server Management Studio (SSMS)" -ForegroundColor Yellow
Write-Host "  - Open: c:\AK\HOCKI5\SWP391\Code\webAPI1\check_role_issue.sql" -ForegroundColor Gray
Write-Host "  - Press F5 to execute" -ForegroundColor Gray
Write-Host ""

Write-Host "Option 2: NetBeans Console Log" -ForegroundColor Yellow
Write-Host "  - Build & Run (F6)" -ForegroundColor Gray
Write-Host "  - Login with user ID $UserId" -ForegroundColor Gray
Write-Host "  - Check Output tab for [checkUserSwaps] logs" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "COMMON ISSUES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Role is not 'Driver'" -ForegroundColor Red
Write-Host "   Fix: UPDATE Users SET Role = 'Driver' WHERE ID = $UserId" -ForegroundColor Green
Write-Host ""
Write-Host "2. No swaps in swap_transactions" -ForegroundColor Red
Write-Host "   Fix: Verify Driver_ID column has correct user ID" -ForegroundColor Green
Write-Host ""
Write-Host "3. JOIN fails (Station_ID missing)" -ForegroundColor Red
Write-Host "   Fix: INSERT missing stations or fix Station_ID" -ForegroundColor Green
Write-Host ""

Write-Host "Read more: DEBUG_GUIDE.md" -ForegroundColor Cyan
