$ErrorActionPreference = "Stop"

Write-Host "1) Health checks..."
curl.exe -s http://127.0.0.1:8080/actuator/health | Out-Host

Write-Host "2) Payment ping..."
curl.exe -s http://127.0.0.1:8080/payments/ping | Out-Host

Write-Host "3) Get token..."
$login = @{ username="admin"; password="password" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/auth/login" -ContentType "application/json" -Body $login).token
Write-Host "Token length: $($token.Length)"

Write-Host "4) Customers (auth required)..."
curl.exe -s http://127.0.0.1:8080/customers -H "Authorization: Bearer $token" | Out-Host

Write-Host "5) Accounts..."
curl.exe -s "http://127.0.0.1:8080/accounts?customerId=1" -H "Authorization: Bearer $token" | Out-Host

Write-Host "6) Transfer $1..."
$body = @{ fromAccountId = 2; toAccountId = 1; amount = 1 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/transfers" -ContentType "application/json" -Body $body -Headers @{ Authorization = "Bearer $token" } | Format-List | Out-Host

Write-Host "7) Transactions..."
curl.exe -s "http://127.0.0.1:8080/transactions?accountId=1" -H "Authorization: Bearer $token" | Out-Host

Write-Host "Smoke test complete."