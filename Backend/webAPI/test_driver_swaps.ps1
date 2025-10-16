# PowerShell script để test API checkUserSwaps cho Driver ID = 1
# Chạy script này để debug vấn đề

Write-Host "=== Testing Driver Swaps API ===" -ForegroundColor Green

# Base URL - thay đổi theo server của bạn
$baseUrl = "http://localhost:8080/webAPI"

# Step 1: Login as Driver
Write-Host "`n1. Logging in as Driver..." -ForegroundColor Yellow
$loginData = @{
    email = "nguyenvana@email.com"
    password = "pass123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/login" -Method Post -Body $loginData -ContentType "application/json" -SessionVariable session
    
    Write-Host "Login Response:" -ForegroundColor Cyan
    $loginResponse | ConvertTo-Json -Depth 3
    
    if ($loginResponse.status -eq "success") {
        Write-Host "✅ Login successful!" -ForegroundColor Green
        Write-Host "User ID: $($loginResponse.user.id)" -ForegroundColor Cyan
        Write-Host "User Role: $($loginResponse.user.role)" -ForegroundColor Cyan
        Write-Host "User Name: $($loginResponse.user.fullName)" -ForegroundColor Cyan
        
        # Step 2: Test checkUserSwaps API
        Write-Host "`n2. Testing checkUserSwaps API..." -ForegroundColor Yellow
        
        try {
            $swapsResponse = Invoke-RestMethod -Uri "$baseUrl/api/checkUserSwaps" -Method Get -WebSession $session
            
            Write-Host "Swaps Response:" -ForegroundColor Cyan
            $swapsResponse | ConvertTo-Json -Depth 3
            
            if ($swapsResponse -is [array]) {
                Write-Host "✅ Found $($swapsResponse.Count) completed swaps!" -ForegroundColor Green
                
                foreach ($swap in $swapsResponse) {
                    Write-Host "  - Swap ID: $($swap.swapId), Station: $($swap.name), Address: $($swap.address)" -ForegroundColor White
                }
            } else {
                Write-Host "⚠️  Response is not an array: $($swapsResponse.GetType())" -ForegroundColor Yellow
            }
            
        } catch {
            Write-Host "❌ Error calling checkUserSwaps API:" -ForegroundColor Red
            Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
            Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
            
            # Try to get response body
            try {
                $errorStream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorStream)
                $errorBody = $reader.ReadToEnd()
                Write-Host "Error Body: $errorBody" -ForegroundColor Red
            } catch {
                Write-Host "Could not read error body" -ForegroundColor Red
            }
        }
        
    } else {
        Write-Host "❌ Login failed!" -ForegroundColor Red
        Write-Host "Response: $loginResponse" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Error during login:" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green

