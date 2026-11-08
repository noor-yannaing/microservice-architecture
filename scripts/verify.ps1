param(
    [switch]$Observability
)
$ErrorActionPreference = "Stop"
$workspace = Split-Path -Parent $PSScriptRoot
$reportDirectory = Join-Path $workspace ".tools/verification"
New-Item -ItemType Directory -Path $reportDirectory -Force | Out-Null
$log = Join-Path $reportDirectory ("verify-{0}.log" -f (Get-Date -Format "yyyyMMdd-HHmmss"))

function Assert-NativeSuccess([string]$Step) {
    if ($LASTEXITCODE -ne 0) { throw "$Step failed (exit code $LASTEXITCODE). See $log" }
}

Push-Location $workspace
Start-Transcript -Path $log | Out-Null
try {
    Get-Command java -ErrorAction Stop | Out-Null
    Get-Command docker -ErrorAction Stop | Out-Null
    docker info --format '{{.ServerVersion}}'
    Assert-NativeSuccess "Docker engine check; start Docker Desktop with Linux containers"
    docker compose version
    Assert-NativeSuccess "Docker Compose check"

    & (Join-Path $workspace "mvnw.cmd") -B -Pintegration verify
    Assert-NativeSuccess "Maven unit and integration tests"

    $composeArguments = @("compose")
    if ($Observability) { $composeArguments += @("--profile", "observability") }
    & docker @composeArguments config --quiet
    Assert-NativeSuccess "Compose configuration check"
    & docker @composeArguments up --build --wait --wait-timeout 300
    Assert-NativeSuccess "Application startup"

    $identityReady = $false
    $deadline = [DateTime]::UtcNow.AddSeconds(120)
    do {
        try {
            $discovery = Invoke-RestMethod -Uri "http://localhost:8181/realms/commerce/.well-known/openid-configuration" -TimeoutSec 5
            $identityReady = $discovery.issuer -eq "http://localhost:8181/realms/commerce"
        } catch {
            $identityReady = $false
        }
        if (!$identityReady) { Start-Sleep -Seconds 2 }
    } while (!$identityReady -and [DateTime]::UtcNow -lt $deadline)
    if (!$identityReady) { throw "Keycloak realm was not ready within 120 seconds" }

    & (Join-Path $PSScriptRoot "smoke-test.ps1")
    & docker @composeArguments ps
    Assert-NativeSuccess "Container status check"
    Write-Host "Verification passed. Services are running. Log: $log"
} finally {
    Stop-Transcript | Out-Null
    Pop-Location
}
