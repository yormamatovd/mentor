# Build System

Professional build system for creating native installers with embedded JRE.

## Quick Start

### macOS

```bash
cd build-scripts
./build.sh
```

This will:
1. Build `.app` and `.dmg` installers locally
2. Optionally trigger Windows build via GitHub Actions

### Windows

```powershell
cd build-scripts
.\build-windows.ps1
```

## Prerequisites

### macOS
- Java 17+
- Maven (or use included wrapper)
- Xcode Command Line Tools

### Windows
- Java 17+
- Maven (or use included wrapper)
- WiX Toolset (automatically used by jpackage)

## Directory Structure

```
build-scripts/
├── build.sh              # Main orchestrator (macOS)
├── build-mac.sh          # macOS installer builder
├── build-windows.ps1     # Windows installer builder
└── README.md             # This file

.github/
└── workflows/
    └── windows-build.yml # GitHub Actions for Windows

build-resources/          # Installer assets (icons, etc.)

target/
└── installer/            # Generated installers
    ├── Mentor.app        # macOS app bundle
    ├── Mentor-1.0.0.dmg  # macOS installer
    └── Mentor-1.0.0.exe  # Windows installer (via Actions)
```

## Scripts

### build.sh
Main orchestrator that:
- Builds macOS installers locally
- Triggers Windows build via GitHub Actions (optional)

### build-mac.sh
Creates macOS installers:
- `.app` bundle with embedded JRE
- `.dmg` disk image

### build-windows.ps1
Creates Windows installer:
- `.exe` installer with embedded JRE
- Adds Start Menu shortcuts
- Adds desktop shortcut
- Allows directory selection

## GitHub Actions

### Setup
1. Push code to GitHub
2. Go to repository **Settings** → **Actions** → **General**
3. Enable "Allow all actions and reusable workflows"

### Trigger Windows Build

**Option 1: Via build.sh**
```bash
./build.sh
# Answer 'y' when prompted
```

**Option 2: GitHub CLI**
```bash
gh workflow run windows-build.yml
gh run list --workflow=windows-build.yml
```

**Option 3: Web UI**
1. Go to **Actions** tab
2. Select **Windows Build** workflow
3. Click **Run workflow**

### Download Artifacts
1. Go to **Actions** tab
2. Click on completed workflow run
3. Download **mentor-windows-installer** artifact
4. Extract and test `.exe` installer

## Output

### macOS
- **Location**: `target/installer/`
- **Files**:
  - `Mentor.app` - Application bundle
  - `Mentor-1.0.0.dmg` - Installer

### Windows (via GitHub Actions)
- **Location**: GitHub Actions artifacts
- **Files**:
  - `Mentor-1.0.0.exe` - Installer

## End User Requirements

**NONE** - Installers include embedded JRE

Users do NOT need Java installed.

## Customization

### App Metadata
Edit variables in scripts:
- `APP_NAME`
- `APP_VERSION`
- `VENDOR`

### JVM Options
Modify `--java-options` in jpackage commands:
- `-Xmx1024m` - Maximum heap
- `-Xms256m` - Initial heap

### Runtime Bundling
jpackage automatically:
- Analyzes bytecode to detect required modules
- Includes all dependencies from `target/libs/`
- Creates minimal JRE with only needed components
- No need to manually specify modules

### Windows Installer Options
In `build-windows.ps1`:
- `--win-dir-chooser` - Let user choose install directory
- `--win-menu` - Add Start Menu entry
- `--win-shortcut` - Add desktop shortcut

### Icons
Place icons in `build-resources/`:
- macOS: `icon.icns`
- Windows: `icon.ico`

Add to jpackage commands:
```bash
--icon build-resources/icon.icns  # macOS
--icon build-resources/icon.ico   # Windows
```

## Troubleshooting

### "jpackage: command not found"
- Ensure Java 17+ is installed
- Verify `JAVA_HOME` is set
- jpackage is included in JDK 17+

### "Dependencies not found"
- Check `pom.xml` has `maven-dependency-plugin`
- Run `mvn package` manually first
- Verify `target/libs/` exists

### GitHub Actions failing
- Check Java 17 is configured
- Verify all files committed
- Check workflow logs for details

### Windows .exe won't run
- Verify WiX Toolset installed on build machine
- Check Windows Defender/antivirus
- Test on clean Windows VM

## CI/CD Integration

### Automatic Builds
Uncomment in `.github/workflows/windows-build.yml`:
```yaml
on:
  push:
    branches:
      - main
      - release/*
```

### Release Workflow
Create `.github/workflows/release.yml` for:
- Build all platforms
- Create GitHub Release
- Upload installers as release assets

## Security

- Installers are unsigned by default
- For production:
  - macOS: Sign with Apple Developer certificate
  - Windows: Sign with Code Signing certificate
- Add signing to jpackage:
  ```bash
  --mac-sign              # macOS
  --win-sign              # Windows
  ```
