# Mentor Repository

## Project Overview

**Mentor** is a JavaFX desktop application built with Java 17 and SQLite. It is a management system designed for educational centers to manage students, groups, lessons, schedules, payments, and generate reports.

- **Group ID**: `org.algo`
- **Artifact ID**: `mentor`
- **Version**: `1.0.0`
- **Java Version**: `17`
- **Database**: SQLite (`mentor.db`)

## Project Structure

```text
mentor/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/algo/mentor/
│       │       ├── config/             # Database and app directory configuration
│       │       │   ├── DatabaseManager.java
│       │       │   └── AppDirectoryManager.java
│       │       ├── controllers/        # FXML Controllers (7 controllers)
│       │       │   ├── LoginController.java
│       │       │   ├── MainController.java
│       │       │   ├── DashboardController.java
│       │       │   ├── StudentsController.java
│       │       │   ├── GroupsController.java
│       │       │   ├── LessonsController.java
│       │       │   ├── ScheduleController.java
│       │       │   └── ReportsController.java
│       │       ├── core/               # Navigation logic and interfaces
│       │       │   ├── NavigationController.java
│       │       │   └── NavigableController.java
│       │       ├── models/             # Data models (12 entities)
│       │       │   ├── Student.java
│       │       │   ├── Group.java
│       │       │   ├── Lesson.java
│       │       │   ├── LessonDetail.java
│       │       │   ├── Schedule.java
│       │       │   ├── Payment.java
│       │       │   ├── User.java
│       │       │   ├── Attendance.java
│       │       │   ├── QuestionResult.java
│       │       │   ├── TestSession.java
│       │       │   ├── Homework.java
│       │       │   ├── QuestionSession.java
│       │       │   └── TestResult.java
│       │       ├── services/           # Business logic and DAO layer (8 services)
│       │       │   ├── StudentService.java
│       │       │   ├── GroupService.java
│       │       │   ├── LessonService.java
│       │       │   ├── ScheduleService.java
│       │       │   ├── PaymentService.java
│       │       │   ├── AuthService.java
│       │       │   ├── ReportService.java
│       │       │   └── PdfExportService.java
│       │       ├── HelloApplication.java # Main JavaFX Application
│       │       └── Launcher.java       # Application Entry Point
│       └── resources/
│           └── org/algo/mentor/
│               ├── styles/             # CSS stylesheets
│               │   └── custom.css
│               └── views/              # FXML layout files (10 views)
│                   ├── login-view.fxml
│                   ├── main-layout.fxml
│                   ├── dashboard-view.fxml
│                   ├── students-view.fxml
│                   ├── groups-view.fxml
│                   ├── lessons-view.fxml
│                   ├── lesson-detail-view.fxml
│                   ├── schedule-view.fxml
│                   ├── reports-view.fxml
│                   └── hello-view.fxml
├── mentor.db                           # SQLite Database file
├── pom.xml                             # Maven Project Object Model
├── mvnw                                # Maven Wrapper (Unix)
└── mvnw.cmd                            # Maven Wrapper (Windows)
```

## Core Components

### HelloApplication
- **File**: `src/main/java/org/algo/mentor/HelloApplication.java`
- Initializes `DatabaseManager` and loads `main-layout.fxml`.
- Sets up the primary stage with **BootstrapFX** styling.
- Handles database connection closure on application exit.

### Launcher
- **File**: `src/main/java/org/algo/mentor/Launcher.java`
- Application entry point that launches `HelloApplication`.

### DatabaseManager
- **File**: `src/main/java/org/algo/mentor/config/DatabaseManager.java`
- Manages SQLite database connectivity and table initialization.
- Handles database schema creation and migrations.

### AppDirectoryManager
- **File**: `src/main/java/org/algo/mentor/config/AppDirectoryManager.java`
- Manages application directories and file system operations.

### Navigation (Core)
- **NavigationController**: (`src/main/java/org/algo/mentor/core/NavigationController.java`) Manages dynamic view switching within the main application layout.
- **NavigableController**: (`src/main/java/org/algo/mentor/core/NavigableController.java`) Interface that allows controllers to receive a reference to the `NavigationController`.

