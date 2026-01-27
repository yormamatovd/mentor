# Build System Documentation

## Folder Structure

```
mentor/
├── build-scripts/
│   ├── build.sh                    # Main orchestrator (run on macOS)
│   ├── build-mac.sh                # macOS .app and .dmg builder
│   ├── build-windows.ps1           # Windows .exe builder
│   └── README.md                   # Build system documentation
│
├── .github/
│   └── workflows/
│       └── windows-build.yml       # GitHub Actions for Windows builds
│
├── build-resources/                # Installer assets (icons, etc.)
│
├── src/                            # Application source code
├── pom.xml                         # Maven configuration
└── target/
    ├── mentor-1.0.0.jar            # Built JAR
    ├── libs/                       # Dependencies (36 JARs)
    ├── jpackage-input/             # Staging dir for jpackage (auto-created)
    │   ├── mentor-1.0.0.jar
    │   └── libs/
    └── installer/                  # Generated installers
        ├── Mentor.app              # macOS app (built locally)
        ├── Mentor-1.0.0.dmg        # macOS installer (built locally)
        └── Mentor-1.0.0.exe        # Windows installer (via GitHub Actions)
```

## Usage

### Build Everything (macOS)

```bash
cd build-scripts
./build.sh
```

This will:
1. ✅ Build macOS `.app` and `.dmg` locally
2. ✅ Trigger Windows `.exe` build via GitHub Actions (optional)

### Build macOS Only

```bash
cd build-scripts
./build-mac.sh
```

Output:
- `target/installer/Mentor.app`
- `target/installer/Mentor-1.0.0.dmg`

### Build Windows (on Windows machine)

```powershell
cd build-scripts
.\build-windows.ps1
```

Output:
- `target/installer/Mentor-1.0.0.exe`

### Build Windows via GitHub Actions

**Method 1: Via build.sh**
```bash
./build.sh
# Answer 'y' when prompted
```

**Method 2: GitHub CLI**
```bash
gh workflow run windows-build.yml
gh run watch
```

**Method 3: Web UI**
1. Go to GitHub repository
2. Click **Actions** tab
3. Select **Windows Build** workflow
4. Click **Run workflow** button

**Download artifact:**
1. Go to completed workflow run
2. Download `mentor-windows-installer` artifact
3. Extract `Mentor-1.0.0.exe`

## Key Features

✅ **Embedded JRE** - End users don't need Java installed  
✅ **Native installers** - Platform-specific .app/.dmg/.exe  
✅ **GitHub Actions** - Automated Windows builds  
✅ **Production ready** - Clean, commented, professional code  
✅ **One command** - `./build.sh` builds everything

## Scripts Overview

### build.sh
- **Purpose**: Main build orchestrator
- **Platform**: macOS only
- **Actions**:
  1. Runs `build-mac.sh` to create macOS installers
  2. Optionally triggers GitHub Actions for Windows build
  3. Uses `gh` CLI to trigger workflow
- **Requirements**: GitHub CLI (`brew install gh`) for auto-trigger

### build-mac.sh
- **Purpose**: Build macOS installers
- **Platform**: macOS only
- **Output**:
  - `Mentor.app` - App bundle with embedded JRE
  - `Mentor-1.0.0.dmg` - Disk image installer
- **Tools**: jpackage (included in JDK 17+)
- **Options**:
  - Embedded Java runtime
  - Memory: 256MB - 1024MB
  - App bundle signed (if certificates configured)

### build-windows.ps1
- **Purpose**: Build Windows installer
- **Platform**: Windows only
- **Output**:
  - `Mentor-1.0.0.exe` - Windows installer with embedded JRE
- **Tools**: jpackage + WiX Toolset
- **Features**:
  - Directory chooser dialog
  - Start Menu entry
  - Desktop shortcut
  - Embedded Java runtime

### windows-build.yml
- **Purpose**: GitHub Actions workflow for Windows builds
- **Trigger**: Manual dispatch or push to main
- **Steps**:
  1. Checkout code
  2. Setup Java 17
  3. Run `build-windows.ps1`
  4. Upload `.exe` as artifact
- **Output**: Downloadable artifact with 30-day retention

## Configuration

### App Metadata
Edit in all three build scripts:

```bash
APP_NAME="Mentor"
APP_VERSION="1.0.0"
VENDOR="Algo"
MAIN_CLASS="org.algo.mentor.Launcher"
MAIN_JAR="mentor-1.0.0.jar"
```

### JVM Memory Settings
Modify `--java-options` in jpackage commands:

