# OmniServ Release Script
# Uso: .\release.ps1 -Version "1.0.0"

param(
    [Parameter(Mandatory=$true)]
    [string]$Version
)

$ErrorActionPreference = "Stop"

Write-Host "=== OmniServ Release Script ===" -ForegroundColor Cyan
Write-Host "Versión: $Version" -ForegroundColor Yellow

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "app\build.gradle.kts")) {
    Write-Error "Debe ejecutarse desde la raíz del proyecto OmniServ"
    exit 1
}

# Verificar que git está limpio
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Error "Hay cambios sin commitear. Haga commit primero."
    exit 1
}

# Actualizar versión en build.gradle.kts
Write-Host "Actualizando versión en build.gradle.kts..." -ForegroundColor Green
$buildFile = Get-Content "app\build.gradle.kts" -Raw
$buildFile = $buildFile -replace 'versionName = ".*"', "versionName = `"$Version`""
$buildFile = $buildFile -replace 'versionCode = \d+', "versionCode = $($Version.Replace('.', ''))"
Set-Content "app\build.gradle.kts" $buildFile

# Compilar release
Write-Host "Compilando release..." -ForegroundColor Green
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$env:ANDROID_HOME = "C:\Users\DeadW\AppData\Local\Android\Sdk"
& .\gradlew.bat assembleRelease --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Error "Error al compilar release"
    exit 1
}

# Crear tag
Write-Host "Creando tag v$Version..." -ForegroundColor Green
git add -A
git commit -m "release: v$Version"
git tag -a "v$Version" -m "Release v$Version"

# Push
Write-Host "Pushing to origin..." -ForegroundColor Green
git push origin main
git push origin "v$Version"

Write-Host "=== Release v$Version completada ===" -ForegroundColor Cyan
Write-Host "El APK está en: app\build\outputs\apk\release\" -ForegroundColor Yellow
Write-Host "Cree un release en GitHub: https://github.com/betobeto00/OmniServ/releases/new" -ForegroundColor Yellow
