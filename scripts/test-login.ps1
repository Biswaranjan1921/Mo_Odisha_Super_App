$body = @{
    email = 'admin@smartlife.odisha.gov.in'
    password = 'AdminPassword123!'
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri 'http://localhost:8081/api/v1/auth/login' -Method Post -ContentType 'application/json' -Body $body
$response
