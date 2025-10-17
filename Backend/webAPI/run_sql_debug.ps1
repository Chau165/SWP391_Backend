# ========================================
# RUN SQL DEBUG - Copy kết quả cho tôi
# ========================================

Write-Host "Đang chạy SQL debug..." -ForegroundColor Yellow
Write-Host "Vui lòng thay YOUR_SERVER, YOUR_USER, YOUR_PASSWORD, YOUR_DB" -ForegroundColor Red
Write-Host ""

# Thay đổi các giá trị này:
$server = "localhost"  # hoặc tên server của bạn
$database = "SWP391_DB"  # tên database của bạn
$username = "sa"  # username SQL Server
$password = "123"  # password SQL Server

Write-Host "Attempting to connect to:" -ForegroundColor Cyan
Write-Host "  Server: $server" -ForegroundColor White
Write-Host "  Database: $database" -ForegroundColor White
Write-Host ""

# Tạo query đơn giản
$simpleQuery = @"
-- Query đơn giản nhất
SELECT 
    ID AS SwapID,
    Driver_ID,
    Status,
    '[' + Status + ']' AS StatusBrackets,
    LEN(Status) AS StatusLength
FROM swap_transactions
WHERE Driver_ID = 1;
"@

try {
    # Thử kết nối và chạy query
    $connectionString = "Server=$server;Database=$database;User Id=$username;Password=$password;TrustServerCertificate=True;"
    
    $connection = New-Object System.Data.SqlClient.SqlConnection
    $connection.ConnectionString = $connectionString
    $connection.Open()
    
    Write-Host "✓ Kết nối database thành công!" -ForegroundColor Green
    Write-Host ""
    
    $command = $connection.CreateCommand()
    $command.CommandText = $simpleQuery
    
    $adapter = New-Object System.Data.SqlClient.SqlDataAdapter $command
    $dataset = New-Object System.Data.DataSet
    $adapter.Fill($dataset) | Out-Null
    
    if ($dataset.Tables[0].Rows.Count -eq 0) {
        Write-Host "✗ KHÔNG TÌM THẤY swap nào cho Driver_ID = 1!" -ForegroundColor Red
        Write-Host "→ Kiểm tra lại: User ID 1 có phải là Driver_ID trong swap_transactions không?" -ForegroundColor Yellow
    } else {
        Write-Host "Kết quả:" -ForegroundColor Cyan
        Write-Host "======================================" -ForegroundColor Cyan
        
        foreach ($row in $dataset.Tables[0].Rows) {
            Write-Host ""
            Write-Host "SwapID: $($row.SwapID)" -ForegroundColor White
            Write-Host "Driver_ID: $($row.Driver_ID)" -ForegroundColor White
            Write-Host "Status: $($row.Status)" -ForegroundColor White
            Write-Host "Status với brackets: $($row.StatusBrackets)" -ForegroundColor Yellow
            Write-Host "Độ dài Status: $($row.StatusLength)" -ForegroundColor White
            Write-Host "---"
        }
        
        Write-Host ""
        Write-Host "======================================" -ForegroundColor Cyan
        Write-Host "QUAN TRỌNG: Copy kết quả ở trên gửi cho tôi!" -ForegroundColor Red
        Write-Host "Đặc biệt chú ý dòng 'Status với brackets'" -ForegroundColor Yellow
        Write-Host "======================================" -ForegroundColor Cyan
    }
    
    $connection.Close()
    
} catch {
    Write-Host "✗ Lỗi kết nối database!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Hãy sửa lại các giá trị:" -ForegroundColor Yellow
    Write-Host "`$server = '$server'" -ForegroundColor White
    Write-Host "`$database = '$database'" -ForegroundColor White
    Write-Host "`$username = '$username'" -ForegroundColor White
    Write-Host "`$password = '***'" -ForegroundColor White
    Write-Host ""
    Write-Host "Hoặc chạy trực tiếp trong SQL Server Management Studio:" -ForegroundColor Yellow
    Write-Host "SELECT ID, Driver_ID, Status, '[' + Status + ']' AS StatusBrackets" -ForegroundColor Gray
    Write-Host "FROM swap_transactions WHERE Driver_ID = 1;" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "NẾU KHÔNG CHẠY ĐƯỢC SCRIPT NÀY:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "1. Mở SQL Server Management Studio" -ForegroundColor White
Write-Host "2. Kết nối vào database của bạn" -ForegroundColor White
Write-Host "3. Chạy query này:" -ForegroundColor White
Write-Host ""
Write-Host "   SELECT ID, Driver_ID, Status, '[' + Status + ']' AS Brackets" -ForegroundColor Gray
Write-Host "   FROM swap_transactions WHERE Driver_ID = 1;" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Copy kết quả gửi cho tôi" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
