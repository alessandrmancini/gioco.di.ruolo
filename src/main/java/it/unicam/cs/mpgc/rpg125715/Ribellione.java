package it.unicam.cs.mpgc.rpg125715;

import java.io.Serializable;

/**
 * Ribellione di una singola città.
 * Conta i turni del proprietario passati senza che la città sia stata riappacificata.
 */
public class Ribellione implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int TURNI_PER_MALUS = 2;     //da qui in poi -1 oro a turno
    public static final int TURNI_PER_PERDITA = 4;   //da qui in poi la città è persa
    public static final int UNITA_PER_PACIFICARE = 4;

    private int turniAttiva;

    public Ribellione() {
        this.turniAttiva = 0;
    }

    /** Turni del proprietario passati senza sedare la ribellione. */
    public int getTurniAttiva() {return turniAttiva;}
    public void nextTurn() {turniAttiva++;}

    /** Vero se la città costa 1 oro a turno (da {@link #TURNI_PER_MALUS} turni in poi). */
    public boolean applicaMalusOro() {return turniAttiva >= TURNI_PER_MALUS;}
    /** Vero se la città va persa e diventa indipendente (da {@link #TURNI_PER_PERDITA} turni in poi). */
    public boolean cittaPersa() {return turniAttiva >= TURNI_PER_PERDITA;}
}