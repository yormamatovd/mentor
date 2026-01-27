#!/bin/bash

###############################################################################
# Production Build Script for Mentor Application
# 
# This script automates the complete build process for creating production-ready
# installers for both Windows and macOS platforms.
#
# Requirements:
# - Java 17 or higher (with jpackage tool)
# - Maven 3.6+
# - ImageMagick (for icon conversion)
# - For Windows builds on Mac: WiX Toolset (optional, for MSI)
#
# Usage:
#   ./build-production.sh [windows|mac|all]
#
# Output:
#   - Windows installer: installers/windows/Mentor-1.0.0.exe
#   - macOS installer: installers/mac/Mentor-1.0.0.dmg
###############################################################################

set -e  # Exit on error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}"
BUILD_DIR="${PROJECT_DIR}/target"
INSTALLERS_DIR="${PROJECT_DIR}/installers"
BUILD_RESOURCES="${PROJECT_DIR}/build-resources"
ICONS_DIR="${BUILD_RESOURCES}/icons"
CONFIG_DIR="${BUILD_RESOURCES}/config"

# Application configuration (can be overridden by app-info.properties)
APP_NAME="Mentor"
APP_VERSION="1.0.0"
APP_VENDOR="AlgoMentor"
APP_DESCRIPTION="Student management and teaching platform"
MAIN_CLASS="org.algo.mentor.Launcher"
MAIN_JAR="mentor-1.0.0.jar"

# Platform detection
OS_TYPE="$(uname -s)"
case "${OS_TYPE}" in
    Darwin*)    CURRENT_OS="mac";;
    Linux*)     CURRENT_OS="linux";;
    MINGW*|MSYS*|CYGWIN*) CURRENT_OS="windows";;
    *)          CURRENT_OS="unknown";;
esac

# Functions
print_header() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

check_java() {
    print_header "Checking Java Installation"
    
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
    
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    print_success "Java $JAVA_VERSION detected"
    
    # Check for jpackage
    if ! command -v jpackage &> /dev/null; then
        print_error "jpackage tool not found. Ensure you're using JDK (not JRE) 17+"
        exit 1
    fi
    
    print_success "jpackage tool found"
}

check_maven() {
    print_header "Checking Maven Installation"
    
    if ! command -v mvn &> /dev/null; then
        # Try using Maven wrapper
        if [ -f "${PROJECT_DIR}/mvnw" ]; then
            print_warning "Maven not found in PATH, using Maven wrapper"
            MVN_CMD="${PROJECT_DIR}/mvnw"
        else
            print_error "Maven is not installed and mvnw wrapper not found"
            exit 1
        fi
    else
        MVN_CMD="mvn"
    fi
    
    MVN_VERSION=$($MVN_CMD -version | head -n 1)
    print_success "$MVN_VERSION"
}

check_imagemagick() {
    print_header "Checking ImageMagick Installation"
    
    if ! command -v convert &> /dev/null; then
        print_warning "ImageMagick not found. Icon conversion will be skipped."
        print_info "Install with: brew install imagemagick (macOS) or apt-get install imagemagick (Linux)"
        IMAGEMAGICK_AVAILABLE=false
    else
        print_success "ImageMagick found"
        IMAGEMAGICK_AVAILABLE=true
    fi
}

