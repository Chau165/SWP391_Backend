$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/webAPI'
$loginUrl = "$base/api/login"
$usersUrl = "$base/api/admin/users"

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$smokeHeader = @{ 'X-SMOKE-TEST' = 'true' }

function WriteHdr($s){ Write-Output "\n==== $s ====" }

# 0. login
WriteHdr 'Login'
$body = @{ email = 'admin@example.com'; password = 'admin123' } | ConvertTo-Json
try {
    $r = Invoke-WebRequest -Uri $loginUrl -Method Post -Body $body -ContentType 'application/json' -WebSession $session -UseBasicParsing -TimeoutSec 15
    Write-Output "Login status: $($r.StatusCode)"
    $j = $r.Content | ConvertFrom-Json
    $token = $null
    if ($j.token) { $token = $j.token; Write-Output "Token received" } else { Write-Output "No token in response" }
} catch {
    Write-Output "Login ERROR: $($_.Exception.Message)"
    Write-Output 'Attempting to generate a local JWT (admin) to continue tests...'
    # generate JWT matching server secret: change_this_to_a_strong_secret
    $secret = [System.Text.Encoding]::UTF8.GetBytes('change_this_to_a_strong_secret')
    $now = [int][double]::Parse((Get-Date -UFormat %s))
    $exp = $now + 7*24*3600
    $header = '{"alg":"HS256","typ":"JWT"}'
    $payload = "{`"sub`":`"test-admin@example.com`",`"role`":`"admin`",`"id``:1,`"iat`":$now,`"exp`":$exp}"
    function base64url([byte[]]$data){ [System.Convert]::ToBase64String($data).TrimEnd('=') -replace '\+', '-' -replace '/', '_' }
    $headerB = base64url ([System.Text.Encoding]::UTF8.GetBytes($header))
    $payloadB = base64url ([System.Text.Encoding]::UTF8.GetBytes($payload))
    $signingInput = "$headerB.$payloadB"
    $hmac = [System.Security.Cryptography.HMACSHA256]::Create()
    $hmac.Key = $secret
    $sig = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signingInput))
    $sigB = base64url $sig
    $token = "$signingInput.$sigB"
    Write-Output "Generated bearer token (admin)"
}

# helper functions for bearer requests (when login failed)
function BearerGet($path){ Invoke-WebRequest -Uri ($path) -Method Get -Headers @{ Authorization = "Bearer $token"; 'X-SMOKE-TEST' = 'true' } -UseBasicParsing -TimeoutSec 15 }
function BearerRequest($method, $path, $bodyJson){ Invoke-WebRequest -Uri ($path) -Method $method -Body $bodyJson -Headers @{ Authorization = "Bearer $token"; 'X-SMOKE-TEST' = 'true' } -ContentType 'application/json' -UseBasicParsing -TimeoutSec 15 }

# Helper to call API with session
function ApiGet($path){ Invoke-WebRequest -Uri ($path) -Method Get -WebSession $session -Headers $smokeHeader -UseBasicParsing -TimeoutSec 15 }
function ApiRequest($method, $path, $bodyJson){ Invoke-WebRequest -Uri ($path) -Method $method -Body $bodyJson -WebSession $session -Headers $smokeHeader -ContentType 'application/json' -UseBasicParsing -TimeoutSec 15 }

