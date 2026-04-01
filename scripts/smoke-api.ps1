param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$AdminUser = "admin",
  [string]$AdminPassword = "admin123",
  [string]$ConsumerUser = "consumer",
  [string]$ConsumerPassword = "consumer123",
  [string]$MerchantUser = "merchant",
  [string]$MerchantPassword = "merchant123",
  [string]$Merchant1User = "merchant1",
  [string]$Merchant1Password = "merchant123",
  [string]$Merchant2User = "merchant2",
  [string]$Merchant2Password = "merchant123"
)

$ErrorActionPreference = "Stop"

function Write-Step([string]$text) {
  Write-Host ("`n== " + $text + " ==") -ForegroundColor Cyan
}

function Assert-True([bool]$cond, [string]$msg) {
  if (-not $cond) {
    throw "ASSERT FAILED: $msg"
  }
}

function Invoke-Api([string]$Method, [string]$Path, $Body = $null, [hashtable]$Headers = @{}) {
  $uri = "$BaseUrl$Path"
  $params = @{
    Uri = $uri
    Method = $Method
    Headers = $Headers
    ContentType = "application/json"
  }
  if ($null -ne $Body) {
    $params.Body = ($Body | ConvertTo-Json -Depth 10)
  }
  return Invoke-RestMethod @params
}

function Invoke-ApiExpectStatus([string]$Method, [string]$Path, [int]$ExpectedStatus, $Body = $null, [hashtable]$Headers = @{}) {
  try {
    $null = Invoke-Api -Method $Method -Path $Path -Body $Body -Headers $Headers
    throw "Expected HTTP $ExpectedStatus but request succeeded: $Path"
  } catch {
    $statusCode = 0
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
      try { $statusCode = [int]$_.Exception.Response.StatusCode.value__ } catch { $statusCode = 0 }
    }
    Assert-True ($statusCode -eq $ExpectedStatus) ("Expected HTTP $ExpectedStatus, got $statusCode for $Path")
  }
}

function Login([string]$username, [string]$password) {
  $res = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body @{ username = $username; password = $password }
  Assert-True ($res.code -eq 200) "Login failed: $username"
  return $res.data
}

Write-Step "1. Auth login"
$adminToken = Login $AdminUser $AdminPassword
$consumerToken = Login $ConsumerUser $ConsumerPassword
$merchantToken = $null
$merchant1Token = $null
$merchant2Token = $null
try { $merchantToken = Login $MerchantUser $MerchantPassword } catch {}
try { $merchant1Token = Login $Merchant1User $Merchant1Password } catch {}
try { $merchant2Token = Login $Merchant2User $Merchant2Password } catch {}
Assert-True ([string]::IsNullOrWhiteSpace($adminToken) -eq $false) "Admin token empty"
Assert-True ([string]::IsNullOrWhiteSpace($consumerToken) -eq $false) "Consumer token empty"
Assert-True (
  [string]::IsNullOrWhiteSpace($merchantToken) -eq $false -or
  [string]::IsNullOrWhiteSpace($merchant1Token) -eq $false -or
  [string]::IsNullOrWhiteSpace($merchant2Token) -eq $false
) "No merchant account can login (tried merchant/merchant1/merchant2)"
Write-Host "Login success for admin/consumer and at least one merchant account."

Write-Step "2. Notification APIs"
$notifyRes = Invoke-Api -Method "GET" -Path "/api/notify/list" -Headers @{ Authorization = "Bearer $adminToken" }
Assert-True ($notifyRes.code -eq 200) "Notify list API failed"
$countRes = Invoke-Api -Method "GET" -Path "/api/notify/unread-count" -Headers @{ Authorization = "Bearer $adminToken" }
Assert-True ($countRes.code -eq 200) "Notify count API failed"
Write-Host ("Admin unread notifications: " + $countRes.data.count)

Write-Step "3. Order + verify flow (with idempotency)"
$communities = Invoke-Api -Method "GET" -Path "/api/consumer/communities" -Headers @{ Authorization = "Bearer $consumerToken" }
Assert-True (($communities.code -eq 200) -and ($communities.data.Count -gt 0)) "No communities found"
$target = $null
foreach ($c in $communities.data) {
  $communityId = $c.communityId
  $bindRes = Invoke-Api -Method "POST" -Path ("/api/consumer/community/bind?communityId=" + $communityId) -Headers @{ Authorization = "Bearer $consumerToken" }
  if ($bindRes.code -ne 200) { continue }
  $products = Invoke-Api -Method "GET" -Path ("/api/consumer/products?communityId=" + $communityId) -Headers @{ Authorization = "Bearer $consumerToken" }
  if ($products.code -ne 200 -or $products.data.Count -eq 0) { continue }
  $target = $products.data | Where-Object { $_.status -eq 1 -and $_.stock -gt 1 } | Select-Object -First 1
  if ($null -ne $target) { break }
}
if ($null -eq $target) {
  throw "No product available for order smoke test (need status=1 and stock>1)"
}

