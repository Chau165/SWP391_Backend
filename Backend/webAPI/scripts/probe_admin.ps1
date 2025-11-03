$ErrorActionPreference = 'SilentlyContinue'
$bases = @('http://localhost:8080/webAPI','http://localhost:8080/webAPI3','http://localhost:8080/TestWebAPI','http://localhost:8080/webAPI2','http://localhost:8080')
foreach ($b in $bases) {
    $url = "$b/api/admin/dashboard"
    try {
        $r = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing -TimeoutSec 3
        Write-Output "$url -> $($r.StatusCode)"
    } catch {
        Write-Output "$url -> ERROR: $($_.Exception.Message)"
    }
}
Write-Output 'Probe finished.'
