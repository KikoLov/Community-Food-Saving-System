param(
  [string]$DbHost = "localhost",
  [int]$DbPort = 3306,
  [string]$DbUser = "root",
  [string]$DbPassword = "EA7music666",
  [string]$DbName = "food_saving"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

function Resolve-MysqlExe {
  $cmd = Get-Command mysql -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }

  $candidates = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.4\bin\mysql.exe"
  )
  foreach ($path in $candidates) {
    if (Test-Path $path) { return $path }
  }
  throw "mysql executable not found. Please install MySQL client or add mysql to PATH."
}

function Invoke-SqlFile([string]$mysqlExe, [string]$sqlPath, [bool]$AllowSqlErrors = $false) {
  if (-not (Test-Path $sqlPath)) {
    throw "SQL file not found: $sqlPath"
  }
  Write-Host ">> Running $sqlPath"
  $args = @(
    "--host=$DbHost",
    "--port=$DbPort",
    "--user=$DbUser",
    "--password=$DbPassword",
    "--default-character-set=utf8mb4"
  )
  if ($AllowSqlErrors) { $args += "--force" }
  $args += @(
    "--database=$DbName",
    "--execute=source `"$sqlPath`""
  )
  & $mysqlExe @args
  if ($LASTEXITCODE -ne 0 -and -not $AllowSqlErrors) {
    throw "mysql returned non-zero exit code while executing: $sqlPath"
  }
  if ($LASTEXITCODE -ne 0 -and $AllowSqlErrors) {
    Write-Warning "SQL execution reported errors but will continue: $sqlPath"
  }
}

$mysql = Resolve-MysqlExe
Write-Host "Using mysql: $mysql"

# Ensure DB exists
& $mysql "--host=$DbHost" "--port=$DbPort" "--user=$DbUser" "--password=$DbPassword" "--execute=CREATE DATABASE IF NOT EXISTS $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if ($LASTEXITCODE -ne 0) {
  throw "Failed to create/check database: $DbName"
}

# init.sql may contain non-idempotent inserts; run with --force to continue.
Invoke-SqlFile $mysql (Join-Path $root "docs\sql\init.sql") $true

$fixMerchant = Join-Path $root "fix-merchant-table.sql"
if (Test-Path $fixMerchant) {
  # Optional patch script; can fail on older MySQL syntax variants.
  Invoke-SqlFile $mysql $fixMerchant $true
}

Invoke-SqlFile $mysql (Join-Path $root "docs\sql\schema-compat.sql")

Invoke-SqlFile $mysql (Join-Path $root "docs\sql\demo-seed.sql")

Write-Host ""
Write-Host "Demo DB setup completed."
Write-Host "Demo accounts:"
Write-Host "  - admin / admin123"
Write-Host "  - consumer / consumer123"
Write-Host "  - merchant / merchant123"
