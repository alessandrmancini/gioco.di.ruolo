package it.unicam.cs.mpgc.rpg125715;

public class MovementService {
    private BattleService battaglia;
    private ArmyService armyService;

    public MovementService(BattleService battaglia, ArmyService armyService) {
        if(battaglia == null|| armyService == null){throw new IllegalArgumentException("battleService o armyService null");}
        this.battaglia = null;
        this.armyService = null;
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

        if(!destinazione.isContesa()){
            if(army.isVuoto()){
                destinazione.removeArmy(army);
            }
            return;
        }
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
            destinazione.removeArmy(sconfitto);
            ritirata.addArmy(army);
            sconfitto.aggiornaPosizione(ritirata);
        }
        if(army.isVuoto()){destinazione.removeArmy(army);}
        if(nemico.isVuoto()){destinazione.removeArmy(nemico);}
    }

    public Location getRitirata(Location destinazione){
        for (Location l : destinazione.getAdiacenti()){
            if(!l.hasArmy()){
                return l;
            }
        }
        return null;
    }
}
