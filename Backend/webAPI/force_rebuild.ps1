# Script to force rebuild and redeploy

Write-Host "=== Force Rebuild and Redeploy ===" -ForegroundColor Cyan

# Step 1: Kill any Java processes that might be locking files
Write-Host "`n[1] Checking for running Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "Found Java processes. Please stop Tomcat from NetBeans first!" -ForegroundColor Red
    Write-Host "Or manually kill process IDs: $($javaProcesses.Id -join ', ')" -ForegroundColor Red
    $choice = Read-Host "Kill Java processes now? (y/n)"
    if ($choice -eq 'y') {
        Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
        Write-Host "Java processes stopped." -ForegroundColor Green
        Start-Sleep -Seconds 2
    } else {
        Write-Host "Please stop Tomcat manually and run this script again." -ForegroundColor Yellow
        exit
    }
} else {
    Write-Host "No Java processes found." -ForegroundColor Green
}

# Step 2: Delete build folder
Write-Host "`n[2] Deleting build folder..." -ForegroundColor Yellow
$buildPath = "C:\AK\HOCKI5\SWP391\Code\webAPI1\Backend\webAPI\build"
if (Test-Path $buildPath) {
    Remove-Item -Path $buildPath -Recurse -Force -ErrorAction Stop
    Write-Host "Build folder deleted." -ForegroundColor Green
} else {
    Write-Host "Build folder already clean." -ForegroundColor Green
}

# Step 3: Delete dist folder
Write-Host "`n[3] Deleting dist folder..." -ForegroundColor Yellow
$distPath = "C:\AK\HOCKI5\SWP391\Code\webAPI1\Backend\webAPI\dist"
if (Test-Path $distPath) {
    Remove-Item -Path $distPath -Recurse -Force -ErrorAction Stop
    Write-Host "Dist folder deleted." -ForegroundColor Green
} else {
    Write-Host "Dist folder already clean." -ForegroundColor Green
}

# Step 4: Compile
Write-Host "`n[4] Compiling project..." -ForegroundColor Yellow
Set-Location "C:\AK\HOCKI5\SWP391\Code\webAPI1\Backend\webAPI"
$compileResult = ant compile 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful." -ForegroundColor Green
} else {
    Write-Host "Compilation FAILED!" -ForegroundColor Red
    Write-Host $compileResult
    exit
}

# Step 5: Build WAR
Write-Host "`n[5] Building WAR file..." -ForegroundColor Yellow
$buildResult = ant dist 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "WAR file created successfully." -ForegroundColor Green
    Write-Host "Location: C:\AK\HOCKI5\SWP391\Code\webAPI1\Backend\webAPI\dist\webAPI3.war" -ForegroundColor Cyan
} else {
    Write-Host "Build FAILED!" -ForegroundColor Red
    Write-Host $buildResult
    exit
}

Write-Host "`n=== BUILD COMPLETE ===" -ForegroundColor Green
Write-Host "Now you can:" -ForegroundColor Cyan
Write-Host "1. Start Tomcat from NetBeans" -ForegroundColor White
Write-Host "2. Deploy the project (Right-click project > Deploy)" -ForegroundColor White
Write-Host "Or manually copy WAR file to Tomcat webapps folder" -ForegroundColor White
