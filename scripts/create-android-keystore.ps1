<#
.SYNOPSIS
  Creates the release signing key once and writes DXNTrainingApp\keystore.properties.

.DESCRIPTION
  Run this yourself in a terminal: it asks for the password and never prints it.
  Both files are gitignored. Back up dxn-release.jks and the password somewhere safe:
  every future update must be signed with this same key, or phones will refuse to
  install it over the current version.
#>
param(
    [string]$JavaHome = $env:JAVA_HOME,
    # Where keystore.properties and dxn-release.jks go; defaults to this checkout.
    [string]$AppDir = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$appDir = if ($AppDir) { $AppDir } else { Join-Path $root "DXNTrainingApp" }
$keystore = Join-Path $appDir "dxn-release.jks"
$props = Join-Path $appDir "keystore.properties"

if (Test-Path $keystore) { throw "$keystore already exists. Keep using it; do not create a new key." }
if (-not $JavaHome) {
    $bundled = Get-ChildItem (Join-Path $root "jdk17\extracted") -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($bundled) { $JavaHome = $bundled.FullName } else { throw "Set JAVA_HOME to a JDK 17." }
}
$keytool = Join-Path $JavaHome "bin\keytool.exe"

$secure = Read-Host "Choose a keystore password (at least 8 characters)" -AsSecureString
$confirm = Read-Host "Repeat the password" -AsSecureString
$plain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR([Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
$plain2 = [Runtime.InteropServices.Marshal]::PtrToStringBSTR([Runtime.InteropServices.Marshal]::SecureStringToBSTR($confirm))
if ($plain -ne $plain2) { throw "Passwords do not match." }
if ($plain.Length -lt 8) { throw "Password must be at least 8 characters." }

& $keytool -genkeypair -keystore $keystore -alias dxn -keyalg RSA -keysize 2048 -validity 10000 `
    -storepass $plain -keypass $plain -dname "CN=Hikaya Tamayoz, O=Hikaya Tamayoz, C=PS"
if ($LASTEXITCODE -ne 0) { throw "keytool failed." }

$content = "storeFile=dxn-release.jks`nstorePassword=$plain`nkeyAlias=dxn`nkeyPassword=$plain`n"
[System.IO.File]::WriteAllText($props, $content, (New-Object System.Text.UTF8Encoding $false))
$plain = $null; $plain2 = $null

Write-Host ""
Write-Host "Created $keystore and keystore.properties (both gitignored)."
Write-Host "Back up dxn-release.jks and your password now. Losing them means users must uninstall to get updates."
