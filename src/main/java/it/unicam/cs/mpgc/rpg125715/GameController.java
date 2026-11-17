package it.unicam.cs.mpgc.rpg125715;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * CONTROLLER della schermata di gioco: riceve i click dell'utente, chiama i servizi del modello
 * e aggiorna la vista. Le parti specifiche sono delegate a:
 * ServiziPartita (creazione dei servizi), ImpostazionePartitaView (schermata nuova partita),
 * EsecutoreTurniBot (turni dei bot), GestoreSalvataggi (salva/carica), PannelloGiocatori,
 * RegistroPartita (log) e Dialoghi (finestre secondarie).
 */
public class GameController implements EsecutoreTurniBot.Ascoltatore {

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
    @FXML private Button undoButton;
    @FXML private Button newArmyButton;
    @FXML private Button foundCityButton;
    @FXML private Button skipBotButton;
    @FXML private Label rebellionLabel;
    @FXML private Label abilityLabel;
    @FXML private Button saveButton;

    // schermata di impostazione partita
    @FXML private Pane setupOverlay;
    @FXML private Spinner<Integer> humanSpinner;
    @FXML private VBox humansBox;
    @FXML private Button cancelSetupButton;
    @FXML private ComboBox<VictoryMode> modeCombo;
    @FXML private Label modeInfoLabel;

    // ---- parti della vista a cui il controller delega ----
    private MapView mapView;
    private RegistroPartita registro;
    private PannelloGiocatori pannelloGiocatori;
    private ImpostazionePartitaView impostazione;
    private EsecutoreTurniBot esecutoreBot;
    private final GestoreSalvataggi salvataggi = new GestoreSalvataggi(new SerializzazionePartitaRepository());

    // ---- partita e servizi (ricreati a ogni nuova partita) ----
    private Game game;
    private ServiziPartita servizi = new ServiziPartita();

    // ---- stato dell'interazione ----
    private Location selectedLocation;
    private Army selectedArmy;
    private final Set<Location> targets = new HashSet<>();
    private boolean fineMostrata;
    private final Set<Player> sconfittiMostrati = new HashSet<>();
    private Army ultimoEsercitoMosso;
    private Location ultimaPartenza;
    private Location animazioneDa;
    private Location animazioneA;
    private Color animazioneColore;

    @FXML
    private void initialize() {
        registro = new RegistroPartita(logArea);
        pannelloGiocatori = new PannelloGiocatori(playersBox);
        impostazione = new ImpostazionePartitaView(setupOverlay, humanSpinner, humansBox, modeCombo, modeInfoLabel, cancelSetupButton);

        // mappa: si adatta alla finestra; Ctrl + rotella per ingrandire, trascinando ci si sposta
        mapView = new MapView(this::onLocationClicked);
        esecutoreBot = new EsecutoreTurniBot(mapView, registro, this);
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
        unitCombo.setConverter(ConvertitoriTesto.enumerato());
        unitCombo.getSelectionModel().selectFirst();
        specCombo.getItems().setAll(CitySpecialization.MILITARE, CitySpecialization.COMMERCIALE, CitySpecialization.AGRICOLA);
        specCombo.setConverter(ConvertitoriTesto.enumerato());
        specCombo.getSelectionModel().selectFirst();

        impostazione.mostra(false);
        registro.scrivi("Scegli quanti giocatori umani ci saranno e con quali leader, poi premi 'Avvia partita'.");
        refresh();
    }

    // ================= NUOVA PARTITA, SALVA, CARICA =================

    @FXML
    private void onNuovaPartitaClick() {
        impostazione.mostra(game != null);
    }

    @FXML
    private void onAnnullaSetupClick() {
        impostazione.nascondi();
    }

    @FXML
    private void onAvviaPartitaClick() {
        try {
            ServiziPartita nuoviServizi = new ServiziPartita();   // servizi nuovi: i contatori degli id ripartono da zero
            Game nuova = nuoviServizi.gameInitializationService().creaPartitaBase(
                    GameInitializationService.configurazioneStandard(impostazione.leaderUmani(), ORO_INIZIALE), impostazione.modalita());
            servizi = nuoviServizi;
            iniziaConPartita(nuova);
            registro.scrivi("Nuova partita: " + game.getPlayers().size() + " leader in gioco, " + impostazione.leaderUmani().size() + " controllati da umani.");
            registro.scrivi("Modalità: " + game.getModalita().getNome() + ".");
            registro.scriviNemiciBot(game);
            registro.scriviNuovoRound(game);
            registro.scrivi("Inizia " + game.getCurrentPlayer().getName() + " (" + GameInfo.nomeLeader(game.getCurrentPlayer().getLeader()) + ").");
            impostazione.nascondi();
        } catch (RuntimeException e) {
            registro.scrivi("Errore creazione partita: " + e.getMessage());
        }
        refresh();
    }

