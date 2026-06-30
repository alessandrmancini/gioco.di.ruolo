package it.unicam.cs.mpgc.rpg125715;

public class RecruitmentService {

    public Unit reclutaUnita(Player player, City city,Army army, UnitType type) {
        if(player == null || city == null || army == null || type == null){throw new IllegalArgumentException("non possono esserci argomenti null");}
        if(city.getOwner() != player || !player.getTerritorio().cercaCity(city)){throw new IllegalArgumentException("la città non è presente nel territorio del giocatore");}
        if(city.getOwner().getTerritorio().hasRibellione()){throw new IllegalArgumentException("ribellione in corso");}
        if(army.getOwner() != player){throw new IllegalArgumentException("posizione esercito errata o esercito non del player");}
        Location posizione = army.getPosizione();
        if(posizione == null || !posizione.hasCity() || posizione.getCity() != city){throw new IllegalArgumentException("l'esercito non si trova nella location della città");}
        if(!puoReclutareDaCitta(city,type,player.getLeader())){throw  new IllegalArgumentException("questa città non può reclutare o non può reclutare questo tipo di unità");}

        int costo = costoUnita(type, city, player.getLeader());
        if(player.getOro()<costo){throw new IllegalArgumentException("oro non sufficiente");}

        Unit unita = UnitFactory.creaUnita(type, player.getLeader());
        player.spendiOro(costo);
        army.addUnit(unita);
        return unita;
    }

    private int costoUnita(UnitType type, City city, LeaderType leader) {
        if(city == null || type == null || leader == null){throw new IllegalArgumentException("non possono esserci argomenti null");}
        int base = switch (type) {
            case FANTERIA -> 2;
            case CAVALLERIA -> 3;
            case ASSEDIO -> 4;
            case SPECIALI -> 6;
            case ELEFANTI -> 5;
        };
        if(leader.haCostoReclutamentoMaggiore(type)){base+=1;}
        if (city.isCitta()) {base -= 1;}
        else if (city.isMetropoli()) {base -= 2;}

        if (city.getSpecialization() == CitySpecialization.MILITARE) {base -= 1;}
        //fa in modo che se un'unità arrivi a costare 0, ci sia sempre un minimo di costo 1
        return Math.max(1, base);
    }

    private boolean puoReclutareDaCitta(City city, UnitType type, LeaderType leader) {
        if (city == null || type == null || leader == null || city.getLevel() == CityLevel.ACCAMPAMENTO) {return false;}
        if (type == UnitType.ELEFANTI) {
            return (leader == LeaderType.ANNIBALE && city.getSpecialization() == CitySpecialization.MILITARE);
        }
        if (type == UnitType.SPECIALI) {return city.isCitta() || city.isMetropoli();}
        return city.isAvamposto() || city.isCitta() || city.isMetropoli();
    }
}