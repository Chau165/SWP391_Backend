# Test Driver Comment Flow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST DRIVER COMMENT FLOW" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$BASE_URL = "http://localhost:8080/webAPI"

# Step 1: Login as Driver (User ID=1, email: nguyenvana@email.com)
Write-Host ""
Write-Host "[STEP 1] Login as Driver..." -ForegroundColor Yellow
$loginBody = @{
    email = "nguyenvana@email.com"
    password = "password123"
} | ConvertTo-Json

try {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $loginResponse = Invoke-WebRequest -Uri "$BASE_URL/api/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json" `
        -WebSession $session `
        -UseBasicParsing
    
    Write-Host "✓ Login Status: $($loginResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($loginResponse.Content)" -ForegroundColor White
    
    $userData = $loginResponse.Content | ConvertFrom-Json
    Write-Host "User ID: $($userData.user.id), Role: $($userData.user.role)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Login FAILED: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Check User Swaps
Write-Host ""
Write-Host "[STEP 2] Check user swaps (GET /api/checkUserSwaps)..." -ForegroundColor Yellow
try {
    $swapsResponse = Invoke-WebRequest -Uri "$BASE_URL/api/checkUserSwaps" `
        -Method GET `
        -WebSession $session `
        -UseBasicParsing
    
    Write-Host "✓ CheckSwaps Status: $($swapsResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($swapsResponse.Content)" -ForegroundColor White
    
    if ($swapsResponse.StatusCode -eq 200) {
        $swaps = $swapsResponse.Content | ConvertFrom-Json
        Write-Host "Found $($swaps.Count) swap(s)" -ForegroundColor Cyan
        foreach ($swap in $swaps) {
            Write-Host "  - Swap ID: $($swap.swapId), Station: $($swap.name) (ID: $($swap.stationId))" -ForegroundColor White
        }
        
        if ($swaps.Count -gt 0) {
            $firstSwapId = $swaps[0].swapId
            
            # Step 3: Submit Comment
            Write-Host ""
            Write-Host "[STEP 3] Submit comment for Swap ID=$firstSwapId..." -ForegroundColor Yellow
            $commentBody = @{
                swapId = $firstSwapId
                content = "Test comment from PowerShell script - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
            } | ConvertTo-Json
            
            try {
                $commentResponse = Invoke-WebRequest -Uri "$BASE_URL/api/comment" `
                    -Method POST `
                    -Body $commentBody `
                    -ContentType "application/json" `
                    -WebSession $session `
                    -UseBasicParsing
                
                Write-Host "✓ Submit Comment Status: $($commentResponse.StatusCode)" -ForegroundColor Green
                Write-Host "Response: $($commentResponse.Content)" -ForegroundColor White
            } catch {
                Write-Host "✗ Submit Comment FAILED: $_" -ForegroundColor Red
                Write-Host "Error Details: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
            }
        } else {
            Write-Host "✗ No swaps found - cannot submit comment" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "✗ CheckSwaps FAILED: $_" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
    Write-Host "Error Details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST COMPLETED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
