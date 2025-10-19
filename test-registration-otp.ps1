# 🧪 Test Registration OTP API - PowerShell Script
# Run: .\test-registration-otp.ps1

$baseUrl = "http://localhost:8080/webAPI3"
$testEmail = "ahkhoinguyen169@gmail.com"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🧪 TESTING REGISTRATION OTP API" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test 1: Send OTP
Write-Host "📧 Step 1: Sending OTP to $testEmail..." -ForegroundColor Yellow

try {
    $sendBody = @{
        email = $testEmail
    } | ConvertTo-Json

    $sendResponse = Invoke-RestMethod `
        -Uri "$baseUrl/api/send-registration-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $sendBody

    Write-Host "✅ SUCCESS: $($sendResponse.message)" -ForegroundColor Green
    Write-Host "   Check email: $testEmail`n" -ForegroundColor Gray

} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Response: $($_.ErrorDetails.Message)`n" -ForegroundColor Gray
    exit 1
}

# Wait for user to check email
Write-Host "📬 Please check your email and enter the OTP code:" -ForegroundColor Yellow
$otp = Read-Host "Enter OTP (6 digits)"

if ($otp.Length -ne 6) {
    Write-Host "❌ Invalid OTP format. Must be 6 digits." -ForegroundColor Red
    exit 1
}

# Test 2: Verify OTP
Write-Host "`n🔍 Step 2: Verifying OTP..." -ForegroundColor Yellow

try {
    $verifyBody = @{
        email = $testEmail
        otp = $otp
    } | ConvertTo-Json

    $verifyResponse = Invoke-RestMethod `
        -Uri "$baseUrl/api/verify-registration-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $verifyBody

    Write-Host "✅ SUCCESS: $($verifyResponse.message)" -ForegroundColor Green
    Write-Host "   OTP verified successfully!`n" -ForegroundColor Gray

} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
    Write-Host "   Common issues:" -ForegroundColor Yellow
    Write-Host "   - Wrong OTP code" -ForegroundColor Gray
    Write-Host "   - OTP expired (> 5 minutes)" -ForegroundColor Gray
    Write-Host "   - Session issue`n" -ForegroundColor Gray
    exit 1
}

# Test 3: Register Account (Optional)
Write-Host "👤 Step 3: Creating account..." -ForegroundColor Yellow
Write-Host "   Do you want to create account with this email? (Y/N): " -ForegroundColor Gray -NoNewline
$createAccount = Read-Host

if ($createAccount -eq "Y" -or $createAccount -eq "y") {
    try {
        $registerBody = @{
            fullName = "Test User"
            phone = "0909123456"
            email = $testEmail
            password = "test123456"
        } | ConvertTo-Json

        $registerResponse = Invoke-RestMethod `
            -Uri "$baseUrl/api/register" `
            -Method POST `
            -ContentType "application/json" `
            -Body $registerBody

        Write-Host "✅ SUCCESS: Account created!" -ForegroundColor Green
        Write-Host "   User ID: $($registerResponse.userId)" -ForegroundColor Gray
        Write-Host "   Role: $($registerResponse.role)" -ForegroundColor Gray
        Write-Host "   Email: $testEmail" -ForegroundColor Gray
        Write-Host "   Password: test123456`n" -ForegroundColor Gray

    } catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
        Write-Host "   Note: Email might already exist in database`n" -ForegroundColor Yellow
    }
} else {
    Write-Host "   Skipped account creation`n" -ForegroundColor Gray
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ TEST COMPLETED" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
