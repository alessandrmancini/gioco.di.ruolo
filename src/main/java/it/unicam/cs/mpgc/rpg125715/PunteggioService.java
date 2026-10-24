package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calcola i punti della modalità a punti e la classifica.
 * I valori sono stati scelti con simulazioni di migliaia di partite tra bot
 * (pareggi per il primo posto ~2,6%, nessuna voce pesa più del 55% del totale).
 */
public class PunteggioService {

    public static final int PUNTI_CAPITALE = 5;          //per ognuna delle 8 capitali iniziali controllata
    public static final int PUNTI_METROPOLI = 3;
    public static final int PUNTI_CITTA = 2;
    public static final int PUNTI_AVAMPOSTO = 1;
    public static final int PUNTI_ACCAMPAMENTO = 0;
    public static final int PENALITA_RIBELLIONE = 1;      //per ogni città in ribellione a fine partita
    public static final int BATTAGLIE_PER_PUNTO = 2;      //1 punto ogni 2 battaglie vinte
    public static final int BONUS_PIU_ORO = 3;
    public static final int BONUS_PIU_CITTA = 2;

    public record Punteggio(Player player, int capitali, int metropoli, int citta, int avamposti,
                            int ribellioni, int battaglieVinte, boolean piuOro, boolean piuCitta, int totale) {}

    private PunteggioService() {}

    /** Classifica ordinata: prima chi ha più punti; a pari punti più città, poi più oro. Gli sconfitti sono in fondo con 0 punti. */
    public static List<Punteggio> classifica(Game game) {
        if (game == null) {throw new IllegalArgumentException("game null");}

        Set<String> capitali = new HashSet<>();
        for (CityType t : CityType.values()) {
            if (t.isCapital()) {capitali.add(t.getDisplayName());}
        }

        int maxOro = 0;
        int maxCitta = 0;
        for (Player p : game.getPlayers()) {
            if (p.isSconfitto()) {continue;}
            maxOro = Math.max(maxOro, p.getOro());
            maxCitta = Math.max(maxCitta, p.numeroCitta());
        }

        List<Punteggio> risultato = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            if (p.isSconfitto()) {
                risultato.add(new Punteggio(p, 0, 0, 0, 0, 0, p.getBattaglieVinte(), false, false, 0));
                continue;
            }
            int cap = 0, metropoli = 0, citta = 0, avamposti = 0, ribellioni = 0;
            for (City c : p.getTerritorio().getCities()) {
                if (capitali.contains(c.getName())) {cap++;}
                if (c.hasRibellione()) {ribellioni++;}
                switch (c.getLevel()) {
                    case METROPOLI -> metropoli++;
                    case CITTA -> citta++;
                    case AVAMPOSTO -> avamposti++;
                    case ACCAMPAMENTO -> {}
                }
            }
            boolean piuOro = maxOro > 0 && p.getOro() == maxOro;
            boolean piuCitta = maxCitta > 0 && p.numeroCitta() == maxCitta;

            int totale = cap * PUNTI_CAPITALE
                    + metropoli * PUNTI_METROPOLI
                    + citta * PUNTI_CITTA
                    + avamposti * PUNTI_AVAMPOSTO
                    - ribellioni * PENALITA_RIBELLIONE
                    + (p.getBattaglieVinte() / BATTAGLIE_PER_PUNTO)
                    + (piuOro ? BONUS_PIU_ORO : 0)
                    + (piuCitta ? BONUS_PIU_CITTA : 0);
            risultato.add(new Punteggio(p, cap, metropoli, citta, avamposti, ribellioni, p.getBattaglieVinte(), piuOro, piuCitta, Math.max(0, totale)));
        }

        risultato.sort(Comparator
                .comparing((Punteggio s) -> s.player().isSconfitto())
                .thenComparing(Comparator.comparingInt(Punteggio::totale).reversed())
                .thenComparing(Comparator.comparingInt((Punteggio s) -> s.player().numeroCitta()).reversed())
                .thenComparing(Comparator.comparingInt((Punteggio s) -> s.player().getOro()).reversed()));
        return risultato;
    }

    /** Testo della classifica, una riga per giocatore. */
    public static String testoClassifica(Game game) {
        StringBuilder sb = new StringBuilder();
        int posto = 1;
        for (Punteggio s : classifica(game)) {
            sb.append(posto++).append(". ").append(s.player().getName())
                    .append(" (").append(s.player().getLeader().getDisplayName()).append(")");
            if (s.player().isSconfitto()) {
                sb.append(" - sconfitto\n");
                continue;
            }
            sb.append(": ").append(s.totale()).append(" punti\n")
                    .append("     capitali ").append(s.capitali()).append("x").append(PUNTI_CAPITALE)
                    .append(", metropoli ").append(s.metropoli()).append("x").append(PUNTI_METROPOLI)
                    .append(", città ").append(s.citta()).append("x").append(PUNTI_CITTA)
                    .append(", avamposti ").append(s.avamposti()).append("x").append(PUNTI_AVAMPOSTO)
                    .append(", battaglie vinte ").append(s.battaglieVinte()).append(" (+").append(s.battaglieVinte() / BATTAGLIE_PER_PUNTO).append(")");
            if (s.ribellioni() > 0) {sb.append(", ribellioni -").append(s.ribellioni() * PENALITA_RIBELLIONE);}
            if (s.piuOro()) {sb.append(", più oro +").append(BONUS_PIU_ORO);}
            if (s.piuCitta()) {sb.append(", più città +").append(BONUS_PIU_CITTA);}
            sb.append("\n");
        }
        return sb.toString();
    }

    /** Punti di un singolo giocatore (per il pannello giocatori). */
    public static int punti(Game game, Player player) {
        for (Punteggio s : classifica(game)) {
            if (s.player() == player) {return s.totale();}
        }
        return 0;
    }
}