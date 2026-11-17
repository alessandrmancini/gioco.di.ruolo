package it.unicam.cs.mpgc.rpg125715;

/**
 * Regole del movimento degli eserciti: spostamento di un nodo, battaglia se nella destinazione
 * c'è un nemico, ritirata dello sconfitto, conquista delle città e annullamento dell'ultimo spostamento.
 */
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

    /**
     * Sposta l'esercito nel nodo adiacente indicato e ne blocca il movimento fino al prossimo turno.
     * Se nella destinazione c'è un esercito nemico si combatte: chi perde si ritira o viene distrutto
     * (e ricompare in una sua città). Se l'attaccante vince, o la città era indifesa, la conquista.
     *
     * @param army         esercito da muovere (deve poter muovere e avere almeno un'unità)
     * @param destinazione nodo adiacente alla posizione attuale
     * @return il risultato della battaglia, oppure null se non c'è stata battaglia
     * @throws IllegalArgumentException se la mossa non è valida (non adiacente, esercito già mosso,
     *         attacco nel primo turno, c'è già un proprio esercito nella destinazione...)
     */
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

        result.vincitore().getOwner().incrementaBattaglieVinte();
        Army sconfitto = result.sconfitto();
        Location ritirata = getRitirata(destinazione, sconfitto.getOwner());

        destinazione.removeArmy(sconfitto);
        if(sconfitto.isVuoto() || ritirata == null){
            armyService.distruggiERespawn(sconfitto);
        }
        else{
            ritirata.addArmy(sconfitto);
            sconfitto.aggiornaPosizione(ritirata);
        }

        if(result.haVintoAttaccante()){conquistaSePossibile(destinazione, army);}
        else{result.vincitore().getOwner().setAttaccanteSconfitto(result.sconfitto());}
        return result;
    }

    /**
     * Nodo in cui si ritira un esercito sconfitto: prima una propria città vicina senza eserciti,
     * poi un nodo vicino libero e senza città.
     *
     * @return il nodo di ritirata, oppure null se non ce n'è nessuno (l'esercito verrà distrutto)
     */
    public Location getRitirata(Location destinazione, Player proprietario){
        //una città propria vicina senza eserciti
        for (Location l : destinazione.getAdiacenti()){
            if(!l.hasArmy() && l.hasCity() && l.getCity().getOwner() == proprietario){
                return l;
            }
        }
        //un nodo vicino senza città e senza eserciti
        for(Location l :destinazione.getAdiacenti()){
            if(!l.hasArmy() && !l.hasCity()){return l;}
        }
        //esercito distrutto
        return null;
    }

    /**
     * Riporta l'esercito nel nodo di partenza e gli permette di muovere di nuovo.
     * Si usa solo per spostamenti senza battaglia e senza conquista.
     *
     * @throws IllegalArgumentException se l'esercito non si è mosso o se il nodo di partenza è occupato
     */
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
        if(city == null){return;}

        Player conquistatore = army.getOwner();
        if(city.getOwner() == conquistatore){return;}

        if(city.getOwner() == null && city.getExProprietario() == conquistatore){return;}

        if(destinazione.hasArmyPlayer(conquistatore)){
            conquestService.conquista(city, conquistatore);
        }
    }

    /**
     * Vero se entrare nella destinazione è un attacco: c'è un esercito nemico
     * oppure c'è una città che appartiene a un altro giocatore.
     */
    public boolean isAttacco(Location destinazione, Player player){
        if(destinazione == null || player == null){throw new IllegalArgumentException("destinazione o army null");}
        if(destinazione.hasEnemyArmiesFor(player)){return true;}
        if(destinazione.hasCity()){
            Player proprietario = destinazione.getCity().getOwner();
            return proprietario != null && proprietario != player;
        }
        return false;
    }
}
