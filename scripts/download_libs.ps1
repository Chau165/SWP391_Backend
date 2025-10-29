# Creates lib folder and downloads required JARs for the project
# Run in PowerShell from the repo root:
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\download_libs.ps1

$ErrorActionPreference = 'Stop'

# Ensure we are at repo root (has nbproject folder)
if (!(Test-Path -Path './nbproject/project.properties')) {
  Write-Error "Please run this script from the project root (folder that contains nbproject/)"
}

# Create lib directory if missing
$libDir = Join-Path (Get-Location) 'lib'
if (!(Test-Path $libDir)) {
  New-Item -ItemType Directory -Path $libDir | Out-Null
}

# Ensure TLS 1.2 during download
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

function Get-File($Url, $OutFile) {
  if (Test-Path $OutFile) {
    Write-Host "Already exists: $OutFile" -ForegroundColor Yellow
    return
  }
  Write-Host "Downloading: $Url" -ForegroundColor Cyan
  Invoke-WebRequest -Uri $Url -OutFile $OutFile -UseBasicParsing
}

# Gson 2.10.1
Get-File "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" "$libDir/gson-2.10.1.jar"

# JJWT 0.11.5
Get-File "https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt-api/0.11.5/jjwt-api-0.11.5.jar" "$libDir/jjwt-api-0.11.5.jar"
Get-File "https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt-impl/0.11.5/jjwt-impl-0.11.5.jar" "$libDir/jjwt-impl-0.11.5.jar"
Get-File "https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt-jackson/0.11.5/jjwt-jackson-0.11.5.jar" "$libDir/jjwt-jackson-0.11.5.jar"
Get-File "https://repo1.maven.org/maven2/io/jsonwebtoken/jjwt-gson/0.11.5/jjwt-gson-0.11.5.jar" "$libDir/jjwt-gson-0.11.5.jar"

# ZXing 3.5.2
Get-File "https://repo1.maven.org/maven2/com/google/zxing/core/3.5.2/core-3.5.2.jar" "$libDir/core-3.5.2.jar"
Get-File "https://repo1.maven.org/maven2/com/google/zxing/javase/3.5.2/javase-3.5.2.jar" "$libDir/javase-3.5.2.jar"

Write-Host "\nNOTE: sqljdbc4.jar is not downloaded automatically." -ForegroundColor Yellow
Write-Host "If you need Microsoft SQL Server JDBC driver, copy it as lib/sqljdbc4.jar" -ForegroundColor Yellow
Write-Host "You can download a modern one from: https://learn.microsoft.com/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server" -ForegroundColor Yellow
Write-Host "Then rename the JAR to 'sqljdbc4.jar' or update nbproject/project.properties accordingly." -ForegroundColor Yellow

Write-Host "\nAll done!" -ForegroundColor Green
