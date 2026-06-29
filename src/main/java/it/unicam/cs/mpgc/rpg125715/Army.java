package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;

public class Army {
    private final int id;
    private final Player owner;
    private Location posizione;
    private final List<Unit> units;
    private boolean puoMuovere;

    public Army(int id, Player owner, Location posizione) {
        if(id < 0){throw new IllegalArgumentException("id negativo");}
        if (owner == null) {
            throw new IllegalArgumentException("Il proprietario dell'esercito non può essere null");
        }
        if (posizione == null) {
            throw new IllegalArgumentException("La posizione dell'esercito non può essere null");
        }

        this.id = id;
        this.owner = owner;
        this.posizione = posizione;
        this.units = new ArrayList<>();
        this.puoMuovere = true;

        owner.addArmy(this);
        posizione.addArmy(this);
    }

    public int getId() {return id;}

    public Player getOwner() {return owner;}

    public Location getPosizione() {return posizione;}
    void aggiornaPosizione(Location l) {
        if(l == null){throw new IllegalArgumentException("posizione null");}
        this.posizione = l;
    }

    //GESTIONE MOVIMENTO
    public boolean isPuoMuovere() {return puoMuovere;}
    public void setPuoMuovere(boolean puoMuovere) {this.puoMuovere = puoMuovere;}
    public void bloccaMovimento() {this.puoMuovere = false;}
    public void sbloccaMovimento() {this.puoMuovere = true;}

    //GESTIONE UNITA
    public List<Unit> getUnits() {return new ArrayList<>(units);}
    public void addUnit(Unit unit) {
        if (unit != null) {
            units.add(unit);
        }
    }
    public void removeUnit(Unit unit) {units.remove(unit);}
    public void removeAll(){
        units.clear();
    }

    //STATISTICHE
    public int getAttaccoTotale() {
        int totale = 0;
        for (Unit unit : units) {
            totale += unit.attacco();
        }
        return totale;
    }

    public int getDifesaTotale() {
        int totale = 0;
        for (Unit unit : units) {
            totale += unit.difesa();
        }
        return totale;
    }

    public int getCostoTotale() {
        int totale = 0;
        for (Unit unit : units) {
            totale += unit.costo();
        }
        return totale;
    }

    public int getNumeroUnita() {return units.size();}

    public int getNumeroUnitaPerTipo(UnitType type) {
        if(type == null){throw new IllegalArgumentException("tipo unità null");}
        int count = 0;
        for (Unit unit : units) {
            if (unit.type() == type) {
                count++;
            }
        }
        return count;
    }

    public boolean isVuoto() {return units.isEmpty();}

    private int rimuoviPerTipo(UnitType type, int quantitaDaRimuovere) {
        int rimaste = quantitaDaRimuovere;
        for (int i = units.size() - 1; i >= 0 && rimaste > 0; i--) {
            if (units.get(i).type() == type) {
                units.remove(i);
                rimaste--;
            }
        }
        return rimaste;
    }

    public void rimuoviUnitaPerPriorita(int quantita) {
        if (quantita < 0) {throw new IllegalArgumentException("La quantità da rimuovere non può essere negativa");}

        int rimanenti = Math.min(quantita, units.size());

        rimanenti = rimuoviPerTipo(UnitType.FANTERIA, rimanenti);
        rimanenti = rimuoviPerTipo(UnitType.CAVALLERIA, rimanenti);
        rimanenti = rimuoviPerTipo(UnitType.ASSEDIO, rimanenti);
        rimanenti = rimuoviPerTipo(UnitType.ELEFANTI, rimanenti);
        rimanenti = rimuoviPerTipo(UnitType.SPECIALI, rimanenti);
    }
}