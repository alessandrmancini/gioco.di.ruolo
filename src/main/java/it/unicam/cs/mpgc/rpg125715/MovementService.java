package it.unicam.cs.mpgc.rpg125715;

public class MovementService {
    private final BattleService battaglia;
    private final ArmyService armyService;
    private final ConquestService conquestService;

    public MovementService(BattleService battaglia, ArmyService armyService, ConquestService conquestService) {
        if(battaglia == null|| armyService == null || conquestService == null){throw new IllegalArgumentException("battleService, armyService o conquestService null");}
        this.battaglia = battaglia;
        this.armyService = armyService;
        this.conquestService = conquestService;
    }

    public void muovi(Army army, Location destinazione) {
        if(army == null || destinazione == null){throw new IllegalArgumentException("esercito o destinazione null");}
        if(!army.isPuoMuovere()){throw new IllegalArgumentException("l'esercito non può muovere");}
        Location partenza = army.getPosizione();
        if(partenza == null){throw new IllegalArgumentException("l'esercito non ha una posizione valida");}
        if(!partenza.isAdiacente(destinazione)){throw new IllegalArgumentException("posizioni non adiacenti");}

        partenza.removeArmy(army);
        destinazione.addArmy(army);
        army.aggiornaPosizione(destinazione);
        army.bloccaMovimento();

        if(!destinazione.isContesa()){return;}
        Army nemico = destinazione.getEnemyArmyFor(army);
        if(nemico == null){throw new IllegalArgumentException("nemico null");}
        BattleResult result = battaglia.combatti(army, nemico, destinazione.getCity());

        Army sconfitto = result.sconfitto();
        Location ritirata = getRitirata(destinazione);

        destinazione.removeArmy(sconfitto);
        if(sconfitto.isVuoto() || ritirata == null){
            armyService.distruggiERespawn(sconfitto);
        }
        else{
            ritirata.addArmy(sconfitto);
            sconfitto.aggiornaPosizione(ritirata);
        }
        if(army.isVuoto()){destinazione.removeArmy(army);}
        if(nemico.isVuoto()){destinazione.removeArmy(nemico);}
        if(result.haVintoAttaccante()){conquistaSePossibile(destinazione, army);}
    }

    public Location getRitirata(Location destinazione){
        for (Location l : destinazione.getAdiacenti()){
            if(!l.hasArmy()){
                return l;
            }
        }
        return null;
    }

    private void conquistaSePossibile(Location destinazione, Army army){
        if(destinazione == null || army == null){throw new IllegalArgumentException("esercito o destinazione e army null");}
        if(!destinazione.hasCity()){return;}

        City city = destinazione.getCity();
        if(city == null || city.getOwner() == null){return;}

        Player conquistatore = army.getOwner();
        if(city.getOwner() != conquistatore && destinazione.hasArmyPlayer(conquistatore)){
            conquestService.conquista(city, conquistatore);
        }
    }
}
