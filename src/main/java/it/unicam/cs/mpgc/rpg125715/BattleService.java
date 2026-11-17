package it.unicam.cs.mpgc.rpg125715;

/**
 * Risolve le battaglie: somma attacco e difesa delle unità, aggiunge i bonus (leader, città, assedio),
 * moltiplica per il dado di ciascun lato e calcola le perdite. A parità vince chi difende.
 */
public class BattleService {

    private final DiceRoller dadi;

    public BattleService(DiceRoller dadi) {
        if(dadi == null) {throw new IllegalArgumentException("il tiro non può essere null");}
        this.dadi = dadi;
    }

    /**
     * Combatte una battaglia e toglie subito le unità perse da entrambi gli eserciti.
     * Perdite: il vincitore perde il 15% delle unità; lo sconfitto il 25% (almeno 1),
     * il 50% se il totale nemico è almeno 1,3 volte il suo, tutte se è almeno il doppio.
     *
     * @param attaccante    esercito che entra nel nodo
     * @param difensore     esercito che si trova nel nodo
     * @param cityDifensore città nel nodo (può essere null): dà il bonus difesa e subisce il bonus assedio
     * @return tiri, totali, perdite, vincitore e sconfitto
     */
    public BattleResult combatti(Army attaccante, Army difensore, City cityDifensore) {
        if (attaccante == null || difensore == null) {throw new IllegalArgumentException("Eserciti non possono essere null");}
        if (attaccante.isVuoto()) {throw new IllegalArgumentException("L'esercito attaccante non può essere vuoto");}
        if (difensore.isVuoto()) {throw new IllegalArgumentException("L'esercito difensore non può essere vuoto");}

        double tiroAttaccante = dadi.lanciaDado(attaccante.getOwner());
        double tiroDifensore = dadi.lanciaDado(difensore.getOwner());

        int bonusAttaccante = calcolaBonusLeaderAttacco(attaccante) + calcolaBonusAssedio(attaccante, cityDifensore);
        int bonusDifensore = calcolaBonusLeaderDifesa(difensore) + calcolaBonusDifesaTerritorio(cityDifensore);

        int totaleAttaccante = (int)Math.round((attaccante.getAttaccoTotale() + bonusAttaccante) * tiroAttaccante);
        int totaleDifensore = (int)Math.round((difensore.getDifesaTotale() + bonusDifensore) * tiroDifensore);

        Army vincitore;
        Army sconfitto;

        if (totaleAttaccante > totaleDifensore) {
            vincitore = attaccante;
            sconfitto = difensore;
        }
        else {
            vincitore = difensore;
            sconfitto = attaccante;
        }

        int perditeAttaccante = calcolaPerdite(attaccante, sconfitto == attaccante, totaleAttaccante, totaleDifensore);
        int perditeDifensore = calcolaPerdite(difensore, sconfitto == difensore, totaleDifensore, totaleAttaccante);

        attaccante.rimuoviUnitaPerPriorita(perditeAttaccante);
        difensore.rimuoviUnitaPerPriorita(perditeDifensore);

        return new BattleResult(attaccante, difensore, tiroAttaccante, tiroDifensore, totaleAttaccante, totaleDifensore, perditeAttaccante, perditeDifensore, vincitore, sconfitto);
    }


    /**
     * Bonus difesa dato da una città: Avamposto +1, Città +2, Metropoli +4, specializzazione militare +2.
     *
     * @param city città da difendere (null = nessun bonus)
     */
    public static int calcolaBonusDifesaTerritorio(City city) {
        if (city == null) {return 0;}
        int bonus = 0;
        if (city.isAvamposto()) {
            bonus += 1;
        } else if (city.isCitta()) {
            bonus += 2;
        } else if (city.isMetropoli()) {
            bonus += 4;
        }

        if (city.getSpecialization() == CitySpecialization.MILITARE) {
            bonus += 2;
        }

        return bonus;
    }

    private int calcolaBonusAssedio(Army attaccante, City city) {
        if (city == null || city.isAccampamento()) {return 0;}

        int unitaAssedio = attaccante.getNumeroUnitaPerTipo(UnitType.ASSEDIO);
        if (unitaAssedio == 0) {return 0;}

        int bonus = 0;

        if (city.isCitta()) {
            bonus = 2 * unitaAssedio;
        } else if (city.isMetropoli()) {
            bonus = 3 * unitaAssedio;
        }
        return bonus;
    }

    private int calcolaPerdite(Army esercito,boolean haPerso, int totaleProprio, int totaleNemico) {
        if (esercito.isVuoto()) {
            return 0;
        }
        int unita = esercito.getNumeroUnita();
        if(!haPerso) {
            return Math.min(unita, (int) Math.round(unita*0.15));
        }

        double rapporto = (double) totaleNemico / Math.max(1, totaleProprio);
        int perdite;

        if(rapporto >= 2){perdite = unita;}
        else if(rapporto >= 1.3){perdite = (int) Math.round(unita * 0.5);}
        else{perdite = Math.max(1, (int) Math.round(unita*0.25));}

        return Math.min(perdite, unita);
    }

    private int calcolaBonusLeaderAttacco(Army army) {
        return army.getOwner().getLeader().getBonusAttacco();
    }

    private int calcolaBonusLeaderDifesa(Army army) {
        return army.getOwner().getLeader().getBonusDifesa();
    }
}