package it.unicam.cs.mpgc.rpg125715;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RpgApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(RpgApplication.class.getResource("game-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setTitle("Conquest Game");
        stage.setScene(scene);
        stage.show();
    }
}
