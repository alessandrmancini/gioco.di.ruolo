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

        if(owner.isSconfitto()){return null;}
        Location destinazione = trovaLocationRespawn(owner);
        if(destinazione == null){return null;}

        for(Army esistente : destinazione.getArmies()){
            if(esistente != null && esistente.getOwner() == owner){return esistente;}
        }
        return creaEsercito(owner, destinazione);
    }

    private Location trovaLocationRespawn(Player owner){
        City capitale = owner.getCapitale();
        if(capitale != null && puoOspitare(capitale.getLocation(), owner)){return capitale.getLocation();}
        for(City c : owner.getTerritorio().getCities()){
            if(puoOspitare(c.getLocation(), owner)){return c.getLocation();}
        }
        return null;
    }

    private boolean puoOspitare(Location location, Player owner){
        return location != null && !location.hasEnemyArmiesFor(owner);
    }

}
