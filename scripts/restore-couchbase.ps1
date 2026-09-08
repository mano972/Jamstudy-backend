<#
.SYNOPSIS
  Restore a bucket from a backup-couchbase.ps1 zip (UPSERTs every document back).

.DESCRIPTION
  Extracts the zip, then feeds cbq.exe one UPSERT per document. UPSERT means the
  restore is idempotent and additive - it will not delete documents that exist now
  but weren't in the backup. Wipe the bucket first (or restore into an empty one)
  if you need an exact point-in-time state.

.EXAMPLE
  .\restore-couchbase.ps1 -BackupZip C:\CouchbaseBackups\JamstudyBucket_20260828-170000.json.zip -Password '...'
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $BackupZip,
    [string] $CbBin      = "C:\Program Files\Couchbase\Server\bin",
    [string] $ClusterUrl = "http://localhost:8091",
    [string] $Bucket     = "JamstudyBucket",
    [string] $User       = "Administrator",
    [string] $Password   = "",
    [switch] $WhatIf
)

$ErrorActionPreference = 'Stop'

if (-not $Password) { $Password = $env:COUCHBASE_BACKUP_PASSWORD }
if (-not $Password) { throw "No password. Pass -Password or set COUCHBASE_BACKUP_PASSWORD." }
if (-not (Test-Path -LiteralPath $BackupZip)) { throw "Backup not found: $BackupZip" }

$cbq = Join-Path $CbBin "cbq.exe"
if (-not (Test-Path -LiteralPath $cbq)) { throw "cbq.exe not found at '$cbq'." }

$work = Join-Path $env:TEMP ("cbrestore_" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $work | Out-Null
try {
    Expand-Archive -LiteralPath $BackupZip -DestinationPath $work -Force
    $jsonFile = Get-ChildItem -LiteralPath $work -Filter *.json | Select-Object -First 1
    if (-not $jsonFile) { throw "No .json inside $BackupZip" }

    $result = Get-Content -LiteralPath $jsonFile.FullName -Raw | ConvertFrom-Json
    $rows = @($result.results)
    Write-Host "==> $($rows.Count) documents to restore into '$Bucket'"

    # one UPSERT statement per doc; the doc JSON is already a valid N1QL object literal
    $script = Join-Path $work "restore.n1ql"
    $sb = [System.Text.StringBuilder]::new()
    foreach ($row in $rows) {
        $key  = ($row.id -replace '"', '\"')
        $body = $row.doc | ConvertTo-Json -Depth 100 -Compress
        [void]$sb.AppendLine("UPSERT INTO ``$Bucket`` (KEY, VALUE) VALUES (`"$key`", $body);")
    }
    [System.IO.File]::WriteAllText($script, $sb.ToString(), [System.Text.UTF8Encoding]::new($false))

    if ($WhatIf) {
        Write-Host "==> -WhatIf: generated $script ($($rows.Count) UPSERTs); not running."
        Write-Host (Get-Content -LiteralPath $script -TotalCount 3 | Out-String)
        return
    }

    Write-Host "==> Running restore via cbq ..."
    & $cbq --engine=$ClusterUrl --user=$User --password=$Password --quiet --exit-on-error --file=$script
    if ($LASTEXITCODE -ne 0) { throw "cbq restore failed (exit $LASTEXITCODE)." }
    Write-Host "==> Restore complete."
}
finally {
    Remove-Item -LiteralPath $work -Recurse -Force -ErrorAction SilentlyContinue
}
