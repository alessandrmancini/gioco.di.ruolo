package it.unicam.cs.mpgc.rpg125715;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RpgApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                RpgApplication.class.getResource("game-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
                RpgApplication.class.getResource("game.css").toExternalForm());

        stage.setTitle("Conquest Game");
        stage.setScene(scene);
        stage.show();
    }
}