    @FXML
    private void onSalvaClick() {
        if (!isTurnoUmano()) {
            registro.scrivi("Puoi salvare solo durante il turno di un giocatore umano");
            return;
        }
        try {
            Path percorso = salvataggi.salva(game, finestra());
            if (percorso != null) {registro.scrivi("Partita salvata in " + percorso + ".");}
        } catch (IOException | RuntimeException e) {
            registro.scrivi("Salvataggio non riuscito: " + e.getMessage());
        }
    }

    @FXML
    private void onCaricaClick() {
        if (esecutoreBot.isInCorso()) {
            registro.scrivi("Aspetta la fine dei turni dei bot prima di caricare una partita");
            return;
        }
        try {
            GestoreSalvataggi.PartitaCaricata caricata = salvataggi.carica(finestra());
            if (caricata == null) {return;}

            servizi = ServiziPartita.perPartitaCaricata(caricata.game());
            iniziaConPartita(caricata.game());
            for (Player p : game.getPlayers()) {
                if (p.isSconfitto()) {sconfittiMostrati.add(p);}
            }
            fineMostrata = game.isGameOver();
            registro.scrivi("Partita caricata da " + caricata.nomeFile() + " (" + game.getModalita().getNome() + ").");
            registro.scriviNuovoRound(game);
            impostazione.nascondi();
            refresh();

            if (game.isGameOver()) {return;}
            if (game.getCurrentPlayer().getKind() == PlayerKind.BOT) {esecutoreBot.avvia(game, servizi.botTurnService());}
            else {turnoUmano();}
        } catch (IOException | RuntimeException e) {
            registro.scrivi("Caricamento non riuscito: " + e.getMessage());
        }
    }

    /** Azzera lo stato dell'interfaccia per giocare la partita indicata. */
    private void iniziaConPartita(Game nuova) {
        esecutoreBot.ferma();
        game = nuova;
        fineMostrata = false;
        sconfittiMostrati.clear();
        azzeraUndo();
        resetSelezione();
        registro.pulisci();
    }

    // ================= TURNI =================

    @FXML
    private void onFineTurnoClick() {
        if (!isTurnoUmano()) {return;}
        eseguiAzione(() -> {
            Player finito = game.getCurrentPlayer();
            servizi.turnService().fineTurno(game);
            registro.scrivi("--- " + finito.getName() + " finisce il turno ---");
            registro.scriviNuovoRound(game);
            registro.scriviEventi(game);
            azzeraUndo();
            resetSelezione();
        });
        esecutoreBot.avvia(game, servizi.botTurnService());
    }

    @FXML
    private void onSaltaBotClick() {
        esecutoreBot.saltaPause();
    }

    // ---- EsecutoreTurniBot.Ascoltatore ----

    @Override
    public void aggiornaVista() {refresh();}

    @Override
    public void fineTurnoBot() {verificaFinePartita();}

    @Override
    public void turnoUmano() {
        Player prossimo = game.getCurrentPlayer();
        registro.scrivi("Tocca a " + prossimo.getName() + " (" + GameInfo.nomeLeader(prossimo.getLeader()) + ").");
        if (prossimo.numeroCittaInRibellione() > 0) {
            registro.scrivi("Attenzione: " + prossimo.numeroCittaInRibellione() + " città di " + prossimo.getName() + " sono in ribellione!");
        }
        if (prossimo.numeroCitta() == 0 && servizi.conquestService().trovaLocationPerFondazione(prossimo) != null) {
            registro.scrivi("Non hai più città: usa 'Fonda città' per trasformare il nodo dove si trova il tuo esercito.");
        }
    }

    // ================= AZIONI DEL GIOCATORE =================

