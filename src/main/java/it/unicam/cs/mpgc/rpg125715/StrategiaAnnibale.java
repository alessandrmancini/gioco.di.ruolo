package it.unicam.cs.mpgc.rpg125715;

import java.util.List;

/**
 * Annibale: attacca solo con l'esercito più forte (contando gli elefanti) e specializza
 * in militare per poter reclutare gli elefanti.
 */
public class StrategiaAnnibale extends StrategiaBase {

    public StrategiaAnnibale(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{2, 3, 4, 3, 5, 3};}

    //attacca solo l'esercito più forte (con più elefanti)
    @Override
    public boolean attaccoConsentito(Player bot, Army esercito, Location destinazione, Army nemico) {
        return esercito == esercitoPiuForte(bot, true);
    }

    //valichi: preferisce i nodi con pochi collegamenti
    @Override
    public int preferenzaMovimento(Player bot, Army esercito, Location destinazione) {
        return -destinazione.getAdiacenti().size();
    }

    /** Prima una città militare, per gli elefanti. */
    @Override
    public List<CitySpecialization> specializzazioniPrioritarie() {return List.of(CitySpecialization.MILITARE);}
}