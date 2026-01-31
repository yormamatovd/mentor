package org.algo.mentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.algo.mentor.config.AppDirectoryManager;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.util.ScrollSpeedFix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);
    
    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting Mentor application");
        AppDirectoryManager.initialize();
        DatabaseManager.initialize();
        
        logger.debug("Loading main layout FXML");
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/main-layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        
        ScrollSpeedFix.applyScrollSpeedFix(scene.getRoot());
        
        stage.setTitle("Mentor");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setMaximized(false);
        
        try {
            stage.getIcons().add(new javafx.scene.image.Image(
                HelloApplication.class.getResourceAsStream("icon.png")
            ));
        } catch (Exception e) {
            logger.warn("Application icon not found: {}", e.getMessage());
        }
        
        logger.info("Showing main application window");
        stage.show();
        
        stage.setOnCloseRequest(event -> {
            logger.info("Application shutdown requested");
            DatabaseManager.closeConnection();
            logger.info("Application terminated successfully");
        });
    }
}

