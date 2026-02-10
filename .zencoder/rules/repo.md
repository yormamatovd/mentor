---
description: Repository Information Overview
alwaysApply: true
---

# Mentor - Student Management Platform

## Summary
Mentor is a JavaFX-based desktop application for student management and teaching platform. It provides functionality for managing students, groups, lessons, schedules, payments, and generating reports with PDF export capabilities. The application uses SQLite for data persistence and includes a complete build system for creating native installers with embedded JRE for both macOS and Windows platforms.

## Structure
**Main Directories:**
- **[./src/main/java/org/algo/mentor](./src/main/java/org/algo/mentor)**: Application source code
  - **controllers/**: JavaFX FXML controllers (Login, Dashboard, Students, Groups, Lessons, Schedule, Reports)
  - **services/**: Business logic layer (StudentService, GroupService, LessonService, ScheduleService, PaymentService, ReportService, PdfExportService, AuthService)
  - **models/**: Data models (Student, Group, Lesson, etc.)
  - **core/**: Core application utilities
  - **config/**: Configuration management
  - **util/**: Helper utilities
- **[./src/main/resources/org/algo/mentor](./src/main/resources/org/algo/mentor)**: Application resources
  - **views/**: FXML view files
  - **styles/**: CSS stylesheets
  - **icons**: Application icons (png, svg, ico, icns)
- **[./build-scripts](./build-scripts)**: Native installer build scripts
  - [build.sh](./build-scripts/build.sh): Main orchestrator (macOS)
  - [build-mac.sh](./build-scripts/build-mac.sh): macOS .app and .dmg builder
  - [build-windows.ps1](./build-scripts/build-windows.ps1): Windows .exe builder
- **[./build-resources](./build-resources)**: Installer assets (icons, WiX configurations)
- **[./.github/workflows](./.github/workflows)**: CI/CD workflows for automated Windows builds

## Language & Runtime
**Language**: Java  
**Version**: Java 17 (configured in [pom.xml:136](./pom.xml:136))  
**Build System**: Apache Maven 3.x  
**Package Manager**: Maven  
**Framework**: JavaFX 17.0.14  
**Architecture**: MVC pattern with FXML-based UI

## Dependencies
**Main Dependencies:**
- **JavaFX 17.0.14**: UI framework (controls, fxml, web, swing modules)
- **ControlsFX 11.2.1**: Enhanced JavaFX controls
- **FormsFX 11.6.0**: Form handling
- **ValidatorFX 0.6.1**: Form validation
- **Ikonli 12.3.1**: Icon library for JavaFX
- **BootstrapFX 0.4.0**: Bootstrap-style theming
- **TilesFX 21.0.9**: Dashboard tiles and widgets
- **SQLite JDBC 3.45.1.0**: Database connectivity
- **Logback 1.4.14**: Logging framework
- **iText 7.2.5**: PDF generation (kernel, io, layout modules)

**Development Dependencies:**
- **JUnit Jupiter 5.12.1**: Testing framework (test scope)

## Build & Installation
**Prerequisites:**
- Java 17+ (automatically bundled in final installers)
- Maven 3.x (or use included wrapper [./mvnw](./mvnw))

**Build Commands:**
```bash
# Clean and compile
./mvnw clean compile

# Package application with dependencies
./mvnw clean package

# Run application (development)
./mvnw javafx:run

# Alternative run
./mvnw exec:exec
```

**Create Native Installers:**
```bash
# macOS: Build .app and .dmg locally
cd build-scripts
./build.sh

# Windows: Build .exe installer (on Windows machine)
cd build-scripts
.\build-windows.ps1

# Windows: Via GitHub Actions (from any platform)
./build.sh
# Answer 'y' when prompted to trigger GitHub Actions
```

**Output Locations:**
- JAR: [./target/mentor-1.0.0.jar](./target/mentor-1.0.0.jar)
- Dependencies: [./target/libs/](./target/libs/)
- Installers: [./target/installer/](./target/installer/)
  - macOS: `Mentor.app`, `Mentor-1.0.0.dmg`
  - Windows: `Mentor-1.0.0.msi` (via GitHub Actions)

## Main Files
**Entry Points:**
- **Main Class**: `org.algo.mentor.Launcher` ([./src/main/java/org/algo/mentor/Launcher.java](./src/main/java/org/algo/mentor/Launcher.java))
- **Application Class**: `org.algo.mentor.HelloApplication` ([./src/main/java/org/algo/mentor/HelloApplication.java](./src/main/java/org/algo/mentor/HelloApplication.java))

**Configuration Files:**
- **[./pom.xml](./pom.xml)**: Maven project configuration
- **[./src/main/resources/logback.xml](./src/main/resources/logback.xml)**: Logging configuration (logs to `~/Documents/mentor/logs.log`)
- **[./mvnw](./mvnw)** / **[./mvnw.cmd](./mvnw.cmd)**: Maven wrapper scripts

**Build Documentation:**
- **[./BUILD_SYSTEM.md](./BUILD_SYSTEM.md)**: Comprehensive build system documentation
- **[./build-scripts/README.md](./build-scripts/README.md)**: Build scripts guide
- **[./build-resources/README.md](./build-resources/README.md)**: Icon generation and installer customization

## CI/CD
**GitHub Actions Workflow:**
- **[./.github/workflows/windows-build.yml](./.github/workflows/windows-build.yml)**: Automated Windows installer builds
  - Trigger: Manual dispatch via Actions tab or `gh workflow run windows-build.yml`
  - Platform: windows-latest
  - Java: Temurin 17
  - Output: Windows MSI installer uploaded as artifact (30-day retention)
  - Artifact name: `mentor-windows-installer`

## Testing
**Framework**: JUnit Jupiter 5.12.1  
**Maven Plugin**: maven-surefire-plugin 3.1.2

**Run Tests:**
```bash
./mvnw test
```

**Note**: No test files currently exist in [./src/test](./src/test) directory, but testing infrastructure is configured and ready to use.

## Application Features
Based on the controller and service architecture:
- **Authentication**: Login system with user authentication
- **Student Management**: CRUD operations for student records
- **Group Management**: Organize students into groups
- **Lesson Management**: Schedule and track lessons
- **Schedule Management**: Calendar and scheduling functionality
- **Payment Tracking**: Monitor student payments
- **Reports & Analytics**: Dashboard with analytics and PDF export capabilities

## Database
**Type**: SQLite (embedded database)  
**Driver**: sqlite-jdbc 3.45.1.0  
**Configuration**: Managed via application services layer

## Logging
**Framework**: Logback 1.4.14  
**Configuration**: [./src/main/resources/logback.xml](./src/main/resources/logback.xml)  
**Log Location**: `~/Documents/mentor/logs.log`  
**Log Level**: DEBUG (root level)  
**Appenders**: FILE and CONSOLE

## Platform-Specific Notes
**macOS Builds:**
- Requires macOS with Xcode Command Line Tools
- Creates .app bundle and .dmg installer
- Embedded JRE size: ~200MB (.app), ~100MB (.dmg compressed)

**Windows Builds:**
- Uses jpackage with WiX Toolset
- Creates MSI installer with installation wizard
- Features: directory chooser, Start Menu entry, desktop shortcut
- Embedded JRE size: ~120MB (compressed)
- Can be built locally on Windows or via GitHub Actions

**End User Requirements:**
- **NONE** - Installers include embedded Java Runtime Environment
- No Java installation required on target machines
