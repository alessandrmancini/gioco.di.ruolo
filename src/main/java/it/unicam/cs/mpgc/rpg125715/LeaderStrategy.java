package it.unicam.cs.mpgc.rpg125715;

import java.util.List;
import java.util.Map;

public interface LeaderStrategy {
    int[] pesiAzioni(); //pesi delle azioni: costruzione specializzazione, ...
    default int bonusCaccia(){return 6;}
    default int margineMinimo(){return -3;} // un leader attacca un esercito solo se (attacco - difesa) non è più basso di questo valore

    //==================ATTACCO==================
    default boolean attaccoConsentito (Player bot, Army esercito, Location destinazione, Army nemico){return true;}
    default int punteggioAttacco(Player bot, Army esercito, Location destinazione, Army nemico, int base){return base;}

    //=================MOVIMENTO====================

    default boolean eGuardia(Player bot, Army esercito){return false;}
    default Army guardiaCapitale(Player bot){return null;}

    //ver se creando un nuovo esercito, il leader preferisce una città di confine
    default boolean preferisceCittaDiConfine(){return false;}

    //preferenza tra più spostamenti equivalenti
    default int preferenzaMovimento(Player bot, Army esercito,Location destinazione){return 0;}

    //vero se l'esercito può fare questo passo verso il bersaglio
    default boolean mossaConsentita(Player bot, Army esercito, Location destinazione, Map<Location, Integer> distanza){return true;}

    //=============SVILUPPO============

    //specializzazioni da fare appena possibile
    default List<CitySpecialization> specializzazioniPrioritarie(){return List.of();}

    default boolean contrattacca(){return false;}

    //la prima specializzazione va fatta in una città vicina ai nemici
    default boolean sviluppoDiFrontiera(){return false;}

    //la prima specializzazione va fatta nella capitale
    default boolean specializzaInCapitale(){return false;}

    //vero se in questo turno il leader può costruire, specializzare, reclutare
    default boolean ammetteSviluppo(Player bot){return true;}

    //vero se in questo turno il leader può attaccare e muovere
    default boolean ammetteGuerra(Player bot){return true;}

    //vero se conviene reclutare prima le macchine d'assedio
    default boolean preferisceAssedio(Player bot){return false;}
}
