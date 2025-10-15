# ========================================
# PowerShell Test Script: Test Comment Feature with Completed Status
# Purpose: Test the comment API with different user scenarios
# ========================================

$baseUrl = "http://localhost:8080/webAPI"  # Adjust port/context as needed

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Comment Feature - Status='Completed' Required" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test 1: Login as Driver with completed swaps (e.g., User ID 1 - Nguyen Van A)
Write-Host "[Test 1] Login as Driver with Completed swaps..." -ForegroundColor Yellow
$loginBody = @{
    email = "nguyenvana@email.com"  # Replace with actual email from your DB
    password = "pass123"              # Replace with actual password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/api/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -SessionVariable session `
        -UseBasicParsing
    
    Write-Host "✓ Login successful" -ForegroundColor Green
    Write-Host $loginResponse.Content
    
    # Test 2: Check user's completed swaps
    Write-Host "`n[Test 2] Check user's completed swap transactions..." -ForegroundColor Yellow
    $swapsResponse = Invoke-WebRequest -Uri "$baseUrl/api/checkUserSwaps" `
        -Method GET `
        -WebSession $session `
        -UseBasicParsing
    
    if ($swapsResponse.StatusCode -eq 200) {
        Write-Host "✓ User has completed swaps - can comment" -ForegroundColor Green
        Write-Host $swapsResponse.Content
        $stations = $swapsResponse.Content | ConvertFrom-Json
        
        if ($stations.Count -gt 0) {
            # Test 3: Submit a comment
            Write-Host "`n[Test 3] Submit comment..." -ForegroundColor Yellow
            $commentBody = @{
                stationId = $stations[0].stationId
                content = "Test comment - Status Completed check working!"
            } | ConvertTo-Json
            
            $commentResponse = Invoke-WebRequest -Uri "$baseUrl/api/comment" `
                -Method POST `
                -ContentType "application/json" `
                -Body $commentBody `
                -WebSession $session `
                -UseBasicParsing
            
            Write-Host "✓ Comment submitted successfully" -ForegroundColor Green
            Write-Host $commentResponse.Content
        }
    }
    elseif ($swapsResponse.StatusCode -eq 204) {
        Write-Host "✗ User has NO completed swaps - cannot comment (Expected for Processing-only users)" -ForegroundColor Yellow
    }
    elseif ($swapsResponse.StatusCode -eq 403) {
        Write-Host "✗ User is not Driver role - cannot comment (Expected for Staff)" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan

# Test 4: Test with Staff user (should be blocked)
Write-Host "[Test 4] Login as Staff (should be blocked from commenting)..." -ForegroundColor Yellow
$staffLoginBody = @{
    email = "staff2@email.com"  # Replace with actual staff email
    password = "staff123"        # Replace with actual password
} | ConvertTo-Json

try {
    $staffLoginResponse = Invoke-WebRequest -Uri "$baseUrl/api/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $staffLoginBody `
        -SessionVariable staffSession `
        -UseBasicParsing
    
    Write-Host "✓ Staff login successful" -ForegroundColor Green
    
    $staffSwapsResponse = Invoke-WebRequest -Uri "$baseUrl/api/checkUserSwaps" `
        -Method GET `
        -WebSession $staffSession `
        -UseBasicParsing
    
    if ($staffSwapsResponse.StatusCode -eq 403) {
        Write-Host "✓ Staff correctly blocked from commenting (403)" -ForegroundColor Green
    } else {
        Write-Host "✗ Staff should be blocked but got: $($staffSwapsResponse.StatusCode)" -ForegroundColor Red
    }
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "✓ Staff correctly blocked from commenting (403)" -ForegroundColor Green
    } else {
        Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Summary:" -ForegroundColor Cyan
Write-Host "- Drivers with Status='Completed' swaps: CAN comment" -ForegroundColor Green
Write-Host "- Drivers with Status='Processing' only: CANNOT comment" -ForegroundColor Yellow
Write-Host "- Staff users: CANNOT comment" -ForegroundColor Yellow
Write-Host "- Non-logged users: CANNOT comment" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Note: Update the email/password values in this script to match your database." -ForegroundColor Magenta
