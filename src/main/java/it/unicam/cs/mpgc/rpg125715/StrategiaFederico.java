package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;


/**
 * Federico Barbarossa: difensivo. Lascia un esercito sulle città di confine, crea gli eserciti
 * lì e contrattacca subito chi lo ha attaccato.
 */
public class StrategiaFederico extends StrategiaBase {

    public StrategiaFederico(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{4, 3, 5, 3, 2, 2};}

    //cintura: un esercito resta in ogni città di confine (ma almeno uno resta libero)
    @Override
    public boolean eGuardia(Player bot, Army esercito) {
        if(!staSuCittaDiConfine(bot, esercito)){return false;}
        List<Army> guardie = new ArrayList<>();
        int eserciti = 0;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto()){continue;}
            eserciti++;
            if(staSuCittaDiConfine(bot, a)){guardie.add(a);}
        }
        //se tutti gli eserciti sarebbero di guardia, il più forte resta libero
        if(guardie.size() >= eserciti){
            Army libero = null;
            for(Army a : guardie){
                if(libero == null || confrontaForza(a, libero, false) > 0){libero = a;}
            }
            return esercito != libero;
        }
        return true;
    }

    /** I nuovi eserciti nascono nelle città di confine. */
    @Override
    public boolean preferisceCittaDiConfine() {return true;}

    /** Dopo aver respinto un attacco, attacca l'esercito che lo ha attaccato. */
    @Override
    public boolean contrattacca() {return true;}

    /** Prima una città militare (+2 difesa). */
    @Override
    public List<CitySpecialization> specializzazioniPrioritarie() {return List.of(CitySpecialization.MILITARE);}

    /** La prima specializzazione va fatta nella capitale. */
    @Override
    public boolean specializzaInCapitale() {return true;}

    private boolean staSuCittaDiConfine(Player bot, Army a) {
        Location l = a.getPosizione();
        return l != null && l.hasCity() && l.getCity().getOwner() == bot && eCittaDiConfine(bot, l);
    }
}