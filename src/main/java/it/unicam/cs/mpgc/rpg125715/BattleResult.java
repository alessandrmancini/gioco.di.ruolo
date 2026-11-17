package it.unicam.cs.mpgc.rpg125715;

/** Risultato di una battaglia: tiri dei dadi, totali, perdite, vincitore e sconfitto. */
public record BattleResult(Army attaccante, Army difensore,
                           double tiroAttaccante, double tiroDifensore,
                           int totaleAttaccante, int totaleDifensore,
                           int perditeAttaccante, int perditeDifensore,
                           Army vincitore, Army sconfitto) {

    /** Vero se ha vinto chi attaccava. */
    public boolean haVintoAttaccante() {return vincitore == attaccante;}
}