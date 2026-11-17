package it.unicam.cs.mpgc.rpg125715;

/** Sviluppo delle città: miglioramento di livello e scelta della specializzazione. */
public class ConstructionService {
    public static final int COSTO_SPECIALIZZAZIONE = 2;

    /**
     * Costo per passare al livello successivo: Avamposto 3, Città 4, Metropoli 6.
     *
     * @throws IllegalArgumentException se la città è già Metropoli
     */
    public int costoMiglioramento(City city){
        if(city == null){throw new IllegalArgumentException("city null");}
        return switch(city.getLevel()){
            case ACCAMPAMENTO -> 3;
            case AVAMPOSTO -> 4;
            case CITTA -> 6;
            case METROPOLI -> throw new IllegalArgumentException("città già al livello massimo");
        };
    }

    /**
     * Paga e porta la città al livello successivo.
     *
     * @throws IllegalArgumentException se la città non è del giocatore, se l'oro non basta
     *         o se Attila prova a fare una Metropoli che non è la capitale
     */
    public void miglioraCitta(Player player, City city){
        if(player == null || city == null){throw new IllegalArgumentException("player or city null");}
        if(city.getOwner() != player){throw new IllegalArgumentException("la città non appartiene al giocatore");}
        if(city.isCitta() && player.getLeader().bloccaMetropoli() && player.getCapitale() != city){throw new IllegalArgumentException(player.getLeader()+" può avere una metropoli solo nella capitale");}

        int costo = costoMiglioramento(city);
        if(player.getOro()<costo){throw new IllegalArgumentException("oro non sufficiente (servono "+costo+")");}
        player.spendiOro(costo);
        city.upgrade();
    }

    /**
     * Paga {@link #COSTO_SPECIALIZZAZIONE} oro e cambia la specializzazione della città.
     *
     * @throws IllegalArgumentException se la città non è del giocatore, se è la stessa specializzazione,
     *         se è NESSUNA o se l'oro non basta
     */
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
