# Script test email OTP nhanh
# Chạy script này để test gửi email

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EMAIL OTP TEST SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\AK\HOCKI5\SWP391\Code\webAPI1\Backend\webAPI"
$libPath = "$projectPath\lib\*"
$srcPath = "$projectPath\src\java"
$testClass = "test.QuickEmailTest"

Write-Host "📁 Project Path: $projectPath" -ForegroundColor Yellow
Write-Host "📚 Library Path: $libPath" -ForegroundColor Yellow
Write-Host ""

# Check if lib folder exists
if (-Not (Test-Path "$projectPath\lib")) {
    Write-Host "❌ Error: lib folder not found!" -ForegroundColor Red
    Write-Host "   Path: $projectPath\lib" -ForegroundColor Red
    exit 1
}

# Navigate to src/java
Set-Location $srcPath
Write-Host "📍 Current directory: $srcPath" -ForegroundColor Yellow
Write-Host ""

# Compile the test class
Write-Host "🔨 Compiling test class..." -ForegroundColor Cyan
javac -cp ".;$libPath" test\QuickEmailTest.java mylib\EmailService.java

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Compilation failed!" -ForegroundColor Red
    Write-Host "   Check for syntax errors in the code." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilation successful!" -ForegroundColor Green
Write-Host ""

# Run the test
Write-Host "🚀 Running email test..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Gray
Write-Host ""

java -cp ".;$libPath" $testClass

Write-Host ""
Write-Host "========================================" -ForegroundColor Gray
Write-Host ""

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Test completed!" -ForegroundColor Green
} else {
    Write-Host "❌ Test failed with exit code: $LASTEXITCODE" -ForegroundColor Red
}

Write-Host ""
Write-Host "💡 Tips:" -ForegroundColor Yellow
Write-Host "   - Nếu gặp lỗi Authentication, đọc file HUONG_DAN_SUA_LOI_EMAIL.md" -ForegroundColor Gray
Write-Host "   - Đảm bảo đã tạo App Password từ Google" -ForegroundColor Gray
Write-Host "   - Kiểm tra Firewall không block port 587" -ForegroundColor Gray
Write-Host ""

# Return to original directory
Set-Location $projectPath
