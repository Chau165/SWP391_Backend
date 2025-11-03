$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/webAPI'
$loginUrl = "$base/api/login"
$dashboardUrl = "$base/api/admin/dashboard"

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$body = @{ email = 'admin@example.com'; password = 'admin123' } | ConvertTo-Json
try {
    $r = Invoke-WebRequest -Uri $loginUrl -Method Post -Body $body -ContentType 'application/json' -WebSession $session -UseBasicParsing -TimeoutSec 10
    Write-Output "Login response status: $($r.StatusCode)"
    Write-Output "Login response body:"
    Write-Output $r.Content
} catch {
    Write-Output "Login ERROR: $($_.Exception.Message)"
}

# Try dashboard with cookie-based session
try {
    $d = Invoke-WebRequest -Uri $dashboardUrl -Method Get -WebSession $session -UseBasicParsing -TimeoutSec 10
    Write-Output "Dashboard (with cookie) status: $($d.StatusCode)"
    Write-Output "Dashboard (with cookie) body:"
    Write-Output $d.Content
} catch {
    Write-Output "Dashboard (with cookie) ERROR: $($_.Exception.Message)"
}

# If login response contains a token, try with Authorization header
try {
    $json = $r.Content | ConvertFrom-Json
    if ($json.token) {
        $token = $json.token
        Write-Output "Found token in login response. Trying Authorization: Bearer"
        $headers = @{ Authorization = "Bearer $token" }
        try {
            $d2 = Invoke-WebRequest -Uri $dashboardUrl -Method Get -Headers $headers -UseBasicParsing -TimeoutSec 10
            Write-Output "Dashboard (with Bearer) status: $($d2.StatusCode)"
            Write-Output "Dashboard (with Bearer) body:"
            Write-Output $d2.Content
        } catch {
            Write-Output "Dashboard (with Bearer) ERROR: $($_.Exception.Message)"
        }
    } else {
        Write-Output "No token field found in login response JSON."
    }
} catch {
    Write-Output "Could not parse login response JSON: $($_.Exception.Message)"
}

Write-Output 'Script finished.'