```bash
--java-options "-Xmx1024m"  # Max heap
--java-options "-Xms256m"   # Initial heap
```

### Runtime Configuration
jpackage automatically:
- Includes all JARs from `--input` directory (target/)
- Detects required Java modules by analyzing bytecode
- Bundles JavaFX libraries from dependencies
- Creates minimal JRE with only needed modules

No need to specify `--add-modules` for most applications.

### Add Icons

1. Create icons:
   - macOS: `build-resources/icon.icns` (512x512)
   - Windows: `build-resources/icon.ico` (256x256)

2. Add to jpackage commands:
   ```bash
   # In build-mac.sh
   --icon build-resources/icon.icns
   
   # In build-windows.ps1
   --icon build-resources/icon.ico
   ```

## Dependencies (pom.xml)

The `pom.xml` is already configured with required plugins:

```xml
<!-- Maven JAR Plugin: Creates executable JAR -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <classpathPrefix>libs/</classpathPrefix>
                <mainClass>org.algo.mentor.Launcher</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>

<!-- Maven Dependency Plugin: Copies dependencies to libs/ -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/libs</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## GitHub Actions Setup

### Enable Actions
1. Push code to GitHub
2. Go to **Settings** → **Actions** → **General**
3. Select "Allow all actions and reusable workflows"
4. Save

### Trigger Workflow
The workflow can be triggered:
- ✅ Manually via Actions tab
- ✅ Via `gh` CLI: `gh workflow run windows-build.yml`
- ✅ Via `build.sh` script
- ✅ Automatically on push (uncomment in workflow)

### Monitor Progress
```bash
gh run list --workflow=windows-build.yml
gh run watch
```

## Troubleshooting

### Java not found
```bash
# Install Java 17
brew install openjdk@17  # macOS

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### jpackage not found
- jpackage is included in JDK 17+
- Verify: `jpackage --version`
- If missing, reinstall JDK 17

### Dependencies not copied
```bash
# Manually run Maven package
./mvnw clean package

# Verify libs directory
ls -la target/libs/
```

### GitHub CLI not found (macOS)
```bash
brew install gh
gh auth login
```

### Windows WiX not found
- WiX is automatically downloaded by jpackage on Windows
- Or install: `choco install wixtoolset`

## Advanced: Code Signing

### macOS Signing
```bash
# In build-mac.sh, add to jpackage:
--mac-sign \
--mac-signing-key-user-name "Developer ID Application: Your Name" \
--mac-signing-keychain "~/Library/Keychains/login.keychain-db"
```

### Windows Signing
```powershell
# In build-windows.ps1, add to jpackage:
--win-sign \
--win-signing-key-file "path/to/certificate.pfx" \
--win-signing-key-password "password"
```

### Notarization (macOS)
```bash
# After building .dmg
xcrun notarytool submit Mentor-1.0.0.dmg \
    --apple-id "your@email.com" \
    --password "app-specific-password" \
    --team-id "TEAM_ID"
```

## CI/CD Integration

### Auto-build on Release
Uncomment in `windows-build.yml`:
```yaml
on:
  push:
    branches:
      - main
      - release/*
```

### Create Release Workflow
Create `.github/workflows/release.yml`:
- Build all platforms
- Create GitHub Release
- Upload installers as assets
- Automatic versioning

## End User Experience

### macOS
1. Download `Mentor-1.0.0.dmg`
2. Open DMG
3. Drag `Mentor.app` to Applications
4. Launch from Applications or Spotlight
5. ✅ No Java installation required

### Windows
1. Download `Mentor-1.0.0.exe`
2. Run installer
3. Choose installation directory
4. Complete installation
5. Launch from Start Menu or Desktop
6. ✅ No Java installation required

## File Sizes (Approximate)

- **JAR only**: ~5 MB
- **macOS .app**: ~200 MB (includes JRE)
- **macOS .dmg**: ~100 MB (compressed)
- **Windows .exe**: ~120 MB (compressed, includes JRE)

## Summary

This build system provides:

1. ✅ **Single command** - `./build.sh` on macOS
2. ✅ **Native installers** - .app, .dmg, .exe
3. ✅ **Embedded JRE** - No Java required for end users
4. ✅ **GitHub Actions** - Automated Windows builds
5. ✅ **Production ready** - Clean, commented, professional
6. ✅ **Cross-platform** - macOS and Windows support
7. ✅ **Maintainable** - Clear separation of concerns

Run `./build-scripts/build.sh` to build everything!
