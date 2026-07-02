package it.unicam.cs.mpgc.rpg125715;

public class ConquestService {

    public void conquista(City city, Player conquistatore) {
        if(city == null || conquistatore == null){throw new IllegalArgumentException("city o conquistatore null");}

        Player vecchioOwner = city.getOwner();

        if(vecchioOwner == conquistatore){throw new IllegalArgumentException("conquistatore già in possesso della città");}
        if(vecchioOwner == null){throw new IllegalStateException("la città non ha proprietario");}

        Territory vecchioTerritory = vecchioOwner.getTerritorio();
        Territory nuovoTerritory = conquistatore.getTerritorio();

        vecchioTerritory.removeCity(city);
        city.setOwner(conquistatore);
        nuovoTerritory.addCity(city);

        if(vecchioTerritory.getCities().isEmpty()){
            vecchioOwner.setSconfitto();
            vecchioTerritory.rimuoviCapitale();
        }
        else if(vecchioTerritory.getCapitale() == null){
            vecchioTerritory.nominaCapitale();
        }
        if(nuovoTerritory.getCapitale() == null){nuovoTerritory.nominaCapitale();}
    }
}
