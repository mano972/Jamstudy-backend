<#
.SYNOPSIS
  Deploy a freshly built WAR to a local WildFly instance via the deployment scanner.

.DESCRIPTION
  Copies the WAR into WildFly's standalone/deployments directory, triggers a
  (re)deploy with a .dodeploy marker, and waits for WildFly to write .deployed
  (success) or .failed (failure).

  WildFly's deployment scanner reliably hot-swaps a WAR only a couple of times
  before its deployment subsystem gets wedged. So on ANY failure (an explicit
  .failed, or a timeout with no verdict) this script does NOT attempt another hot
  swap: it restores the previous WAR, fully stops the WildFly Windows service,
  clears standalone/tmp, starts the service again, and waits for the clean-boot
  deploy. That path needs -ServiceName.

  Intended for a self-hosted GitHub Actions runner on the same machine as WildFly.

.EXAMPLE
  .\deploy-wildfly.ps1 -ServiceName "WildFly"
  .\deploy-wildfly.ps1 -ServiceName "WildFly" -HealthUrl "http://localhost:8080/Jamstudy/v1/stats"
#>
[CmdletBinding()]
param(
    [string] $WarSource              = "$PSScriptRoot\..\target\Jamstudy.war",
    [string] $DeployDir              = "C:\Wildfly\wildfly-22.0.0.Final\standalone\deployments",
    [string] $DeployName             = "Jamstudy.war",

    # WildFly Windows service name. REQUIRED for the restart-based recovery on a
    # failed deploy. Find it with:
    #   Get-Service | Where-Object { $_.Name -match 'wildfly|jboss' } | Select-Object Name,DisplayName
    [string] $ServiceName            = "",

    # Wait for the scanner's .deployed / .failed verdict.
    [int]    $TimeoutSec             = 360,
    # WildFly's graceful shutdown often hangs on a non-daemon thread; after this
    # long the script kills the JVM (it's about to cold-restart anyway).
    [int]    $ServiceStopTimeoutSec  = 45,
    [int]    $ServiceStartTimeoutSec = 240,

    # Optional smoke test after a successful deploy.
    [string] $HealthUrl             = "",
    [int]    $HealthTimeoutSec      = 90,

    [int]    $KeepBackups           = 5,

    # Bounce the service even when the hot deploy succeeded (clean-restart every time).
    [switch] $AlwaysRestart
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Write-Step($msg) { Write-Host "==> $msg" }

# --- resolve paths ---------------------------------------------------------
if (-not (Test-Path -LiteralPath $WarSource)) { throw "Source WAR not found: $WarSource" }
$WarSource = (Resolve-Path -LiteralPath $WarSource).Path
if (-not (Test-Path -LiteralPath $DeployDir)) { throw "Deployments directory not found: $DeployDir" }

# Never let a truncated / mid-build WAR overwrite the running deployment.
$srcLen = (Get-Item -LiteralPath $WarSource).Length
if ($srcLen -lt 1MB) {
    throw "Source WAR is only $srcLen bytes - looks empty or mid-build. Aborting before it touches the live deployment."
}
try {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($WarSource)
    $entryCount = $zip.Entries.Count
    $zip.Dispose()
    if ($entryCount -lt 1) { throw "no entries" }
}
catch {
    throw "Source WAR is not a readable zip ($($_.Exception.Message)). Aborting before it touches the live deployment."
}

$standaloneDir = Split-Path -Parent $DeployDir          # ...\standalone
$tmpDir        = Join-Path $standaloneDir "tmp"
$logFile       = Join-Path $standaloneDir "log\server.log"

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

# --- back up the currently deployed WAR --------------------------------------
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

# --- helpers -------------------------------------------------------------
function Clear-Markers {
    foreach ($m in @($deployedMark, $failedMark, $doDeployMark, $isDeployMark, $pendingMark, $undeployMark, $skipMark)) {
        Remove-Item -LiteralPath $m -Force -ErrorAction SilentlyContinue
    }
}

function Wait-ForDeploy([string] $context, [int] $timeout) {
    $deadline = (Get-Date).AddSeconds($timeout)
    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $failedMark) {
            $reason = Get-Content -LiteralPath $failedMark -Raw -ErrorAction SilentlyContinue
            throw "WildFly reported FAILED for ${context}:`n$reason"
        }
        if (Test-Path -LiteralPath $deployedMark) { return }
        Start-Sleep -Seconds 2
    }
    throw "Timed out after ${timeout}s waiting for WildFly to deploy $context."
}

function Show-ServerLogTail {
    if (Test-Path -LiteralPath $logFile) {
        Write-Step "--- last 60 lines of server.log ---------------------------------"
        Get-Content -LiteralPath $logFile -Tail 60 | ForEach-Object { Write-Host "    $_" }
        Write-Step "----------------------------------------------------------------"
    }
}

