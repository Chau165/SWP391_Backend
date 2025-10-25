$ErrorActionPreference = 'Stop'
$dist = 'C:\AK\HOCKI5\SWP391\Code\Forgot-password, Email-otp-registration, User-profile\Backend\webAPI\dist'
if (-not (Test-Path $dist)) {
  Write-Output "dist folder not found: $dist"
  exit 0
}
Write-Output '--- dist content ---'
Get-ChildItem -Path $dist -Force | Select-Object Name, Length, LastWriteTime | Format-Table -AutoSize
$war = Join-Path $dist 'TestWebAPI.war'
if (-not (Test-Path $war)) {
  Write-Output "WAR not found at: $war"
  exit 0
}
Write-Output "WAR found at: $war"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$tmp = Join-Path $env:TEMP ('war_explore_' + [guid]::NewGuid().Guid)
New-Item -ItemType Directory -Path $tmp | Out-Null
try {
  [System.IO.Compression.ZipFile]::ExtractToDirectory($war, $tmp)
  Write-Output '--- extracted files (first 200) ---'
  Get-ChildItem -Path $tmp -Recurse | Select-Object -First 200 FullName | ForEach-Object { $_.FullName }
} catch {
  Write-Output 'Extraction failed: ' + $_.Exception.Message
  Write-Output "Temporary path (may be empty): $tmp"
  exit 0
}
Write-Output '--- searching for DispatchController.class ---'
$found = Get-ChildItem -Path $tmp -Recurse -Filter '*DispatchController*.class' -ErrorAction SilentlyContinue
if ($found -and $found.Count -gt 0) {
  $found | Select-Object FullName
} else {
  Write-Output 'No DispatchController.class found in WAR'
}
Write-Output "Temporary extraction path: $tmp"
