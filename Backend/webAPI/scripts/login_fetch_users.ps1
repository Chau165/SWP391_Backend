$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/webAPI'
$loginUrl = "$base/api/login"
$usersUrl = "$base/api/admin/users?role=Staff"

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$body = @{ email = 'admin@example.com'; password = 'admin123' } | ConvertTo-Json
try {
    $r = Invoke-WebRequest -Uri $loginUrl -Method Post -Body $body -ContentType 'application/json' -WebSession $session -UseBasicParsing -TimeoutSec 10
    Write-Output "Login response status: $($r.StatusCode)"
    Write-Output "Login response body:"
    Write-Output $r.Content
} catch {
    Write-Output "Login ERROR: $($_.Exception.Message)"
    exit 1
}

# Fetch users with cookie session
try {
    $u = Invoke-WebRequest -Uri $usersUrl -Method Get -WebSession $session -UseBasicParsing -TimeoutSec 10
    Write-Output "Users GET (cookie) status: $($u.StatusCode)"
    Write-Output $u.Content
} catch {
    Write-Output "Users GET (cookie) ERROR: $($_.Exception.Message)"
}

# Try with Bearer token
try {
    $json = $r.Content | ConvertFrom-Json
    if ($json.token) {
        $token = $json.token
        Write-Output "Trying with Bearer token..."
        try {
            $ub = Invoke-WebRequest -Uri $usersUrl -Method Get -Headers @{ Authorization = "Bearer $token" } -UseBasicParsing -TimeoutSec 10
            Write-Output "Users GET (bearer) status: $($ub.StatusCode)"
            Write-Output $ub.Content
        } catch {
            Write-Output "Users GET (bearer) ERROR: $($_.Exception.Message)"
        }
    } else { Write-Output 'No token in login response' }
} catch {
    Write-Output "Parse login response failed: $($_.Exception.Message)"
}

Write-Output 'Done.'
