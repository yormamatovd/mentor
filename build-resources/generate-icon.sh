#!/bin/bash
# Generate platform-specific icons from SVG

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SVG_FILE="$SCRIPT_DIR/icon.svg"

echo "Generating icons from $SVG_FILE..."

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null && ! command -v magick &> /dev/null; then
    echo "ERROR: ImageMagick is not installed"
    echo ""
    echo "Install ImageMagick:"
    echo "  macOS:   brew install imagemagick"
    echo "  Windows: choco install imagemagick"
    echo "  Linux:   sudo apt-get install imagemagick"
    echo ""
    echo "Or use online converter: https://convertio.co/svg-ico/"
    exit 1
fi

# Determine ImageMagick command
if command -v magick &> /dev/null; then
    CONVERT_CMD="magick convert"
else
    CONVERT_CMD="convert"
fi

echo "Using: $CONVERT_CMD"

# Generate Windows icon (.ico)
echo "Generating Windows icon (icon.ico)..."
$CONVERT_CMD "$SVG_FILE" -define icon:auto-resize=256,128,64,48,32,16 "$SCRIPT_DIR/icon.ico"

# Generate PNG for Linux
echo "Generating Linux icon (icon.png)..."
$CONVERT_CMD "$SVG_FILE" -resize 256x256 "$SCRIPT_DIR/icon.png"

# Generate macOS iconset
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Generating macOS icon (icon.icns)..."
    
    ICONSET_DIR="$SCRIPT_DIR/icon.iconset"
    mkdir -p "$ICONSET_DIR"
    
    $CONVERT_CMD "$SVG_FILE" -resize 1024x1024 "$ICONSET_DIR/icon_512x512@2x.png"
    $CONVERT_CMD "$SVG_FILE" -resize 512x512 "$ICONSET_DIR/icon_512x512.png"
    $CONVERT_CMD "$SVG_FILE" -resize 512x512 "$ICONSET_DIR/icon_256x256@2x.png"
    $CONVERT_CMD "$SVG_FILE" -resize 256x256 "$ICONSET_DIR/icon_256x256.png"
    $CONVERT_CMD "$SVG_FILE" -resize 256x256 "$ICONSET_DIR/icon_128x128@2x.png"
    $CONVERT_CMD "$SVG_FILE" -resize 128x128 "$ICONSET_DIR/icon_128x128.png"
    $CONVERT_CMD "$SVG_FILE" -resize 64x64 "$ICONSET_DIR/icon_32x32@2x.png"
    $CONVERT_CMD "$SVG_FILE" -resize 32x32 "$ICONSET_DIR/icon_32x32.png"
    $CONVERT_CMD "$SVG_FILE" -resize 32x32 "$ICONSET_DIR/icon_16x16@2x.png"
    $CONVERT_CMD "$SVG_FILE" -resize 16x16 "$ICONSET_DIR/icon_16x16.png"
    
    iconutil -c icns "$ICONSET_DIR" -o "$SCRIPT_DIR/icon.icns"
    rm -rf "$ICONSET_DIR"
    
    echo "✓ macOS icon created: icon.icns"
fi

echo ""
echo "✓ Icon generation complete!"
echo ""
echo "Generated files:"
ls -lh "$SCRIPT_DIR"/*.{ico,png,icns} 2>/dev/null || true
