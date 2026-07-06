package it.unicam.cs.mpgc.rpg125715;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class GameController {
    @FXML
    private Label statusLabel;

    private Game game;
    private final GameInitializationService gameInitializationService;

    public GameController() {
        IdGenerator idGenerator = new IdGenerator();

        GameSetUpService gameSetUpService = new GameSetUpService(idGenerator);
        MapSetupService mapSetupService = new MapSetupService(gameSetUpService);
        ArmyService armyService = new ArmyService(idGenerator);

        this.gameInitializationService = new GameInitializationService(gameSetUpService, mapSetupService, armyService);
    }
    @FXML
    protected void onNuovaPartitaClick() {
        try {
            //creazione giocatori
            game = gameInitializationService.creaPartitaBase(configurazioneBase());

            statusLabel.setText("Partita creata. Giocatore corrente: "+game.getCurrentPlayer().getName()+", locations: "+game.getLocations().size());
        }catch(Exception e){
            statusLabel.setText("Errore: "+e.getMessage());
            e.printStackTrace();
        }
    }
    private List<GameInitializationService.PlayerConfig> configurazioneBase(){
        return List.of(
                new GameInitializationService.PlayerConfig(
                        "Giocatore 1", LeaderType.ALESSANDRO_MAGNO,10),
                new GameInitializationService.PlayerConfig(
                        "Giocatore 2", LeaderType.ANNIBALE,10));
    }
    public Game getGame() {return game;}
}
