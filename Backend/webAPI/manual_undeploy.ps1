# Script to manually undeploy webAPI3 from Tomcat

Write-Host "=== Manual Undeploy webAPI3 ===" -ForegroundColor Cyan

# Find Tomcat webapps directory
$possiblePaths = @(
    "C:\apache-tomee-plus-8.0.16\webapps",
    "C:\apache-tomee-plus-9.0.0\webapps",
    "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps",
    "C:\Program Files\Apache Software Foundation\Tomcat 10.0\webapps",
    "$env:CATALINA_HOME\webapps"
)

$tomcatWebapps = $null
foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $tomcatWebapps = $path
        Write-Host "Found Tomcat webapps: $path" -ForegroundColor Green
        break
    }
}

if (-not $tomcatWebapps) {
    Write-Host "ERROR: Cannot find Tomcat webapps folder!" -ForegroundColor Red
    Write-Host "Please manually find your Tomcat installation folder." -ForegroundColor Yellow
    exit
}

# Check if webAPI3 is deployed
$webAPI3Path = Join-Path $tomcatWebapps "webAPI3"
$webAPI3War = Join-Path $tomcatWebapps "webAPI3.war"

Write-Host "`nChecking for deployed webAPI3..." -ForegroundColor Yellow

if (Test-Path $webAPI3Path) {
    Write-Host "Found deployed folder: $webAPI3Path" -ForegroundColor Yellow
    $choice = Read-Host "Delete deployed folder? (y/n)"
    if ($choice -eq 'y') {
        Remove-Item -Path $webAPI3Path -Recurse -Force -ErrorAction Stop
        Write-Host "Deleted: $webAPI3Path" -ForegroundColor Green
    }
}

if (Test-Path $webAPI3War) {
    Write-Host "Found WAR file: $webAPI3War" -ForegroundColor Yellow
    $choice = Read-Host "Delete WAR file? (y/n)"
    if ($choice -eq 'y') {
        Remove-Item -Path $webAPI3War -Force -ErrorAction Stop
        Write-Host "Deleted: $webAPI3War" -ForegroundColor Green
    }
}

Write-Host "`n=== Undeploy Complete ===" -ForegroundColor Green
Write-Host "You can now deploy the new version from NetBeans." -ForegroundColor Cyan
