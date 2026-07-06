package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;

public class GameInitializationService {

    private final GameSetUpService gameSetUpService;
    private final MapSetupService  mapSetupService;
    private final ArmyService armyService;

    public GameInitializationService(GameSetUpService gameSetUpService, MapSetupService mapSetupService, ArmyService armyService) {
        if(gameSetUpService == null){throw new IllegalArgumentException("gameSetUpService is null");}
        if(mapSetupService == null){throw new IllegalArgumentException("mapSetupService is null");}
        if(armyService == null){throw new IllegalArgumentException("armyService is null");}

        this.gameSetUpService = gameSetUpService;
        this.mapSetupService = mapSetupService;
        this.armyService = armyService;
    }
    public Game creaPartitaBase(List<PlayerConfig> configurazioneGiocatori){
        if(configurazioneGiocatori == null || configurazioneGiocatori.isEmpty()){throw new IllegalArgumentException("configurazioneGiocatori null or vuota");}
        List<Player> players = creaPlayersIniziali(configurazioneGiocatori);
        List<Location> locations = mapSetupService.creaMappaGioco(players);

        creaEsercitiIniziali(players);
        Game game = new Game(players, locations);
        game.iniziaPartita();
        return game;
    }

    private List<Player> creaPlayersIniziali(List<PlayerConfig> configPlayer){
        List<Player> players = new ArrayList<>();
        for(PlayerConfig config : configPlayer){
            if(config == null){throw new IllegalArgumentException("config is null");}

            Player player = gameSetUpService.creaPlayerIniziale(config.nome(), config.leader(), config.oroIniziale());
            players.add(player);
        }
        return players;
    }

    private void creaEsercitiIniziali(List<Player> players){
        if(players == null){throw new IllegalArgumentException("players is null");}
        for(Player player : players){
            if(player == null){throw new IllegalArgumentException("player is null");}

            City capitale = player.getCapitale();
            if(capitale == null){throw new IllegalArgumentException("capitale is null");}

            Location posizioneIniziale = capitale.getLocation();
            if(posizioneIniziale == null){throw new IllegalArgumentException("posizione is null");}

            Army army = armyService.creaEsercito(player, posizioneIniziale);

            reclutaUnitaIniziali(player, capitale, army);
        }
    }
    private void reclutaUnitaIniziali(Player player, City capitale, Army army){
        if(player == null || capitale == null || army == null){throw new IllegalArgumentException("player, capitale o esercito null");}

        army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, player.getLeader()));
        army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, player.getLeader()));
        army.addUnit(UnitFactory.creaUnita(UnitType.CAVALLERIA, player.getLeader()));

        if(player.getLeader() == LeaderType.ANNIBALE){
            army.addUnit(UnitFactory.creaUnita(UnitType.ELEFANTI, player.getLeader()));
        }

        if(player.getLeader() != LeaderType.ANNIBALE){
            army.addUnit(UnitFactory.creaUnita(UnitType.SPECIALI, player.getLeader()));
        }
    }

    public record PlayerConfig(String nome, LeaderType leader, int oroIniziale){
        public PlayerConfig{
            if(nome == null || nome.isBlank()){throw new IllegalArgumentException("nome is null");}
            if(leader == null){throw new IllegalArgumentException("leader is null");}
            if(oroIniziale < 0){throw new IllegalArgumentException("oro iniziale is negative");}
        }
    }
}
