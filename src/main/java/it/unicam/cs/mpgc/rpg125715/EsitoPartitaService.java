package it.unicam.cs.mpgc.rpg125715;

public final class EsitoPartitaService {

    public enum Tipo {
        /** E' rimasto un solo giocatore. */
        CONQUISTA,
        /** Finiti i round della modalità a punti: vince chi ha più punti. */
        PUNTI,
        /** Nessun giocatore umano è rimasto in gioco. */
        UMANI_SCONFITTI
    }

    /** Esito finale; vincitore è null se hanno perso tutti gli umani; classifica è vuota se non si è giocato a punti. */
    public record Esito(Tipo tipo, Player vincitore, String classifica) {}

    private EsitoPartitaService() {}

    /** Segna la partita come finita se resta un solo giocatore o se non ci sono più umani. */
    public static void aggiornaFine(Game game) {
        if(game == null){throw new IllegalArgumentException("game null");}
        if(!game.isGameOver() && (game.haSoloUnPlayerAttivo() || !game.haUmaniAttivi())){game.setFinita();}
    }

    /** Esito di una partita finita. */
    public static Esito esito(Game game) {
        if(game == null){throw new IllegalArgumentException("game null");}
        if(!game.isGameOver()){throw new IllegalStateException("la partita non è finita");}

        if(game.haSoloUnPlayerAttivo()){
            for(Player p : game.getPlayers()){
                if(!p.isSconfitto()){return new Esito(Tipo.CONQUISTA, p, "");}
            }
        }
        if(!game.haUmaniAttivi()){return new Esito(Tipo.UMANI_SCONFITTI, null, "");}
        Player primo = PunteggioService.classifica(game).get(0).player();
        return new Esito(Tipo.PUNTI, primo, PunteggioService.testoClassifica(game));
    }
}