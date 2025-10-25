$ErrorActionPreference = 'Continue'
$roots = @('C:\Program Files','C:\Program Files (x86)','C:\Users','C:\')
$distWar = 'C:\AK\HOCKI5\SWP391\Code\Forgot-password, Email-otp-registration, User-profile\Backend\webAPI\dist\TestWebAPI.war'
if (-not (Test-Path $distWar)) { Write-Output "Source WAR not found: $distWar"; exit 0 }
$webapps = @()
foreach ($r in $roots) {
  try {
    Write-Output "Searching for webapps under $r (depth 6) ..."
    $found = Get-ChildItem -Path $r -Directory -Recurse -ErrorAction SilentlyContinue -Depth 6 | Where-Object { $_.Name -ieq 'webapps' }
    foreach ($f in $found) { $webapps += $f.FullName }
  } catch {}
}
$webapps = $webapps | Sort-Object -Unique
if ($webapps.Count -eq 0) { Write-Output 'No webapps directories found in common roots (depth-limited).'; exit 0 }
Write-Output 'Candidate webapps directories:'
$webapps | ForEach-Object { Write-Output " - $_" }

# Filter to directories that contain typical Tomcat subfolders (ROOT, manager, host-manager) or other wars
$real = @()
foreach ($w in $webapps) {
  $contains = Get-ChildItem -Path $w -Force -ErrorAction SilentlyContinue | Select-Object -First 20
  $names = $contains | ForEach-Object { $_.Name }
  if ($names -contains 'ROOT' -or $names -contains 'manager' -or $names -contains 'host-manager' -or ($contains | Where-Object { $_.Name -match '\.war$' })) {
    $real += $w
  }
}
if ($real.Count -eq 0) { Write-Output 'No webapps directories look like Tomcat webapps (no ROOT/manager/host-manager or existing .war). Showing candidates above.'; exit 0 }
Write-Output 'Likely Tomcat webapps directories:'
$real | ForEach-Object { Write-Output " -> $_" }

# Copy WAR as webAPI3.war into each real webapps dir
foreach ($wa in $real) {
  $dst = Join-Path $wa 'webAPI3.war'
  if (Test-Path $dst) { $bak = $dst + '.bak_' + (Get-Date -Format 'yyyyMMddHHmmss'); Copy-Item -Path $dst -Destination $bak -Force; Write-Output "Backed up existing $dst -> $bak" }
  Copy-Item -Path $distWar -Destination $dst -Force
  Write-Output "Copied $distWar -> $dst"
}
Write-Output "Done. If Tomcat is running with auto-deploy, it should deploy webAPI3 context. If not, you'll need to restart Tomcat."
