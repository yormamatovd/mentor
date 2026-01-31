# Mentor Repository

## Project Overview

**Mentor** is a comprehensive JavaFX desktop application designed for educational centers to manage students, groups, lessons, schedules, payments, and generate reports. Built with Java 17, JavaFX, and SQLite, it provides a complete solution for teaching management with native installers for macOS and Windows.

### Project Information
- **Group ID**: `org.algo`
- **Artifact ID**: `mentor`
- **Version**: `1.0.0`
- **Main Class**: `org.algo.mentor.Launcher`
- **Java Version**: `17`
- **Database**: SQLite (`mentor.db`)
- **UI Framework**: JavaFX 17.0.14

## Project Structure

```text
mentor/
├── .github/
│   └── workflows/
│       └── windows-build.yml          # GitHub Actions for automated Windows builds
│
├── build-resources/                    # Installer assets and icon files
│   ├── icon.svg                        # Source icon design
│   ├── icon.icns                       # macOS app icon
│   ├── icon.ico                        # Windows app icon
│   ├── icon.png                        # PNG icon (11.79 KB)
│   ├── generate-icon.sh                # Icon generation script (macOS/Linux)
│   ├── main.wxs.backup                 # WiX installer configuration backup
│   ├── README.md                       # Build resources documentation
│   └── INSTALLER_CUSTOMIZATION.md      # Advanced installer customization guide
│
├── build-scripts/                      # Build automation scripts
│   ├── build.sh                        # Main build orchestrator (macOS)
│   ├── build-mac.sh                    # macOS .app and .dmg builder
│   ├── build-windows.ps1               # Windows .exe builder
│   └── README.md                       # Build system documentation
│
├── src/
│   └── main/
│       ├── java/
│       │   └── org/algo/mentor/
│       │       ├── config/             # Configuration and database management
│       │       │   ├── DatabaseManager.java
│       │       │   └── AppDirectoryManager.java
│       │       │
│       │       ├── controllers/        # FXML Controllers (8 controllers)
│       │       │   ├── DashboardController.java
│       │       │   ├── GroupsController.java
│       │       │   ├── LessonsController.java
│       │       │   ├── LoginController.java
│       │       │   ├── MainController.java
│       │       │   ├── ReportsController.java
│       │       │   ├── ScheduleController.java
│       │       │   └── StudentsController.java
│       │       │
│       │       ├── core/               # Navigation system
│       │       │   ├── NavigableController.java
│       │       │   └── NavigationController.java
│       │       │
│       │       ├── models/             # Data models (14 entities)
│       │       │   ├── Attendance.java
│       │       │   ├── Group.java
│       │       │   ├── Homework.java
│       │       │   ├── Lesson.java
│       │       │   ├── LessonDetail.java
│       │       │   ├── Payment.java
│       │       │   ├── QuestionResult.java
│       │       │   ├── QuestionSession.java
│       │       │   ├── Schedule.java
│       │       │   ├── Student.java
│       │       │   ├── TestResult.java
│       │       │   ├── TestSession.java
│       │       │   └── User.java
│       │       │
│       │       ├── services/           # Business logic layer (8 services)
│       │       │   ├── AuthService.java
│       │       │   ├── GroupService.java
│       │       │   ├── LessonService.java
│       │       │   ├── PaymentService.java
│       │       │   ├── PdfExportService.java
│       │       │   ├── ReportService.java
│       │       │   ├── ScheduleService.java
│       │       │   └── StudentService.java
│       │       │
│       │       ├── HelloApplication.java    # Main JavaFX Application
│       │       └── Launcher.java            # Application entry point
│       │
│       └── resources/
│           ├── org/algo/mentor/
│           │   ├── styles/
│           │   │   └── custom.css           # Application styling (12.09 KB)
│           │   │
│           │   ├── views/                   # FXML layout files (10 views)
│           │   │   ├── dashboard-view.fxml
│           │   │   ├── groups-view.fxml
│           │   │   ├── hello-view.fxml
│           │   │   ├── lesson-detail-view.fxml
│           │   │   ├── lessons-view.fxml
│           │   │   ├── login-view.fxml
│           │   │   ├── main-layout.fxml
│           │   │   ├── reports-view.fxml
│           │   │   ├── schedule-view.fxml
│           │   │   └── students-view.fxml
│           │   │
│           │   ├── icon.icns               # Bundled macOS icon
│           │   ├── icon.ico                # Bundled Windows icon
│           │   ├── icon.png                # Bundled PNG icon
│           │   └── icon.svg                # Bundled SVG icon
│           │
│           └── logback.xml                  # Logging configuration
│
├── target/                                  # Build output directory
│   ├── mentor-1.0.0.jar                    # Application JAR
│   ├── libs/                               # Dependencies (36 JARs)
│   ├── jpackage-input/                     # Staging for native builds
│   └── installer/                          # Native installers
│       ├── Mentor.app                      # macOS app bundle
│       ├── Mentor-1.0.0.dmg                # macOS disk image installer
│       └── Mentor-1.0.0.exe                # Windows installer
│
├── mentor.db                               # SQLite database file
├── pom.xml                                 # Maven project configuration
├── mvnw                                    # Maven wrapper (Unix)
├── mvnw.cmd                                # Maven wrapper (Windows)
├── BUILD_SYSTEM.md                         # Build system documentation
├── CHANGES_SUMMARY.md                      # Recent changes and fixes
└── repo.md                                 # This file
```