function Get-WildflyProcess {
    Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -and $_.CommandLine -match 'jboss-modules|org\.jboss\.as\.standalone' } |
        Select-Object -First 1
}

function Restart-WildflyService([string] $reason) {
    if (-not $ServiceName) {
        throw "Cannot recover ($reason): -ServiceName was not provided, so the WildFly service can't be restarted. Restart it by hand on the VM."
    }
    $null = Get-Service -Name $ServiceName -ErrorAction Stop  # fail early if the name is wrong

    Write-Step "Stopping service '$ServiceName' ($reason)"
    if ((Get-Service -Name $ServiceName).Status -ne 'Stopped') {
        # sc.exe returns immediately; we do the waiting ourselves so a hung
        # shutdown can't block the script forever (Stop-Service -Force does).
        & sc.exe stop $ServiceName | Out-Null
    }
    $deadline = (Get-Date).AddSeconds($ServiceStopTimeoutSec)
    while ((Get-Service -Name $ServiceName).Status -ne 'Stopped') {
        if ((Get-Date) -gt $deadline) {
            $proc = Get-WildflyProcess
            if ($proc) {
                Write-Warning "Service '$ServiceName' didn't stop in ${ServiceStopTimeoutSec}s - killing PID $($proc.ProcessId)."
                Stop-Process -Id $proc.ProcessId -Force -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 5
            }
            if ((Get-Service -Name $ServiceName).Status -ne 'Stopped') {
                throw "Service '$ServiceName' could not be stopped (even after killing the JVM)."
            }
            break
        }
        Start-Sleep -Seconds 3
    }
    Write-Step "Service stopped."

    # The JVM can hold file locks for a moment after the service reports Stopped.
    Start-Sleep -Seconds 5
    if (Test-Path -LiteralPath $tmpDir) {
        Write-Step "Clearing $tmpDir"
        Get-ChildItem -LiteralPath $tmpDir -Force -ErrorAction SilentlyContinue |
            Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    }

    Write-Step "Starting service '$ServiceName'"
    Start-Service -Name $ServiceName
    $deadline = (Get-Date).AddSeconds($ServiceStartTimeoutSec)
    while ((Get-Service -Name $ServiceName).Status -ne 'Running') {
        if ((Get-Date) -gt $deadline) { throw "Service '$ServiceName' did not reach Running within ${ServiceStartTimeoutSec}s." }
        Start-Sleep -Seconds 2
    }
    Write-Step "Service running."
}

# --- deploy ------------------------------------------------------------
try {
    Write-Step "Staging new WAR"
    Copy-Item -LiteralPath $WarSource -Destination $staging -Force

    Clear-Markers
    Write-Step "Swapping WAR into place"
    Move-Item -LiteralPath $staging -Destination $target -Force
    New-Item -ItemType File -Path $doDeployMark -Force | Out-Null

    if ($AlwaysRestart) {
        # This app leaks class metadata on hot redeploy (Metaspace fills after a
        # swap or two), so don't hot-deploy at all - bounce the JVM and let it
        # deploy on boot.
        Restart-WildflyService "-AlwaysRestart (clean deploy)"
        Write-Step "Waiting for boot deploy (timeout ${TimeoutSec}s)..."
        Wait-ForDeploy "new deployment after restart" $TimeoutSec
    }
    else {
        Write-Step "Waiting for WildFly hot deploy (timeout ${TimeoutSec}s)..."
        Wait-ForDeploy "new deployment" $TimeoutSec
    }
    Write-Step "WildFly reports DEPLOYED."
}
catch {
    $err = $_.Exception.Message
    Write-Warning "Deploy failed: $err"
    Remove-Item -LiteralPath $staging -Force -ErrorAction SilentlyContinue
    Show-ServerLogTail

    # WildFly's scanner is unreliable after a failed hot swap, so don't try
    # another one. Restore the last-good WAR and bounce the service so it
    # deploys from a clean JVM.
    if ($backupPath -and (Test-Path -LiteralPath $backupPath)) {
        Write-Step "Restoring previous WAR: $backupPath"
        Clear-Markers
        Copy-Item -LiteralPath $backupPath -Destination $target -Force
        New-Item -ItemType File -Path $doDeployMark -Force | Out-Null
    }
    else {
        Write-Warning "No backup WAR to restore - restarting with whatever is in $DeployDir."
    }

    try {
        Restart-WildflyService "deployment failed"
        Wait-ForDeploy "rollback after restart" $TimeoutSec
        Write-Step "Server is back on the PREVIOUS build (rolled back via restart)."
    }
    catch {
        Show-ServerLogTail
        Write-Error "RECOVERY FAILED: $($_.Exception.Message)"
        Write-Error "The site is likely DOWN - manual intervention needed on the WildFly VM."
    }

    throw "Deployment failed: $err"
}

# --- optional health check --------------------------------------------
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