prepare_icons() {
    print_header "Preparing Application Icons"
    
    # Create icon output directories
    mkdir -p "${ICONS_DIR}/windows"
    mkdir -p "${ICONS_DIR}/mac"
    
    if [ "$IMAGEMAGICK_AVAILABLE" = true ]; then
        # Convert SVG to PNG (various sizes)
        print_info "Converting SVG icon to PNG formats..."
        
        # For Windows
        convert "${ICONS_DIR}/icon.svg" -resize 256x256 "${ICONS_DIR}/windows/icon-256.png"
        convert "${ICONS_DIR}/icon.svg" -resize 128x128 "${ICONS_DIR}/windows/icon-128.png"
        convert "${ICONS_DIR}/icon.svg" -resize 64x64 "${ICONS_DIR}/windows/icon-64.png"
        convert "${ICONS_DIR}/icon.svg" -resize 48x48 "${ICONS_DIR}/windows/icon-48.png"
        convert "${ICONS_DIR}/icon.svg" -resize 32x32 "${ICONS_DIR}/windows/icon-32.png"
        convert "${ICONS_DIR}/icon.svg" -resize 16x16 "${ICONS_DIR}/windows/icon-16.png"
        
        # Create ICO file (Windows)
        convert "${ICONS_DIR}/windows/icon-256.png" \
                "${ICONS_DIR}/windows/icon-128.png" \
                "${ICONS_DIR}/windows/icon-64.png" \
                "${ICONS_DIR}/windows/icon-48.png" \
                "${ICONS_DIR}/windows/icon-32.png" \
                "${ICONS_DIR}/windows/icon-16.png" \
                "${ICONS_DIR}/windows/icon.ico"
        
        # For macOS
        convert "${ICONS_DIR}/icon.svg" -resize 1024x1024 "${ICONS_DIR}/mac/icon-1024.png"
        convert "${ICONS_DIR}/icon.svg" -resize 512x512 "${ICONS_DIR}/mac/icon-512.png"
        convert "${ICONS_DIR}/icon.svg" -resize 256x256 "${ICONS_DIR}/mac/icon-256.png"
        convert "${ICONS_DIR}/icon.svg" -resize 128x128 "${ICONS_DIR}/mac/icon-128.png"
        
        # Create ICNS file (macOS)
        if command -v iconutil &> /dev/null; then
            # Create iconset directory
            ICONSET_DIR="${ICONS_DIR}/mac/AppIcon.iconset"
            mkdir -p "$ICONSET_DIR"
            
            # Copy and rename files for iconset
            cp "${ICONS_DIR}/mac/icon-1024.png" "$ICONSET_DIR/icon_512x512@2x.png"
            cp "${ICONS_DIR}/mac/icon-512.png" "$ICONSET_DIR/icon_512x512.png"
            cp "${ICONS_DIR}/mac/icon-512.png" "$ICONSET_DIR/icon_256x256@2x.png"
            cp "${ICONS_DIR}/mac/icon-256.png" "$ICONSET_DIR/icon_256x256.png"
            cp "${ICONS_DIR}/mac/icon-256.png" "$ICONSET_DIR/icon_128x128@2x.png"
            cp "${ICONS_DIR}/mac/icon-128.png" "$ICONSET_DIR/icon_128x128.png"
            convert "${ICONS_DIR}/icon.svg" -resize 64x64 "$ICONSET_DIR/icon_32x32@2x.png"
            convert "${ICONS_DIR}/icon.svg" -resize 32x32 "$ICONSET_DIR/icon_32x32.png"
            convert "${ICONS_DIR}/icon.svg" -resize 32x32 "$ICONSET_DIR/icon_16x16@2x.png"
            convert "${ICONS_DIR}/icon.svg" -resize 16x16 "$ICONSET_DIR/icon_16x16.png"
            
            # Create ICNS
            iconutil -c icns "$ICONSET_DIR" -o "${ICONS_DIR}/mac/icon.icns"
            rm -rf "$ICONSET_DIR"
            
            print_success "macOS ICNS file created"
        else
            print_warning "iconutil not available (macOS only). Using PNG for macOS"
        fi
        
        print_success "Icon conversion completed"
    else
        print_warning "Skipping icon conversion. Using default icons."
    fi
}

clean_build() {
    print_header "Cleaning Previous Build"
    
    rm -rf "${BUILD_DIR}"
    rm -rf "${INSTALLERS_DIR}"
    
    print_success "Build directories cleaned"
}

build_maven_project() {
    print_header "Building Maven Project"
    
    cd "${PROJECT_DIR}"
    
    print_info "Running Maven clean package..."
    $MVN_CMD clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        print_error "Maven build failed"
        exit 1
    fi
    
    print_success "Maven build completed"
}

