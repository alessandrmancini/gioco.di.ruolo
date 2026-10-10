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

    public BattleResult muovi(Army army, Location destinazione) {
        if(army == null || destinazione == null){throw new IllegalArgumentException("esercito o destinazione null");}
        if(!army.isPuoMuovere()){throw new IllegalArgumentException("l'esercito non può muovere");}
        if(army.isVuoto()){throw new IllegalArgumentException("un esercito senza unità non può muovere");}

        Location partenza = army.getPosizione();

        if(partenza == null){throw new IllegalArgumentException("l'esercito non ha una posizione valida");}
        if(!partenza.isAdiacente(destinazione)){throw new IllegalArgumentException("posizioni non adiacenti");}
        if(destinazione.hasArmyPlayer(army.getOwner())){throw new IllegalArgumentException("c'è già un esercito nella destinazione");}
        if(army.getOwner().getTurniGiocati() == 0 && isAttacco(destinazione, army.getOwner())){
            throw new IllegalArgumentException("nel primo turno non si può attaccare");
        }

        partenza.removeArmy(army);
        destinazione.addArmy(army);
        army.aggiornaPosizione(destinazione);
        army.bloccaMovimento();

        if(!destinazione.isContesa()){
            conquistaSePossibile(destinazione, army);
            return null;
        }
        Army nemico = destinazione.getEnemyArmyFor(army);
        if(nemico == null){throw new IllegalArgumentException("nemico null");}
        if(nemico.isVuoto()){
            destinazione.removeArmy(nemico);
            conquistaSePossibile(destinazione, army);
            armyService.distruggiERespawn(nemico);
            return null;
        }
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
        return result;
    }

    public Location getRitirata(Location destinazione){
        for (Location l : destinazione.getAdiacenti()){
            if(!l.hasArmy()){
                return l;
            }
        }
        return null;
    }

    public void annullaMovimento(Army army, Location partenza){
        if(army == null || partenza == null){throw new IllegalArgumentException("esercito o destinazione null");}
        Location attuale = army.getPosizione();
        if(attuale == partenza){throw new IllegalArgumentException("l'esercito non ha mosso");}
        if(partenza.hasArmyPlayer(army.getOwner())){throw new IllegalArgumentException("nella posizione di partenza c'è già un tuo esercito");}
        if(partenza.hasEnemyArmiesFor(army.getOwner())){throw new IllegalArgumentException("nella posizione di partenza ci sono eserciti nemici");}
        attuale.removeArmy(army);
        partenza.addArmy(army);
        army.aggiornaPosizione(partenza);
        army.sbloccaMovimento();
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

    public boolean isAttacco(Location destinazione, Player player){
        if(destinazione == null || player == null){throw new IllegalArgumentException("destinazione o army null");}
        if(destinazione.hasEnemyArmiesFor(player)){return false;}
        if(destinazione.hasCity()){
            Player proprietario = destinazione.getCity().getOwner();
            return proprietario != null && proprietario != player;
        }
        return false;
    }
}
