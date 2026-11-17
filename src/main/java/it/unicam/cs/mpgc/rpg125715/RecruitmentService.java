package it.unicam.cs.mpgc.rpg125715;

/** Reclutamento delle unità: dove si può reclutare cosa e quanto costa. */
public class RecruitmentService {

    /**
     * Paga e aggiunge un'unità all'esercito che si trova nella città.
     *
     * @param army esercito del giocatore che si trova nel nodo della città
     * @return l'unità reclutata
     * @throws IllegalArgumentException se la città non è del giocatore o è in ribellione, se l'esercito
     *         non è lì, se la città non può reclutare quel tipo o se l'oro non basta
     */
    public Unit reclutaUnita(Player player, City city,Army army, UnitType type) {
        if(player == null || city == null || army == null || type == null){throw new IllegalArgumentException("non possono esserci argomenti null");}
        if(city.getOwner() != player || !player.getTerritorio().cercaCity(city)){throw new IllegalArgumentException("la città non è presente nel territorio del giocatore");}
        if(city.hasRibellione()){throw new IllegalArgumentException("la città è in ribellione");}
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

    /**
     * Costo dell'unità in questa città: costo base (Fanteria 2, Cavalleria 3, Assedio 4, Elefanti 5,
     * Speciale 6), +1 per Cesare (tranne le Speciali), -1 Città, -2 Metropoli, -1 Militare.
     * Non scende mai sotto la metà del costo base arrotondata per eccesso.
     */
    public int costoUnita(UnitType type, City city, LeaderType leader) {
        if(city == null || type == null || leader == null){throw new IllegalArgumentException("non possono esserci argomenti null");}
        int costoBase = switch (type) {
            case FANTERIA -> 2;
            case CAVALLERIA -> 3;
            case ASSEDIO -> 4;
            case SPECIALI -> 6;
            case ELEFANTI -> 5;
        };
        int minimo = (costoBase +1)/2;
        int base = costoBase;
        if(leader.haCostoReclutamentoMaggiore(type)){base+=1;}
        if (city.isCitta()) {base -= 1;}
        else if (city.isMetropoli()) {base -= 2;}

        if (city.getSpecialization() == CitySpecialization.MILITARE) {base -= 1;}
        return Math.max(minimo, base);
    }

    /**
     * Vero se la città può reclutare quel tipo di unità: Fanteria, Cavalleria e Assedio da Avamposto in su,
     * Speciali solo da Città o Metropoli, Elefanti solo Annibale in una città Militare.
     */
    public boolean puoReclutareDaCitta(City city, UnitType type, LeaderType leader) {
        if (city == null || type == null || leader == null || city.getLevel() == CityLevel.ACCAMPAMENTO) {return false;}
        if (type == UnitType.ELEFANTI) {
            return (leader == LeaderType.ANNIBALE && city.getSpecialization() == CitySpecialization.MILITARE);
        }
        if (type == UnitType.SPECIALI) {return city.isCitta() || city.isMetropoli();}
        return city.isAvamposto() || city.isCitta() || city.isMetropoli();
    }
}