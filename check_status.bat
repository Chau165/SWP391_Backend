@echo off
echo Testing Driver ID=1 SwapTransaction Status values...
echo.

REM Replace with your actual SQL Server connection details
set SERVER=localhost
set DATABASE=BatterySwapDBVer2
set USERNAME=sa
set PASSWORD=12345

sqlcmd -S %SERVER% -d %DATABASE% -U %USERNAME% -P %PASSWORD% -Q "SELECT TOP 10 ID as SwapID, Driver_ID, Status, LEN(Status) as StatusLength, Station_ID, Swap_Time FROM SwapTransaction WHERE Driver_ID = 1 ORDER BY ID DESC;"

echo.
echo All distinct Status values in database:
sqlcmd -S %SERVER% -d %DATABASE% -U %USERNAME% -P %PASSWORD% -Q "SELECT DISTINCT Status, COUNT(*) as Count FROM SwapTransaction GROUP BY Status;"

pause
