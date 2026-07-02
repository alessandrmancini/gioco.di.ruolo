package it.unicam.cs.mpgc.rpg125715;

import java.util.Random;

public class RibellioneService {

    private final Random random;

    public RibellioneService() {
        this.random = new Random();
    }

    public void verificaNuovaRibellione(Territory territory, boolean effettoLeaderNemico){
        if(territory == null){throw new IllegalArgumentException("territory null");}

        //non può esserci più di una ribellione per territorio
        if(territory.hasRibellione()){return;}

        int probabilita = calcolaProbabilitaRibellione(territory, effettoLeaderNemico);
        if(probabilita <= 0){return;}

        int tiro = random.nextInt(100)+1;
        if(tiro <= probabilita){
            territory.attivaRibellione();
        }
    }

    public void aggiornaRibellioneTurno(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}
        if(!territory.hasRibellione()){return;}

        Ribellione ribellione = territory.getRibellione();
        if(ribellione.isPacificazioneProgrammata()){
            ribellione.pacifica();
            territory.rimuoviRibellione();
            return;
        }
        if(puoEsserePacificato(territory)){
            ribellione.programmaPacificazione();
        }
        else{ribellione.annullaPacificazione();}

        ribellione.nextTurn();

        if(ribellione.getTurniAttiva()>7){territory.getOwner().setSconfitto();}
    }

    public boolean territorioProduceOro(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}
        return !territory.hasRibellione();
    }
    public boolean applicaMalusOro(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}
        if(!territory.hasRibellione()){return false;}
        return territory.getRibellione().applicaMalusOro();
    }


    private int calcolaProbabilitaRibellione(Territory territory, boolean effettoLeaderNemico){
        int probabilita = 0;
        if(tutteCittaSenzaGuarnigione(territory)){probabilita += 35;}
        if(tutteCittaNonSviluppate(territory)){probabilita += 20;}
        if(!haCittaMilitare(territory)){probabilita +=25;}
        if(effettoLeaderNemico){probabilita += 20;}
        return probabilita;
    }

    //CONDIZIONI PER RIBELLIONE
    //1
    private boolean tutteCittaNonSviluppate(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}
        for(City city : territory.getCities()){
            if(city.getLevel() != CityLevel.ACCAMPAMENTO){
                return false;
            }
        }
        return true;
    }
    //2
    private boolean tutteCittaSenzaGuarnigione(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}
        for(City city : territory.getCities()){
            Location l = city.getLocation();
            for(Army a: l.getArmies()){
                if(a != null && a.getOwner() == territory.getOwner()){return false;}
            }
        }
        return true;
    }
    //3
    private boolean haCittaMilitare(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}

        for(City city : territory.getCities()){
            if(city.getSpecialization() == CitySpecialization.MILITARE){return true;}
        }
        return false;
    }

    private boolean puoEsserePacificato(Territory territory){
        if(territory == null){throw new IllegalArgumentException("territory null");}
        for(City city : territory.getCities()){
            Location l = city.getLocation();
            for(Army a: l.getArmies()){
                if(a != null && a.getOwner() == territory.getOwner() && a.getNumeroUnita()>=4){return true;}
            }
        }
        return false;
    }


}
