package org.algo.mentor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.algo.mentor.config.DatabaseManager;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.initialize();
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/main-layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setTitle("Mentor");
        stage.setScene(scene);
        stage.show();
        
        stage.setOnCloseRequest(event -> DatabaseManager.closeConnection());
    }
}

