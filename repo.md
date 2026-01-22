# Mentor Repository

## Project Overview

**Mentor** is a JavaFX desktop application built with Java 17 and SQLite. It is a management system for educational centers, allowing users to manage students, groups, lessons, and payments.

- **Group ID**: `org.algo`
- **Artifact ID**: `mentor`
- **Version**: `1.0-SNAPSHOT`
- **Java Version**: `17`
- **Database**: SQLite (`mentor.db`)

## Project Structure

```text
mentor/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/algo/mentor/
│   │   │       ├── config/             # Database configuration
│   │   │       ├── controllers/        # FXML Controllers
│   │   │       ├── core/               # Navigation and core logic
│   │   │       ├── models/             # Data models
│   │   │       ├── services/           # Business logic and DB access
│   │   │       ├── HelloApplication.java
│   │   │       └── Launcher.java
│   │   └── resources/
│   │       └── org/algo/mentor/
│   │           ├── styles/             # CSS files
│   │           └── views/              # FXML layouts
│   └── test/                           # Unit tests
├── mentor.db                           # SQLite Database
├── pom.xml                             # Maven configuration
├── mvnw                                # Maven Wrapper (Unix)
└── mvnw.cmd                            # Maven Wrapper (Windows)
```

## Core Components

### HelloApplication
- **File**: `src/main/java/org/algo/mentor/HelloApplication.java`
- Initializes `DatabaseManager` and loads the main layout (`main-layout.fxml`).
- Sets up the primary stage with BootstrapFX styling.

### DatabaseManager
- **File**: `src/main/java/org/algo/mentor/config/DatabaseManager.java`
- Handles SQLite database connection and table initialization.

### NavigationController
- **File**: `src/main/java/org/algo/mentor/core/NavigationController.java`
- Manages view switching within the application.

## Controllers
Located in `src/main/java/org/algo/mentor/controllers/`:
- **LoginController**: Handles user authentication.
- **MainController**: Manages the sidebar and content area.
- **DashboardController**: Displays overview statistics.
- **StudentsController**: CRUD operations for student management.
- **GroupsController**: Management of student groups.
- **LessonsController**: Scheduling and tracking lessons.

## Services & Models
The application follows a service-oriented architecture for data access:
- **Models**: `Student`, `Group`, `Lesson`, `LessonDetail`, `Payment`, `User`.
- **Services**: `StudentService`, `GroupService`, `LessonService`, `PaymentService`, `AuthService`.

## Technologies & Dependencies

### JavaFX Stack
- **javafx-controls** (17.0.14)
- **javafx-fxml** (17.0.14)
- **javafx-web** (17.0.14)
- **javafx-swing** (17.0.14)

### UI Enhancement
- **ControlsFX** (11.2.1)
- **FormsFX** (11.6.0)
- **ValidatorFX** (0.6.1)
- **Ikonli** (12.3.1)
- **BootstrapFX** (0.4.0)
- **TilesFX** (21.0.9)

### Persistence & Logging
- **SQLite JDBC** (3.45.1.0)
- **SLF4J Simple** (1.7.36)

### Testing
- **JUnit 5** (5.12.1)

## Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Building
```bash
./mvnw clean install
```

### Running the Application
```bash
./mvnw clean javafx:run
```

### Running Tests
```bash
./mvnw test
```

## Development Notes
- The project uses **SQLite** for local data persistence.
- **BootstrapFX** is used for modern UI styling.
- Navigation is centralized through a `NavigationController` that dynamically loads FXML views into the main layout.
- The application uses a service layer to abstract database operations from the UI controllers.
