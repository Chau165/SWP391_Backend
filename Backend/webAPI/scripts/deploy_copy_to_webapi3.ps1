$ErrorActionPreference = 'Continue'
$roots = @('C:\Program Files','C:\Program Files (x86)','C:\Users','C:\','C:\Apache Software Foundation')
$found = @()
foreach ($r in $roots) {
  try {
    Write-Output "Searching under $r ..."
    $files = Get-ChildItem -Path $r -Filter 'TestWebAPI.war' -Recurse -ErrorAction SilentlyContinue -Force | ForEach-Object { $_ }
    foreach ($f in $files) { $found += $f.FullName }
  } catch {}
}
if ($found.Count -eq 0) { Write-Output 'No TestWebAPI.war found in common roots.'; exit 0 }
Write-Output 'Found TestWebAPI.war at:'
$found | ForEach-Object { Write-Output $_ }

# Try to detect webapps folder from found paths
$webappsCandidates = @()
foreach ($p in $found) {
  $dir = Split-Path -Parent $p
  # walk up until find 'webapps' in path
  $cur = $dir
  while ($cur -ne [IO.Path]::GetPathRoot($cur)) {
    if ($cur -match '\\webapps$') { $webappsCandidates += $cur; break }
    $cur = Split-Path -Parent $cur
  }
}
$webappsCandidates = $webappsCandidates | Select-Object -Unique
if ($webappsCandidates.Count -eq 0) { Write-Output 'No webapps folder found adjacent to TestWebAPI.war. Showing parent dirs instead:'; $found | ForEach-Object { Split-Path -Parent $_ | Write-Output } ; exit 0 }
Write-Output 'Detected webapps directories:'
$webappsCandidates | ForEach-Object { Write-Output $_ }

# Copy TestWebAPI.war as webAPI3.war to each found webapps candidate (create backup if exists)
foreach ($wa in $webappsCandidates) {
  $src = Join-Path $wa 'TestWebAPI.war'
  $dst = Join-Path $wa 'webAPI3.war'
  if (Test-Path $dst) {
    $bak = $dst + '.bak_' + (Get-Date -Format 'yyyyMMddHHmmss')
    Write-Output "Backing up existing $dst -> $bak"
    Copy-Item -Path $dst -Destination $bak -Force
  }
  Write-Output "Copying $src -> $dst"
  Copy-Item -Path $src -Destination $dst -Force
}
Write-Output 'Copy complete. Tomcat should auto-deploy webAPI3.war (if auto-deploy enabled).'
