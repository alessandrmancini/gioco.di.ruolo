package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int id;
    private final String name;
    private final LeaderType leader;
    private int oro;
    private final Territory territorio;
    private final List<Army> eserciti;
    private boolean sconfitto;

    public Player(int id, String name, LeaderType leader,int oroIniziale, Territory territorio) {
        if(id<0){throw new IllegalArgumentException("id negativo");}
        if(name == null || name.isBlank()){throw  new IllegalArgumentException("il nome del player non può essere vuoto");}
        if(leader == null){throw new IllegalArgumentException("il leader non può essere null");}
        if(oroIniziale<0){throw new IllegalArgumentException("l'oro non può essere negativo");}
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.oro = oroIniziale;
        this.territorio = territorio;
        this.eserciti = new ArrayList<>();
        this.sconfitto = false;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public LeaderType getLeader() {return leader;}
    public boolean isSconfitto() {return sconfitto;}
    public void setSconfitto() {this.sconfitto = true;}

    // ORO
    public int getOro() {return oro;}
    public void setOro(int oro){
        if(oro<0){throw new IllegalArgumentException("l'oro non può essere negativo");}
        this.oro = oro;
    }
    public void aggiungiOro(int q) {
        if(q<0){
            throw new IllegalArgumentException("l'oro da aggiungere non può essere negativo");
        }
        this.oro += q;
    }
    public void spendiOro(int q){
        if(q<0){throw new IllegalArgumentException("la quantità da spendere non può essere negativa");}
        if(q>oro){return;}
        this.oro -= q;
    }

    public Territory getTerritorio() {return territorio;}
    public List<Army> getEserciti() {return new ArrayList<>(eserciti);}

    //GESTIONE CAPITALE
    public City getCapitale() {return territorio.getCapitale();}
    public boolean hasCapitale(){return getCapitale()!=null;}

    public void addArmy(Army a){
        if(a!=null && !eserciti.contains(a)){
            eserciti.add(a);
        }
    }
    public void removeArmy(Army a){eserciti.remove(a);}

    public int numeroCitta(){return territorio.getCities().size();}
    public int numeroEserciti(){return eserciti.size();}
}