## Core Components

### Application Entry Points

#### HelloApplication
- **File**: `src/main/java/org/algo/mentor/HelloApplication.java:15`
- **Purpose**: Main JavaFX application class
- **Responsibilities**:
  - Initializes `DatabaseManager` for database connectivity
  - Loads `main-layout.fxml` as the primary scene
  - Applies **BootstrapFX** styling
  - Sets window properties (resizable, title, size)
  - Handles database connection cleanup on application exit

#### Launcher
- **File**: `src/main/java/org/algo/mentor/Launcher.java`
- **Purpose**: Application entry point
- **Why**: Provides compatibility layer for JavaFX module system

### Configuration & Database

#### DatabaseManager
- **File**: `src/main/java/org/algo/mentor/config/DatabaseManager.java`
- **Purpose**: SQLite database lifecycle management
- **Features**:
  - Database connection pooling
  - Schema initialization and migrations
  - Table creation for all entities
  - Connection cleanup

#### AppDirectoryManager
- **File**: `src/main/java/org/algo/mentor/config/AppDirectoryManager.java`
- **Purpose**: Application directory and file system operations
- **Features**:
  - Platform-specific directory management
  - Configuration file handling
  - Resource path resolution

### Navigation System

#### NavigationController
- **File**: `src/main/java/org/algo/mentor/core/NavigationController.java`
- **Purpose**: Dynamic view switching and routing
- **Features**:
  - Loads FXML views dynamically
  - Manages content area updates
  - Controller lifecycle management
  - Passes navigation reference to child controllers

#### NavigableController
- **File**: `src/main/java/org/algo/mentor/core/NavigableController.java`
- **Purpose**: Interface for controllers that need navigation
- **Method**: `setNavigationController(NavigationController controller)`

## Controllers

All controllers are located in `src/main/java/org/algo/mentor/controllers/`.

### LoginController
- **View**: `login-view.fxml`
- **Purpose**: User authentication and access control
- **Features**:
  - Username/password validation
  - Session management
  - Error handling for invalid credentials

### MainController
- **View**: `main-layout.fxml`
- **Purpose**: Main application frame
- **Features**:
  - Sidebar navigation menu
  - Header with user information
  - Content area for dynamic views
  - Menu item highlighting

### DashboardController
- **View**: `dashboard-view.fxml`
- **Purpose**: Overview and statistics
- **Features**:
  - Key metrics display (using TilesFX)
  - Summary cards
  - Quick access links

