package it.unicam.cs.mpgc.rpg125715;

import java.util.List;

/** Qin Shi Huang: prudente, consolida le città e attacca solo con una forte superiorità. */
public class StrategiaQin extends StrategiaBase {

    //attacca solo se il suo attacco è almeno questa percentuale della difesa
    private static final int PERCENTUALE_SUPERIORITA = 200;

    public StrategiaQin(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{5, 3, 4, 3, 3, 3};}

    /** Attacca solo se il suo attacco è almeno {@link #PERCENTUALE_SUPERIORITA}% della difesa nemica. */
    @Override
    public boolean attaccoConsentito(Player bot, Army esercito, Location destinazione, Army nemico) {
        return attaccoTotale(bot, esercito) * 100 >= difesaBersaglio(destinazione, nemico) * PERCENTUALE_SUPERIORITA;
    }

    //tra i passi equivalenti sceglie quello meno esposto ai nemici
    @Override
    public int preferenzaMovimento(Player bot, Army esercito, Location destinazione) {
        return -nemiciAccanto(bot, destinazione);
    }

    /** Prima commerciale, poi agricola. */
    @Override
    public List<CitySpecialization> specializzazioniPrioritarie() {
        return List.of(CitySpecialization.COMMERCIALE, CitySpecialization.AGRICOLA);
    }
}