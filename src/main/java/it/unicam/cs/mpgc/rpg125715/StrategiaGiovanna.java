package it.unicam.cs.mpgc.rpg125715;

import java.util.List;
import java.util.Map;

public class StrategiaGiovanna extends StrategiaBase {

    public StrategiaGiovanna(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{3, 3, 3, 3, 4, 5};}

    // preferisce le città che erano di un altro giocatore e sono state conquistate
    @Override
    public int punteggioAttacco(Player bot, Army esercito, Location destinazione, Army nemico, int base) {
        return base + (cittaConquistata(bot, destinazione) ? 5 : 0);
    }

    //l'esercito più forte guida, gli altri non lo superano verso il bersaglio
    @Override
    public boolean mossaConsentita(Player bot, Army esercito, Location destinazione, Map<Location, Integer> distanza) {
        Army guida = esercitoPiuForte(bot, false);
        if(guida == null || guida == esercito || guida.getPosizione() == null){return true;}
        Integer distanzaGuida = distanza.get(guida.getPosizione());
        Integer distanzaDestinazione = distanza.get(destinazione);
        if(distanzaGuida == null || distanzaDestinazione == null){return true;}
        return distanzaDestinazione >= distanzaGuida;
    }

    @Override
    public List<CitySpecialization> specializzazioniPrioritarie() {return List.of(CitySpecialization.AGRICOLA);}

    //città ora di un giocatore ma che all'inizio era di un altro
    private boolean cittaConquistata(Player bot, Location l) {
        if(!l.hasCity()){return false;}
        City c = l.getCity();
        return c.getOwner() != null && c.getOwner() != bot
                && c.getProprietarioIniziale() != null && c.getProprietarioIniziale() != c.getOwner();
    }
}