## Controllers
Located in `src/main/java/org/algo/mentor/controllers/`:
- **LoginController**: Handles user authentication and access.
- **MainController**: Manages the sidebar, header, and the central content area.
- **DashboardController**: Displays summary statistics and overview data.
- **StudentsController**: Manages student records (CRUD operations).
- **GroupsController**: Manages student groups and assignments.
- **LessonsController**: Tracks lesson attendance and scheduling.
- **ScheduleController**: Manages scheduling and timetables.
- **ReportsController**: Generates and displays various performance and attendance reports.

## Services & Models
The application follows a layered architecture with clear separation between models and services.

### Models
Located in `src/main/java/org/algo/mentor/models/`:
- **Student**: Student information and records
- **Group**: Student group data
- **Lesson**: Lesson information
- **LessonDetail**: Detailed lesson data
- **Schedule**: Scheduling and timetable data
- **Payment**: Payment and transaction records
- **User**: User authentication data
- **Attendance**: Attendance tracking
- **QuestionResult**: Quiz/test question results
- **TestSession**: Test session data
- **Homework**: Homework assignments
- **QuestionSession**: Question session tracking
- **TestResult**: Test result data

### Services
Located in `src/main/java/org/algo/mentor/services/`:
- **StudentService**: Student CRUD operations and business logic
- **GroupService**: Group management operations
- **LessonService**: Lesson management and tracking
- **ScheduleService**: Schedule management operations
- **PaymentService**: Payment processing and tracking
- **AuthService**: Authentication and authorization
- **ReportService**: Report generation and data aggregation
- **PdfExportService**: PDF export functionality

## Technologies & Dependencies

### Frameworks & UI
- **JavaFX 17.0.14**: Core UI framework (`controls`, `fxml`, `web`, `swing`)
- **BootstrapFX 0.4.0**: Bootstrap-style CSS for JavaFX
- **ControlsFX 11.2.1**: Advanced UI controls
- **FormsFX 11.6.0**: Form creation and management
- **Ikonli 12.3.1**: Icon packs for JavaFX
- **TilesFX 21.0.9**: Dashboard-style tiles
- **ValidatorFX 0.6.1**: Form validation

### Persistence & Data
- **SQLite JDBC 3.45.1.0**: Driver for SQLite database

### PDF Generation
- **iText PDF 7.2.5**: PDF document generation
  - Kernel
  - IO
  - Layout

### Logging
- **Logback 1.4.14**: Logging framework

### Testing
- **JUnit Jupiter 5.12.1**: Unit testing framework

## Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use the provided wrapper)

### Commands
- **Build**: `./mvnw clean install`
- **Run**: `./mvnw clean javafx:run`
- **Package**: `./mvnw clean package`
- **Execute**: `./mvnw exec:exec`
- **Test**: `./mvnw test`

### Build Plugins
- **Maven Compiler Plugin 3.13.0**: Java compilation
- **JavaFX Maven Plugin 0.0.8**: JavaFX application packaging
- **Exec Maven Plugin 3.1.0**: Application execution
- **Maven Surefire Plugin 3.1.2**: Test execution
- **Maven JAR Plugin 3.3.0**: JAR file creation with manifest
- **Maven Dependency Plugin 3.6.1**: Dependency management and copying
- **Maven Resources Plugin 3.3.1**: Resource copying

## Features

### Student Management
- Add, edit, and delete student records
- Track student information and performance
- Manage student enrollments in groups

### Group Management
- Create and manage student groups
- Assign students to groups
- Track group activities

### Lesson Management
- Schedule and track lessons
- Record attendance
- Manage lesson details

### Schedule Management
- Create and manage timetables
- Track scheduling conflicts
- View calendar-based schedules

### Payment Tracking
- Record payments
- Track payment history
- Generate payment reports

### Reporting
- Generate attendance reports
- Export reports to PDF format
- View performance analytics

## Development Notes
- The project uses a **Service Layer** to encapsulate database operations
- **FXML** is used for UI layouts, located in `src/main/resources`
- The `NavigationController` provides a decoupled way to switch between different views in the main layout
- Custom CSS styling is applied via `custom.css`
- Logging configuration is managed through `logback.xml`
- The application supports PDF export functionality for reports
- Database operations are centralized through service classes
