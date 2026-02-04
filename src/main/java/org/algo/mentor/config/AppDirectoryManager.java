package org.algo.mentor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppDirectoryManager {
    private static final Logger logger = LoggerFactory.getLogger(AppDirectoryManager.class);
    private static final String APP_FOLDER_NAME = "mentor";
    private static Path appDirectory;
    private static Path databasePath;
    private static Path logsPath;

    public static void initialize() {
        try {
            logger.info("Initializing application directory manager");
            validateOperatingSystem();
            // Default initialization if not already set
            if (appDirectory == null) {
                createAppDirectory();
            }
            // Initialize logs at least
            logsPath = appDirectory.resolve("logs.log");
            if (!Files.exists(logsPath)) {
                Files.createFile(logsPath);
            }
            logger.info("Application directory initialized successfully at: {}", appDirectory);
        } catch (Exception e) {
            logger.error("Failed to initialize application directory", e);
            throw new RuntimeException("Failed to initialize application directory", e);
        }
    }

    public static void createDatabaseFile() throws IOException {
        if (appDirectory == null) return;
        databasePath = appDirectory.resolve("database.db");
        if (!Files.exists(databasePath)) {
            logger.info("Creating database file: {}", databasePath);
            Files.createFile(databasePath);
            logger.info("Database file created successfully");
        }
    }

    public static void setAppDirectory(Path path) {
        appDirectory = path;
        databasePath = appDirectory.resolve("database.db");
        logsPath = appDirectory.resolve("logs.log");
        logger.info("Application directory manually set to: {}", appDirectory);
    }

    public static boolean checkDatabaseExists(Path directory) {
        return Files.exists(directory.resolve("database.db"));
    }

    private static void validateOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        logger.debug("Detected operating system: {}", os);
        
        if (!os.contains("win") && !os.contains("mac")) {
            logger.error("Unsupported operating system detected: {}. Only Windows and macOS are supported.", os);
            throw new UnsupportedOperationException("This application only supports Windows and macOS operating systems.");
        }
        
        logger.info("Operating system validation passed: {}", os);
    }

    private static void createAppDirectory() throws IOException {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        
        Path documentsPath;
        if (os.contains("win")) {
            documentsPath = Paths.get(userHome, "Documents");
            logger.debug("Windows detected, using Documents path: {}", documentsPath);
        } else if (os.contains("mac")) {
            documentsPath = Paths.get(userHome, "Documents");
            logger.debug("macOS detected, using Documents path: {}", documentsPath);
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }

        if (!Files.exists(documentsPath)) {
            logger.warn("Documents folder does not exist at: {}. Creating it...", documentsPath);
            Files.createDirectories(documentsPath);
        }

        appDirectory = documentsPath.resolve(APP_FOLDER_NAME);
        
        if (!Files.exists(appDirectory)) {
            logger.info("Creating application directory: {}", appDirectory);
            Files.createDirectories(appDirectory);
            logger.info("Application directory created successfully");
        } else {
            logger.debug("Application directory already exists: {}", appDirectory);
        }
    }

    public static Path getAppDirectory() {
        return appDirectory;
    }

    public static Path getDatabasePath() {
        if (databasePath == null && appDirectory != null) {
            databasePath = appDirectory.resolve("database.db");
        }
        return databasePath;
    }

    public static Path getLogsPath() {
        return logsPath;
    }

    public static String getDatabaseUrl() {
        if (databasePath == null) {
            // Try to resolve it if appDirectory exists
            if (appDirectory != null) {
                databasePath = appDirectory.resolve("database.db");
            } else {
                throw new IllegalStateException("Database path not set and AppDirectory not initialized.");
            }
        }
        return "jdbc:sqlite:" + databasePath.toString();
    }
}
