<#
.SYNOPSIS
  Logical backup of the Couchbase bucket to a timestamped, compressed JSON file.

.DESCRIPTION
  Runs one N1QL query via cbq.exe that returns { id, doc } for every document in
  the bucket, writes it to <BackupDir>\<Bucket>_<stamp>.json, zips it, and prunes
  zips older than -KeepDays.

  Why a logical dump and not a file copy: Couchbase's on-disk data files can't be
  safely copied while the server runs. This bucket is small and single-purpose, so
  one query is a good-enough consistent snapshot and restores with a simple UPSERT
  loop (see restore-couchbase.ps1).

  cbq.exe ships with every Couchbase edition. The user needs query-select on the
  bucket - the cluster Administrator account works.

.EXAMPLE
  .\backup-couchbase.ps1 -Password 'clusterAdminPass'
  $env:COUCHBASE_BACKUP_PASSWORD='...'; .\backup-couchbase.ps1

.NOTES
  For a full physical backup (indexes, design docs, all buckets) use cbbackup
  (Community Edition) or cbbackupmgr (Enterprise). This script is the lightweight,
  restore-anywhere option.
#>
[CmdletBinding()]
param(
    [string] $CbBin      = "C:\Program Files\Couchbase\Server\bin",
    [string] $ClusterUrl = "http://localhost:8091",
    [string] $Bucket     = "JamstudyBucket",
    [string] $User       = "JamstudyBucket",
    [string] $Password   = "jamstudy123",                 # or set env COUCHBASE_BACKUP_PASSWORD
    [string] $BackupDir  = "C:\CouchbaseBackups",
    [int]    $KeepDays    = 30,
    [int]    $MinDocs     = 1                   # abort if the dump has fewer rows (don't rotate over a good backup with a broken one)
)

$ErrorActionPreference = 'Stop'

if (-not $Password) { $Password = $env:COUCHBASE_BACKUP_PASSWORD }
if (-not $Password) { throw "No password. Pass -Password or set COUCHBASE_BACKUP_PASSWORD." }

$cbq = Join-Path $CbBin "cbq.exe"
if (-not (Test-Path -LiteralPath $cbq)) { throw "cbq.exe not found at '$cbq' - set -CbBin to your Couchbase bin folder." }

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
$stamp   = Get-Date -Format 'yyyyMMdd-HHmmss'
$jsonOut = Join-Path $BackupDir ("{0}_{1}.json" -f $Bucket, $stamp)
$zipOut  = "$jsonOut.zip"

# id + full document body for every doc, in one snapshot
$stmt = "SELECT META(b).id AS id, b AS doc FROM ``$Bucket`` AS b"

Write-Host "==> Dumping '$Bucket' via cbq ..."
& $cbq --engine=$ClusterUrl --user=$User --password=$Password --quiet --exit-on-error --script=$stmt |
    Out-File -LiteralPath $jsonOut -Encoding utf8
if ($LASTEXITCODE -ne 0) {
    Remove-Item -LiteralPath $jsonOut -Force -ErrorAction SilentlyContinue
    throw "cbq failed (exit $LASTEXITCODE)."
}

# cbq prints an envelope: { "results": [...], "metrics": { "resultCount": N, ... }, "errors": [...] }
$result = Get-Content -LiteralPath $jsonOut -Raw | ConvertFrom-Json
if ($result.PSObject.Properties.Name -contains 'errors' -and $result.errors) {
    Remove-Item -LiteralPath $jsonOut -Force -ErrorAction SilentlyContinue
    throw "Query returned errors: $($result.errors | ConvertTo-Json -Compress)"
}
$count = [int]$result.metrics.resultCount
if ($count -lt $MinDocs) {
    Remove-Item -LiteralPath $jsonOut -Force -ErrorAction SilentlyContinue
    throw "Backup had only $count docs (< $MinDocs) - not saving; check the database."
}
Write-Host "==> $count documents"

Compress-Archive -LiteralPath $jsonOut -DestinationPath $zipOut -Force
Remove-Item -LiteralPath $jsonOut -Force
Write-Host ("==> Wrote {0} ({1:N1} KB)" -f $zipOut, ((Get-Item $zipOut).Length / 1KB))

# prune old backups
$cutoff = (Get-Date).AddDays(-$KeepDays)
Get-ChildItem -LiteralPath $BackupDir -Filter ("{0}_*.json.zip" -f $Bucket) |
    Where-Object { $_.LastWriteTime -lt $cutoff } |
    ForEach-Object { Write-Host "==> pruning $($_.Name)"; Remove-Item -LiteralPath $_.FullName -Force }

Write-Host "==> Backup complete."
