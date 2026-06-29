package it.unicam.cs.mpgc.rpg125715;

public class BattleService {

    private final DiceService dadi;

    public BattleService(DiceService dadi) {
        if(dadi == null) {throw new IllegalArgumentException("il tiro non può essere null");}
        this.dadi = dadi;
    }

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

        int perditeAttaccante = calcolaPerdite(attaccante, totaleAttaccante, totaleDifensore);
        int perditeDifensore = calcolaPerdite(difensore, totaleDifensore, totaleAttaccante);

        attaccante.rimuoviUnitaPerPriorita(perditeAttaccante);
        difensore.rimuoviUnitaPerPriorita(perditeDifensore);

        return new BattleResult(attaccante, difensore, tiroAttaccante, tiroDifensore, totaleAttaccante, totaleDifensore, perditeAttaccante, perditeDifensore, vincitore, sconfitto);
    }


    private int calcolaBonusDifesaTerritorio(City city) {
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

    private int calcolaPerdite(Army esercito, int totaleEsercito, int totaleNemico) {
        if (esercito.isVuoto()) {
            return 0;
        }
        int perdite = 1;

        if (totaleEsercito < totaleNemico) {
            int differenza = totaleNemico - totaleEsercito;

            if (differenza >= 10) {
                perdite = 3;
            } else if (differenza >= 5) {
                perdite = 2;
            }
        }

        return Math.min(perdite, esercito.getNumeroUnita());
    }

    private int calcolaBonusLeaderAttacco(Army army) {
        return army.getOwner().getLeader().getBonusAttacco();
    }

    private int calcolaBonusLeaderDifesa(Army army) {
        return army.getOwner().getLeader().getBonusDifesa();
    }
}