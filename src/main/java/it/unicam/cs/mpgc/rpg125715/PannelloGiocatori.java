package it.unicam.cs.mpgc.rpg125715;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** VISTA: l'elenco dei giocatori nel pannello laterale (colore, nome, oro, città, eserciti, punti). */
public class PannelloGiocatori {

    private final VBox contenitore;

    public PannelloGiocatori(VBox contenitore) {
        if(contenitore == null){throw new IllegalArgumentException("contenitore null");}
        this.contenitore = contenitore;
    }

    public void aggiorna(Game game) {
        contenitore.getChildren().clear();
        if(game == null){return;}

        Player corrente = game.getCurrentPlayer();
        boolean aPunti = game.getModalita() == VictoryMode.PUNTI;
        for(Player p : game.getPlayers()){
            boolean inTurno = (p == corrente && !game.isGameOver());
            contenitore.getChildren().add(creaRiga(game, p, inTurno, aPunti));
        }
    }

    private HBox creaRiga(Game game, Player p, boolean inTurno, boolean aPunti) {
        Rectangle campione = new Rectangle(14, 14, MapView.colorOf(p.getLeader()));
        campione.setArcWidth(4);
        campione.setArcHeight(4);
        campione.setStroke(Color.web("#10162a"));

        String titolo = (inTurno ? "> " : "") + p.getName()
                + (p.getKind() == PlayerKind.HUMAN ? " - " + GameInfo.nomeLeader(p.getLeader()) : "")
                + (p.isSconfitto() ? " (sconfitto)" : "");
        Label nome = new Label(titolo);
        nome.getStyleClass().add(inTurno ? "player-current" : "player-name");

        int ribellioni = p.numeroCittaInRibellione();
        Label statistiche = new Label((aPunti && !p.isSconfitto() ? "Punti " + PunteggioService.punti(game, p) + " | " : "")
                + "Oro " + p.getOro() + "  |  Città " + p.numeroCitta()
                + "  |  Eserciti " + p.numeroEserciti() + "/" + ArmyService.MAX_ESERCITI
                + (ribellioni > 0 ? " | Ribellione " + ribellioni : ""));
        statistiche.getStyleClass().add("player-stats");

        HBox riga = new HBox(8, campione, new VBox(1, nome, statistiche));
        riga.setAlignment(Pos.CENTER_LEFT);
        return riga;
    }
}
