package it.unicam.cs.mpgc.rpg125715;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    // ---- servizi del modello (ricreati a ogni nuova partita) ----
    private MovementService movementService;
    private RecruitmentService recruitmentService;
    private ConstructionService constructionService;
    private TurnService turnService;
    private BotTurnService botTurnService;
    private GameInitializationService gameInitializationService;

    public GameController() {
        creaServizi();
    }

    private void creaServizi() {
        IdGenerator idGenerator = new IdGenerator();
        DiceService diceService = new DiceService();
        BattleService battleService = new BattleService(diceService);
        this.armyService = new ArmyService(idGenerator);
        ConquestService conquestService = new ConquestService();
        this.movementService = new MovementService(battleService, armyService, conquestService);
        this.recruitmentService = new RecruitmentService();
        this.constructionService = new ConstructionService();
        RibellioneService ribellioneService = new RibellioneService();
        this.turnService = new TurnService(ribellioneService);
        BotDecisionService botDecisionService = new BotDecisionService(constructionService);
        this.botTurnService = new BotTurnService(botDecisionService, turnService, recruitmentService, movementService, constructionService);
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

            humanLeaderCombos.add(combo);
            combo.valueProperty().addListener((obs, vecchio, nuovo) -> {
                if (nuovo == null) {return;}
                campione.setFill(MapView.colorOf(nuovo));
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

            HBox riga = new HBox(10, campione, etichetta, combo);
            riga.setAlignment(Pos.CENTER_LEFT);
            humansBox.getChildren().add(riga);
        }
    }
    private void azzeraUndo(){
        ultimoEsercitoMosso = null;
        ultimaPartenza = null;
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
            azzeraUndo();
            resetSelezione();
            logArea.clear();
            log("Nuova partita: " + game.getPlayers().size() + " leader in gioco, " + umani.size() + " controllati da umani.");
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

            // i bot giocano finché non tocca di nuovo a un umano
            int guardia = 0;
            int limite = game.getPlayers().size() * 2;
            while (!game.isGameOver()
                    && game.getCurrentPlayer().getKind() == PlayerKind.BOT
                    && guardia++ < limite) {
                log(botTurnService.eseguiTurnoBot(game));
            }
            if (!game.isGameOver()) {
                Player prossimo = game.getCurrentPlayer();
                log("Tocca a " + prossimo.getName() + " (" + leaderName(prossimo.getLeader()) + ").");
            }
            azzeraUndo();
            resetSelezione();
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
            for (Location n : l.getAdiacenti()) {
                if (!n.hasArmyPlayer(current)) {targets.add(n);}
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
        refresh();
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
            return;
        }

        Player current = game.getCurrentPlayer();
        turnLabel.setText(game.isGameOver()
                ? "Partita finita"
                : "Turno " + game.getNumeroTurno() + " - " + current.getName());

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
            Label statistiche = new Label("Oro " + p.getOro() + "  |  Città " + p.numeroCitta() + "  |  Eserciti " + p.numeroEserciti() + "/"+ ArmyService.MAX_ESERCITI);
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
        newArmyButton.setDisable(!miaCitta || !maxEserciti);
        newArmyButton.setText(maxEserciti ? "Crea esercito(massimo "+ ArmyService.MAX_ESERCITI+ ")" : "Crea esercito ("+ armyService.costoNuovoEsercito(current) + " oro)");

        undoButton.setDisable(!umano || ultimoEsercitoMosso == null);
        upgradeButton.setText(miaCitta && !city.isMetropoli()
                ? "Migliora città (" + constructionService.costoMiglioramento(city) + " oro)"
                : "Migliora città");
    }

    public Game getGame() {
        return game;
    }
}