$orderBody = @{
  productId = $target.productId
  quantity = 1
}
$idem = "smoke-" + [guid]::NewGuid().ToString("N")
$orderHeaders = @{ Authorization = "Bearer $consumerToken"; "X-Idempotency-Key" = $idem }
$order1 = Invoke-Api -Method "POST" -Path "/api/consumer/order/create" -Body $orderBody -Headers $orderHeaders
Assert-True ($order1.code -eq 200) "First create order failed"
$order2 = Invoke-Api -Method "POST" -Path "/api/consumer/order/create" -Body $orderBody -Headers $orderHeaders
if ($order2.code -ne 200) {
  Start-Sleep -Seconds 1
  $order2 = Invoke-Api -Method "POST" -Path "/api/consumer/order/create" -Body $orderBody -Headers $orderHeaders
}
Assert-True ($order2.code -eq 200) ("Second create order failed: " + $order2.msg)
Assert-True ($order1.data.orderId -eq $order2.data.orderId) ("Idempotency failed: " + $order1.data.orderId + " != " + $order2.data.orderId)
Write-Host ("Idempotency passed, orderId: " + $order1.data.orderId)

$expectedMerchantId = $order1.data.merchantId
$candidateTokens = @()
if (-not [string]::IsNullOrWhiteSpace($merchantToken)) { $candidateTokens += $merchantToken }
if (-not [string]::IsNullOrWhiteSpace($merchant1Token)) { $candidateTokens += $merchant1Token }
if (-not [string]::IsNullOrWhiteSpace($merchant2Token)) { $candidateTokens += $merchant2Token }

$selectedMerchantToken = $null
$selectedMerchantOrders = $null
foreach ($tk in $candidateTokens) {
  $ordersRes = Invoke-Api -Method "GET" -Path "/api/merchant/orders" -Headers @{ Authorization = "Bearer $tk" }
  if ($ordersRes.code -ne 200) { continue }
  $hasTarget = $ordersRes.data | Where-Object { $_.merchantId -eq $expectedMerchantId } | Select-Object -First 1
  if ($null -ne $hasTarget) {
    $selectedMerchantToken = $tk
    $selectedMerchantOrders = $ordersRes.data
    break
  }
}
Assert-True ($null -ne $selectedMerchantToken) ("Cannot find merchant token for merchantId=" + $expectedMerchantId)

$pending = $selectedMerchantOrders | Where-Object { $_.orderStatus -eq 0 -and $_.orderId -eq $order1.data.orderId } | Select-Object -First 1
if ($null -eq $pending) {
  $pending = $selectedMerchantOrders | Where-Object { $_.orderStatus -eq 0 } | Select-Object -First 1
}
Assert-True ($null -ne $pending) "No pending order for verify preview"
$preview = Invoke-Api -Method "POST" -Path "/api/merchant/order/preview" -Body @{ verifyCode = $pending.verifyCode } -Headers @{ Authorization = "Bearer $selectedMerchantToken" }
Assert-True ($preview.code -eq 200) "Verify preview failed"
Write-Host ("Verify preview passed, orderNo: " + $preview.data.orderNo)

Write-Step "3.5 Security negative cases"
Invoke-ApiExpectStatus -Method "GET" -Path "/api/admin/orders" -ExpectedStatus 401
Invoke-ApiExpectStatus -Method "GET" -Path "/api/admin/orders" -ExpectedStatus 403 -Headers @{ Authorization = "Bearer $consumerToken" }
Write-Host "Security checks passed (401/403)."

Write-Step "4. Admin key APIs"
$ordersRes = Invoke-Api -Method "GET" -Path "/api/admin/orders" -Headers @{ Authorization = "Bearer $adminToken" }
Assert-True ($ordersRes.code -eq 200) "Admin orders API failed"
$logsRes = Invoke-Api -Method "GET" -Path "/api/admin/operation-logs" -Headers @{ Authorization = "Bearer $adminToken" }
Assert-True ($logsRes.code -eq 200) "Admin operation logs API failed"

Write-Host "`n[PASS] API smoke test completed successfully." -ForegroundColor Green
