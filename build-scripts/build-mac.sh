#!/bin/bash

################################################################################
# macOS Installer Build Script
# Creates .app and .dmg installers using jpackage
# Includes embedded JRE - end users don't need Java installed
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Build configuration
APP_NAME="Mentor"
APP_VERSION="1.0.0"
VENDOR="Algo"
MAIN_CLASS="org.algo.mentor.Launcher"
MAIN_JAR="mentor-1.0.0.jar"

# Directories
TARGET_DIR="$PROJECT_ROOT/target"
INSTALLER_DIR="$TARGET_DIR/installer"
LIBS_DIR="$TARGET_DIR/libs"
JPACKAGE_INPUT="$TARGET_DIR/jpackage-input"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verify macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    log_error "This script must be run on macOS"
    exit 1
fi

# Verify Java 17+
log_info "Checking Java version..."
if ! command -v java &> /dev/null; then
    log_error "Java not found. Install Java 17+ to build the application"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    log_error "Java 17+ required. Found Java $JAVA_VERSION"
    exit 1
fi

log_info "Java $JAVA_VERSION detected"

cd "$PROJECT_ROOT"

# Clean previous builds
log_info "Cleaning previous builds..."
rm -rf "$INSTALLER_DIR"
mkdir -p "$INSTALLER_DIR"

# Build with Maven
log_info "Building project with Maven..."
./mvnw clean package -DskipTests

if [ ! -f "$TARGET_DIR/$MAIN_JAR" ]; then
    log_error "Build failed: JAR not found at $TARGET_DIR/$MAIN_JAR"
    exit 1
fi

log_info "Build successful: $MAIN_JAR"

# Verify dependencies copied
if [ ! -d "$LIBS_DIR" ] || [ -z "$(ls -A $LIBS_DIR)" ]; then
    log_error "Dependencies not found in $LIBS_DIR"
    log_error "Ensure maven-dependency-plugin is configured in pom.xml"
    exit 1
fi

log_info "Dependencies found: $(ls $LIBS_DIR | wc -l | xargs) files"

# Prepare jpackage input directory (avoid recursive copy issue)
log_info "Preparing jpackage input directory..."
rm -rf "$JPACKAGE_INPUT"
mkdir -p "$JPACKAGE_INPUT"

# Copy JAR and dependencies to staging directory
cp "$TARGET_DIR/$MAIN_JAR" "$JPACKAGE_INPUT/"
cp -r "$LIBS_DIR" "$JPACKAGE_INPUT/"

log_info "Staging directory ready: $JPACKAGE_INPUT"

# Check for icon file
ICON_PATH="$PROJECT_ROOT/build-resources/icon.icns"
ICON_ARGS=""
if [ -f "$ICON_PATH" ]; then
    ICON_ARGS="--icon $ICON_PATH"
    log_info "Using application icon: $ICON_PATH"
else
    log_warn "Icon file not found at $ICON_PATH"
fi

# Create .app bundle with jpackage
log_info "Creating .app bundle with embedded JRE..."

jpackage \
    --type app-image \
    --dest "$INSTALLER_DIR" \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --vendor "$VENDOR" \
    --input "$JPACKAGE_INPUT" \
    --main-jar "$MAIN_JAR" \
    --main-class "$MAIN_CLASS" \
    --java-options "-Xmx1024m" \
    --java-options "-Xms256m" \
    $ICON_ARGS

if [ $? -ne 0 ]; then
    log_error ".app bundle creation failed"
    exit 1
fi

log_info ".app bundle created: $INSTALLER_DIR/$APP_NAME.app"

# Create .dmg installer
log_info "Creating .dmg installer..."

jpackage \
    --type dmg \
    --dest "$INSTALLER_DIR" \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --vendor "$VENDOR" \
    --input "$JPACKAGE_INPUT" \
    --main-jar "$MAIN_JAR" \
    --main-class "$MAIN_CLASS" \
    --java-options "-Xmx1024m" \
    --java-options "-Xms256m" \
    --mac-package-name "$APP_NAME" \
    $ICON_ARGS

if [ $? -ne 0 ]; then
    log_error ".dmg creation failed"
    exit 1
fi

# Find and display results
DMG_FILE=$(find "$INSTALLER_DIR" -name "*.dmg" -type f | head -n 1)

log_info "================================"
log_info "macOS Build Complete!"
log_info "================================"
log_info ".app bundle: $INSTALLER_DIR/$APP_NAME.app"
log_info ".dmg installer: $DMG_FILE"
log_info ""
log_info "Installer size: $(du -sh "$DMG_FILE" | cut -f1)"
log_info ""
log_info "End users can install without Java!"

exit 0
