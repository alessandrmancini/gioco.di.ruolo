package it.unicam.cs.mpgc.rpg125715;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * CONTROLLER dei bot: fa giocare i bot un'azione alla volta, con una pausa e l'animazione
 * dello spostamento, così l'utente può seguire le loro mosse. La logica delle scelte è in BotTurnService.
 */
public class EsecutoreTurniBot {

    /** Cosa deve fare il controller principale mentre giocano i bot. */
    public interface Ascoltatore {
        /** Aggiorna mappa e pannelli. */
        void aggiornaVista();
        /** Un bot ha finito il turno: controlla se la partita è finita. */
        void fineTurnoBot();
        /** Tocca di nuovo a un giocatore umano. */
        void turnoUmano();
    }

    private static final int PAUSA_BOT_MS = 900;

    private final MapView mapView;
    private final RegistroPartita registro;
    private final Ascoltatore ascoltatore;

    private Game game;
    private BotTurnService botTurnService;
    private Player botCorrente;
    private boolean saltaPause;
    private int generazione;    //cambia a ogni avvio/stop: le pause rimaste in sospeso di una partita vecchia vengono ignorate

    public EsecutoreTurniBot(MapView mapView, RegistroPartita registro, Ascoltatore ascoltatore) {
        if(mapView == null || registro == null || ascoltatore == null){throw new IllegalArgumentException("argomenti null");}
        this.mapView = mapView;
        this.registro = registro;
        this.ascoltatore = ascoltatore;
    }

    /** Fa giocare i bot finché non tocca a un umano o la partita finisce. */
    public void avvia(Game game, BotTurnService botTurnService) {
        if(game == null || botTurnService == null){throw new IllegalArgumentException("game o botTurnService null");}
        this.game = game;
        this.botTurnService = botTurnService;
        this.saltaPause = false;
        this.botCorrente = null;
        generazione++;
        passo(generazione);
    }

    /** Interrompe i bot (per esempio quando si inizia o si carica un'altra partita). */
    public void ferma() {
        generazione++;
        botCorrente = null;
        game = null;
    }

    /** I bot rimanenti giocano senza pause né animazioni. */
    public void saltaPause() {saltaPause = true;}

    /** Vero mentre sta giocando un bot. */
    public boolean isInCorso() {return botCorrente != null;}

    private void passo(int gen) {
        if(gen != generazione || game == null){return;}
        if(game.isGameOver() || game.getCurrentPlayer().getKind() != PlayerKind.BOT){
            botCorrente = null;
            if(!game.isGameOver()){ascoltatore.turnoUmano();}
            ascoltatore.aggiornaVista();
            return;
        }
        Player bot = game.getCurrentPlayer();
        if(botCorrente != bot){
            botCorrente = bot;
            botTurnService.iniziaTurnoBot();
            registro.scrivi("Gioca " + bot.getName() + " (" + GameInfo.nomeLeader(bot.getLeader()) + ")");
        }
        ascoltatore.aggiornaVista();

        PauseTransition pausa = new PauseTransition(Duration.millis(saltaPause ? 0 : PAUSA_BOT_MS));
        pausa.setOnFinished(e -> eseguiAzione(gen, bot));
        pausa.play();
    }

    private void eseguiAzione(int gen, Player bot) {
        if(gen != generazione || game == null){return;}
        String esito;
        try{
            esito = botTurnService.eseguiProssimaAzione(game);
        }catch(RuntimeException ex){
            registro.scrivi("Errore del bot: " + ex.getMessage());
            esito = null;
        }
        if(esito != null){
            registro.scrivi(esito);
            registro.scriviEventi(game);
            Location da = botTurnService.getUltimaPartenza();
            Location a = botTurnService.getUltimaDestinazione();
            if(da != null && a != null && !saltaPause){
                mapView.animaSpostamento(da, a, MapView.colorOf(bot.getLeader()), () -> passo(gen));
            } else {
                passo(gen);
            }
        } else {
            botTurnService.terminaTurno(game);
            registro.scriviNuovoRound(game);
            registro.scriviEventi(game);
            ascoltatore.fineTurnoBot();
            botCorrente = null;
            passo(gen);
        }
    }
}
