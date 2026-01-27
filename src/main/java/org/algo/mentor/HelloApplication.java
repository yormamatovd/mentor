package org.algo.mentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.algo.mentor.config.AppDirectoryManager;
import org.algo.mentor.config.DatabaseManager;
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
        Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setTitle("Mentor");
        stage.setScene(scene);
        logger.info("Showing main application window");
        stage.show();
        
        stage.setOnCloseRequest(event -> {
            logger.info("Application shutdown requested");
            DatabaseManager.closeConnection();
            logger.info("Application terminated successfully");
        });
    }
}