### StudentsController
- **View**: `students-view.fxml`
- **Purpose**: Student record management
- **Features**:
  - Student CRUD operations
  - Search and filter functionality
  - Student details sidebar
  - Delete confirmation dialog with proper button layout
  - Payment tracking

### GroupsController
- **View**: `groups-view.fxml`
- **Purpose**: Student group management
- **Features**:
  - Group CRUD operations
  - Student assignment to groups
  - Deferred student removal (changes apply on Save)
  - Group roster display

### LessonsController
- **View**: `lessons-view.fxml` and `lesson-detail-view.fxml`
- **Purpose**: Lesson tracking and attendance
- **Features**:
  - Lesson creation and editing
  - Attendance tracking
  - Lesson history
  - Detailed lesson view with questions and homework

### ScheduleController
- **View**: `schedule-view.fxml`
- **Purpose**: Timetable and schedule management
- **Features**:
  - Calendar-based scheduling
  - Conflict detection
  - Schedule visualization

### ReportsController
- **View**: `reports-view.fxml`
- **Purpose**: Report generation and analytics
- **Features**:
  - Attendance reports
  - Performance analytics
  - Payment summaries
  - PDF export functionality

## Models

All models are located in `src/main/java/org/algo/mentor/models/`.

| Model | Description |
|-------|-------------|
| **Student** | Student personal information, contact details, enrollment data |
| **Group** | Group name, description, schedule, student assignments |
| **Lesson** | Lesson date, time, topic, group association |
| **LessonDetail** | Detailed lesson information including questions and homework |
| **Schedule** | Timetable entries with day, time, and recurrence |
| **Payment** | Payment records, amount, date, student association |
| **User** | Authentication credentials, user roles |
| **Attendance** | Lesson attendance tracking per student |
| **QuestionResult** | Student answers to lesson questions |
| **TestSession** | Test/quiz session metadata |
| **Homework** | Homework assignments and submissions |
| **QuestionSession** | Individual question session tracking |
| **TestResult** | Test scores and results |

## Services

All services are located in `src/main/java/org/algo/mentor/services/`.

### StudentService
- Student CRUD operations
- Student search and filtering
- Student enrollment management
- Payment history retrieval

### GroupService
- Group CRUD operations
- Student-to-group assignments
- Group roster management

### LessonService
- Lesson creation and scheduling
- Attendance recording
- Lesson history tracking

### ScheduleService
- Schedule CRUD operations
- Conflict detection
- Timetable generation

### PaymentService
- Payment recording
- Payment history
- Balance tracking

### AuthService
- User authentication
- Password validation
- Session management

### ReportService
- Data aggregation for reports
- Statistical calculations
- Report data formatting

### PdfExportService
- PDF document generation using iText
- Report formatting
- Export to file system

## Technologies & Dependencies

### UI Framework & Components
- **JavaFX 17.0.14**: Core UI framework
  - `javafx-controls`: UI controls
  - `javafx-fxml`: FXML support
  - `javafx-web`: WebView component
  - `javafx-swing`: Swing integration
- **ControlsFX 11.2.1**: Advanced UI controls and dialogs
- **FormsFX 11.6.0**: Form creation and binding
- **ValidatorFX 0.6.1**: Form validation
- **Ikonli 12.3.1**: Icon library for JavaFX
- **BootstrapFX 0.4.0**: Bootstrap-inspired CSS styling
- **TilesFX 21.0.9**: Dashboard tiles and visualizations

### Database
- **SQLite JDBC 3.45.1.0**: Embedded database driver

### PDF Generation
- **iText 7.2.5**: PDF document creation
  - `kernel`: Core PDF functionality
  - `io`: I/O operations
  - `layout`: Layout and formatting

### Logging
- **Logback 1.4.14**: Logging framework (SLF4J implementation)

### Testing
- **JUnit Jupiter 5.12.1**: Unit testing framework
  - `junit-jupiter-api`: Test API
  - `junit-jupiter-engine`: Test engine

## Build System

### Maven Configuration
The project uses Maven with the following key plugins:

