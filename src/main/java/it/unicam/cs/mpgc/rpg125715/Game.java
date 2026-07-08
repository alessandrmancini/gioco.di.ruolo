package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<Player> players;
    private List<Location> locations;
    private int turno;
    private boolean iniziato;
    private boolean finita;
    private int numeroTurno;

    public Game(List<Player> players, List<Location> locations){
        if(players == null ||players.isEmpty()){throw new IllegalArgumentException("players null o vuota");}
        if(locations == null || locations.isEmpty()){throw new IllegalArgumentException("locations null o vuota");}
        this.players = new  ArrayList<>(players);
        this.locations = new  ArrayList<>(locations);
        this.turno = 0;
        this.iniziato = false;
        this.finita = false;
        this.numeroTurno = 1;
    }
    //PLAYER
    public List<Player> getActivePlayers(){
        List<Player> activePlayers = new ArrayList<>();
        for(Player p : players){
            if(!p.isSconfitto()){
                activePlayers.add(p);
            }
        }
        return activePlayers;
    }
    public Player getCurrentPlayer(){return players.get(turno);}
    public void nextPlayer(){
        if(finita){return;}
        int start = turno;
        do{
            turno = (turno+1)%players.size();
        }while(players.get(turno).isSconfitto() && turno != start);
    }
    public List<Player> getPlayers(){return new ArrayList<>(players);}
    public int numeroPlayerAttivi(){return getActivePlayers().size();}
    public boolean haSoloUnPlayerAttivo(){
        int count =  0;
        for(Player p : players){
            if(!p.isSconfitto()){
                count++;
                if(count > 1){return false;}
            }
        }
        return count == 1;
    }

    //LOCATION
    public List<Location> getLocations(){return new ArrayList<>(locations);}
    public Location getLocationById(int id){
        for(Location l : locations){
            if(l.getId() == id){return l;}
        }
        throw new IllegalArgumentException("location non presente");
    }

    //TURNI
    public int getTurno(){return turno;}
    public int getNumeroTurno(){return numeroTurno;}
    public void incrementaNumeroTurno(){this.numeroTurno++;}


    //GAME INIZIATO
    public void iniziaPartita(){iniziato = true;}
    public boolean isIniziato(){return iniziato;}

    //GAME FINITO
    public void setFinita(){finita = true;}
    public boolean isGameOver(){return finita;}
}
