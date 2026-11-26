<#
.SYNOPSIS
  Builds the signed release APK and publishes it next to the API so the login page
  link and the in-app update prompt pick it up after the next deploy.

.DESCRIPTION
  1. Builds :app:assembleRelease with DXN_RELEASE_BASE_URL.
  2. Copies the APK to dxn-api/public/downloads/hikaya-tamayoz-<versionName>.apk
     and deletes older APKs there (only the latest build is kept).
  3. Writes dxn-api/public/downloads/latest.json (versionCode, versionName, file, sha256).
  It does not commit or push. Bump versionCode/versionName in app/build.gradle.kts first:
  installed apps only offer the update when versionCode increases.

.EXAMPLE
  .\scripts\release-android.ps1
#>
param(
    [string]$BaseUrl = "https://dxn-train.dockhosting.cloud/",
    [string]$JavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$appDir = Join-Path $root "DXNTrainingApp"
$downloads = Join-Path $root "dxn-api\public\downloads"

if (-not (Test-Path (Join-Path $appDir "keystore.properties"))) {
    throw "DXNTrainingApp\keystore.properties is missing. Run scripts\create-android-keystore.ps1 once first."
}
if (-not $JavaHome) {
    $bundled = Get-ChildItem (Join-Path $root "jdk17\extracted") -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($bundled) { $JavaHome = $bundled.FullName } else { throw "Set JAVA_HOME to a JDK 17." }
}

$env:JAVA_HOME = $JavaHome
$env:DXN_RELEASE_BASE_URL = $BaseUrl

Push-Location $appDir
try {
    & .\gradlew.bat :app:assembleRelease --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Gradle release build failed." }
} finally {
    Pop-Location
}

$outDir = Join-Path $appDir "app\build\outputs\apk\release"
$meta = Get-Content (Join-Path $outDir "output-metadata.json") -Raw | ConvertFrom-Json
$element = $meta.elements[0]
$versionCode = [int]$element.versionCode
$versionName = [string]$element.versionName
$apk = Join-Path $outDir $element.outputFile
if ($versionName -notmatch '^[0-9A-Za-z.\-]+$') { throw "Unexpected versionName '$versionName'." }

New-Item -ItemType Directory -Force -Path $downloads | Out-Null
$fileName = "hikaya-tamayoz-$versionName.apk"
Get-ChildItem $downloads -Filter "*.apk" | Where-Object { $_.Name -ne $fileName } | Remove-Item -Force -Confirm:$false
Copy-Item $apk (Join-Path $downloads $fileName) -Force

$sha256 = (Get-FileHash (Join-Path $downloads $fileName) -Algorithm SHA256).Hash.ToLower()
$manifest = [ordered]@{
    versionCode = $versionCode
    versionName = $versionName
    file        = $fileName
    sha256      = $sha256
}
# UTF-8 without BOM so PHP's json_decode reads it.
[System.IO.File]::WriteAllText((Join-Path $downloads "latest.json"), ($manifest | ConvertTo-Json), (New-Object System.Text.UTF8Encoding $false))

$sizeMb = [math]::Round((Get-Item (Join-Path $downloads $fileName)).Length / 1MB, 1)
Write-Host ""
Write-Host "Published $fileName (versionCode $versionCode, $sizeMb MB) to dxn-api\public\downloads."
Write-Host "Next: commit dxn-api/public/downloads and push to main; DockHosting redeploys and the app offers the update."