- **Maven Compiler Plugin 3.13.0**: Java 17 compilation
- **JavaFX Maven Plugin 0.0.8**: JavaFX runtime and packaging
- **Exec Maven Plugin 3.1.0**: Direct application execution
- **Maven Surefire Plugin 3.1.2**: Test execution
- **Maven JAR Plugin 3.3.0**: JAR creation with manifest
- **Maven Dependency Plugin 3.6.1**: Dependency copying
- **Maven Resources Plugin 3.3.1**: Resource management

### Build Commands

#### Development
```bash
# Run application (with JavaFX)
./mvnw clean javafx:run

# Run application (with exec)
./mvnw exec:exec

# Compile only
./mvnw compile

# Run tests
./mvnw test
```

#### Production Build
```bash
# Build JAR with dependencies
./mvnw clean package

# Build macOS native installers
cd build-scripts && ./build-mac.sh

# Build Windows native installer (on Windows)
cd build-scripts
.\build-windows.ps1

# Build all (macOS + trigger Windows build via GitHub Actions)
cd build-scripts && ./build.sh
```

### Native Installers

The project includes a complete build system for creating native installers with embedded JRE.

#### Features
✅ **Embedded JRE** - End users don't need Java installed  
✅ **Native installers** - Platform-specific .app/.dmg/.exe  
✅ **GitHub Actions** - Automated Windows builds  
✅ **Custom icons** - Professional branding  
✅ **Desktop shortcuts** - Automatic shortcut creation  
✅ **Start Menu integration** - Windows Start Menu group  

#### Build Scripts
- **`build-scripts/build.sh`**: Main orchestrator (macOS)
- **`build-scripts/build-mac.sh`**: macOS .app and .dmg builder
- **`build-scripts/build-windows.ps1`**: Windows installer builder
- **`.github/workflows/windows-build.yml`**: GitHub Actions workflow

#### Installer Output
- **macOS**: `target/installer/Mentor.app` and `Mentor-1.0.0.dmg`
- **Windows**: `target/installer/Mentor-1.0.0.exe`

#### File Sizes
- JAR only: ~5 MB
- macOS .app: ~200 MB (includes JRE)
- macOS .dmg: ~100 MB (compressed)
- Windows .exe: ~120 MB (compressed, includes JRE)

See `BUILD_SYSTEM.md` for complete build system documentation.

## Application Features

### 📚 Student Management
- ✅ Add, edit, and delete student records
- ✅ Track personal information and contact details
- ✅ Manage student enrollments in groups
- ✅ View student payment history
- ✅ Search and filter students
- ✅ Delete confirmation with proper UI

### 👥 Group Management
- ✅ Create and manage student groups
- ✅ Assign students to groups
- ✅ Track group schedules
- ✅ Deferred student removal (applies on Save)
- ✅ View group rosters

### 📖 Lesson Management
- ✅ Schedule and track lessons
- ✅ Record attendance for each lesson
- ✅ Manage lesson topics and details
- ✅ Track questions and homework
- ✅ View lesson history

### 📅 Schedule Management
- ✅ Create and manage timetables
- ✅ Calendar-based schedule view
- ✅ Conflict detection
- ✅ Recurring schedule support

### 💰 Payment Tracking
- ✅ Record student payments
- ✅ Track payment history
- ✅ View payment summaries
- ✅ Generate payment reports

### 📊 Reporting & Analytics
- ✅ Attendance reports
- ✅ Performance analytics
- ✅ Payment summaries
- ✅ Export reports to PDF
- ✅ Dashboard with key metrics

### 🔐 Authentication
- ✅ User login system
- ✅ Session management
- ✅ Secure authentication

### 🎨 User Interface
- ✅ Modern, Bootstrap-inspired design
- ✅ Responsive layouts
- ✅ Dark/light color scheme
- ✅ Icon integration (Ikonli)
- ✅ Dashboard tiles and visualizations
- ✅ Sidebar navigation
- ✅ Custom CSS styling

## Recent Improvements

