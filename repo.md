# Mentor Repository

## Project Overview

**Mentor** is a JavaFX desktop application built with Java 17 and SQLite. It is a management system designed for educational centers to manage students, groups, lessons, and payments.

- **Group ID**: `org.algo`
- **Artifact ID**: `mentor`
- **Version**: `1.0-SNAPSHOT`
- **Java Version**: `17`
- **Database**: SQLite (`mentor.db`)

## Project Structure

```text
mentor/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/algo/mentor/
│       │       ├── config/             # Database configuration
│       │       ├── controllers/        # FXML Controllers
│       │       ├── core/               # Navigation logic and interfaces
│       │       ├── models/             # Data models (Entities)
│       │       ├── services/           # Business logic and DAO layer
│       │       ├── HelloApplication.java # Main JavaFX Application
│       │       └── Launcher.java       # Application Entry Point
│       └── resources/
│           └── org/algo/mentor/
│               ├── styles/             # CSS stylesheets
│               └── views/              # FXML layout files
├── core/                               # (Redundant) Placeholder directory
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

### DatabaseManager
- **File**: `src/main/java/org/algo/mentor/config/DatabaseManager.java`
- Manages SQLite database connectivity and table initialization.

### Navigation (Core)
- **NavigationController**: (`src/main/java/org/algo/mentor/core/NavigationController.java`) Manages dynamic view switching within the main application layout.
- **NavigableController**: (`src/main/java/org/algo/mentor/core/NavigableController.java`) Interface that allows controllers to receive a reference to the `NavigationController`.

## Controllers
Located in `src/main/java/org/algo/mentor/controllers/`:
- **LoginController**: Handles user authentication and access.
- **MainController**: Manages the sidebar, header, and the central content area.
- **DashboardController**: Displays summary statistics and overview data.
- **StudentsController**: Manages student records (CRUD).
- **GroupsController**: Manages student groups and assignments.
- **LessonsController**: Tracks lesson attendance and scheduling.
- **ReportsController**: Generates and displays various performance and attendance reports.

## Services & Models
The application follows a layered architecture:

### Models
Located in `src/main/java/org/algo/mentor/models/`:
- `Student`, `Group`, `Lesson`, `LessonDetail`, `Payment`, `User`, `Attendance`, `QuestionResult`, `TestSession`, `Homework`, `QuestionSession`, `TestResult`.

### Services
Located in `src/main/java/org/algo/mentor/services/`:
- `StudentService`, `GroupService`, `LessonService`, `PaymentService`, `AuthService`, `ReportService`.

## Technologies & Dependencies

### Frameworks & UI
- **JavaFX 17**: Core UI framework (`controls`, `fxml`, `web`, `swing`).
- **BootstrapFX**: Bootstrap-style CSS for JavaFX.
- **ControlsFX**: Advanced UI controls.
- **FormsFX**: Simple way to create forms.
- **Ikonli**: Icon packs for JavaFX.
- **TilesFX**: Dashboard-style tiles.
- **ValidatorFX**: Form validation.

### Persistence
- **SQLite JDBC**: Driver for SQLite database.
- **SLF4J**: Simple logging facade.

## Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use the provided wrapper)

### Commands
- **Build**: `./mvnw clean install`
- **Run**: `./mvnw clean javafx:run`
- **Package**: `./mvnw clean package`

## Development Notes
- The project uses a **Service Layer** to encapsulate database operations.
- **FXML** is used for UI layouts, located in `src/main/resources`.
- The `NavigationController` provides a decoupled way to switch between different views in the main layout.
