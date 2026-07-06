package it.unicam.cs.mpgc.rpg125715;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GameController {
    @FXML
    private Label statusLabel;

    @FXML
    protected void onNuovaPartitaClick() {
        statusLabel.setText("Nuova partita in preparazione");
    }
}
