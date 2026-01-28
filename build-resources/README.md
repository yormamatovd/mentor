# Build Resources

This directory contains resources for building platform-specific installers.

## Quick Start

### Generate Icons (Required for branded installer)

**Option 1: Automated Script (Recommended)**

On **Windows**:
```powershell
.\build-resources\generate-icon.ps1
```

On **macOS/Linux**:
```bash
./build-resources/generate-icon.sh
```

**Option 2: Online Converter (No installation required)**
1. Go to https://convertio.co/svg-ico/
2. Upload `icon.svg`
3. Download `icon.ico` 
4. Place `icon.ico` in this directory

**Option 3: Manual with ImageMagick**
```bash
# Install ImageMagick first
# Windows: choco install imagemagick
# macOS: brew install imagemagick
# Linux: sudo apt-get install imagemagick

# Generate icon
magick convert icon.svg -define icon:auto-resize=256,128,64,48,32,16 icon.ico
```

### Build Installer

After generating icons:

**Windows**:
```powershell
.\build-scripts\build-windows.ps1
```

The installer will include:
- ✅ Custom application icon
- ✅ Information screen during installation
- ✅ Desktop shortcut option (checked by default)
- ✅ 15-second installation with progress bar
- ✅ Application description

## Icon Design

The `icon.svg` features:
- Minimalist design
- Purple gradient background (#4F46E5 → #7C3AED)
- Teacher/mentor figure with students
- Letter "M" for Mentor

## Advanced: Custom WiX Configuration

The `main.wxs` file provides:
- **Information Dialog**: Shows app features during installation
- **Shortcut Options Dialog**: Checkbox for desktop shortcut (checked by default)
- **Installation Delay**: 15-second progress with status updates
- **Enhanced Progress Messages**: Detailed installation steps

These customizations are automatically included when you build with the scripts.

## Files in This Directory

- `icon.svg` - Source SVG icon design
- `icon.ico` - Windows icon (generated)
- `icon.png` - Linux icon (generated)
- `icon.icns` - macOS icon (generated on macOS)
- `main.wxs` - WiX customization for Windows installer
- `generate-icon.sh` - Icon generation script (macOS/Linux)
- `generate-icon.ps1` - Icon generation script (Windows)
- `README.md` - This file
- `INSTALLER_CUSTOMIZATION.md` - Advanced customization guide

## Troubleshooting

### "ImageMagick not found"
Use the online converter option or install ImageMagick for your platform.

### "Icon.ico not found" during build
Run the icon generation script before building, or the installer will use default Java icon.

### Custom WiX not applied
Ensure `main.wxs` exists in `build-resources/` directory. The build script automatically detects and uses it.
