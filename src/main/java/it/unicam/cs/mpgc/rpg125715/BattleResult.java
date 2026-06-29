package it.unicam.cs.mpgc.rpg125715;

public record BattleResult(Army attaccante, Army difensore,
                           double tiroAttaccante, double tiroDifensore,
                           int totaleAttaccante, int totaleDifensore,
                           int perditeAttaccante, int perditeDifensore,
                           Army vincitore, Army sconfitto) {

    public boolean haVintoAttaccante() {return vincitore == attaccante;}
}