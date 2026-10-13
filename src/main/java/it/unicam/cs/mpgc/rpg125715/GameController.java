package it.unicam.cs.mpgc.rpg125715;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CONTROLLER: collega gli eventi della vista (click) ai servizi del modello.
 * Partecipano sempre tutti i leader: prima di iniziare si sceglie quanti sono controllati da
 * giocatori umani (a turno, sullo stesso computer) e quali leader usano; gli altri sono bot.
 */
public class GameController {

    private static final int ORO_INIZIALE = 10;

    // ---- elementi della vista (fx:id) ----
    @FXML private ScrollPane mapScroll;
    @FXML private Label turnLabel;
    @FXML private VBox playersBox;
    @FXML private Label selectionLabel;
    @FXML private ComboBox<UnitType> unitCombo;
    @FXML private ComboBox<CitySpecialization> specCombo;
    @FXML private Button recruitButton;
    @FXML private Button upgradeButton;
    @FXML private Button specButton;
    @FXML private Button endTurnButton;
    @FXML private TextArea logArea;

    // schermata di impostazione partita
    @FXML private Pane setupOverlay;
    @FXML private Spinner<Integer> humanSpinner;
    @FXML private VBox humansBox;
    @FXML private Button cancelSetupButton;
    @FXML private Button undoButton;
    @FXML private Button newArmyButton;
    @FXML private Button foundCityButton;
    @FXML private Button skipBotButton;
    @FXML private Label rebellionLabel;
    @FXML private Label abilityLabel;

    private MapView mapView;

    // ---- stato di interazione ----
    private Game game;
    private Location selectedLocation;
    private Army selectedArmy;
    private final Set<Location> targets = new HashSet<>();
    private boolean fineMostrata;
    private final List<ComboBox<LeaderType>> humanLeaderCombos = new ArrayList<>();
    private boolean aggiornandoCombo;
    private Army ultimoEsercitoMosso;
    private Location ultimaPartenza;
    private ArmyService armyService;
    private int roundMostrato;

    // bot visibili passo per passo
    private static final int PAUSA_BOT_MS = 900;
    private boolean saltaBot;
    private Player botCorrente;
    private Location animazioneDa;
    private Location animazioneA;
    private Color animazioneColore;
    private final Set<Player> sconfittiMostrati = new HashSet<>();

    // ---- servizi del modello (ricreati a ogni nuova partita) ----
    private MovementService movementService;
    private RecruitmentService recruitmentService;
    private ConstructionService constructionService;
    private TurnService turnService;
    private BotTurnService botTurnService;
    private ConquestService conquestService;
    private RibellioneService ribellioneService;
    private GameInitializationService gameInitializationService;

    public GameController() {
        creaServizi();
    }

    private void creaServizi() {
        IdGenerator idGenerator = new IdGenerator();
        DiceService diceService = new DiceService();
        BattleService battleService = new BattleService(diceService);
        this.armyService = new ArmyService(idGenerator);
        this.conquestService = new ConquestService();
        this.movementService = new MovementService(battleService, armyService, conquestService);
        this.recruitmentService = new RecruitmentService();
        this.constructionService = new ConstructionService();
        this.ribellioneService = new RibellioneService(conquestService);
        this.turnService = new TurnService(ribellioneService);
        BotDecisionService botDecisionService = new BotDecisionService(constructionService);
        this.botTurnService = new BotTurnService(botDecisionService, turnService, recruitmentService, movementService, constructionService, armyService, conquestService);
        GameSetUpService gameSetUpService = new GameSetUpService(idGenerator);
        MapSetupService mapSetupService = new MapSetupService(gameSetUpService);
        this.gameInitializationService = new GameInitializationService(gameSetUpService, mapSetupService, armyService);
    }