create_app_image() {
    local platform=$1
    print_header "Creating Application Image for ${platform}"
    
    local APP_IMAGE_DIR="${BUILD_DIR}/app-image-${platform}"
    rm -rf "${APP_IMAGE_DIR}"
    
    # Create input directory with JAR and dependencies
    local INPUT_DIR="${BUILD_DIR}/jpackage-input"
    rm -rf "${INPUT_DIR}"
    mkdir -p "${INPUT_DIR}"
    
    # Copy main JAR and all dependencies
    cp "${BUILD_DIR}/${MAIN_JAR}" "${INPUT_DIR}/"
    cp -r "${BUILD_DIR}/libs"/* "${INPUT_DIR}/" 2>/dev/null || true
    
    print_success "Application image directory prepared"
}

build_windows_installer() {
    print_header "Building Windows Installer"
    
    create_app_image "windows"
    
    local INPUT_DIR="${BUILD_DIR}/jpackage-input"
    local OUTPUT_DIR="${INSTALLERS_DIR}/windows"
    mkdir -p "${OUTPUT_DIR}"
    
    local ICON_FILE="${ICONS_DIR}/windows/icon.ico"
    if [ ! -f "$ICON_FILE" ]; then
        ICON_FILE=""
        print_warning "Windows icon not found, using default"
    fi
    
    # Build jpackage command
    local JPACKAGE_CMD=(
        jpackage
        --type exe
        --input "${INPUT_DIR}"
        --dest "${OUTPUT_DIR}"
        --name "${APP_NAME}"
        --main-jar "${MAIN_JAR}"
        --main-class "${MAIN_CLASS}"
        --app-version "${APP_VERSION}"
        --vendor "${APP_VENDOR}"
        --description "${APP_DESCRIPTION}"
        --copyright "Copyright © 2025 ${APP_VENDOR}"
        --win-dir-chooser
        --win-menu
        --win-menu-group "${APP_VENDOR}"
        --win-shortcut
        --win-shortcut-prompt
    )
    
    # Add icon if available
    if [ -n "$ICON_FILE" ]; then
        JPACKAGE_CMD+=(--icon "$ICON_FILE")
    fi
    
    print_info "Running jpackage for Windows..."
    "${JPACKAGE_CMD[@]}"
    
    if [ $? -eq 0 ]; then
        print_success "Windows installer created: ${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}.exe"
    else
        print_error "Windows installer creation failed"
        return 1
    fi
}

build_mac_installer() {
    print_header "Building macOS Installer"
    
    create_app_image "mac"
    
    local INPUT_DIR="${BUILD_DIR}/jpackage-input"
    local OUTPUT_DIR="${INSTALLERS_DIR}/mac"
    mkdir -p "${OUTPUT_DIR}"
    
    local ICON_FILE="${ICONS_DIR}/mac/icon.icns"
    if [ ! -f "$ICON_FILE" ]; then
        ICON_FILE="${ICONS_DIR}/mac/icon-512.png"
        if [ ! -f "$ICON_FILE" ]; then
            ICON_FILE=""
            print_warning "macOS icon not found, using default"
        fi
    fi
    
    # Build jpackage command
    local JPACKAGE_CMD=(
        jpackage
        --type dmg
        --input "${INPUT_DIR}"
        --dest "${OUTPUT_DIR}"
        --name "${APP_NAME}"
        --main-jar "${MAIN_JAR}"
        --main-class "${MAIN_CLASS}"
        --app-version "${APP_VERSION}"
        --vendor "${APP_VENDOR}"
        --description "${APP_DESCRIPTION}"
        --copyright "Copyright © 2025 ${APP_VENDOR}"
        --mac-package-identifier "org.algo.mentor"
        --mac-package-name "${APP_NAME}"
    )
    
    # Add icon if available
    if [ -n "$ICON_FILE" ]; then
        JPACKAGE_CMD+=(--icon "$ICON_FILE")
    fi
    
    print_info "Running jpackage for macOS..."
    "${JPACKAGE_CMD[@]}"
    
    if [ $? -eq 0 ]; then
        print_success "macOS installer created: ${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}.dmg"
    else
        print_error "macOS installer creation failed"
        return 1
    fi
}

create_build_info() {
    print_header "Creating Build Information File"
    
    local BUILD_INFO_FILE="${INSTALLERS_DIR}/BUILD_INFO.txt"
    
    cat > "$BUILD_INFO_FILE" <<EOF
Mentor Application - Build Information
========================================

Build Date: $(date)
Application Version: ${APP_VERSION}
Build Platform: ${CURRENT_OS}

Components:
-----------
- Java Version: ${JAVA_VERSION}
- Maven: ${MVN_VERSION}

Installers:
-----------
EOF
    
    if [ -d "${INSTALLERS_DIR}/windows" ]; then
        echo "- Windows: installers/windows/${APP_NAME}-${APP_VERSION}.exe" >> "$BUILD_INFO_FILE"
        ls -lh "${INSTALLERS_DIR}/windows"/*.exe 2>/dev/null | awk '{print "  Size: " $5}' >> "$BUILD_INFO_FILE"
    fi
    
    if [ -d "${INSTALLERS_DIR}/mac" ]; then
        echo "- macOS: installers/mac/${APP_NAME}-${APP_VERSION}.dmg" >> "$BUILD_INFO_FILE"
        ls -lh "${INSTALLERS_DIR}/mac"/*.dmg 2>/dev/null | awk '{print "  Size: " $5}' >> "$BUILD_INFO_FILE"
    fi
    
    print_success "Build info created: ${BUILD_INFO_FILE}"
}

show_summary() {
    print_header "Build Summary"
    
    echo ""
    print_info "Application: ${APP_NAME} v${APP_VERSION}"
    print_info "Build Directory: ${INSTALLERS_DIR}"
    echo ""
    
    if [ -d "${INSTALLERS_DIR}/windows" ]; then
        print_success "Windows Installer:"
        ls -lh "${INSTALLERS_DIR}/windows"/*.exe 2>/dev/null | while read line; do
            echo "  $line"
        done
    fi
    
    if [ -d "${INSTALLERS_DIR}/mac" ]; then
        print_success "macOS Installer:"
        ls -lh "${INSTALLERS_DIR}/mac"/*.dmg 2>/dev/null | while read line; do
            echo "  $line"
        done
    fi
    
    echo ""
    print_success "Build completed successfully!"
    echo ""
}

# Main script execution
main() {
    local BUILD_TARGET="${1:-all}"
    
    print_header "Mentor Application - Production Build"
    print_info "Build target: ${BUILD_TARGET}"
    print_info "Current platform: ${CURRENT_OS}"
    echo ""
    
    # Pre-build checks
    check_java
    check_maven
    check_imagemagick
    
    # Prepare resources
    prepare_icons
    
    # Clean and build
    clean_build
    build_maven_project
    
    # Create installers based on target
    case "$BUILD_TARGET" in
        windows)
            build_windows_installer
            ;;
        mac)
            build_mac_installer
            ;;
        all)
            if [ "$CURRENT_OS" = "mac" ]; then
                build_mac_installer
                print_warning "Windows installer build skipped (requires Windows or cross-compile tools)"
            elif [ "$CURRENT_OS" = "windows" ]; then
                build_windows_installer
                print_warning "macOS installer build skipped (requires macOS)"
            else
                print_error "Unsupported platform for building installers: $CURRENT_OS"
                exit 1
            fi
            ;;
        *)
            print_error "Invalid build target: $BUILD_TARGET"
            print_info "Usage: $0 [windows|mac|all]"
            exit 1
            ;;
    esac
    
    # Create build information
    create_build_info
    
    # Show summary
    show_summary
}

# Run main function
main "$@"
