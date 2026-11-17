package it.unicam.cs.mpgc.rpg125715;

/** Creazione e distruzione degli eserciti, ricomparsa dopo una sconfitta e costo dei nuovi eserciti. */
public class ArmyService {

    private final IdGenerator idGenerator;
    public static final int MAX_ESERCITI = 6;


    public ArmyService(IdGenerator idGenerator) {
        if(idGenerator == null){throw new IllegalArgumentException("idGenerator null");}
        this.idGenerator = idGenerator;
    }

    /** Crea un esercito vuoto con un id nuovo e lo registra nel giocatore e nel nodo. */
    public Army creaEsercito(Player owner, Location posizione) {
        if(owner == null || posizione == null){throw new IllegalArgumentException("owner o posizione null");}
        return new Army(idGenerator.nextId(EntityType.ARMY) , owner, posizione);
    }
    /** Toglie l'esercito dal nodo e dal giocatore e ne elimina tutte le unità. */
    public void distruggiEsercito(Army army){
        if(army == null){throw new IllegalArgumentException("army null");}

        Location posizione = army.getPosizione();
        if(posizione != null){posizione.removeArmy(army);}

        army.removeAll();
        army.getOwner().removeArmy(army);
    }

    /**
     * Crea un esercito vuoto nella capitale del giocatore.
     *
     * @throws IllegalArgumentException se il giocatore non ha una capitale
     */
    public Army ricreaEsercitoInCapitale(Player player){
        if(player == null){throw new IllegalArgumentException("player null");}
        if(!player.hasCapitale()){throw new IllegalArgumentException("player has no capitale");}

        City capitale = player.getCapitale();
        Location locationCapitale = capitale.getLocation();

        if(locationCapitale == null){throw new IllegalArgumentException("location has no capitale");}
        return creaEsercito(player, locationCapitale);
    }

    /**
     * Distrugge l'esercito e ne fa ricomparire uno nuovo (1 fanteria e 1 unità speciale) nella capitale
     * o in un'altra città libera del proprietario. Il nuovo esercito non può muovere fino al prossimo turno.
     *
     * @return il nuovo esercito, oppure null se non c'è una città libera (se il giocatore
     *         non ha più né città né eserciti diventa sconfitto)
     */
    public Army distruggiERespawn(Army army){
        if(army == null){throw new IllegalArgumentException("army null");}
        Player owner = army.getOwner();
        distruggiEsercito(army);

        if(owner.isSconfitto()){return null;}
        Location destinazione = trovaLocationRespawn(owner);
        if(destinazione == null){
            if(owner.numeroCitta() == 0 && owner.numeroEserciti() == 0){owner.setSconfitto();}
            return null;
        }

        Army nuovo = creaEsercito(owner, destinazione);
        nuovo.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, owner.getLeader()));
        nuovo.addUnit(UnitFactory.creaUnita(UnitType.SPECIALI, owner.getLeader()));
        nuovo.bloccaMovimento();
        return nuovo;
    }

    /**
     * Costo di un nuovo esercito: gratis se non se ne ha nessuno, altrimenti 2 elevato al numero
     * di eserciti posseduti (2, 4, 8, 16, 32).
     */
    public static int costoNuovoEsercito(Player player){
        if(player == null){throw new IllegalArgumentException("player null");}
        if(player.numeroEserciti()==0){return 0;}
        return 1 << player.numeroEserciti();    //moltiplicazione dell'1 per 2^numeroEserciti()
    }
    /**
     * Compra un nuovo esercito (3 fanterie e 1 cavalleria) in una propria città
     * senza propri eserciti e senza nemici.
     *
     * @throws IllegalArgumentException se la città non è del giocatore, è occupata,
     *         si è al massimo di eserciti o l'oro non basta
     */
    public Army creaNuovoEsercito(Player player, City city){
        if(player == null || city == null){throw new IllegalArgumentException("player or city null");}
        if(city.getOwner() != player){throw new IllegalArgumentException("la città non è tua");}
        if(player.numeroEserciti() >= MAX_ESERCITI){throw new IllegalArgumentException("numero massimo di eserciti raggiunto");}

        Location location = city.getLocation();

        if(location == null){throw new IllegalArgumentException("la città non ha una posizione");}
        if(location.hasArmyPlayer(player)){throw new IllegalArgumentException("in questa città hai già un esercito");}
        if(location.hasEnemyArmiesFor(player)){throw new IllegalArgumentException("in questa città ci sono eserciti nemici");}

        int costo = costoNuovoEsercito(player);
        if(player.getOro()<costo){throw new IllegalArgumentException("l'oro non è suficiente, servono: "+ costo);}
        player.spendiOro(costo);

        Army army = creaEsercito(player, location);
        for(int i = 0;i <3 ; i++){
            army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, player.getLeader()));
        }
        army.addUnit(UnitFactory.creaUnita(UnitType.CAVALLERIA, player.getLeader()));
        return army;
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
        return location != null && !location.hasEnemyArmiesFor(owner) && !location.hasArmyPlayer(owner);
    }
}
