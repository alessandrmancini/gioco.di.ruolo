package it.unicam.cs.mpgc.rpg125715;

import java.util.List;
import java.util.Map;

/**
 * Base comune delle strategie dei leader: contiene i calcoli che servono a più strategie
 * (forza degli eserciti, margine di attacco, distanze, città di confine).
 */
public abstract class StrategiaBase implements LeaderStrategy {
    protected final NemiciService nemiciService;

    protected StrategiaBase(NemiciService nemiciService) {
        if(nemiciService == null){throw new NullPointerException("nemiciService is null");}
        this.nemiciService = nemiciService;
    }

    //Attacco dell'esercito con il bonus del leader
    protected static int attaccoTotale(Player bot, Army esercito){
        return esercito.getAttaccoTotale()+ bot.getLeader().getBonusAttacco();
    }
    //difesa del bersaglio: unità del difensore più bonus della città (0 se non c'è un esercito nemico=
    protected static int difesaBersaglio (Location destinazione, Army nemico){
        if(nemico == null){return 0;}
        return nemico.getDifesaTotale() + BattleService.calcolaBonusDifesaTerritorio(destinazione.getCity());
    }
    //se return > 0 -> attacco
    protected static int margineAttacco(Player bot, Army esercito, Location destinazione, Army nemico){
        return attaccoTotale(bot, esercito) - difesaBersaglio(destinazione, nemico);
    }
    //eserciti che possono ancora muovere e vicini alla destinazione
    protected int esercitiAdiacentiA(Player bot, Location destinazione){
        int totale = 0;
        for(Army a : bot.getEserciti()){
            if(!a.isVuoto() && a.isPuoMuovere() && a.getPosizione() != null && a.getPosizione().isAdiacente(destinazione)){
                totale++;
            }
        }
        return totale;
    }

    /** Numero di altri propri eserciti adiacenti alla destinazione. */
    protected int alleatiAccanto(Player bot, Army esercito, Location destinazione){
        int totale = 0;
        for(Army a : bot.getEserciti()){
            if(a!=esercito && !a.isVuoto() && a.getPosizione()!= null && a.getPosizione().isAdiacente(destinazione)){
                totale++;
            }
        }
        return totale;
    }

    /** Numero di nodi adiacenti alla destinazione in cui c'è un esercito nemico. */
    protected int nemiciAccanto(Player bot, Location destinazione){
        int totale = 0;
        for(Location l : destinazione.getAdiacenti()){
            if(l.hasEnemyArmiesFor(bot)){totale++;}
        }
        return totale;
    }

    /**
     * Confronta due eserciti: prima per numero di elefanti (se richiesto), poi per numero di unità,
     * poi per attacco totale. Positivo se {@code a} è più forte.
     */
    protected int confrontaForza(Army a, Army b, boolean contaElefanti){
        if(contaElefanti){
            int c = Integer.compare(a.getNumeroUnitaPerTipo(UnitType.ELEFANTI), b.getNumeroUnitaPerTipo(UnitType.ELEFANTI));
            if(c != 0){return c;}
        }
        int c = Integer.compare(a.getNumeroUnita(), b.getNumeroUnita());
        if(c != 0){return c;}
        return Integer.compare(a.getAttaccoTotale(), b.getAttaccoTotale());
    }

    /** Esercito più forte del bot secondo {@link #confrontaForza}, oppure null. */
    protected Army esercitoPiuForte(Player bot, boolean contaElefanti){
        Army migliore = null;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto()){continue;}
            if(migliore == null || confrontaForza(a, migliore, contaElefanti)>0){migliore = a;}
        }
        return migliore;
    }

    /** Numero di passi tra l'esercito e la capitale del bot (Integer.MAX_VALUE se non raggiungibile). */
    protected int distanzaDallaCapitale(Player bot, Army esercito) {
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null || esercito.getPosizione() == null){return Integer.MAX_VALUE;}
        Integer d = nemiciService.distanzeDa(List.of(capitale.getLocation())).get(esercito.getPosizione());
        return d == null ? Integer.MAX_VALUE : d;
    }

    // Una città è "di confine" se accanto c'è un esercito nemico.
    protected static boolean eCittaDiConfine(Player bot, Location l) {
        for(Location vicina : l.getAdiacenti()){
            if(vicina.hasEnemyArmiesFor(bot)){return true;}
        }
        return false;
    }

    /** Distanza in passi di ogni nodo dalla capitale del bot. */
    protected Map<Location, Integer> distanzeDallaCapitale(Player bot) {
        return nemiciService.distanzeDa(List.of(bot.getCapitale().getLocation()));
    }
}
