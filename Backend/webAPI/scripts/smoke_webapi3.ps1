$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8080/webAPI3'
Write-Output "=== GET $base/api/admin/dashboard ==="
try {
  $dash = Invoke-RestMethod -Uri "$base/api/admin/dashboard" -Method Get -TimeoutSec 10
  Write-Output ($dash | ConvertTo-Json -Depth 4)
} catch {
  Write-Output "DASHBOARD ERROR: $($_.Exception.Message)"
}

# create a web session to preserve cookies for admin actions
$sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Write-Output "=== POST $base/api/login ==="
try {
  $login = Invoke-RestMethod -Uri "$base/api/login" -Method Post -Body (ConvertTo-Json @{email='admin@example.com'; password='admin123'}) -ContentType 'application/json' -WebSession $sess -TimeoutSec 10
  Write-Output ($login | ConvertTo-Json -Depth 4)
} catch {
  Write-Output "LOGIN ERROR: $($_.Exception.Message)"
}

Write-Output "Cookies after login:";
try { $sess.Cookies.GetCookies($base) | ForEach-Object { $_.ToString() } } catch {}

Write-Output "=== POST $base/api/dispatch (create request) ==="
try {
  $create = Invoke-RestMethod -Uri "$base/api/dispatch" -Method Post -Body (ConvertTo-Json @{stationId=1; quantity=1; note='ps test from smoke'} ) -ContentType 'application/json' -WebSession $sess -TimeoutSec 10
  Write-Output ($create | ConvertTo-Json -Depth 4)
} catch {
  Write-Output "CREATE ERROR: $($_.Exception.Message)"
}

Write-Output "=== GET $base/api/dispatch (list requests) ==="
try {
  $list = Invoke-RestMethod -Uri "$base/api/dispatch" -Method Get -WebSession $sess -TimeoutSec 10
  Write-Output ($list | ConvertTo-Json -Depth 6)
} catch {
  Write-Output "LIST ERROR: $($_.Exception.Message)"
}

# attempt approve if we got an id
$id = $null
if ($create -ne $null) {
  if ($create.PSObject.Properties.Name -contains 'id') { $id = $create.id }
  elseif ($create.PSObject.Properties.Name -contains 'Id') { $id = $create.Id }
}
if ($id) {
  Write-Output "=== PUT $base/api/dispatch/$id/approve ==="
  try {
    $ap = Invoke-RestMethod -Uri ("$base/api/dispatch/$id/approve") -Method Put -WebSession $sess -TimeoutSec 10
    Write-Output ($ap | ConvertTo-Json -Depth 4)
  } catch {
    Write-Output "APPROVE ERROR: $($_.Exception.Message)"
  }
} else {
  Write-Output "No create id available, skipping approve"
}
