package it.unicam.cs.mpgc.rpg125715;

public class ConstructionService {
    public static final int COSTO_SPECIALIZZAZIONE = 2;

    public int costoMiglioramento(City city){
        if(city == null){throw new IllegalArgumentException("city null");}
        return switch(city.getLevel()){
            case ACCAMPAMENTO -> 3;
            case AVAMPOSTO -> 4;
            case CITTA -> 6;
            case METROPOLI -> throw new IllegalArgumentException("città già al livello massimo");
        };
    }

    public void miglioraCitta(Player player, City city){
        if(player == null || city == null){throw new IllegalArgumentException("player or city null");}
        if(city.getOwner() != player){throw new IllegalArgumentException("la città non appartiene al giocatore");}
        if(city.isCitta() && player.getLeader().bloccaMetropoli() && player.getCapitale() != city){throw new IllegalArgumentException(player.getLeader()+" può avere una metropoli solo nella capitale");}

        int costo = costoMiglioramento(city);
        if(player.getOro()<costo){throw new IllegalArgumentException("oro non sufficiente (servono "+costo+")");}
        player.spendiOro(costo);
        city.upgrade();
    }

    public void impostaSpecializzazione(Player player, City city, CitySpecialization citySpecialization){
        if(player == null || city == null || citySpecialization == null){throw new IllegalArgumentException("player or city null");}
        if(city.getOwner() != player){throw new IllegalArgumentException("la città non appartiene al giocatore");}
        if(citySpecialization == CitySpecialization.NESSUNA){throw new IllegalArgumentException("scegli una specializzazione");}
        if(city.getSpecialization() == citySpecialization){throw new IllegalArgumentException("la città ha già questa specializzazione");}
        if(player.getOro()<COSTO_SPECIALIZZAZIONE){throw new IllegalArgumentException("oro insufficiente");}
        player.spendiOro(COSTO_SPECIALIZZAZIONE);
        city.setSpecialization(citySpecialization);
    }
}