### UI/UX Enhancements
✅ **Delete Confirmation Dialog**: Fixed button layout (both "Yes" and "No" buttons now visible)  
✅ **Student Edit Sidebar**: Improved button sizing to prevent text truncation  
✅ **Group Edit Behavior**: Deferred student removal until Save is clicked  
✅ **Window Controls**: Ensured proper window decorations on Windows builds  

### Installer Improvements
✅ **Application Icons**: Professional icon design in multiple formats (SVG, ICNS, ICO, PNG)  
✅ **Desktop Shortcuts**: Automatically created on installation  
✅ **Start Menu Integration**: Organized under "Algo" group (Windows)  
✅ **Per-User Installation**: No admin rights required (Windows)  
✅ **Enhanced Descriptions**: Better app metadata in installers  

### Build System
✅ **Automated Windows Builds**: GitHub Actions workflow for CI/CD  
✅ **Icon Generation Scripts**: Automated icon conversion tools  
✅ **Comprehensive Documentation**: BUILD_SYSTEM.md and INSTALLER_CUSTOMIZATION.md  

See `CHANGES_SUMMARY.md` for detailed change history.

## Architecture & Design Patterns

### Layered Architecture
The application follows a clean layered architecture:
- **Presentation Layer**: FXML views and controllers
- **Business Logic Layer**: Service classes
- **Data Access Layer**: Database operations in services
- **Model Layer**: Entity classes

### Design Patterns
- **MVC Pattern**: Model-View-Controller separation
- **Service Layer Pattern**: Business logic encapsulation
- **DAO Pattern**: Data access abstraction (in services)
- **Singleton Pattern**: DatabaseManager
- **Observer Pattern**: JavaFX property binding

### Key Design Decisions
- **FXML for UI**: Separation of view and logic
- **Service Layer**: Centralized business logic and database operations
- **Navigation System**: Decoupled view switching via NavigationController
- **Custom CSS**: Consistent styling across the application
- **SQLite**: Lightweight, embedded database requiring no server

## Development Guidelines

### Code Organization
- Controllers handle UI logic only
- Services contain business logic and database operations
- Models are simple POJOs with properties
- FXML files define UI structure
- CSS files define styling

### Adding New Features
1. Create model class in `models/` if needed
2. Add database table in `DatabaseManager`
3. Create service class in `services/`
4. Design FXML view in `resources/views/`
5. Create controller in `controllers/`
6. Add navigation route in `MainController`

### Testing
- Unit tests use JUnit Jupiter
- Test service layer logic
- Mock database operations when appropriate
- Run tests with: `./mvnw test`

### Logging
- Configure logging in `src/main/resources/logback.xml`
- Use SLF4J API in code
- Log levels: ERROR, WARN, INFO, DEBUG, TRACE

## Prerequisites

### Development
- **Java 17** or higher
- **Maven 3.6+** (or use provided wrapper)
- **JavaFX 17** (automatically managed by Maven)

### Building Native Installers
- **macOS**:
  - JDK 17+ with jpackage
  - macOS 10.14+ for building
- **Windows**:
  - JDK 17+ with jpackage
  - WiX Toolset (automatically downloaded by jpackage)

## Database Schema

The application uses SQLite with the following main tables:
- `users`: User authentication
- `students`: Student records
- `groups`: Student groups
- `lessons`: Lesson records
- `lesson_details`: Detailed lesson information
- `schedules`: Timetable entries
- `payments`: Payment records
- `attendance`: Attendance tracking
- `question_results`: Quiz/test results
- `test_sessions`: Test session data
- `homework`: Homework assignments
- `question_sessions`: Question tracking
- `test_results`: Test scores

Schema is automatically initialized by `DatabaseManager` on first run.

## License

Not specified. Please add license information to this section.

## Contributors

Not specified. Please add contributor information to this section.

## Support & Contact

For questions, issues, or contributions, please contact the project maintainer.

---

**Last Updated**: January 29, 2026  
**Version**: 1.0.0  
**Status**: Production Ready
