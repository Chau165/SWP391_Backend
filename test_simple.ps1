# Test Driver Comment Flow
Write-Host "========================================"
Write-Host "TEST DRIVER COMMENT FLOW"
Write-Host "========================================"

$BASE_URL = "http://localhost:8080/webAPI"

# Step 1: Login as Driver
Write-Host ""
Write-Host "[STEP 1] Login as Driver..."
$loginBody = '{"email":"nguyenvana@email.com","password":"password123"}'

try {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $loginResponse = Invoke-WebRequest -Uri "$BASE_URL/api/login" -Method POST -Body $loginBody -ContentType "application/json" -WebSession $session -UseBasicParsing
    
    Write-Host "Login Status: $($loginResponse.StatusCode)"
    Write-Host "Response: $($loginResponse.Content)"
} catch {
    Write-Host "Login FAILED: $_"
    exit 1
}

# Step 2: Check User Swaps
Write-Host ""
Write-Host "[STEP 2] Check user swaps..."
try {
    $swapsResponse = Invoke-WebRequest -Uri "$BASE_URL/api/checkUserSwaps" -Method GET -WebSession $session -UseBasicParsing
    
    Write-Host "CheckSwaps Status: $($swapsResponse.StatusCode)"
    Write-Host "Response: $($swapsResponse.Content)"
} catch {
    Write-Host "CheckSwaps FAILED"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.Value__)"
    Write-Host "Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "========================================"
Write-Host "TEST COMPLETED"
Write-Host "========================================"
