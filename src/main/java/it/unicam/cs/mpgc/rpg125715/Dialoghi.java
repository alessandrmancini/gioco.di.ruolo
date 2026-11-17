package it.unicam.cs.mpgc.rpg125715;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/** VISTA: le finestre secondarie del gioco (tutorial, legenda, fine partita, giocatore eliminato). */
public final class Dialoghi {

    private Dialoghi() {}

    // ================= FINE PARTITA =================
    /** Frase che annuncia il risultato della partita. */
    public static String testoEsito(EsitoPartitaService.Esito esito) {
        return switch (esito.tipo()){
            case CONQUISTA -> "Vincitore per conquista: " + nomeVincitore(esito.vincitore());
            case PUNTI -> "Vincitore ai punti: " + nomeVincitore(esito.vincitore());
            case UMANI_SCONFITTI -> "Tutti i giocatori umani sono stati sconfitti.";
        };
    }

    /** Finestra di fine partita, con la classifica se si è giocato a punti. */
    public static void mostraEsito(EsitoPartitaService.Esito esito) {
        String testo = testoEsito(esito);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, testo);
        alert.setTitle("Partita finita");
        alert.setHeaderText("Partita finita");
        if(!esito.classifica().isEmpty()){
            TextArea tabella = new TextArea(esito.classifica());
            tabella.setEditable(false);
            tabella.setPrefSize(640, 360);
            alert.getDialogPane().setContent(new VBox(8, new Label(testo), tabella));
            alert.setResizable(true);
        }
        alert.show();
    }

    private static String nomeVincitore(Player p) {
        return p.getName() + " (" + GameInfo.nomeLeader(p.getLeader()) + ")";
    }

    // ================= GIOCATORE ELIMINATO =================

    /** Avviso che uno o più giocatori sono stati eliminati. */
    public static void mostraEliminati(String testo) {
        Alert alert = new Alert(Alert.AlertType.WARNING, testo);
        alert.setTitle("Giocatore eliminato");
        alert.setHeaderText("Giocatore eliminato");
        alert.show();
    }

    // ================= LEGENDA =================

    /** Finestra con la legenda delle regole (testo di GameInfo.legenda()). */
    public static void mostraLegenda() {
        TextArea testo = new TextArea(GameInfo.legenda());
        testo.setEditable(false);
        testo.setWrapText(true);
        testo.setPrefSize(660, 500);

        Alert finestra = new Alert(Alert.AlertType.INFORMATION);
        finestra.setTitle("Legenda");
        finestra.setHeaderText("Costi, regole e leader");
        finestra.getDialogPane().setContent(testo);
        finestra.setResizable(true);
        finestra.show();
    }

    // ================= TUTORIAL =================

    /** Finestra del tutorial a pagine; non è modale, così si può continuare a guardare il gioco. */
    public static void mostraTutorial(Window proprietario) {
        List<Tutorial.Pagina> pagine = Tutorial.pagine();
        int[] indice = {0};

        Label titolo = new Label();
        titolo.getStyleClass().add("title");
        Label contatore = new Label();
        TextArea testo = new TextArea();
        testo.setEditable(false);
        testo.setWrapText(true);
        testo.setPrefSize(600, 380);
        VBox.setVgrow(testo, Priority.ALWAYS);

        Button indietro = new Button("Indietro");
        Button avanti = new Button("Avanti");
        Button chiudi = new Button("Chiudi");

        Runnable aggiorna = () -> {
            Tutorial.Pagina p = pagine.get(indice[0]);
            titolo.setText(p.titolo());
            testo.setText(p.testo());
            contatore.setText("Pagina " + (indice[0] + 1) + " di " + pagine.size());
            indietro.setDisable(indice[0] == 0);
            avanti.setDisable(indice[0] == pagine.size() - 1);
        };
        indietro.setOnAction(e -> {indice[0]--; aggiorna.run();});
        avanti.setOnAction(e -> {indice[0]++; aggiorna.run();});

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox pulsanti = new HBox(8, contatore, spazio, indietro, avanti, chiudi);
        pulsanti.setAlignment(Pos.CENTER_LEFT);

        VBox radice = new VBox(10, titolo, testo, pulsanti);
        radice.setPadding(new Insets(14));
        radice.getStyleClass().add("setup-card");

        Stage finestra = new Stage();
        finestra.setTitle("Tutorial");
        finestra.initOwner(proprietario);
        finestra.initModality(Modality.NONE);
        Scene scena = new Scene(radice);
        scena.getStylesheets().add(Dialoghi.class.getResource("game.css").toExternalForm());
        finestra.setScene(scena);
        chiudi.setOnAction(e -> finestra.close());
        aggiorna.run();
        finestra.show();
    }
}
