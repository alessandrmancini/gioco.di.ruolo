package it.unicam.cs.mpgc.rpg125715;

public class City {

    private final String name;
    private CityLevel level;
    private CitySpecialization specialization;
    private Player owner;
    private Location location;

    public City(String name) {
        if(name == null || name.isBlank()) {throw new IllegalArgumentException("il nome non può essere vuoto");}
        this.name = name;
        this.level = CityLevel.ACCAMPAMENTO;
        this.specialization = CitySpecialization.NESSUNA;
        this.owner = null;
        this.location = null;
    }

    public String getName(){return name;}
    public CityLevel getLevel(){return level;}

    //SPECIALIZZAZIONI E LIVELLI
    public CitySpecialization getSpecialization(){return specialization;}
    public void setSpecialization(CitySpecialization specialization){
        if(specialization == null){throw new IllegalArgumentException("specializzazione null");}
        this.specialization = specialization;
    }

    public boolean isAccampamento(){return level == CityLevel.ACCAMPAMENTO;}
    public boolean isAvamposto(){return level == CityLevel.AVAMPOSTO;}
    public boolean isCitta(){return level == CityLevel.CITTA;}
    public boolean isMetropoli(){return level == CityLevel.METROPOLI;}
    public boolean isSviluppata(){return level == CityLevel.CITTA || level == CityLevel.METROPOLI;}
    public void upgrade(){
        switch (level){
            case ACCAMPAMENTO -> level = CityLevel.AVAMPOSTO;
            case AVAMPOSTO -> level = CityLevel.CITTA;
            case CITTA -> level = CityLevel.METROPOLI;
            case METROPOLI -> throw new IllegalStateException("città già al livello massimo");
        }
    }
    //OWNER
    public Player getOwner(){return owner;}
    public void resetOwner(){owner = null;}
    public void setOwner(Player owner){
        if(owner == null){throw new IllegalArgumentException("owner null");}
        this.owner = owner;
    }

    //LOCATION
    public Location getLocation() {return location;}
    public void setLocation(Location l){
        if(l == null){throw new IllegalArgumentException("location null");}
        this.location = l;
    }
}
