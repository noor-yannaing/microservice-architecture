param(
    [string]$Gateway = "http://localhost:9000",
    [string]$Identity = "http://localhost:8181",
    [string]$Mailpit = "http://localhost:8025"
)
$ErrorActionPreference = "Stop"

function Get-Token([string]$Username, [string]$Password) {
    $body = @{ grant_type = "password"; client_id = "commerce-cli"; username = $Username; password = $Password }
    return (Invoke-RestMethod -Method Post -Uri "$Identity/realms/commerce/protocol/openid-connect/token" -Body $body).access_token
}
$adminToken = Get-Token "catalog-admin" "local-catalog-password"
$customerToken = Get-Token "customer" "local-customer-password"
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
$key = [guid]::NewGuid().ToString()
$sku = "keyboard-$key"
$product = @{ skuCode = $sku; name = "Mechanical Keyboard"; description = "Hot swap keyboard"; price = 79.90 } | ConvertTo-Json
$created = Invoke-RestMethod -Method Post -Uri "$Gateway/api/product" -Headers $adminHeaders -ContentType "application/json" -Body $product
if ($created.skuCode -ne $sku) { throw "Product response did not match the request" }
Invoke-RestMethod -Method Put -Uri "$Gateway/api/inventory/$sku" -Headers $adminHeaders -ContentType "application/json" -Body '{"quantity":5}' | Out-Null

$headers = @{ Authorization = "Bearer $customerToken"; "Idempotency-Key" = $key }
$body = @{ skuCode = $sku; quantity = 2; email = "buyer@example.com" } | ConvertTo-Json
$order = Invoke-RestMethod -Method Post -Uri "$Gateway/api/order" -Headers $headers -ContentType "application/json" -Body $body
$replay = Invoke-RestMethod -Method Post -Uri "$Gateway/api/order" -Headers $headers -ContentType "application/json" -Body $body
if ($replay.orderNumber -ne $order.orderNumber) { throw "Duplicate request created another order" }
if ([decimal]$order.total -ne 159.80) { throw "Order total is incorrect" }

$deadline = [DateTime]::UtcNow.AddSeconds(60)
do {
    $status = Invoke-RestMethod -Uri "$Gateway/api/order/$($order.orderNumber)" -Headers $headers
    if ($status.status -eq "CONFIRMED") { break }
    if ($status.status -eq "REJECTED") { throw "Order unexpectedly rejected" }
    Start-Sleep -Milliseconds 500
} while ([DateTime]::UtcNow -lt $deadline)
if ($status.status -ne "CONFIRMED") { throw "Order did not confirm within 60 seconds" }

$available = Invoke-RestMethod -Uri "$Gateway/api/inventory?skuCode=$sku&quantity=4" -Headers $headers
if ($available -ne $false) { throw "Inventory was not deducted correctly" }
$deadline = [DateTime]::UtcNow.AddSeconds(60)
$delivered = $false
do {
    $messages = Invoke-RestMethod -Uri "$Mailpit/api/v1/messages"
    $delivered = @($messages.messages | Where-Object { $_.Subject -like "*$($order.orderNumber)*" }).Count -gt 0
    if ($delivered) { break }
    Start-Sleep -Milliseconds 1000
} while ([DateTime]::UtcNow -lt $deadline)
if (!$delivered) { throw "No confirmation email arrived in Mailpit" }
Write-Host "Passed: product creation, stock update, order confirmation, idempotent replay, stock deduction and email delivery."
Write-Host "Order: $($order.orderNumber)"
