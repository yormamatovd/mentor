# Production Build System - Summary

A complete, automated production build system has been created for the Mentor JavaFX application.

## What Has Been Created

### 1. Build Scripts
- **`build-production.sh`** - Main build script for macOS/Linux (executable)
- **`build-production.bat`** - Build script for Windows
- Both scripts are fully automated and production-ready

### 2. Configuration Files
Located in `build-resources/config/`:
- **`app-info.properties`** - Application metadata and versioning
- **`windows-config.cfg`** - Windows-specific installer settings
- **`macos-config.cfg`** - macOS-specific installer settings
- **`jvm-options.txt`** - JVM runtime options and memory settings

### 3. Application Icon
Located in `build-resources/icons/`:
- **`icon.svg`** - Source SVG icon (blue "M" logo)
- Automatically converted to `.ico` (Windows) and `.icns` (macOS) during build

### 4. Documentation
- **`BUILD_GUIDE.md`** - Comprehensive build and distribution guide
- **`QUICKSTART_BUILD.txt`** - Quick reference for building
- **`build-resources/README.md`** - Build resources documentation

### 5. Updated Project Files
- **`pom.xml`** - Enhanced with build plugins and configuration
- **`.gitignore`** - Updated to exclude build artifacts

## How to Use

### Quick Start

#### On macOS/Linux:
```bash
./build-production.sh
```

#### On Windows:
```cmd
build-production.bat
```

### Output
Installers will be created in:
```
installers/
├── mac/
│   └── Mentor-1.0.0.dmg
├── windows/
│   └── Mentor-1.0.0.exe
└── BUILD_INFO.txt
```

## Key Features

### ✓ Fully Automated
- One command builds everything
- Automatic dependency resolution
- Icon conversion (if ImageMagick available)
- Error checking and validation

### ✓ Self-Contained Installers
- **Bundled Java Runtime** - Users don't need Java installed
- **All Dependencies** - JavaFX, SQLite, and all libraries included
- **Desktop Shortcuts** - Automatic creation on both platforms
- **Menu Integration** - Start Menu (Windows) and Launchpad (macOS)

### ✓ Production Ready
- Clean, professional installers
- Proper versioning
- Application icon
- Upgrade support (Windows)
- Code signing ready (optional)

### ✓ Easy Customization
- Simple version updates in `pom.xml` and `app-info.properties`
- Replace `icon.svg` for custom branding
- Adjust memory settings in `jvm-options.txt`
- Configure installer behavior in platform config files

## Build System Architecture

```
Build Process Flow:
1. Environment Check (Java, Maven, ImageMagick)
2. Icon Preparation (SVG → ICO/ICNS conversion)
3. Clean Previous Build
4. Maven Build (compile, package, copy dependencies)
5. Prepare Application Image (JAR + libs)
6. Run jpackage (create native installer)
7. Generate Build Info
```

## What's Bundled in Installers

Each installer (~250-350 MB) contains:

1. **Application Code**
   - Compiled JAR with all resources
   - FXML views, CSS styles, configuration

2. **Java Runtime**
   - Custom JRE image with only required modules
   - Optimized for JavaFX applications

3. **Dependencies**
   - JavaFX 17.0.14 (controls, fxml, web, swing)
   - SQLite JDBC 3.45.1.0
   - ControlsFX, FormsFX, ValidatorFX
   - iTextPDF for PDF generation
   - Logback for logging
   - All other Maven dependencies

4. **Platform Integration**
   - Application icon
   - Desktop shortcut
   - Start Menu/Launchpad entry
   - File associations (if configured)

## Database Handling

The SQLite database is **NOT** bundled in the installer. Instead:

- Database is created automatically on first launch
- Location: `~/Documents/mentor/database.db`
- Schema is initialized by `DatabaseManager.java`
- Each user gets a fresh, clean database

**Benefits:**
- Clean installation for every user
- No data conflicts
- Easy backup (copy `~/Documents/mentor/` folder)
- Simple data migration

## Version Management

### To Release New Version:

1. Update `pom.xml`:
   ```xml
   <version>1.0.1</version>
   ```

2. Update `build-resources/config/app-info.properties`:
   ```properties
   app.version=1.0.1
   ```

3. Update `MAIN_JAR` in build scripts (if version changes):
   - `build-production.sh` line 47
   - `build-production.bat` line 26

4. Run build script:
   ```bash
   ./build-production.sh
   ```

### Upgrade Behavior:

**Windows:**
- In-place upgrade (installs over previous version)
- Preserves user data in Documents folder
- Same UUID ensures smooth upgrade path

**macOS:**
- User drags new app over old app
- User data in Documents folder preserved
- No special upgrade logic needed

## Platform-Specific Notes

### macOS (.dmg)
- **Pros:** Drag-and-drop installation, familiar to Mac users
- **Cons:** Unsigned apps show security warning on first launch
- **Workaround:** Right-click → Open (first time only)
- **Solution:** Code sign with Apple Developer certificate

### Windows (.exe)
- **Pros:** Guided installation wizard, familiar to Windows users
- **Cons:** May show "Unknown Publisher" if not signed
- **Workaround:** Click "More info" → "Run anyway"
- **Solution:** Code sign with valid certificate

## Requirements for Building

### Minimum:
- Java 17 or higher (JDK)
- Maven 3.6+
- 2 GB free disk space

### Recommended:
- ImageMagick (for icon conversion)
- 4 GB RAM
- SSD for faster builds

### Platform-Specific:
- **macOS builds:** Must be on macOS
- **Windows builds:** Must be on Windows
- Cross-platform builds require VM or separate machines

## File Size Breakdown

| Component | Size |
|-----------|------|
| Java Runtime (JRE) | ~150-200 MB |
| JavaFX Libraries | ~40-50 MB |
| Application JAR | ~5-10 MB |
| Dependencies | ~20-30 MB |
| **Total Installer** | **~250-350 MB** |

## Troubleshooting

### Common Issues:

**"jpackage: command not found"**
- Install JDK 17+ (not JRE)
- Ensure JDK bin directory is in PATH

**"Maven build failed"**
- Check internet connection (for dependencies)
- Run `./mvnw clean install` to diagnose
- Clear Maven cache: `rm -rf ~/.m2/repository`

**"Icon conversion failed"**
- Install ImageMagick: `brew install imagemagick`
- Or manually create and place icon files

**Build succeeds but installer doesn't work**
- Check Java version in installer matches runtime
- Verify all dependencies are included
- Test on clean machine without Java installed

## Next Steps

### For Development:
```bash
# Quick test without installer
./mvnw javafx:run

# Build and test installer
./build-production.sh
```

### For Production:
1. Test installer on clean machine
2. Verify all features work
3. Consider code signing for distribution
4. Upload to distribution platform
5. Provide download instructions to users

### For Distribution:
- Upload installers to file hosting (GitHub Releases, website, etc.)
- Provide checksums (SHA256) for verification
- Include installation instructions
- Mention system requirements (Windows 10+, macOS 10.14+)

## Support

For issues or questions:
- See `BUILD_GUIDE.md` for detailed documentation
- Check `QUICKSTART_BUILD.txt` for quick reference
- Review build script logs for errors
- Test on clean system to reproduce issues

---

## Summary

You now have a **complete, production-ready build system** that:

✅ Builds native installers for Windows and macOS
✅ Bundles Java runtime (users don't need Java)
✅ Includes all dependencies and resources
✅ Creates desktop shortcuts automatically
✅ Supports easy version updates
✅ Is fully automated with one command
✅ Is well-documented and maintainable

**Ready to build!** Run: `./build-production.sh`
