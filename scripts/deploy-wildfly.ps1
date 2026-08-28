<#
.SYNOPSIS
  Deploy a freshly built WAR to a local WildFly instance via the deployment scanner.

.DESCRIPTION
  Copies the WAR into WildFly's standalone/deployments directory using an atomic
  rename, triggers a (re)deploy with a .dodeploy marker, and waits for WildFly to
  write .deployed (success) or .failed (failure). On failure it rolls back to the
  previously deployed WAR. No service restart is needed - WildFly hot-swaps the
  app through the scanner.

  Intended to be run by a self-hosted GitHub Actions runner installed on the same
  machine as WildFly, but it also works when run by hand.

.EXAMPLE
  .\deploy-wildfly.ps1
  .\deploy-wildfly.ps1 -HealthUrl "http://localhost:8080/Jamstudy/stats" -TimeoutSec 300
#>
[CmdletBinding()]
param(
    # Path to the WAR produced by the build.
    [string] $WarSource        = "$PSScriptRoot\..\target\Jamstudy.war",

    # WildFly's deployment-scanner directory.
    [string] $DeployDir        = "C:\Wildfly\wildfly-22.0.0.Final\standalone\deployments",

    # Name the WAR must have inside the deployments dir. Drives the app's URL
    # context path, so keep it identical to whatever is deployed today.
    [string] $DeployName       = "Jamstudy.war",

    # How long to wait for the scanner's .deployed / .failed verdict.
    [int]    $TimeoutSec       = 240,

    # Optional: URL to GET after deploy; non-2xx/3xx (or no response) fails the run.
    [string] $HealthUrl        = "",
    [int]    $HealthTimeoutSec = 90,

    # How many previous WARs to retain under _deploy-backups.
    [int]    $KeepBackups      = 5,

    # Optional nuclear option: bounce the Windows service after deploying.
    [switch] $RestartService,
    [string] $ServiceName      = ""
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Write-Step($msg) { Write-Host "==> $msg" }

# --- resolve paths --------------------------------------------------------------
if (-not (Test-Path -LiteralPath $WarSource)) { throw "Source WAR not found: $WarSource" }
$WarSource = (Resolve-Path -LiteralPath $WarSource).Path
if (-not (Test-Path -LiteralPath $DeployDir)) { throw "Deployments directory not found: $DeployDir" }

$target       = Join-Path $DeployDir $DeployName
$deployedMark = "$target.deployed"
$failedMark   = "$target.failed"
$doDeployMark = "$target.dodeploy"
$isDeployMark = "$target.isdeploying"
$pendingMark  = "$target.pending"
$undeployMark = "$target.undeployed"
$skipMark     = "$target.skipdeploy"
$staging      = Join-Path $DeployDir ".$DeployName.upload"
$backupDir    = Join-Path $DeployDir "_deploy-backups"

$srcInfo = Get-Item -LiteralPath $WarSource
Write-Step ("Source WAR : {0}  ({1} MB, built {2})" -f $WarSource, [math]::Round($srcInfo.Length/1MB,1), $srcInfo.LastWriteTime)
Write-Step "Target     : $target"

# --- back up the currently deployed WAR ---------------------------------------
$backupPath = $null
if (Test-Path -LiteralPath $target) {
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
    $backupPath = Join-Path $backupDir ("{0}.{1}" -f $DeployName, (Get-Date -Format 'yyyyMMdd-HHmmss'))
    Copy-Item -LiteralPath $target -Destination $backupPath -Force
    Write-Step "Backed up current WAR -> $backupPath"

    Get-ChildItem -LiteralPath $backupDir -Filter "$DeployName.*" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -Skip $KeepBackups |
        Remove-Item -Force -ErrorAction SilentlyContinue
}

# --- helpers ------------------------------------------------------------------
function Clear-Markers {
    foreach ($m in @($deployedMark, $failedMark, $doDeployMark, $isDeployMark, $pendingMark, $undeployMark, $skipMark)) {
        Remove-Item -LiteralPath $m -Force -ErrorAction SilentlyContinue
    }
}

function Wait-ForDeploy([string] $context) {
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $failedMark) {
            $reason = Get-Content -LiteralPath $failedMark -Raw -ErrorAction SilentlyContinue
            throw "WildFly reported FAILED for ${context}:`n$reason"
        }
        if (Test-Path -LiteralPath $deployedMark) { return }
        Start-Sleep -Seconds 2
    }
    throw "Timed out after ${TimeoutSec}s waiting for WildFly to deploy $context."
}

# --- deploy -----------------------------------------------------------------
try {
    Write-Step "Staging new WAR"
    Copy-Item -LiteralPath $WarSource -Destination $staging -Force

    Clear-Markers
    Write-Step "Swapping WAR into place"
    Move-Item -LiteralPath $staging -Destination $target -Force

    Write-Step "Writing .dodeploy marker"
    New-Item -ItemType File -Path $doDeployMark -Force | Out-Null

    Write-Step "Waiting for WildFly (timeout ${TimeoutSec}s)..."
    Wait-ForDeploy "new deployment"
    Write-Step "WildFly reports DEPLOYED."
}
catch {
    $err = $_.Exception.Message
    Write-Warning "Deploy failed: $err"
    Remove-Item -LiteralPath $staging -Force -ErrorAction SilentlyContinue

    if ($backupPath -and (Test-Path -LiteralPath $backupPath)) {
        Write-Step "Rolling back to previous WAR"
        try {
            Clear-Markers
            Copy-Item -LiteralPath $backupPath -Destination $target -Force
            New-Item -ItemType File -Path $doDeployMark -Force | Out-Null
            Wait-ForDeploy "rollback"
            Write-Step "Rollback DEPLOYED - server is back on the previous build."
        }
        catch {
            Write-Error "ROLLBACK ALSO FAILED: $($_.Exception.Message)"
        }
    }
    throw "Deployment failed: $err"
}

# --- optional service restart -----------------------------------------------
if ($RestartService) {
    if (-not $ServiceName) { throw "-RestartService requires -ServiceName." }
    Write-Step "Restarting service '$ServiceName'"
    Restart-Service -Name $ServiceName -Force
}

# --- optional health check -------------------------------------------------
if ($HealthUrl) {
    Write-Step "Health check: $HealthUrl"
    $deadline = (Get-Date).AddSeconds($HealthTimeoutSec)
    $ok = $false
    while ((Get-Date) -lt $deadline) {
        try {
            $resp = Invoke-WebRequest -Uri $HealthUrl -UseBasicParsing -TimeoutSec 10
            if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 400) { $ok = $true; break }
        } catch { }
        Start-Sleep -Seconds 3
    }
    if (-not $ok) { throw "Health check failed for $HealthUrl within ${HealthTimeoutSec}s." }
    Write-Step "Health check OK."
}

Write-Step "Deployment complete."
