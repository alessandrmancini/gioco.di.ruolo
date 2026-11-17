package it.unicam.cs.mpgc.rpg125715;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * VISTA: la schermata "Nuova partita" (numero di umani, leader di ogni umano, modalità di vittoria).
 * Non crea la partita: dice solo al controller cosa ha scelto l'utente.
 */
public class ImpostazionePartitaView {

    private final Pane overlay;
    private final Spinner<Integer> spinnerUmani;
    private final VBox righeUmani;
    private final ComboBox<VictoryMode> comboModalita;
    private final Label infoModalita;
    private final Button pulsanteAnnulla;

    private final List<ComboBox<LeaderType>> comboLeader = new ArrayList<>();
    private boolean aggiornandoCombo;

    public ImpostazionePartitaView(Pane overlay, Spinner<Integer> spinnerUmani, VBox righeUmani,
                                   ComboBox<VictoryMode> comboModalita, Label infoModalita, Button pulsanteAnnulla) {
        if(overlay == null || spinnerUmani == null || righeUmani == null || comboModalita == null || infoModalita == null || pulsanteAnnulla == null){
            throw new IllegalArgumentException("elementi della schermata null");
        }
        this.overlay = overlay;
        this.spinnerUmani = spinnerUmani;
        this.righeUmani = righeUmani;
        this.comboModalita = comboModalita;
        this.infoModalita = infoModalita;
        this.pulsanteAnnulla = pulsanteAnnulla;
        inizializza();
    }

    private void inizializza() {
        // da 1 a tutti i leader controllati da umani
        spinnerUmani.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, LeaderType.values().length, 1));
        spinnerUmani.valueProperty().addListener((obs, vecchio, nuovo) -> ricostruisciRighe());
        ricostruisciRighe();

        comboModalita.getItems().setAll(VictoryMode.values());
        comboModalita.setConverter(ConvertitoriTesto.modalita());
        comboModalita.valueProperty().addListener((obs, vecchio, nuovo) -> {
            if(nuovo != null){infoModalita.setText(nuovo.getDescrizione());}
        });
        comboModalita.setValue(VictoryMode.PUNTI);
    }

    // ================= VISIBILITA' =================

    /** Mostra la schermata; "Annulla" c'è solo se esiste già una partita a cui tornare. */
    public void mostra(boolean partitaInCorso) {
        pulsanteAnnulla.setVisible(partitaInCorso);
        pulsanteAnnulla.setManaged(partitaInCorso);
        overlay.setVisible(true);
    }

    public void nascondi() {overlay.setVisible(false);}

    // ================= SCELTE DELL'UTENTE =================

    /** Leader scelti dai giocatori umani, nell'ordine Giocatore 1, 2, ... */
    public List<LeaderType> leaderUmani() {
        List<LeaderType> umani = new ArrayList<>();
        for(ComboBox<LeaderType> c : comboLeader){umani.add(c.getValue());}
        return umani;
    }

    public VictoryMode modalita() {return comboModalita.getValue();}

    // ================= RIGHE DEI GIOCATORI =================

    /** Ricostruisce le righe "Giocatore N - leader" in base al numero di umani scelto. */
    private void ricostruisciRighe() {
        int n = spinnerUmani.getValue();
        List<LeaderType> scelte = leaderUmani();
        comboLeader.clear();
        righeUmani.getChildren().clear();

        for(int i = 0; i < n; i++){
            LeaderType iniziale;
            if(i < scelte.size()){
                iniziale = scelte.get(i);
            } else {
                iniziale = primoLeaderLibero(scelte);
                scelte.add(iniziale);
            }
            righeUmani.getChildren().add(creaRiga(i, iniziale));
        }
    }

    private VBox creaRiga(int indice, LeaderType iniziale) {
        ComboBox<LeaderType> combo = new ComboBox<>();
        combo.getItems().setAll(LeaderType.values());
        combo.setConverter(ConvertitoriTesto.leader());
        combo.setValue(iniziale);
        combo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(combo, Priority.ALWAYS);

        Rectangle campione = new Rectangle(16, 16, MapView.colorOf(iniziale));
        campione.setArcWidth(4);
        campione.setArcHeight(4);
        Label etichetta = new Label("Giocatore " + (indice + 1));
        etichetta.setMinWidth(90);

        Label info = new Label(GameInfo.riassuntoLeader(iniziale));
        info.setWrapText(true);
        info.getStyleClass().add("leader-info");

        ImageView faccia = new ImageView();
        faccia.setFitWidth(56);
        faccia.setFitHeight(56);
        faccia.setPreserveRatio(true);
        faccia.setImage(immagineLeader(iniziale));

        comboLeader.add(combo);
        combo.valueProperty().addListener((obs, vecchio, nuovo) -> {
            if(nuovo == null){return;}
            campione.setFill(MapView.colorOf(nuovo));
            info.setText(GameInfo.riassuntoLeader(nuovo));
            faccia.setImage(immagineLeader(nuovo));
            if(aggiornandoCombo){return;}
            // se il leader era già di un altro umano, i due si scambiano
            for(ComboBox<LeaderType> altro : comboLeader){
                if(altro != combo && altro.getValue() == nuovo){
                    aggiornandoCombo = true;
                    altro.setValue(vecchio);
                    aggiornandoCombo = false;
                    break;
                }
            }
        });

        HBox riga = new HBox(10, faccia, campione, etichetta, combo);
        riga.setAlignment(Pos.CENTER_LEFT);
        return new VBox(4, riga, info);
    }

    private static LeaderType primoLeaderLibero(List<LeaderType> usati) {
        for(LeaderType l : LeaderType.values()){
            if(!usati.contains(l)){return l;}
        }
        throw new IllegalStateException("nessun leader libero");
    }

    private static Image immagineLeader(LeaderType leader) {
        String nomeFile = "leaders/" + leader.name().toLowerCase() + ".png";
        try(InputStream in = ImpostazionePartitaView.class.getResourceAsStream(nomeFile)){
            if(in == null){return null;}
            return new Image(in);
        }catch(IOException e){
            return null;
        }
    }
}
