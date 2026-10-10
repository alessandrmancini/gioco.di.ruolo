package it.unicam.cs.mpgc.rpg125715;

public class ConquestService {


    public void conquista(City city, Player conquistatore) {
        if(city == null || conquistatore == null){throw new IllegalArgumentException("city o conquistatore null");}
        if(city.getOwner() == conquistatore){throw new IllegalArgumentException("conquistatore già in possesso della città");}

        togliCittaAlProprietario(city);

        Territory nuovoTerritory = conquistatore.getTerritorio();
        nuovoTerritory.addCity(city);
        if(nuovoTerritory.getCapitale() == null){nuovoTerritory.nominaCapitale();}
    }

    private void togliCittaAlProprietario(City city){
        Player vecchioOwner = city.getOwner();
        if(vecchioOwner == null){return;}

        Territory vecchioTerritory = vecchioOwner.getTerritorio();
        vecchioTerritory.removeCity(city);
        city.resetOwner();

        if(vecchioTerritory.getCities().isEmpty()){
            vecchioTerritory.rimuoviCapitale();
            if(vecchioOwner.numeroEserciti() == 0){vecchioOwner.setSconfitto();}
        }
        else if(vecchioTerritory.getCapitale() == null){
            vecchioTerritory.nominaCapitale();
        }
    }

    //un giocatore senza città può trasformare in città il nodo dove si trova un suo esercito
    public Location trovaLocationPerFondazione(Player player){
        if(player == null){throw new IllegalArgumentException("player null");}
        if(player.isSconfitto() || player.numeroCitta() > 0){return null;}
        for(Army a : player.getEserciti()){
            Location l = a.getPosizione();
            if(l != null && !l.hasCity()){return l;}
        }
        return null;
    }

    public City fondaCitta(Player player, Location location, String nome){
        if(player == null || location == null){throw new IllegalArgumentException("player o location null");}
        if(nome == null || nome.isBlank()){throw new IllegalArgumentException("il nome non può essere vuoto");}
        if(player.isSconfitto() || player.numeroCitta() > 0){throw new IllegalArgumentException("puoi fondare una città solo se non ne hai più");}
        if(location.hasCity()){throw new IllegalArgumentException("in questo nodo c'è già una città");}
        if(!location.hasArmyPlayer(player)){throw new IllegalArgumentException("serve un tuo esercito nel nodo");}

        City city = new City(nome.trim());
        location.addCity(city);
        Territory territory = player.getTerritorio();
        territory.addCity(city);
        territory.nominaCapitale();
        return city;
    }
}