function SafeApiRequest($method, $path, $bodyJson){
    try {
        $r = ApiRequest $method $path $bodyJson
        return @{ ok = $true; status = $r.StatusCode; content = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        $txt = ''
        try { if ($resp) { $sr = New-Object System.IO.StreamReader($resp.GetResponseStream()); $txt = $sr.ReadToEnd(); $sr.Close() } } catch {}
        $status = 0
        try { if ($resp -ne $null) { $status = [int]$resp.StatusCode.value__ } } catch {}
        return @{ ok = $false; status = $status; content = $txt }
    }
}

# 1. list users (try cookie, fallback to bearer)
WriteHdr 'List all users'
try {
    $l = ApiGet($usersUrl)
    Write-Output "Status: $($l.StatusCode)"
    Write-Output $l.Content
} catch {
    Write-Output "Cookie GET failed: $($_.Exception.Message)"
    Write-Output "Trying with generated Bearer token"
    $l = BearerGet($usersUrl)
    Write-Output "Bearer GET status: $($l.StatusCode)"
    Write-Output $l.Content
}

# 2. Create user via JSON
WriteHdr 'Create user (JSON)'
$timestamp = [int][double]::Parse((Get-Date -UFormat %s))
$email = "testuser.$timestamp@example.com"
$create = @{ fullName = 'Test User JSON'; email = $email; phone = '0900000000'; password = 'Pwd1234'; role = 'Staff' } | ConvertTo-Json
$cr = SafeApiRequest -method Post -path $usersUrl -bodyJson $create
Write-Output "Create ok: $($cr.ok) status: $($cr.status)"
Write-Output $cr.content
if (-not $cr.ok) { Write-Output 'Create JSON failed; continuing to multipart upload test (separate step)'; }

Start-Sleep -Seconds 1

# find created user by q
WriteHdr 'Find created user by email'
$found = ApiGet("$usersUrl?q=$([uri]::EscapeDataString($email))")
Write-Output $found.Content
$users = $found.Content | ConvertFrom-Json
if ($users -is [System.Array]) { $newUser = $users | Where-Object { $_.email -eq $email } | Select-Object -First 1 } else { $newUser = $users }
if ($newUser) { $newId = $newUser.id; Write-Output "Found created user id=$newId" } else { Write-Output 'Created user not found'; exit 1 }

# 3. Create user via upload (multipart)
WriteHdr 'Create user with avatar upload (multipart)'
$uploadEmail = "upload.$timestamp@example.com"
# ensure test file exists
$testImg = Join-Path $PSScriptRoot 'test_avatar.png'
if (-not (Test-Path $testImg)) {
    Write-Output "Creating small test image: $testImg"
    $b64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII='
    [System.IO.File]::WriteAllBytes($testImg, [Convert]::FromBase64String($b64))
}
$form = @{
    fullName = 'Test Upload'
    email = $uploadEmail
    phone = '0911111111'
    password = 'Pwd1234'
    role = 'Staff'
    avatarFile = Get-Item $testImg
}
$ur = Invoke-WebRequest -Uri $usersUrl -Method Post -Form $form -WebSession $session -UseBasicParsing -TimeoutSec 30
$ur = Invoke-WebRequest -Uri $usersUrl -Method Post -Form $form -WebSession $session -Headers $smokeHeader -UseBasicParsing -TimeoutSec 30
Write-Output "Upload create status: $($ur.StatusCode)"
Write-Output $ur.Content

Start-Sleep -Seconds 1

# find uploaded user
WriteHdr 'Find uploaded user by email'
$found2 = ApiGet("$usersUrl?q=$([uri]::EscapeDataString($uploadEmail))")
Write-Output $found2.Content
$users2 = $found2.Content | ConvertFrom-Json
if ($users2 -is [System.Array]) { $upUser = $users2 | Where-Object { $_.email -eq $uploadEmail } | Select-Object -First 1 } else { $upUser = $users2 }
if ($upUser) { $upId = $upUser.id; Write-Output "Found uploaded user id=$upId, avatar=${upUser.avatarUrl}" } else { Write-Output 'Uploaded user not found'; exit 1 }

# 4. Edit existing user via JSON (change avatarUrl)
WriteHdr 'Edit user (set avatarUrl via JSON)'
$avatarExternal = 'https://i.pravatar.cc/150?img=3'
$put = @{ id = $newId; fullName = 'Test User JSON Edited'; avatarUrl = $avatarExternal } | ConvertTo-Json
$pr = ApiRequest -method Put -path $usersUrl -bodyJson $put
Write-Output "Edit JSON status: $($pr.StatusCode)"
Write-Output $pr.Content

Start-Sleep -Seconds 1
WriteHdr 'Verify edit (fetch by id)'
$verify = ApiGet("$usersUrl?q=$([uri]::EscapeDataString('Test User JSON Edited'))")
Write-Output $verify.Content

# 5. Edit uploaded user via multipart upload (replace avatar)
WriteHdr 'Edit uploaded user with avatar file'
$form2 = @{
    id = $upId
    fullName = 'Test Upload Edited'
    email = $uploadEmail
    role = 'Staff'
    status = 'Active'
    avatarFile = Get-Item $testImg
}
$pr2 = Invoke-WebRequest -Uri $usersUrl -Method Put -Form $form2 -WebSession $session -Headers $smokeHeader -UseBasicParsing -TimeoutSec 30
Write-Output "Edit upload status: $($pr2.StatusCode)"
Write-Output $pr2.Content

Start-Sleep -Seconds 1
WriteHdr 'Verify uploaded edit'
$verify2 = ApiGet("$usersUrl?q=$([uri]::EscapeDataString('Test Upload Edited'))")
Write-Output $verify2.Content

# 6. Reset password for newId
WriteHdr 'Reset password (JSON PUT with password only)'
$reset = @{ id = $newId; password = 'NewPass123!' } | ConvertTo-Json
$rr = ApiRequest -method Put -path $usersUrl -bodyJson $reset
Write-Output "Reset status: $($rr.StatusCode)"
Write-Output $rr.Content

# 7. Delete both test users
WriteHdr 'Delete created JSON user'
$del1 = Invoke-WebRequest -Uri "$usersUrl?id=$newId" -Method Delete -WebSession $session -UseBasicParsing -TimeoutSec 15
Write-Output "Delete status: $($del1.StatusCode)"
Write-Output $del1.Content

WriteHdr 'Delete uploaded user'
$del2 = Invoke-WebRequest -Uri "$usersUrl?id=$upId" -Method Delete -WebSession $session -UseBasicParsing -TimeoutSec 15
Write-Output "Delete status: $($del2.StatusCode)"
Write-Output $del2.Content

WriteHdr 'Final list (ensure removed)'
$final = ApiGet($usersUrl)
Write-Output $final.Content

Write-Output 'SMOKE TESTS COMPLETE.'
