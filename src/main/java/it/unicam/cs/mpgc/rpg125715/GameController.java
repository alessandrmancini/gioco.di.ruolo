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

            statusLabel.setText(creaMessaggioPartita());
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

    private String creaMessaggioPartita(){
        Player currentPlayer = game.getCurrentPlayer();
        int numeroCitta = currentPlayer.getTerritorio().getCities().size();
        String nomeCapitale = currentPlayer.getCapitale().getName();

        String elencoGiocatori = "";
        for(Player player : game.getPlayers()){
            elencoGiocatori = elencoGiocatori
                    + "- "
                    + player.getName()
                    +" ("+player.getLeader()
                    +") \n";
        }

        return "Partita creata \n"+
                "Giocatore corrente: "+game.getCurrentPlayer().getName()+"\n"+
                ", numero città: "+game.getCurrentPlayer().getTerritorio().getCities().size()+"\n"+
                "Giocatori creati:\n"+ elencoGiocatori;
    }

    public Game getGame() {return game;}
}
