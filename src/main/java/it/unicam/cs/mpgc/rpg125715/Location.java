package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Location {
    private int id;
    private int x;
    private int y;
    private List<Location> adiacenti;
    private City city;
    private Army[] armies;
    private boolean contesa;


    public Location(int id, int x, int y) {
        if(id<0){throw new IllegalArgumentException("l'id non può essere negativo");}
        this.id = id;
        this.x = x;
        this.y = y;
        this.adiacenti = new ArrayList<Location>();
        this.armies = new Army[2];
        this.city = null;
        this.contesa = false;
    }

    public int getId() {return id;}

    public int getX() {return x;}
    public int getY() {return y;}

    // CITTA
    public boolean hasCity(){return this.city != null;}
    public City getCity(){return this.city;}
    public void addCity(City c){
        if(c == null){throw new IllegalArgumentException("city null");}
        if(this.city != null){throw new IllegalArgumentException("location già associata a città");}
        if(c.getLocation() != null){throw new IllegalArgumentException("la città è già associata a una location");}
        this.city = c;
        c.setLocation(this);
    }

    // ADIACENTI
    public boolean isAdiacente(Location loc){return this.adiacenti.contains(loc);}
    public void addAdiacente(Location loc){
        if(loc == null || this.adiacenti.contains(loc) || loc == this){return;}
        this.adiacenti.add(loc);
        loc.addAdiacente(this);
    }
    public List<Location> getAdiacenti() {return new ArrayList<Location>(this.adiacenti);}

    // ESERCITI
    public void addArmy(Army a){
        if(a == null){throw new IllegalArgumentException("l'esercito non può essere null");}
        for (Army army : armies) {
            if (army == a) {throw new IllegalArgumentException("esercito già nella location");}
            if(army != null && army.getOwner() == a.getOwner()){throw new IllegalArgumentException("non possono esseri due eserciti della stessa fazione nella stessa location");}
        }

        if(armies[0] == null){armies[0] = a;}
        else if(armies[1] == null){armies[1] = a;}
        else{throw new IllegalArgumentException("massimo due eserciti");}
        aggiornaContesa();
    }
    public void removeArmy(Army a){
        if(a == null){throw new IllegalArgumentException("l'esercito da rimuovere non può essere null");}
        for (int i = 0; i < armies.length; i++) {
            if (a ==  armies[i]) {
                armies[i] = null;
            }
        }
        if(armies[0] == null && armies[1] != null){armies[0] = armies[1];armies[1] = null;}
        aggiornaContesa();
    }
    public boolean hasArmy(){return armies[0] != null || armies[1] != null;}
    public boolean hasArmyPlayer(Player p){
        if(p == null){throw new IllegalArgumentException("p null");}
        for (Army army : armies) {
            if(army != null && army.getOwner() == p){return true;}
        }
        return false;
    }
    public int numeroArmy(){
        int count = 0;
        for(Army army : armies){
            if(army != null){count++;}
        }
        return count;
    }
    //da true se nella location ci sono eserciti nemici per player
    public boolean hasEnemyArmiesFor(Player player){
        if(player==null){throw new IllegalArgumentException("players null");}
        for(Army army : armies){
            if(army != null && army.getOwner() != player){return true;}
        }
        return false;
    }
    public Army getEnemyArmyFor(Army army){
        if(army == null){throw new IllegalArgumentException("army null");}
        for(Army army1 : armies){
            if( army1 != null && army1 != army && army1.getOwner() != army.getOwner()){return army1;}
        }
        return null;
    }
    public Army[] getArmies(){
        return Arrays.copyOf(armies, armies.length);
    }
    // LOCATION CONTESA
    public boolean isContesa(){return contesa;}
    private void aggiornaContesa(){
        contesa = armies[0] != null && armies[1] != null;
    }
}
