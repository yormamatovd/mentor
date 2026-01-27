################################################################################
# Windows Installer Build Script
# Creates .exe installer using jpackage
# Includes embedded JRE - end users don't need Java installed
################################################################################

param(
    [switch]$SkipTests = $false
)

$ErrorActionPreference = "Stop"

# Build configuration
$APP_NAME = "Mentor"
$APP_VERSION = "1.0.0"
$VENDOR = "Algo"
$MAIN_CLASS = "org.algo.mentor.Launcher"
$MAIN_JAR = "mentor-1.0.0.jar"

# Get script and project directories
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR

# Directories
$TARGET_DIR = Join-Path $PROJECT_ROOT "target"
$INSTALLER_DIR = Join-Path $TARGET_DIR "installer"
$LIBS_DIR = Join-Path $TARGET_DIR "libs"

# Color output functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Err {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
}

# Verify Windows
if (-not $IsWindows -and $PSVersionTable.PSVersion.Major -ge 6) {
    Write-Err "This script must be run on Windows"
    exit 1
}

# Verify Java 17+
Write-Info "Checking Java version..."
try {
    $javaVersion = & java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*"(\d+).*', '$1' }
    $javaVersionNum = [int]$javaVersion
    
    if ($javaVersionNum -lt 17) {
        Write-Err "Java 17+ required. Found Java $javaVersionNum"
        exit 1
    }
    
    Write-Info "Java $javaVersionNum detected"
}
catch {
    Write-Err "Java not found. Install Java 17+ to build the application"
    exit 1
}

# Verify JAVA_HOME
if (-not $env:JAVA_HOME) {
    Write-Err "JAVA_HOME environment variable not set"
    exit 1
}

Write-Info "JAVA_HOME: $env:JAVA_HOME"

# Change to project root
Set-Location $PROJECT_ROOT

# Clean previous builds
Write-Info "Cleaning previous builds..."
if (Test-Path $INSTALLER_DIR) {
    Remove-Item -Path $INSTALLER_DIR -Recurse -Force
}
New-Item -ItemType Directory -Path $INSTALLER_DIR -Force | Out-Null

# Build with Maven
Write-Header "Building with Maven"
$mvnCmd = ".\mvnw.cmd"
if (-not (Test-Path $mvnCmd)) {
    Write-Err "Maven wrapper not found: $mvnCmd"
    exit 1
}

$buildArgs = "clean", "package"
if ($SkipTests) {
    $buildArgs += "-DskipTests"
}

Write-Info "Running: $mvnCmd $($buildArgs -join ' ')"
& $mvnCmd $buildArgs

if ($LASTEXITCODE -ne 0) {
    Write-Err "Maven build failed"
    exit 1
}

# Verify JAR
$jarPath = Join-Path $TARGET_DIR $MAIN_JAR
if (-not (Test-Path $jarPath)) {
    Write-Err "Build failed: JAR not found at $jarPath"
    exit 1
}

Write-Info "Build successful: $MAIN_JAR"

# Verify dependencies
if (-not (Test-Path $LIBS_DIR) -or (Get-ChildItem $LIBS_DIR).Count -eq 0) {
    Write-Err "Dependencies not found in $LIBS_DIR"
    Write-Err "Ensure maven-dependency-plugin is configured in pom.xml"
    exit 1
}

$depCount = (Get-ChildItem $LIBS_DIR).Count
Write-Info "Dependencies found: $depCount files"

# Create .exe installer with jpackage
Write-Header "Creating Windows .exe Installer"
Write-Info "Using jpackage with embedded JRE..."

$jpackageArgs = @(
    "--type", "exe",
    "--dest", $INSTALLER_DIR,
    "--name", $APP_NAME,
    "--app-version", $APP_VERSION,
    "--vendor", $VENDOR,
    "--input", $TARGET_DIR,
    "--main-jar", $MAIN_JAR,
    "--main-class", $MAIN_CLASS,
    "--runtime-image", $env:JAVA_HOME,
    "--java-options", "-Xmx1024m",
    "--java-options", "-Xms256m",
    "--win-dir-chooser",
    "--win-menu",
    "--win-shortcut"
)

Write-Info "Running jpackage..."
& jpackage $jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Err ".exe installer creation failed"
    exit 1
}

# Find installer
$exeFile = Get-ChildItem -Path $INSTALLER_DIR -Filter "*.exe" | Select-Object -First 1

if (-not $exeFile) {
    Write-Err "Installer not found in $INSTALLER_DIR"
    exit 1
}

# Display results
Write-Header "Windows Build Complete!"
Write-Info "Installer: $($exeFile.FullName)"
Write-Info "Size: $([math]::Round($exeFile.Length / 1MB, 2)) MB"
Write-Info ""
Write-Info "End users can install without Java!"

exit 0
