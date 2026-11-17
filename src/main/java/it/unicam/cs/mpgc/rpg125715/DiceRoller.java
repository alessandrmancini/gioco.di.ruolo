package it.unicam.cs.mpgc.rpg125715;

/** Dado usato nelle battaglie. È un'interfaccia così nei test si può usare un dado prevedibile. */
public interface DiceRoller {
    /** Moltiplicatore della battaglia per il giocatore (per esempio 0.8 ... 1.3). */
    double lanciaDado(Player p);
}
