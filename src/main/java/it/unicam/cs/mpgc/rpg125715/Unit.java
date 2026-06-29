package it.unicam.cs.mpgc.rpg125715;

public record Unit(UnitType type, int attacco, int difesa, int costo) {
    public Unit {
        if (type == null) {
            throw new IllegalArgumentException("il tipo di unità non può essere null");
        }
        if (attacco < 0 || difesa < 0 || costo < 0) {
            throw new IllegalArgumentException("le statistiche dell'unità non possono essere negative");
        }
    }
}
