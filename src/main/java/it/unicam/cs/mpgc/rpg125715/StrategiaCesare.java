package it.unicam.cs.mpgc.rpg125715;

import java.util.List;
import java.util.Map;

public class StrategiaCesare extends StrategiaBase {

    //tiene un esercito di guardia alla capitale solo se ha almeno questo numero di eserciti
    private static final int ESERCITI_MINIMI_GUARDIA = 3;

    public StrategiaCesare(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{4, 3, 3, 3, 4, 3};}

    //dado fortunato: preferisce le battaglie equilibrate (i suoi dadi fanno la differenza)
    @Override
    public int punteggioAttacco(Player bot, Army esercito, Location destinazione, Army nemico, int base) {
        return nemico == null ? 100 : -Math.abs(base);
    }

    // capitale coperta: l'esercito più vicino alla capitale resta a un passo da essa
    @Override
    public boolean eGuardia(Player bot, Army esercito) {
        return bot.numeroEserciti() >= ESERCITI_MINIMI_GUARDIA && esercito == guardiaCapitale(bot) && distanzaDallaCapitale(bot, esercito) <= 1;
    }

    //l'esercito più vicino alla capitale (a pari distanza il più debole)
    @Override
    public Army guardiaCapitale(Player bot) {
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return null;}
        Map<Location, Integer> distanza = distanzeDallaCapitale(bot);
        Army migliore = null;
        int migliorDistanza = Integer.MAX_VALUE;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto() || a.getPosizione() == null){continue;}
            Integer d = distanza.get(a.getPosizione());
            if(d == null){continue;}
            if(d < migliorDistanza || (d == migliorDistanza && a.getNumeroUnita() < migliore.getNumeroUnita())){
                migliorDistanza = d;
                migliore = a;
            }
        }
        return migliore;
    }

    @Override
    public List<CitySpecialization> specializzazioniPrioritarie() {return List.of(CitySpecialization.COMMERCIALE);}
}