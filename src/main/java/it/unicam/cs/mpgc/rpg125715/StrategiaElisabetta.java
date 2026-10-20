package it.unicam.cs.mpgc.rpg125715;

import java.util.List;

public class StrategiaElisabetta extends StrategiaBase {

    //attacca solo se il suo attacco supera la difesa di almeno questo valore
    private static final int MARGINE_COLPO_CALCOLATO = 5;

    public StrategiaElisabetta(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{4, 4, 3, 2, 2, 2};}

    @Override
    public boolean attaccoConsentito(Player bot, Army esercito, Location destinazione, Army nemico) {
        return margineAttacco(bot, esercito, destinazione, nemico) >= MARGINE_COLPO_CALCOLATO;
    }

    @Override
    public List<CitySpecialization> specializzazioniPrioritarie() {
        return List.of(CitySpecialization.COMMERCIALE, CitySpecialization.AGRICOLA);
    }
}