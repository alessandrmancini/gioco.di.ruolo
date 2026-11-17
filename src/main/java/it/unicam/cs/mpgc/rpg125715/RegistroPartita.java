package it.unicam.cs.mpgc.rpg125715;

import javafx.scene.control.TextArea;

/**
 * VISTA: il registro testuale della partita (l'area di testo in basso).
 * Scrive i messaggi, gli eventi del modello e l'intestazione di ogni nuovo round.
 */
public class RegistroPartita {

    private final TextArea area;
    private int roundMostrato;

    public RegistroPartita(TextArea area) {
        if(area == null){throw new IllegalArgumentException("area null");}
        this.area = area;
    }

    /** Aggiunge una riga al registro. */
    public void scrivi(String messaggio) {
        area.appendText(messaggio + "\n");
    }

    /** Svuota il registro (nuova partita o partita caricata). */
    public void pulisci() {
        area.clear();
        roundMostrato = 0;
    }

    /** Scrive gli eventi accumulati nel modello (ribellioni, città perse...). */
    public void scriviEventi(Game game) {
        for(String evento : game.prelevaEventi()){
            scrivi("! " + evento);
        }
    }

    /** Scrive l'intestazione del round se è cominciato un round nuovo. */
    public void scriviNuovoRound(Game game) {
        if(game.getRound() != roundMostrato && !game.isGameOver()){
            roundMostrato = game.getRound();
            scrivi("===== ROUND " + roundMostrato + (game.getModalita() == VictoryMode.PUNTI ? " di " + TurnService.MAX_ROUND : "") + " ======");
        }
    }

    /** Elenca i nemici assegnati a ogni bot. */
    public void scriviNemiciBot(Game game) {
        for(Player p : game.getPlayers()){
            if(p.getKind() != PlayerKind.BOT){continue;}
            String elenco = "";
            for(Player nemico : p.getNemici()){
                if(!elenco.isEmpty()){elenco += " e ";}
                elenco += nemico.getName();
            }
            scrivi(p.getName() + " ha come nemici: " + elenco);
        }
    }
}
