param(
  [int]$BackendPort = 8080,
  [int]$FrontendPort = 5173
)

$ErrorActionPreference = "Stop"

function Stop-ProcessByPort([int]$Port, [string]$Name) {
  Write-Host "Checking $Name port $Port ..."
  $killed = $false

  try {
    $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
      $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
      foreach ($procId in $pids) {
        try {
          Stop-Process -Id $procId -Force -ErrorAction Stop
          Write-Host "Stopped $Name process PID=$procId on port $Port."
          $killed = $true
        } catch {
          Write-Warning "Failed to stop PID=$procId on port ${Port}: $($_.Exception.Message)"
        }
      }
    }
  } catch {
    # Ignore and fallback to netstat below.
  }

  if (-not $killed) {
    $lines = netstat -ano | Select-String -Pattern (":$Port\s+.*LISTENING")
    if ($lines) {
      $pids = @()
      foreach ($line in $lines) {
        $parts = ($line.Line -replace "\s+", " ").Trim().Split(" ")
        if ($parts.Length -ge 5) {
          $pids += $parts[-1]
        }
      }
      $pids = $pids | Select-Object -Unique
      foreach ($pidText in $pids) {
        if ($pidText -match "^\d+$") {
          $procId = [int]$pidText
          try {
            Stop-Process -Id $procId -Force -ErrorAction Stop
            Write-Host "Stopped $Name process PID=$procId on port $Port."
            $killed = $true
          } catch {
            Write-Warning "Failed to stop PID=$procId on port ${Port}: $($_.Exception.Message)"
          }
        }
      }
    }
  }

  if (-not $killed) {
    Write-Host "No listening process found on port $Port."
  }
}

Write-Host "== Stopping project services =="
Stop-ProcessByPort -Port $BackendPort -Name "backend"
Stop-ProcessByPort -Port $FrontendPort -Name "frontend"
Write-Host "Done."
