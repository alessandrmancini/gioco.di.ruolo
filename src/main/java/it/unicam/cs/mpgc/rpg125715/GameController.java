package it.unicam.cs.mpgc.rpg125715;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class GameController {
    @FXML
    private Label statusLabel;

    private Game game;

    private final GameInitializationService gameInitializationService;
    private final BotDecisionService botDecisionService;
    private final BotTurnService botTurnService;
    private final TurnService turnService;
    private final RecruitmentService recruitmentService;

    public GameController() {

        IdGenerator idGenerator = new IdGenerator();

        GameSetUpService gameSetUpService = new GameSetUpService(idGenerator);
        MapSetupService mapSetupService = new MapSetupService(gameSetUpService);
        ArmyService armyService = new ArmyService(idGenerator);
        RibellioneService ribellioneService = new RibellioneService();
        RecruitmentService recruitmentService = new RecruitmentService();

        this.gameInitializationService = new GameInitializationService(gameSetUpService, mapSetupService, armyService);
        this.turnService = new TurnService(ribellioneService);
        this.botDecisionService = new BotDecisionService();
        this.botTurnService = new BotTurnService(botDecisionService, turnService, recruitmentService);
        this.recruitmentService = recruitmentService;
    }

    @FXML
    private void initialize(){
        statusLabel.setText("Premi 'Nuova partita' per iniziare");
    }

    @FXML
    protected void onNuovaPartitaClick() {
        try {
            //creazione giocatori
            game = gameInitializationService.creaPartitaBase(configurazioneBase());

            statusLabel.setText(creaMessaggioStatoPartita());

        }catch(Exception e){
            statusLabel.setText("Errore: "+e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    protected void onTurnoBotClick(){
        try{
            controllaPartitaCreata();

            Player currentPlayer = game.getCurrentPlayer();

            if(currentPlayer.getKind() != PlayerKind.BOT){
                statusLabel.setText("Il giocatore corrente non è un bot. \n" + "Giocatore corrente: "+ currentPlayer.getName());
                return;
            }

            String esitoBot = botTurnService.eseguiTurnoBot(game);

            statusLabel.setText(esitoBot+ "\n\n" + creaMessaggioStatoPartita());
        }catch(Exception e){
            statusLabel.setText("Errore bot: "+e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    protected void onFineTurnoClick(){
        try{
            controllaPartitaCreata();

            turnService.fineTurno(game);

            statusLabel.setText("Turno terminato\n\n" + creaMessaggioStatoPartita());
        } catch (Exception e) {
            statusLabel.setText("Errore fine turno: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void controllaPartitaCreata(){
        if(game == null){throw new IllegalArgumentException("devi prima creare una partita");}
    }

    private String creaMessaggioStatoPartita(){

        if (game == null){return "Nessuna partita creata";}

        Player currentPlayer = game.getCurrentPlayer();
        String elencoGiocatori = creaElencoGiocatori();

        String nomeCapitale = currentPlayer.hasCapitale() ? currentPlayer.getCapitale().getName() : "Nessuna";

        return "Partita creata \n"+
                "Giocatore corrente: "+currentPlayer.getName()+"\n"+
                "Tipo giocatore corrente: " + currentPlayer.getKind() + "\n" +
                "Numero città: " + currentPlayer.numeroCitta() + "\n" +
                "Capitale: " + nomeCapitale + "\n" +
                "Oro: " + currentPlayer.getOro() + "\n" +
                "Giocatori creati:\n" +
                elencoGiocatori;
    }

    private String creaElencoGiocatori(){
        String elencoGiocatori = "";

        for(Player player : game.getPlayers()){
            elencoGiocatori = elencoGiocatori
                    + "- "
                    +player.getName()
                    +" ("
                    +player.getLeader()
                    +", "
                    +player.getKind()
                    +")\n";
        }
        return elencoGiocatori;
    }

    private List<GameInitializationService.PlayerConfig> configurazioneBase(){
        return List.of(
                new GameInitializationService.PlayerConfig(
                        "Giocatore 1", LeaderType.ALESSANDRO_MAGNO,10, PlayerKind.HUMAN),
                new GameInitializationService.PlayerConfig(
                        "Giocatore 2", LeaderType.ANNIBALE,10, PlayerKind.BOT));
    }
    public Game getGame() {return game;}
}
