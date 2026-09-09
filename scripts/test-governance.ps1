$body = @{
    email = 'admin@smartlife.odisha.gov.in'
    password = 'AdminPassword123!'
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri 'http://localhost:8081/api/v1/auth/login' -Method Post -ContentType 'application/json' -Body $body
$token = $loginRes.accessToken
Write-Host "Token retrieved: $token"

$headers = @{ Authorization = "Bearer $token" }
$overview = Invoke-RestMethod -Uri 'http://localhost:8081/api/v1/governance/analytics/overview' -Method Get -Headers $headers
$overview | Format-List
