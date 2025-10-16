# Test script to check Driver swap transactions
# This tests the API endpoint /api/checkUserSwaps

Write-Host "=== Testing Driver Swap Transactions ===" -ForegroundColor Cyan

# Step 1: Login as Driver (ID=1, Nguyen Van A)
Write-Host "`n[1] Logging in as Driver (nguyenvana@email.com)..." -ForegroundColor Yellow

$loginUrl = "http://localhost:8080/webAPI/api/login"
$loginBody = @{
    email = "nguyenvana@email.com"
    password = "pass123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri $loginUrl -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -SessionVariable session `
        -UseBasicParsing
    
    Write-Host "Login Response:" -ForegroundColor Green
    Write-Host $loginResponse.Content
    
    # Step 2: Check user swaps
    Write-Host "`n[2] Fetching user swaps..." -ForegroundColor Yellow
    $swapsUrl = "http://localhost:8080/webAPI/api/checkUserSwaps"
    
    $swapsResponse = Invoke-WebRequest -Uri $swapsUrl -Method GET `
        -WebSession $session `
        -UseBasicParsing
    
    Write-Host "Status Code: $($swapsResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $swapsResponse.Content
    
    # Parse and display formatted
    $swapsData = $swapsResponse.Content | ConvertFrom-Json
    
    if ($swapsData -and $swapsData.Count -gt 0) {
        Write-Host "`n=== Found $($swapsData.Count) Completed Swap(s) ===" -ForegroundColor Cyan
        foreach ($swap in $swapsData) {
            Write-Host "  Swap ID: $($swap.swapId)" -ForegroundColor White
            Write-Host "  Station: $($swap.name) (ID: $($swap.stationId))" -ForegroundColor White
            Write-Host "  Address: $($swap.address)" -ForegroundColor Gray
            Write-Host "  ---"
        }
    } else {
        Write-Host "`n[WARNING] No completed swaps found!" -ForegroundColor Red
    }
    
} catch {
    Write-Host "`n[ERROR] $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
