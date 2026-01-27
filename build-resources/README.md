# Build Resources

This directory contains resources required for building production installers for the Mentor application.

## Directory Structure

```
build-resources/
├── config/
│   ├── app-info.properties      # Application metadata
│   ├── windows-config.cfg       # Windows installer configuration
│   ├── macos-config.cfg         # macOS installer configuration
│   └── jvm-options.txt          # JVM runtime options
└── icons/
    ├── icon.svg                 # Source SVG icon
    ├── windows/                 # Generated Windows icons (.ico)
    └── mac/                     # Generated macOS icons (.icns)
```

## Icons

### Source Icon
- `icon.svg`: The main application icon in SVG format
- Size: 512x512 pixels recommended
- Format: SVG for scalability

### Generated Icons
During the build process, the script automatically generates:
- **Windows**: `icon.ico` (multi-resolution ICO file)
- **macOS**: `icon.icns` (Apple Icon Image format)

To use a custom icon:
1. Replace `icons/icon.svg` with your own SVG icon
2. Or manually place `icon.ico` in `icons/windows/` and `icon.icns` in `icons/mac/`

## Configuration Files

### app-info.properties
Contains application metadata used during the build:
- Application name and version
- Vendor information
- Main class and JAR file name
- Platform-specific identifiers

### windows-config.cfg
Windows-specific installer options:
- Shortcut creation settings
- Start menu configuration
- Installation directory options
- Upgrade UUID for version management

### macos-config.cfg
macOS-specific installer options:
- Bundle identifier
- Application category
- Code signing settings (if applicable)
- Notarization options

### jvm-options.txt
JVM runtime options for the application:
- Memory allocation (-Xms, -Xmx)
- Garbage collection settings
- JavaFX optimizations
- Logging configuration

## Modifying Build Configuration

### Change Application Version
Edit `app-info.properties`:
```properties
app.version=1.0.1
```

Also update `pom.xml`:
```xml
<version>1.0.1</version>
```

### Customize Installer Options
Edit the respective configuration file:
- Windows: `config/windows-config.cfg`
- macOS: `config/macos-config.cfg`

### Adjust Memory Settings
Edit `config/jvm-options.txt` and modify:
```
-Xms256m    # Initial heap size
-Xmx1024m   # Maximum heap size
```

## Notes

- Icon conversion requires ImageMagick
- Windows ICO creation requires ImageMagick on the build machine
- macOS ICNS creation requires `iconutil` (available on macOS only)
- If icon tools are not available, the build script will use default icons
