package it.unicam.cs.mpgc.rpg125715;

public class ArmyService {

    private final IdGenerator idGenerator;

    public ArmyService(IdGenerator idGenerator) {
        if(idGenerator == null){throw new IllegalArgumentException("idGenerator null");}
        this.idGenerator = idGenerator;
    }

    public Army creaEsercito(Player owner, Location posizione) {
        if(owner == null || posizione == null){throw new IllegalArgumentException("owner o posizione null");}
        return new Army(idGenerator.nextId(EntityType.ARMY) , owner, posizione);
    }
    public void distruggiEsercito(Army army){
        if(army == null){throw new IllegalArgumentException("army null");}

        Location posizione = army.getPosizione();
        if(posizione != null){posizione.removeArmy(army);}

        army.removeAll();
        army.getOwner().removeArmy(army);
    }

    public Army ricreaEsercitoInCapitale(Player player){
        if(player == null){throw new IllegalArgumentException("player null");}
        if(!player.hasCapitale()){throw new IllegalArgumentException("player has no capitale");}

        City capitale = player.getCapitale();
        Location locationCapitale = capitale.getLocation();

        if(locationCapitale == null){throw new IllegalArgumentException("location has no capitale");}
        return creaEsercito(player, locationCapitale);
    }

    public Army distruggiERespawn(Army army){
        if(army == null){throw new IllegalArgumentException("army null");}
        Player owner = army.getOwner();
        distruggiEsercito(army);
        return ricreaEsercitoInCapitale(owner);
    }
}
