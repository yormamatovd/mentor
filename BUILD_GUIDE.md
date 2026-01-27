# Mentor Application - Production Build Guide

## Quick Start

Build production installers for your platform:

```bash
./build-production.sh
```

Build for specific platform:

```bash
./build-production.sh mac      # Build macOS installer only
./build-production.sh windows  # Build Windows installer only
```

## Prerequisites

### Required
- **Java 17 or higher** (JDK with jpackage tool)
- **Maven 3.6+** (or use included Maven wrapper)

### Optional
- **ImageMagick** - For automatic icon conversion
  - macOS: `brew install imagemagick`
  - Linux: `apt-get install imagemagick`
  - Windows: Download from https://imagemagick.org/

## Build Output

After successful build, installers will be located in:

```
installers/
├── mac/
│   └── Mentor-1.0.0.dmg          # macOS installer
├── windows/
│   └── Mentor-1.0.0.exe          # Windows installer
└── BUILD_INFO.txt                # Build information
```

## Distribution

### macOS (.dmg)
- Double-click to mount the DMG
- Drag application to Applications folder
- Eject the DMG
- Launch from Applications

**First Launch on macOS:**
If you see "App cannot be opened because the developer cannot be verified":
1. Right-click the app and select "Open"
2. Click "Open" in the dialog
3. The app will open and be remembered as safe

### Windows (.exe)
- Double-click the installer
- Follow installation wizard
- Choose installation directory
- Creates Start Menu and Desktop shortcuts
- Launch from shortcuts or Start Menu

## Customization

### Change Application Version

1. Edit `build-resources/config/app-info.properties`:
   ```properties
   app.version=1.0.1
   ```

2. Edit `pom.xml`:
   ```xml
   <version>1.0.1</version>
   ```

### Replace Application Icon

**Option 1: Replace SVG (Recommended)**
```bash
cp your-icon.svg build-resources/icons/icon.svg
./build-production.sh
```

**Option 2: Use Pre-made Icons**
- Windows: Place `icon.ico` in `build-resources/icons/windows/`
- macOS: Place `icon.icns` in `build-resources/icons/mac/`

### Modify JVM Options

Edit `build-resources/config/jvm-options.txt`:

```properties
-Xms512m     # Increase initial memory
-Xmx2048m    # Increase max memory
```

### Customize Installer Behavior

**Windows:**
Edit `build-resources/config/windows-config.cfg`

**macOS:**
Edit `build-resources/config/macos-config.cfg`

## Troubleshooting

### "jpackage: command not found"
- Ensure you're using JDK 17+ (not JRE)
- Add JDK bin directory to PATH

### "Maven build failed"
- Run `./mvnw clean install` to check for errors
- Ensure all dependencies are available

### Icons not generated
- Install ImageMagick: `brew install imagemagick`
- Or manually create icon files and place in respective directories

### Windows installer fails on macOS
- Windows installers can only be built on Windows
- Use Windows machine or VM for cross-platform builds

### macOS installer fails on Windows
- macOS installers can only be built on macOS
- Use macOS machine or VM for cross-platform builds

## Build Process Details

The build script performs these steps:

1. **Verify Environment**: Check Java, Maven, ImageMagick
2. **Prepare Icons**: Convert SVG to platform-specific formats
3. **Clean Build**: Remove previous build artifacts
4. **Maven Build**: Compile and package application
5. **Create Application Image**: Prepare runtime image with dependencies
6. **Generate Installer**: Use jpackage to create platform installer
7. **Create Build Info**: Generate build information file

## What's Included in the Installer

Each installer includes:

✓ **Application JAR**: Compiled application code
✓ **Dependencies**: All required libraries (JavaFX, SQLite, etc.)
✓ **Java Runtime**: Bundled JRE (no separate Java installation needed)
✓ **Application Icon**: Custom icon for desktop and menus
✓ **Launcher Scripts**: Platform-specific launchers with JVM options
✓ **Desktop Shortcuts**: Quick access to application

## Database and Data

The application **does NOT bundle** the SQLite database. Instead:

- Database is created on first launch
- Location: `~/Documents/mentor/database.db`
- Automatically initialized with schema
- User data is stored locally on each machine

This approach ensures:
- Clean installation for each user
- No data conflicts between installations
- Easy backup and migration (just copy the `mentor` folder)

## File Sizes

Approximate installer sizes:
- **macOS DMG**: ~250-350 MB (includes JavaFX runtime)
- **Windows EXE**: ~200-300 MB (includes JavaFX runtime)

Sizes include:
- Java runtime (JRE)
- JavaFX libraries
- Application code and dependencies
- SQLite JDBC driver
- All third-party libraries

## Version Updates

When releasing a new version:

1. Update version in `pom.xml`
2. Update version in `build-resources/config/app-info.properties`
3. Run build script: `./build-production.sh`
4. Test the installer on target platform
5. Distribute the new installer

**Windows Upgrade Behavior:**
- Users can install over previous version (in-place upgrade)
- Same `win-upgrade-uuid` ensures smooth updates
- User data in Documents folder is preserved

**macOS Upgrade Behavior:**
- Users replace old app with new app in Applications folder
- User data in Documents folder is preserved

## Production Distribution Checklist

Before distributing to end users:

- [ ] Test installer on clean machine (no Java installed)
- [ ] Verify application launches successfully
- [ ] Test database creation and initialization
- [ ] Verify all features work as expected
- [ ] Check desktop shortcuts are created
- [ ] Test uninstall process (Windows)
- [ ] Create release notes documenting changes
- [ ] Upload installers to distribution platform

## Advanced: Code Signing

### macOS Code Signing

Requires Apple Developer account ($99/year):

1. Obtain Developer ID certificate from Apple
2. Edit `build-resources/config/macos-config.cfg`:
   ```properties
   mac-signing-key-user-name=Your Apple Developer Name
   mac-sign=true
   ```
3. Run build script

Benefits:
- No security warnings on first launch
- Required for distribution outside App Store

### Windows Code Signing

Requires code signing certificate from Certificate Authority:

1. Obtain code signing certificate
2. Use `signtool` after build to sign the EXE:
   ```cmd
   signtool sign /f certificate.pfx /p password Mentor-1.0.0.exe
   ```

Benefits:
- No "Unknown Publisher" warnings
- Users trust the installer more readily

## Support and Updates

For build issues or questions:
- Check logs in the build script output
- Review Maven build logs in `target/` directory
- Ensure all prerequisites are installed
- Test on a clean system if possible

---

**Quick Commands Reference:**

```bash
# Build for current platform
./build-production.sh

# Build macOS installer
./build-production.sh mac

# Build Windows installer (on Windows)
./build-production.sh windows

# Clean build artifacts
rm -rf installers/ target/

# Test run without installer
./mvnw javafx:run
```
