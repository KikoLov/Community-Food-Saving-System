param(
  [switch]$InitDb = $false,
  [string]$DbHost = "localhost",
  [int]$DbPort = 3306,
  [string]$DbUser = "root",
  [string]$DbPassword = "EA7music666",
  [string]$DbName = "food_saving",
  [string]$JwtSecret = "replace-with-a-long-random-secret",
  [int]$FrontendPort = 5173,
  [int]$BackendPort = 8080
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

function Assert-Cmd([string]$name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    throw "Required command not found: $name. Please install it or add to PATH."
  }
}

function Resolve-MysqlExe {
  $cmd = Get-Command mysql -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  $candidates = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.4\bin\mysql.exe"
  )
  foreach ($path in $candidates) { if (Test-Path $path) { return $path } }
  return $null
}

Write-Host "== Checking prerequisites =="
Assert-Cmd "java"
Assert-Cmd "mvn"
Assert-Cmd "node"
Assert-Cmd "npm"
$mysqlExe = Resolve-MysqlExe
if (-not $mysqlExe) {
  Write-Warning "mysql client not found. Database init will be skipped unless already available."
}

Write-Host ""
Write-Host "== Environment =="
$env:DB_URL = "jdbc:mysql://$($DbHost):$DbPort/$($DbName)?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = $DbUser
$env:DB_PASSWORD = $DbPassword
$env:JWT_SECRET = $JwtSecret
$env:JWT_EXPIRATION = "86400000"
$env:ALLOW_TEST_ENDPOINTS = "true"
Write-Host ("DB_URL=" + $env:DB_URL)
Write-Host ("DB_USERNAME=" + $env:DB_USERNAME)
Write-Host ("Backend port=" + $BackendPort + ", Frontend port=" + $FrontendPort)

if ($InitDb) {
  if (-not $mysqlExe) {
    throw "Cannot init DB because mysql client was not found."
  }
  Write-Host ""
  Write-Host "== Initializing database =="
  powershell -ExecutionPolicy Bypass -File (Join-Path $root "setup-demo.ps1") `
    -DbHost $DbHost -DbPort $DbPort -DbUser $DbUser -DbPassword $DbPassword -DbName $DbName
}

Write-Host ""
Write-Host "== Starting backend (Spring Boot) =="
$backendDir = Join-Path $root "..\backend"
if (-not (Test-Path $backendDir)) { throw "Backend directory not found: $backendDir" }
Start-Process -WindowStyle Minimized -WorkingDirectory $backendDir `
  -FilePath "mvn" `
  -ArgumentList @("spring-boot:run","-Dspring-boot.run.profiles=dev","-Dspring-boot.run.jvmArguments=-Dserver.port=$BackendPort") `
  -PassThru | Out-Null

Write-Host "Waiting for backend to respond on http://localhost:$BackendPort ..."
for ($i=0; $i -lt 40; $i++) {
  try {
    $resp = Invoke-WebRequest -Uri "http://localhost:$BackendPort/actuator/health" -UseBasicParsing -TimeoutSec 2
    if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) { break }
  } catch { Start-Sleep -Milliseconds 500 }
}

Write-Host ""
Write-Host "== Installing frontend deps (first run may take a while) =="
$frontendDir = Join-Path $root "..\frontend"
if (-not (Test-Path $frontendDir)) { throw "Frontend directory not found: $frontendDir" }
Push-Location $frontendDir
if (-not (Test-Path "node_modules")) {
  npm install --silent
}
Pop-Location

Write-Host ""
Write-Host "== Starting frontend (Vite dev server) =="
Start-Process -WindowStyle Minimized -WorkingDirectory $frontendDir `
  -FilePath "npm" -ArgumentList @("run","dev","--","--port",$FrontendPort) `
  -PassThru | Out-Null

Write-Host ""
Write-Host "All set!"
Write-Host ("Frontend: http://localhost:" + $FrontendPort)
Write-Host ("Backend:  http://localhost:" + $BackendPort)
Write-Host "Demo accounts: admin/admin123, consumer/consumer123, merchant/merchant123"
