param([string]$Cluster = "commerce")
$ErrorActionPreference = "Stop"
$workspace = Split-Path -Parent $PSScriptRoot
Push-Location $workspace
try {
    foreach ($service in @("product-service", "inventory-service", "order-service", "notification-service", "api-gateway")) {
        docker build --build-arg "SERVICE=$service" -t "commerce/${service}:local" .
        if ($LASTEXITCODE -ne 0) { throw "Docker build failed for $service" }
        kind load docker-image "commerce/${service}:local" --name $Cluster
        if ($LASTEXITCODE -ne 0) { throw "Kind image load failed for $service" }
    }
} finally { Pop-Location }
