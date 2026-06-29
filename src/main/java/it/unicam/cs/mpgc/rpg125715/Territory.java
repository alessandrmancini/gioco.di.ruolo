package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;

public class Territory {
    private final int id;
    private final String name;
    private final List<City> cities;
    private final List<Territory> confinanti;
    private Player owner;
    private City capitale;
    private Ribellione ribellione;

    public Territory(int id, String name){
        if(id < 0){throw new IllegalArgumentException("l'id non può essere negativo");}
        if(name == null){throw new IllegalArgumentException("nome null");}
        this.id = id;
        this.name = name;
        this.cities = new ArrayList<>();
        this.confinanti = new ArrayList<>();
        this.owner = null;
        this.capitale = null;
        this.ribellione = null;
    }

    public int getId() {return id;}
    public String getName() {return name;}

    public Player getOwner() {return owner;}
    public void setOwner(Player owner) {
        if(owner == null){throw new IllegalArgumentException("il proprietario non può essere null");}
        this.owner = owner;
    }

    //SPECIALIZZAZIONI CAPITALE
    private int punteggioSviluppo(City c){
        return switch (c.getLevel()){
            case ACCAMPAMENTO -> 0;
            case AVAMPOSTO -> 1;
            case CITTA -> 2;
            case METROPOLI -> 3;
        };
    }
    private int punteggioSpecializzazione(City c){
        return switch (c.getSpecialization()){
            case NESSUNA -> 0;
            case AGRICOLA -> 1;
            case MILITARE -> 2;
            case COMMERCIALE -> 3;
        };
    }

    //CAPITALE
    private City prioritaCapitale(List<City> cities){
        if(cities.isEmpty()){throw new IllegalArgumentException("non hai città");}
        City migliore = cities.getFirst();
        for(int i = 1; i < cities.size(); i++){
            int confrontoLivello = Integer.compare(punteggioSviluppo(migliore), punteggioSviluppo(cities.get(i)));
            if(confrontoLivello == 0){
                int confrontoSpecializzazione = Integer.compare(punteggioSpecializzazione(migliore), punteggioSpecializzazione(cities.get(i)));
                if(confrontoSpecializzazione == 0 || confrontoSpecializzazione == 1){continue;}
                else {migliore = cities.get(i);}
            }
            else if(confrontoLivello == 1){continue;}
            else {migliore = cities.get(i);}
        }
        return migliore;
    }

    public City getCapitale() {return capitale;}
    public boolean isCapitale(City c){return c!= null  && c == capitale;}

    public void nominaCapitale(Player p){
        if(p == null) {throw new IllegalArgumentException("Player o città non possono essere null");}
        if(owner != p){throw new IllegalArgumentException("solo il proprietario del territorio può nominare la capitale");}
        if(cities.isEmpty()){
            p.getTerritorio().capitale = null;
            p.setSconfitto();
        }
        this.capitale = prioritaCapitale(cities);
    }
    public void rimuoviCapitale(){this.capitale = null;}


    //CITTA
    private void removeCity(City c){
        if(c == null || !cities.contains(c)){return;}
        c.resetOwner();
        cities.remove(c);
    }
    public void addCity(City c){
        if(c == null || cities.contains(c) || c.getOwner()==null){throw new IllegalArgumentException("città null o già presente");}
        if(owner== null){throw new IllegalArgumentException("owner null");}
        Territory vecchioTerritorio = c.getOwner().getTerritorio();
        if(vecchioTerritorio.isCapitale(c)){
            vecchioTerritorio.removeCity(c);
            if(!vecchioTerritorio.getCities().isEmpty()){
                vecchioTerritorio.nominaCapitale(vecchioTerritorio.getOwner());
            }
            else{
                vecchioTerritorio.getOwner().setSconfitto();
                vecchioTerritorio.rimuoviCapitale();
            }
        }
        else{
            vecchioTerritorio.removeCity(c);
        }
        c.setOwner(owner);
        cities.add(c);
    }

    public List<City> getCities() {return new ArrayList<>(cities);}
    public boolean cercaCity(City c){
        return c!=null && cities.contains(c);
    }

    //RIBELLIONE
    public Ribellione getRibellione() {return ribellione;}
    public void setRibellione(Ribellione ribellione) {this.ribellione = ribellione;}
    public boolean hasRibellione(){
        return ribellione != null && ribellione.isAttiva();
    }
    public void attivaRibellione(){
        if(ribellione == null){
            ribellione = new Ribellione();
        }
        else {ribellione.setAttiva();}
    }
    public void rimuoviRibellione(){ribellione = null;}

}
