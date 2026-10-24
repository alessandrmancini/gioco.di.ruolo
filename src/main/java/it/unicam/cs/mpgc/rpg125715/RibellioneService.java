package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RibellioneService {

    private final Random random;
    private final ConquestService conquestService;

    public RibellioneService(ConquestService conquestService) {
        this(conquestService, new Random());
    }

    public RibellioneService(ConquestService conquestService, Random random) {
        if(conquestService == null){throw new IllegalArgumentException("conquestService null");}
        this.random = new Random();
        this.conquestService = conquestService;
    }

    //chiamato all'inizio del turno del giocatore
    public void aggiornaRibellioni(Player player, Game game){
        if(player == null || game == null){throw new IllegalArgumentException("player o game null");}

        //1) città già in ribellione
        for(City city : player.getTerritorio().getCities()){
            Ribellione ribellione = city.getRibellione();
            if(ribellione == null){continue;}

            if(puoEssereRiappacificata(city, player)){
                city.rimuoviRibellione();
                game.aggiungiEvento("La ribellione di " + city.getName() + " è stata sedata");
                continue;
            }
            ribellione.nextTurn();
            if(ribellione.cittaPersa()){
                conquestService.abbandona(city);
                game.aggiungiEvento(player.getName() + " perde " + city.getName() + ": la ribellione l'ha resa indipendente");
            }
        }

        //2) possibile nuova ribellione (non nel primo turno del giocatore)
        if(player.isSconfitto() || player.getTurniGiocati() == 0){return;}
        verificaNuovaRibellione(player, game);
    }

    private void verificaNuovaRibellione(Player player, Game game){
        int probabilita = calcolaProbabilita(player, game);
        if(probabilita <= 0){return;}

        City bersaglio = scegliCittaBersaglio(player);
        if(bersaglio == null){return;}

        int tiro = random.nextInt(100) + 1;
        if(tiro <= probabilita){
            bersaglio.attivaRibellione();
            game.aggiungiEvento("Ribellione a " + bersaglio.getName() + " (" + player.getName() + ")!");
        }
    }

    //la ribellione colpisce una città senza esercito, altrimenti quella con l'esercito più piccolo
    private City scegliCittaBersaglio(Player player){
        List<City> senzaEsercito = new ArrayList<>();
        City piuDebole = null;
        int minimo = Integer.MAX_VALUE;

        for(City city : player.getTerritorio().getCities()){
            if(city.hasRibellione()){continue;}
            Army presidio = esercitoInCitta(city, player);
            if(presidio == null){
                senzaEsercito.add(city);
            }
            else if(presidio.getNumeroUnita() < minimo){
                minimo = presidio.getNumeroUnita();
                piuDebole = city;
            }
        }
        if(!senzaEsercito.isEmpty()){
            return senzaEsercito.get(random.nextInt(senzaEsercito.size()));
        }
        return piuDebole;
    }

    private Army esercitoInCitta(City city, Player player){
        Location l = city.getLocation();
        if(l == null){return null;}
        for(Army a : l.getArmies()){
            if(a != null && a.getOwner() == player){return a;}
        }
        return null;
    }

    private boolean puoEssereRiappacificata(City city, Player player){
        Army presidio = esercitoInCitta(city, player);
        return presidio != null && presidio.getNumeroUnita() >= Ribellione.UNITA_PER_PACIFICARE;
    }

    //PROBABILITA' (mostrata anche nell'interfaccia)
    public int calcolaProbabilita(Player player, Game game){
        int probabilita = 0;
        Territory territory = player.getTerritorio();
        if(territory.getCities().isEmpty()){return 0;}
        if(tutteCittaSenzaGuarnigione(territory)){probabilita += 20;}
        if(tutteCittaNonSviluppate(territory)){probabilita += 10;}
        if(!haCittaMilitare(territory)){probabilita += 15;}
        if(subisceEffettoLeaderNemico(player, game)){probabilita += 20;}
        return probabilita;
    }

    public List<String> causeAttive(Player player, Game game){
        List<String> cause = new ArrayList<>();
        Territory territory = player.getTerritorio();
        if(territory.getCities().isEmpty()){return cause;}
        if(tutteCittaSenzaGuarnigione(territory)){cause.add("+20%  nessuna città con un tuo esercito");}
        if(tutteCittaNonSviluppate(territory)){cause.add("+10%  tutte le città sono accampamenti");}
        if(!haCittaMilitare(territory)){cause.add("+15%  nessuna città militare");}
        if(subisceEffettoLeaderNemico(player, game)){cause.add("+20%  leader nemico vicino");}
        return cause;
    }

    //Giovanna d'Arco: un suo esercito vicino a una città nemica ne aumenta il rischio
    public boolean subisceEffettoLeaderNemico(Player player, Game game){
        for(Player altro : game.getPlayers()){
            if(altro == player || altro.isSconfitto() || !altro.getLeader().aumentaProbabilitaInsurrezioniNemiche()){continue;}
            for(Army army : altro.getEserciti()){
                for(City city : player.getTerritorio().getCities()){
                    Location l = city.getLocation();
                    if(l != null && army.getPosizione() != null && l.isAdiacente(army.getPosizione())){return true;}
                }
            }
        }
        return false;
    }

    //CONDIZIONI
    private boolean tutteCittaNonSviluppate(Territory territory){
        for(City city : territory.getCities()){
            if(city.getLevel() != CityLevel.ACCAMPAMENTO){return false;}
        }
        return true;
    }
    private boolean tutteCittaSenzaGuarnigione(Territory territory){
        for(City city : territory.getCities()){
            if(esercitoInCitta(city, territory.getOwner()) != null){return false;}
        }
        return true;
    }
    private boolean haCittaMilitare(Territory territory){
        for(City city : territory.getCities()){
            if(city.getSpecialization() == CitySpecialization.MILITARE){return true;}
        }
        return false;
    }
}