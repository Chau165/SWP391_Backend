# ========================================
# Quick Test - User ID 1 Comment Permission
# ========================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "QUICK TEST: User ID 1 Comment Permission" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/webAPI"  # Adjust if needed

# Test 1: Login as User ID 1
Write-Host "[1] Đang login với nguyenvana@email.com..." -ForegroundColor Yellow
$loginBody = @{
    email = "nguyenvana@email.com"
    password = "pass123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest `
        -Uri "$baseUrl/api/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -SessionVariable session `
        -UseBasicParsing
    
    Write-Host "✓ Login thành công!" -ForegroundColor Green
    Write-Host "Response: $($loginResponse.Content)`n" -ForegroundColor Gray
    
    # Parse user info from response
    $loginData = $loginResponse.Content | ConvertFrom-Json
    if ($loginData.user) {
        Write-Host "User Info:" -ForegroundColor Cyan
        Write-Host "  - ID: $($loginData.user.id)" -ForegroundColor White
        Write-Host "  - Email: $($loginData.user.email)" -ForegroundColor White
        Write-Host "  - Role: $($loginData.user.role)" -ForegroundColor White
        Write-Host ""
    }
    
    # Test 2: Check user's swap transactions
    Write-Host "[2] Đang kiểm tra swap transactions (Status='Completed')..." -ForegroundColor Yellow
    
    try {
        $swapsResponse = Invoke-WebRequest `
            -Uri "$baseUrl/api/checkUserSwaps" `
            -Method GET `
            -WebSession $session `
            -UseBasicParsing
        
        if ($swapsResponse.StatusCode -eq 200) {
            Write-Host "✓ User có swap Completed - ĐƯỢC PHÉP COMMENT!" -ForegroundColor Green
            $stations = $swapsResponse.Content | ConvertFrom-Json
            Write-Host "Số trạm có thể comment: $($stations.Count)" -ForegroundColor Cyan
            
            if ($stations.Count -gt 0) {
                Write-Host "`nDanh sách stations:" -ForegroundColor Cyan
                foreach ($st in $stations) {
                    Write-Host "  - $($st.name) (ID: $($st.stationId), Swap: $($st.swapId))" -ForegroundColor White
                }
            }
            
            # Test 3: Try to post a comment
            if ($stations.Count -gt 0) {
                Write-Host "`n[3] Đang thử gửi comment..." -ForegroundColor Yellow
                $commentBody = @{
                    stationId = $stations[0].stationId
                    content = "Test comment từ PowerShell - User ID 1 có swap Completed!"
                } | ConvertTo-Json
                
                try {
                    $commentResponse = Invoke-WebRequest `
                        -Uri "$baseUrl/api/comment" `
                        -Method POST `
                        -ContentType "application/json" `
                        -Body $commentBody `
                        -WebSession $session `
                        -UseBasicParsing
                    
                    Write-Host "✓ Comment đã gửi thành công!" -ForegroundColor Green
                    Write-Host "Response: $($commentResponse.Content)" -ForegroundColor Gray
                    
                } catch {
                    Write-Host "✗ Gửi comment THẤT BẠI!" -ForegroundColor Red
                    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
                    if ($_.Exception.Response) {
                        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                        $responseBody = $reader.ReadToEnd()
                        Write-Host "Server Response: $responseBody" -ForegroundColor Yellow
                    }
                }
            }
            
        } elseif ($swapsResponse.StatusCode -eq 204) {
            Write-Host "✗ User KHÔNG có swap Completed - KHÔNG được comment" -ForegroundColor Red
            Write-Host "→ Kiểm tra database: Status có phải 'Completed' không?" -ForegroundColor Yellow
        }
        
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        
        if ($statusCode -eq 204) {
            Write-Host "✗ HTTP 204 - User KHÔNG có swap Completed" -ForegroundColor Red
            Write-Host "→ Kiểm tra database swap_transactions WHERE Driver_ID=1 AND Status='Completed'" -ForegroundColor Yellow
            
        } elseif ($statusCode -eq 403) {
            Write-Host "✗ HTTP 403 - User không phải Driver role" -ForegroundColor Red
            Write-Host "→ Kiểm tra database Users table, cột Role phải là 'Driver'" -ForegroundColor Yellow
            
        } elseif ($statusCode -eq 401) {
            Write-Host "✗ HTTP 401 - Session không tồn tại" -ForegroundColor Red
            Write-Host "→ Login failed hoặc session expired" -ForegroundColor Yellow
            
        } else {
            Write-Host "✗ HTTP $statusCode - Lỗi không xác định" -ForegroundColor Red
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        }
        
        if ($_.Exception.Response) {
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $responseBody = $reader.ReadToEnd()
                Write-Host "Server Response: $responseBody" -ForegroundColor Yellow
            } catch {}
        }
    }
    
} catch {
    Write-Host "✗ Login THẤT BẠI!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Server Response: $responseBody" -ForegroundColor Yellow
        } catch {}
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "HƯỚNG DẪN DEBUG TIẾP:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "1. Nếu thấy 'HTTP 204' → Chạy SQL:" -ForegroundColor White
Write-Host "   SELECT * FROM swap_transactions WHERE Driver_ID = 1 AND Status = 'Completed';" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Nếu thấy 'HTTP 403' → Chạy SQL:" -ForegroundColor White
Write-Host "   SELECT ID, Email, Role FROM Users WHERE ID = 1;" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Xem NetBeans Output tab, tìm dòng 'DEBUG'" -ForegroundColor White
Write-Host ""
Write-Host "4. Đọc file DEBUG_GUIDE.md để debug chi tiết hơn" -ForegroundColor White
Write-Host "========================================`n" -ForegroundColor Cyan