    @FXML
    private void onFondaCittaClick() {
        if (!isTurnoUmano()) {return;}
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            Location posto = servizi.conquestService().trovaLocationPerFondazione(p);
            if (posto == null) {throw new IllegalArgumentException("serve un tuo esercito in un nodo libero per fondare la città");}

            TextInputDialog dialogo = new TextInputDialog("Nuova città");
            dialogo.setTitle("Fonda una città");
            dialogo.setHeaderText("Hai perso tutte le città: il nodo dove si trova il tuo esercito diventa una città");
            dialogo.setContentText("Nome della nuova città:");
            String nome = dialogo.showAndWait().orElse(null);
            if (nome == null) {return;}

            City nuova = servizi.conquestService().fondaCitta(p, posto, nome);
            registro.scrivi(p.getName() + " fonda la città " + nuova.getName() + "!");
        });
    }

    @FXML
    private void onReclutaClick() {
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            Army army = trovaEsercito(p, selectedLocation);
            if (army == null) {throw new IllegalArgumentException("serve un tuo esercito nella città per reclutare");}
            Unit u = servizi.recruitmentService().reclutaUnita(p, city, army, unitCombo.getValue());
            registro.scrivi(p.getName() + " recluta " + MapView.bello(u.type().name()) + " a " + city.getName());
        });
    }

    @FXML
    private void onMigliorarClick() {
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            int costo = servizi.constructionService().costoMiglioramento(city);
            servizi.constructionService().miglioraCitta(p, city);
            registro.scrivi(p.getName() + " migliora " + city.getName() + " a " + MapView.bello(city.getLevel().name()) + " (-" + costo + " oro)");
        });
    }

    @FXML
    private void onSpecializzaClick() {
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            servizi.constructionService().impostaSpecializzazione(p, city, specCombo.getValue());
            registro.scrivi(city.getName() + " ora è " + MapView.bello(city.getSpecialization().name())
                    + " (-" + ConstructionService.COSTO_SPECIALIZZAZIONE + " oro)");
        });
    }

    @FXML
    private void onAnnullaSpostamentoClick() {
        if (!isTurnoUmano()) {return;}
        eseguiAzione(() -> {
            if (ultimoEsercitoMosso == null) {throw new IllegalArgumentException("non c'è stato nessuno spostamento da annullare");}
            Location da = ultimoEsercitoMosso.getPosizione();
            servizi.movementService().annullaMovimento(ultimoEsercitoMosso, ultimaPartenza);
            registro.scrivi(game.getCurrentPlayer().getName() + " annulla lo spostamento: l'esercito torna da " + nomeLuogo(da) + " a " + nomeLuogo(ultimaPartenza));
            azzeraUndo();
            resetSelezione();
        });
    }

    @FXML
    private void onNuovoEsercitoClick() {
        if (!isTurnoUmano()) {return;}
        eseguiAzione(() -> {
            Player p = game.getCurrentPlayer();
            City city = cittaSelezionata();
            int costo = ArmyService.costoNuovoEsercito(p);
            servizi.armyService().creaNuovoEsercito(p, city);
            registro.scrivi(p.getName() + " crea nuovo esercito a " + city.getName() + " per " + costo + " oro");
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
                if (primoTurno && servizi.movementService().isAttacco(n, current)) {continue;}
                targets.add(n);
            }
        }
        refresh();
    }

    private void muoviEsercito(Army army, Location destinazione) {
        Player p = army.getOwner();
        Location partenza = army.getPosizione();
        Player vecchioOwner = destinazione.hasCity() ? destinazione.getCity().getOwner() : null;
        boolean eraNemico = destinazione.hasEnemyArmiesFor(p);

        BattleResult r = servizi.movementService().muovi(army, destinazione);

        if (r == null) {
            registro.scrivi(p.getName() + " muove verso " + nomeLuogo(destinazione));
        } else {
            registro.scrivi(String.format(Locale.ITALY,
                    "Battaglia a %s: attacco %d (x%.1f) contro difesa %d (x%.1f) - vince %s. Perdite: attaccante %d, difensore %d.",
                    nomeLuogo(destinazione),
                    r.totaleAttaccante(), r.tiroAttaccante(),
                    r.totaleDifensore(), r.tiroDifensore(),
                    r.haVintoAttaccante() ? "l'attaccante" : "il difensore",
                    r.perditeAttaccante(), r.perditeDifensore()));
            scriviDopoBattaglia(r);
        }
        boolean conquista = destinazione.hasCity() && vecchioOwner != p && destinazione.getCity().getOwner() == p;
        if (conquista) {
            registro.scrivi(p.getName() + " conquista " + destinazione.getCity().getName() + "!");
        }
        if (r == null && !eraNemico && !conquista) {
            ultimoEsercitoMosso = army;
            ultimaPartenza = partenza;
        } else {
            azzeraUndo();
        }
        resetSelezione();
        selectedLocation = destinazione;

        animazioneDa = partenza;
        animazioneA = destinazione;
        animazioneColore = MapView.colorOf(p.getLeader());
    }

    private void scriviDopoBattaglia(BattleResult r) {
        Army sconfitto = r.sconfitto();
        Player proprietario = sconfitto.getOwner();
        if (proprietario.getEserciti().contains(sconfitto)) {
            registro.scrivi("L'esercito di " + proprietario.getName() + " si ritira a " + nomeLuogo(sconfitto.getPosizione()) + ".");
        } else {
            registro.scrivi("L'esercito di " + proprietario.getName() + " è distrutto"
                    + (proprietario.isSconfitto() ? "." : " e ricompare in una sua città con una fanteria e un'unità speciale."));
        }
    }

    /** Esegue un'azione dell'utente gestendo errori di regole, fine partita e aggiornamento vista. */
    private void eseguiAzione(Runnable azione) {
        if (game == null) {
            registro.scrivi("Devi prima creare una partita.");
            return;
        }
        try {
            azione.run();
        } catch (RuntimeException e) {
            registro.scrivi("Azione non valida: " + e.getMessage());
        }
        verificaFinePartita();
        if (animazioneDa != null) {
            Location da = animazioneDa;
            Location a = animazioneA;
            Color colore = animazioneColore;
            animazioneDa = null;
            mapView.animaSpostamento(da, a, colore, this::refresh);
        } else {
            refresh();
        }
    }

    // ================= FINE PARTITA ED ELIMINAZIONI =================

    private void verificaFinePartita() {
        if (game == null) {return;}
        EsitoPartitaService.aggiornaFine(game);
        if (game.isGameOver() && !fineMostrata) {
            fineMostrata = true;
            EsitoPartitaService.Esito esito = EsitoPartitaService.esito(game);
            registro.scrivi("PARTITA FINITA. " + Dialoghi.testoEsito(esito));
            if (!esito.classifica().isEmpty()) {registro.scrivi("Classifica finale:\n" + esito.classifica());}
            Dialoghi.mostraEsito(esito);
        }
    }

    private void verificaEliminazioni() {
        String nuovi = "";
        for (Player p : game.getPlayers()) {
            if (p.isSconfitto() && !sconfittiMostrati.contains(p)) {
                sconfittiMostrati.add(p);
                String riga = p.getName() + " (" + GameInfo.nomeLeader(p.getLeader()) + ") è stato eliminato!";
                registro.scrivi(riga);
                nuovi += riga + "\n";
            }
        }
        if (!nuovi.isEmpty() && !game.isGameOver()) {Dialoghi.mostraEliminati(nuovi.trim());}
    }

    // ================= FINESTRE =================

    @FXML
    private void onTutorialClick() {Dialoghi.mostraTutorial(finestra());}

    @FXML
    private void onLegendaClick() {Dialoghi.mostraLegenda();}

    // ================= SUPPORTO =================

    private Window finestra() {return mapScroll.getScene().getWindow();}

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

    private void azzeraUndo() {
        ultimoEsercitoMosso = null;
        ultimaPartenza = null;
    }

    private void resetSelezione() {
        selectedLocation = null;
        selectedArmy = null;
        targets.clear();
    }

    // ================= AGGIORNAMENTO VISTA =================

    private void refresh() {
        mapView.render(game, selectedLocation, selectedArmy, targets);
        pannelloGiocatori.aggiorna(game);

        if (game == null) {
            mostraNessunaPartita();
            return;
        }

        verificaEliminazioni();
        Player current = game.getCurrentPlayer();
        turnLabel.setText(game.isGameOver()
                ? "Partita finita"
                : "Round " + game.getRound() + (game.getModalita() == VictoryMode.PUNTI ? " / " + TurnService.MAX_ROUND : "")
                + " (turno " + (game.getTurno() + 1) + "/" + game.getPlayers().size()
                + ") - " + current.getName());

        String sel = MapView.descrizione(selectedLocation);
        if (selectedArmy != null && selectedArmy.isPuoMuovere() && !selectedArmy.isVuoto()) {
            sel += "\n\nClicca un nodo evidenziato per muovere (verde = libero, rosso = nemico).";
        }
        selectionLabel.setText(sel);

        aggiornaPulsanti(current);
        aggiornaInfoGiocatore(current);
    }

    private void mostraNessunaPartita() {
        turnLabel.setText("Nessuna partita");
        selectionLabel.setText("Nessuna selezione");
        saveButton.setDisable(true);
        endTurnButton.setDisable(true);
        recruitButton.setDisable(true);
        upgradeButton.setDisable(true);
        specButton.setDisable(true);
        undoButton.setDisable(true);
        newArmyButton.setDisable(true);
        mostra(foundCityButton, false);
        mostra(skipBotButton, false);
        rebellionLabel.setText("");
        abilityLabel.setText("");
    }

    private void aggiornaPulsanti(Player current) {
        boolean umano = isTurnoUmano();
        City city = (selectedLocation != null) ? selectedLocation.getCity() : null;
        boolean miaCitta = umano && city != null && city.getOwner() == current;

        endTurnButton.setDisable(!umano);
        saveButton.setDisable(!umano);
        recruitButton.setDisable(!miaCitta);
        upgradeButton.setDisable(!miaCitta || city.isMetropoli());
        specButton.setDisable(!miaCitta);
        upgradeButton.setText(miaCitta && !city.isMetropoli()
                ? "Migliora città (" + servizi.constructionService().costoMiglioramento(city) + " oro)"
                : "Migliora città");

        boolean maxEserciti = current.numeroEserciti() >= ArmyService.MAX_ESERCITI;
        newArmyButton.setDisable(!miaCitta || maxEserciti);
        newArmyButton.setText(maxEserciti
                ? "Crea esercito(massimo " + ArmyService.MAX_ESERCITI + ")"
                : "Crea esercito (" + ArmyService.costoNuovoEsercito(current) + " oro)");

        undoButton.setDisable(!umano || ultimoEsercitoMosso == null);
        mostra(foundCityButton, umano && servizi.conquestService().trovaLocationPerFondazione(current) != null);
        mostra(skipBotButton, esecutoreBot.isInCorso() && !game.isGameOver());   // salta le pause dei bot

        aggiornaCostiUnita(miaCitta ? city : null, current);
    }

    private void aggiornaInfoGiocatore(Player current) {
        if (!isTurnoUmano()) {
            rebellionLabel.setText("");
            abilityLabel.setText("");
            return;
        }
        RibellioneService ribellioni = servizi.ribellioneService();
        String testoRibellione = "Rischio a inizio prossimo turno: " + ribellioni.calcolaProbabilita(current, game) + "%";
        for (String causa : ribellioni.causeAttive(current, game)) {
            testoRibellione += "\n " + causa;
        }
        rebellionLabel.setText(testoRibellione);
        abilityLabel.setText(GameInfo.abilitaDelTurno(current.getLeader()));
    }

    private static void mostra(Button pulsante, boolean visibile) {
        pulsante.setVisible(visibile);
        pulsante.setManaged(visibile);
    }

    private void aggiornaCostiUnita(City city, Player current) {
        UnitType scelta = unitCombo.getValue();
        unitCombo.setCellFactory(lista -> creaCellaUnita(city, current));
        unitCombo.setButtonCell(creaCellaUnita(city, current));
        unitCombo.setValue(scelta);
    }

    private ListCell<UnitType> creaCellaUnita(City city, Player current) {
        RecruitmentService reclutamento = servizi.recruitmentService();
        return new ListCell<>() {
            @Override
            protected void updateItem(UnitType tipo, boolean vuota) {
                super.updateItem(tipo, vuota);
                if (vuota || tipo == null) {
                    setText(null);
                    return;
                }
                String testo = MapView.bello(tipo.name());
                if (city != null) {
                    if (reclutamento.puoReclutareDaCitta(city, tipo, current.getLeader())) {
                        testo += " (" + reclutamento.costoUnita(tipo, city, current.getLeader()) + " oro)";
                    } else {
                        testo += " (non disponibile qui)";
                    }
                }
                setText(testo);
            }
        };
    }

    public Game getGame() {
        return game;
    }
}