    @FXML
    private void initialize() {
        // mappa: si adatta alla finestra; Ctrl + rotella per ingrandire, trascinando ci si sposta
        mapView = new MapView(this::onLocationClicked);
        mapScroll.setContent(mapView);
        mapScroll.setPannable(true);
        mapScroll.viewportBoundsProperty().addListener((obs, vecchio, nuovo) -> {
            mapView.setViewport(nuovo.getWidth(), nuovo.getHeight());
            refresh();
        });
        mapScroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                mapView.zoomBy(e.getDeltaY() > 0 ? 1.15 : 1 / 1.15);
                refresh();
                e.consume();
            }
        });

        unitCombo.getItems().setAll(UnitType.values());
        unitCombo.setConverter(convertitoreEnum());
        unitCombo.getSelectionModel().selectFirst();
        specCombo.getItems().setAll(CitySpecialization.MILITARE, CitySpecialization.COMMERCIALE, CitySpecialization.AGRICOLA);
        specCombo.setConverter(convertitoreEnum());
        specCombo.getSelectionModel().selectFirst();

        // impostazione partita: da 1 a tutti i leader controllati da umani
        humanSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, LeaderType.values().length, 1));
        humanSpinner.valueProperty().addListener((obs, vecchio, nuovo) -> rebuildHumanRows());
        rebuildHumanRows();
        mostraSetup();

        log("Scegli quanti giocatori umani ci saranno e con quali leader, poi premi 'Avvia partita'.");
        refresh();
    }

    // ================= IMPOSTAZIONE PARTITA =================

    private void mostraSetup() {
        cancelSetupButton.setVisible(game != null);
        cancelSetupButton.setManaged(game != null);
        setupOverlay.setVisible(true);
    }

    /** Ricostruisce le righe "Giocatore N - leader" in base al numero di umani scelto. */
    private void rebuildHumanRows() {
        int n = humanSpinner.getValue();
        List<LeaderType> scelte = new ArrayList<>();
        for (ComboBox<LeaderType> c : humanLeaderCombos) {scelte.add(c.getValue());}
        humanLeaderCombos.clear();
        humansBox.getChildren().clear();

        for (int i = 0; i < n; i++) {
            LeaderType iniziale;
            if (i < scelte.size()) {
                iniziale = scelte.get(i);
            } else {
                iniziale = primoLeaderLibero(scelte);
                scelte.add(iniziale);
            }

            ComboBox<LeaderType> combo = new ComboBox<>();
            combo.getItems().setAll(LeaderType.values());
            combo.setConverter(convertitoreLeader());
            combo.setValue(iniziale);
            combo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(combo, Priority.ALWAYS);

            Rectangle campione = new Rectangle(16, 16, MapView.colorOf(iniziale));
            campione.setArcWidth(4);
            campione.setArcHeight(4);
            Label etichetta = new Label("Giocatore " + (i + 1));
            etichetta.setMinWidth(90);

            Label info = new Label(GameInfo.riassuntoLeader(iniziale));
            info.setWrapText(true);
            info.getStyleClass().add("leader-info");

            humanLeaderCombos.add(combo);
            combo.valueProperty().addListener((obs, vecchio, nuovo) -> {
                if (nuovo == null) {return;}
                campione.setFill(MapView.colorOf(nuovo));
                info.setText(GameInfo.riassuntoLeader(nuovo));
                if (aggiornandoCombo) {return;}
                // se il leader era già di un altro umano, i due si scambiano
                for (ComboBox<LeaderType> altro : humanLeaderCombos) {
                    if (altro != combo && altro.getValue() == nuovo) {
                        aggiornandoCombo = true;
                        altro.setValue(vecchio);
                        aggiornandoCombo = false;
                        break;
                    }
                }
            });

            ImageView faccia = new ImageView();
            faccia.setFitWidth(56);
            faccia.setFitHeight(56);
            faccia.setPreserveRatio(true);
            faccia.setImage(immagineLeader(iniziale));
            combo.valueProperty().addListener((obs, vecchio, nuovo) -> {
               if(nuovo != null){faccia.setImage(immagineLeader(nuovo));}
            });

            HBox riga = new HBox(10, faccia ,campione, etichetta, combo);
            riga.setAlignment(Pos.CENTER_LEFT);
            humansBox.getChildren().add(new VBox(4, riga, info));
        }
    }

    private Image immagineLeader(LeaderType leader){
        String nomeFile = "leaders/" + leader.name().toLowerCase() + ".png";
        try(java.io.InputStream in = GameController.class.getResourceAsStream(nomeFile)){
            if(in == null){return null;}
            return new Image(in);
        }catch(java.io.IOException e){
            return null;
        }
    }

    private void azzeraUndo(){
        ultimoEsercitoMosso = null;
        ultimaPartenza = null;
    }
    private void logNuovoRound(){
        if(game.getRound() != roundMostrato && !game.isGameOver()){
            roundMostrato = game.getRound();
            log("===== ROUND "+ roundMostrato + " di "+ TurnService.MAX_ROUND+" ======");
        }
    }

    private LeaderType primoLeaderLibero(List<LeaderType> usati) {
        for (LeaderType l : LeaderType.values()) {
            if (!usati.contains(l)) {return l;}
        }
        throw new IllegalStateException("nessun leader libero");
    }

    /** Tutti i leader partecipano: prima gli umani (nell'ordine scelto), poi i bot. */
    private List<GameInitializationService.PlayerConfig> configurazione(List<LeaderType> umani) {
        List<GameInitializationService.PlayerConfig> config = new ArrayList<>();
        int numero = 1;
        for (LeaderType l : umani) {
            config.add(new GameInitializationService.PlayerConfig("Giocatore " + numero, l, ORO_INIZIALE, PlayerKind.HUMAN));
            numero++;
        }
        for (LeaderType l : LeaderType.values()) {
            if (!umani.contains(l)) {
                config.add(new GameInitializationService.PlayerConfig("Bot " + leaderName(l), l, ORO_INIZIALE, PlayerKind.BOT));
            }
        }
        return config;
    }

    // ================= EVENTI =================

    @FXML
    private void onNuovaPartitaClick() {
        mostraSetup();
    }

    @FXML
    private void onAnnullaSetupClick() {
        setupOverlay.setVisible(false);
    }

    @FXML
    private void onAvviaPartitaClick() {
        List<LeaderType> umani = new ArrayList<>();
        for (ComboBox<LeaderType> c : humanLeaderCombos) {umani.add(c.getValue());}
        try {
            creaServizi();   // servizi nuovi: i contatori degli id ripartono da zero
            game = gameInitializationService.creaPartitaBase(configurazione(umani));
            fineMostrata = false;
            sconfittiMostrati.clear();
            botCorrente = null;
            azzeraUndo();
            resetSelezione();
            logArea.clear();
            log("Nuova partita: " + game.getPlayers().size() + " leader in gioco, " + umani.size() + " controllati da umani.");
            roundMostrato = 0;
            logNuovoRound();
            log("Inizia " + game.getCurrentPlayer().getName() + " (" + leaderName(game.getCurrentPlayer().getLeader()) + ").");
            setupOverlay.setVisible(false);
        } catch (RuntimeException e) {
            log("Errore creazione partita: " + e.getMessage());
            e.printStackTrace();
        }
        refresh();
    }

    @FXML
    private void onFineTurnoClick() {
        if (!isTurnoUmano()) {return;}
        eseguiAzione(() -> {
            Player finito = game.getCurrentPlayer();
            turnService.fineTurno(game);
            log("--- " + finito.getName() + " finisce il turno ---");
            logNuovoRound();
            logEventi();

            azzeraUndo();
            resetSelezione();
        });
        avviaTurniBot();
    }
    private void avviaTurniBot() {
        saltaBot = false;
        passoBot();
    }

    @FXML
    private void onSaltaBotClick() {
        saltaBot = true;
    }

    private void passoBot() {
        if (game == null || game.isGameOver() || game.getCurrentPlayer().getKind() != PlayerKind.BOT) {
            botCorrente = null;
            if (game != null && !game.isGameOver()) {annunciaTurnoUmano();}
            refresh();
            return;
        }
        Player bot = game.getCurrentPlayer();
        if (botCorrente != bot) {
            botCorrente = bot;
            botTurnService.iniziaTurnoBot();
            log("Gioca " + bot.getName() + " (" + leaderName(bot.getLeader()) + ")");
        }
        refresh();

        PauseTransition pausa = new PauseTransition(Duration.millis(saltaBot ? 0 : PAUSA_BOT_MS));
        pausa.setOnFinished(e -> {
            String esito;
            try {
                esito = botTurnService.eseguiProssimaAzione(game);
            } catch (RuntimeException ex) {
                log("Errore del bot: " + ex.getMessage());
                esito = null;
            }
            if (esito != null) {
                log(esito);
                logEventi();
                Location da = botTurnService.getUltimaPartenza();
                Location a = botTurnService.getUltimaDestinazione();
                if(da != null && a != null && !saltaBot){
                    mapView.animaSpostamento(da, a, MapView.colorOf(bot.getLeader()), this::passoBot);
                }
                else{
                    passoBot();
                }
            } else {
                botTurnService.terminaTurno(game);
                logNuovoRound();
                logEventi();
                verificaFinePartita();
                botCorrente = null;
                passoBot();
            }
        });
        pausa.play();
    }

    private void annunciaTurnoUmano() {
        Player prossimo = game.getCurrentPlayer();
        log("Tocca a " + prossimo.getName() + " (" + leaderName(prossimo.getLeader()) + ").");
        if (numeroCittaInRibellione(prossimo) > 0) {
            log("Attenzione: " + numeroCittaInRibellione(prossimo) + " città di " + prossimo.getName() + " sono in ribellione!");
        }
        if (prossimo.numeroCitta() == 0 && conquestService.trovaLocationPerFondazione(prossimo) != null) {
            log("Non hai più città: usa 'Fonda città' per trasformare il nodo dove si trova il tuo esercito.");
        }
    }

    @FXML
    private void onFondaCittaClick(){
        if(!isTurnoUmano()){return;}
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            Location posto = conquestService.trovaLocationPerFondazione(p);
            if(posto == null){throw new IllegalArgumentException("serve un tuo esercito in un nodo libero per fondare la città");}

            TextInputDialog dialogo = new TextInputDialog("Nuova città");
            dialogo.setTitle("Fonda una città");
            dialogo.setHeaderText("Hai perso tutte le città: il nodo dove si trova il tuo esercito diventa una città");
            dialogo.setContentText("Nome della nuova città:");
            String nome = dialogo.showAndWait().orElse(null);
            if(nome == null){return;}

            City nuova = conquestService.fondaCitta(p, posto, nome);
            log(p.getName()+ " fonda la città "+ nuova.getName()+ "!");
        });
    }

    @FXML
    private void onReclutaClick() {
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            Army army = trovaEsercito(p, selectedLocation);
            if (army == null) {throw new IllegalArgumentException("serve un tuo esercito nella città per reclutare");}
            Unit u = recruitmentService.reclutaUnita(p, city, army, unitCombo.getValue());
            log(p.getName() + " recluta " + MapView.bello(u.type().name()) + " a " + city.getName());
        });
    }

    @FXML
    private void onMigliorarClick() {
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            int costo = constructionService.costoMiglioramento(city);
            constructionService.miglioraCitta(p, city);
            log(p.getName() + " migliora " + city.getName() + " a " + MapView.bello(city.getLevel().name()) + " (-" + costo + " oro)");
        });
    }

    @FXML
    private void onSpecializzaClick() {
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            constructionService.impostaSpecializzazione(p, city, specCombo.getValue());
            log(city.getName() + " ora è " + MapView.bello(city.getSpecialization().name())
                    + " (-" + ConstructionService.COSTO_SPECIALIZZAZIONE + " oro)");
        });
    }
    @FXML
    private void onAnnullaSpostamentoClick(){
        if(!isTurnoUmano()){return;}
        eseguiAzione(() -> {
            if(ultimoEsercitoMosso == null){throw new IllegalArgumentException("non c'è stato nessuno spostamento da annullare");}
            Location da = ultimoEsercitoMosso.getPosizione();
            movementService.annullaMovimento(ultimoEsercitoMosso, ultimaPartenza);
            log(game.getCurrentPlayer().getName()+ " annulla lo spostamento: l'esercito torna da "+ nomeLuogo(da)+ " a "+ nomeLuogo(ultimaPartenza));
            azzeraUndo();
            resetSelezione();
        });
    }
    @FXML
    private void onNuovoEsercitoClick(){
        if(!isTurnoUmano()){return;}
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            int costo = armyService.costoNuovoEsercito(p);
            armyService.creaNuovoEsercito(p,city);
            log(p.getName()+ " crea nuovo esercito a "+ city.getName()+ " per "+ costo +" oro");

        });
    }

    /** Click su un nodo della mappa (callback di MapView). */
    private void onLocationClicked(Location l) {
        if (!isTurnoUmano()) {return;}
        Player current = game.getCurrentPlayer();

        if (selectedArmy != null && targets.contains(l)) {
            Army army = selectedArmy;
            eseguiAzione(() -> muoviEsercito(army, l));
            return;
        }

        selectedLocation = l;
        selectedArmy = trovaEsercito(current, l);
        targets.clear();
        if (selectedArmy != null && selectedArmy.isPuoMuovere() && !selectedArmy.isVuoto()) {
            boolean primoTurno = current.getTurniGiocati() == 0;
            for (Location n : l.getAdiacenti()) {
                if (n.hasArmyPlayer(current)) {continue;}
                if(primoTurno && movementService.isAttacco(n, current)){continue;}
                targets.add(n);
            }
        }
        refresh();
    }

    // ================= LOGICA DI SUPPORTO =================

    private void muoviEsercito(Army army, Location destinazione) {
        Player p = army.getOwner();
        Location partenza = army.getPosizione();
        Player vecchioOwner = destinazione.hasCity() ? destinazione.getCity().getOwner() : null;
        boolean eraNemico = destinazione.hasEnemyArmiesFor(p);

        BattleResult r = movementService.muovi(army, destinazione);

        if (r == null) {
            log(p.getName() + " muove verso " + nomeLuogo(destinazione));
        } else {
            log(String.format(Locale.ITALY,
                    "Battaglia a %s: attacco %d (x%.1f) contro difesa %d (x%.1f) - vince %s. Perdite: attaccante %d, difensore %d.",
                    nomeLuogo(destinazione),
                    r.totaleAttaccante(), r.tiroAttaccante(),
                    r.totaleDifensore(), r.tiroDifensore(),
                    r.haVintoAttaccante() ? "l'attaccante" : "il difensore",
                    r.perditeAttaccante(), r.perditeDifensore()));
        }
        if(r != null){logDopoBattaglia(r);}
        boolean conquista = destinazione.hasCity() && vecchioOwner != null && vecchioOwner != p && destinazione.getCity().getOwner() == p;
        if(conquista){
            log(p.getName() + " conquista " + destinazione.getCity().getName()+ "!");
        }
        if(r == null && !eraNemico && !conquista){
            ultimoEsercitoMosso = army;
            ultimaPartenza = partenza;
        }
        else {azzeraUndo();}
        resetSelezione();
        selectedLocation = destinazione;

        animazioneDa = partenza;
        animazioneA = destinazione;
        animazioneColore = MapView.colorOf(p.getLeader());
    }

    private void logDopoBattaglia(BattleResult r){
        Army sconfitto = r.sconfitto();
        Player proprietario = sconfitto.getOwner();
        if(proprietario.getEserciti().contains(sconfitto)){
            log("L'esercito di "+ proprietario.getName()+ " si ritira a "+ nomeLuogo(sconfitto.getPosizione())+ ".");
        }
        else{
            log("L'esercito di " + proprietario.getName() + " è distrutto"
                    + (proprietario.isSconfitto() ? "." : " e ricompare in una sua città con una sola fanteria."));
        }
    }

    /** Esegue un'azione dell'utente gestendo errori di regole, fine partita e aggiornamento vista. */
    private void eseguiAzione(Runnable azione) {
        if (game == null) {
            log("Devi prima creare una partita.");
            return;
        }
        try {
            azione.run();
        } catch (RuntimeException e) {
            log("Azione non valida: " + e.getMessage());
        }
        verificaFinePartita();
        if(animazioneDa != null){
            Location da = animazioneDa;
            Location a = animazioneA;
            Color colore = animazioneColore;
            animazioneDa = null;
            mapView.animaSpostamento(da, a, colore, this::refresh);
        }
        else{refresh();}
    }

    private void verificaFinePartita() {
        if (game == null) {return;}
        if (!game.isGameOver() && (game.haSoloUnPlayerAttivo() || !esisteUmanoAttivo())) {game.setFinita();}
        if (game.isGameOver() && !fineMostrata) {
            fineMostrata = true;
            Player vincitore = null;
            if (game.haSoloUnPlayerAttivo()) {
                for (Player p : game.getPlayers()) {
                    if (!p.isSconfitto()) {vincitore = p;}
                }
            }
            String esito = vincitore != null
                    ? "Vincitore: " + vincitore.getName() + " (" + leaderName(vincitore.getLeader()) + ")"
                    : "Tutti i giocatori umani sono stati sconfitti.";
            log("PARTITA FINITA. " + esito);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, esito);
            alert.setTitle("Partita finita");
            alert.setHeaderText("Partita finita");
            alert.show();
        }
    }

    private void logEventi(){
        for(String evento : game.prelevaEventi()){
            log("! "+ evento);
        }
    }

    private int numeroCittaInRibellione(Player p) {
        int n = 0;
        for (City c : p.getTerritorio().getCities()) {
            if (c.hasRibellione()) {n++;}
        }
        return n;
    }

    private void verificaEliminazioni(){
        String nuovi = "";
        for(Player p : game.getPlayers()){
            if(p.isSconfitto() && !sconfittiMostrati.contains(p)){
                sconfittiMostrati.add(p);
                String riga = p.getName() + " ("+ leaderName(p.getLeader()) + ") è stato eliminato!";
                log(riga);
                nuovi += riga+ "\n";
            }
        }
        if(!nuovi.isEmpty() && !game.isGameOver()){
            Alert alert = new Alert(Alert.AlertType.WARNING, nuovi.trim());
            alert.setTitle("Giocatore eliminato");
            alert.setHeaderText("Giocatore eliminato");
            alert.show();
        }
    }

    private boolean esisteUmanoAttivo() {
        for (Player p : game.getPlayers()) {
            if (p.getKind() == PlayerKind.HUMAN && !p.isSconfitto()) {return true;}
        }
        return false;
    }

    private boolean isTurnoUmano() {
        return game != null && !game.isGameOver() && game.getCurrentPlayer().getKind() == PlayerKind.HUMAN;
    }

    private City cittaSelezionata() {
        if (selectedLocation == null || !selectedLocation.hasCity()) {
            throw new IllegalArgumentException("seleziona prima una tua città");
        }
        return selectedLocation.getCity();
    }

    private Army trovaEsercito(Player p, Location l) {
        if (p == null || l == null) {return null;}
        for (Army a : l.getArmies()) {
            if (a != null && a.getOwner() == p) {return a;}
        }
        return null;
    }

    private String nomeLuogo(Location l) {
        return l.hasCity() ? l.getCity().getName() : "posizione " + l.getId();
    }

    private void resetSelezione() {
        selectedLocation = null;
        selectedArmy = null;
        targets.clear();
    }

    private void log(String msg) {
        logArea.appendText(msg + "\n");
    }

    // ================= NOMI DA MOSTRARE =================

    private static String leaderName(LeaderType leader) {
        return switch (leader) {
            case ALESSANDRO_MAGNO -> "Alessandro Magno";
            case ANNIBALE -> "Annibale";
            case ATTILA -> "Attila";
            case GIULIO_CESARE -> "Giulio Cesare";
            case REGINA_ELISABETTA -> "Regina Elisabetta";
            case GIOVANNA_D_ARCO -> "Giovanna d'Arco";
            case FEDERICO_BARBAROSSA -> "Federico Barbarossa";
            case QIN_SHI_HUANG -> "Qin Shi Huang";
        };
    }

    private static StringConverter<LeaderType> convertitoreLeader() {
        return new StringConverter<>() {
            @Override
            public String toString(LeaderType leader) {
                return leader == null ? "" : leaderName(leader);
            }

            @Override
            public LeaderType fromString(String testo) {
                return null;
            }
        };
    }

    private static <T extends Enum<T>> StringConverter<T> convertitoreEnum() {
        return new StringConverter<>() {
            @Override
            public String toString(T valore) {
                return valore == null ? "" : MapView.bello(valore.name());
            }

            @Override
            public T fromString(String testo) {
                return null;
            }
        };
    }

    // ================= AGGIORNAMENTO VISTA =================

    private void refresh() {
        mapView.render(game, selectedLocation, selectedArmy, targets);
        playersBox.getChildren().clear();

        if (game == null) {
            turnLabel.setText("Nessuna partita");
            selectionLabel.setText("Nessuna selezione");
            endTurnButton.setDisable(true);
            recruitButton.setDisable(true);
            upgradeButton.setDisable(true);
            specButton.setDisable(true);
            undoButton.setDisable(true);
            newArmyButton.setDisable(true);
            foundCityButton.setVisible(false);
            foundCityButton.setManaged(false);
            skipBotButton.setVisible(false);
            skipBotButton.setManaged(false);
            rebellionLabel.setText("");
            abilityLabel.setText("");
            return;
        }

        verificaEliminazioni();
        Player current = game.getCurrentPlayer();
        turnLabel.setText(game.isGameOver()
                ? "Partita finita"
                : "Round " + game.getRound() + " / " + TurnService.MAX_ROUND
                + " (turno " + (game.getTurno() + 1) + "/" + game.getPlayers().size()
                + ") - " + current.getName());

        for (Player p : game.getPlayers()) {
            boolean inTurno = (p == current && !game.isGameOver());
            Rectangle campione = new Rectangle(14, 14, MapView.colorOf(p.getLeader()));
            campione.setArcWidth(4);
            campione.setArcHeight(4);
            campione.setStroke(Color.web("#10162a"));

            String titolo = (inTurno ? "> " : "") + p.getName()
                    + (p.getKind() == PlayerKind.HUMAN ? " - " + leaderName(p.getLeader()) : "")
                    + (p.isSconfitto() ? " (sconfitto)" : "");
            Label nome = new Label(titolo);
            nome.getStyleClass().add(inTurno ? "player-current" : "player-name");

            Label statistiche = new Label("Oro " + p.getOro() + "  |  Città " + p.numeroCitta() +
                    "  |  Eserciti " + p.numeroEserciti() + "/"+ ArmyService.MAX_ESERCITI +
                    (numeroCittaInRibellione(p)>0 ? " | Ribellione " + numeroCittaInRibellione(p) : ""));

            statistiche.getStyleClass().add("player-stats");

            HBox riga = new HBox(8, campione, new VBox(1, nome, statistiche));
            riga.setAlignment(Pos.CENTER_LEFT);
            playersBox.getChildren().add(riga);
        }

        String sel = MapView.descrizione(selectedLocation);
        if (selectedArmy != null && selectedArmy.isPuoMuovere() && !selectedArmy.isVuoto()) {
            sel += "\n\nClicca un nodo evidenziato per muovere (verde = libero, rosso = nemico).";
        }
        selectionLabel.setText(sel);

        boolean umano = isTurnoUmano();
        City city = (selectedLocation != null) ? selectedLocation.getCity() : null;
        boolean miaCitta = umano && city != null && city.getOwner() == current;

        endTurnButton.setDisable(!umano);
        recruitButton.setDisable(!miaCitta);
        upgradeButton.setDisable(!miaCitta || city.isMetropoli());
        specButton.setDisable(!miaCitta);


        boolean maxEserciti = current.numeroEserciti() >= ArmyService.MAX_ESERCITI;
        newArmyButton.setDisable(!miaCitta || maxEserciti);
        newArmyButton.setText(maxEserciti ? "Crea esercito(massimo "+ ArmyService.MAX_ESERCITI+ ")" : "Crea esercito ("+ armyService.costoNuovoEsercito(current) + " oro)");

        undoButton.setDisable(!umano || ultimoEsercitoMosso == null);

        boolean puoFondare = umano && conquestService.trovaLocationPerFondazione(current) != null;
        foundCityButton.setVisible(puoFondare);
        foundCityButton.setManaged(puoFondare);

        //pulsante per saltare le pause dei bot
        boolean botInGioco = botCorrente != null && !game.isGameOver();
        skipBotButton.setVisible(botInGioco);
        skipBotButton.setManaged(botInGioco);

        if(umano){
            String testoRibellione = "Rischio a inizio prossimo turno: "+ ribellioneService.calcolaProbabilita(current, game) + "%";
            for(String causa : ribellioneService.causeAttive(current, game)){
                testoRibellione+= "\n " + causa;
            }
            rebellionLabel.setText(testoRibellione);
        }
        else{rebellionLabel.setText("");}

        abilityLabel.setText(umano ? GameInfo.abilitaDelTurno(current.getLeader()) : "");
        aggiornaCostiUnita(miaCitta ? city : null, current);

        upgradeButton.setText(miaCitta && !city.isMetropoli()
                ? "Migliora città (" + constructionService.costoMiglioramento(city) + " oro)"
                : "Migliora città");
    }

    private void aggiornaCostiUnita(City city, Player current){
        UnitType scelta = unitCombo.getValue();
        unitCombo.setCellFactory(lista -> creaCellaUnita(city, current));
        unitCombo.setButtonCell(creaCellaUnita(city, current));
        unitCombo.setValue(scelta);
    }

    private ListCell<UnitType> creaCellaUnita(City city, Player current){
        return new ListCell<>(){
            @Override
            protected void updateItem(UnitType tipo, boolean vuota){
                super.updateItem(tipo, vuota);
                if(vuota || tipo == null){
                    setText(null);
                    return;
                }
                String testo = MapView.bello(tipo.name());
                if(city != null){
                    if(recruitmentService.puoReclutareDaCitta(city, tipo, current.getLeader())){
                        testo += " ("+ recruitmentService.costoUnita(tipo, city, current.getLeader())+ " oro)";
                    }
                    else{
                        testo += " (non disponibile qui)";
                    }
                }
                setText(testo);
            }
        };
    }

    @FXML
    private void onLegendaClick() {
        TextArea testo = new TextArea(GameInfo.legenda());
        testo.setEditable(false);
        testo.setWrapText(true);
        testo.setPrefSize(660,500);

        Alert finestra = new Alert(Alert.AlertType.INFORMATION);
        finestra.setTitle("Legenda");
        finestra.setHeaderText("Costi, regole e leader");
        finestra.getDialogPane().setContent(testo);
        finestra.setResizable(true);
        finestra.show();
    }

    public Game getGame() {
        return game;
    }
}