package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSetupService {

    private static final String MAPPA_PREEDEFINITA = "mappa.txt";

    private final GameSetUpService gameSetUpService;
    private final MapDefinition mappa;

    public MapSetupService(GameSetUpService gameSetUpService) {
        this(gameSetUpService, MapDefinition.daRisorsa(MAPPA_PREEDEFINITA));
    }

    public MapSetupService(GameSetUpService gameSetUpService, MapDefinition mappa) {
        if(gameSetUpService == null){throw new IllegalArgumentException("GameSetUpService is null");}
        if(mappa == null){throw new IllegalArgumentException("mappa is null");}
        this.gameSetUpService = gameSetUpService;
        this.mappa = mappa;
    }

    public List<Location> creaMappaBase(Map<Integer,int[]> coordinates,List<int[]> adiacenze, Map<Integer, City> cityPerLocation){
        if(coordinates == null || coordinates.isEmpty()){throw new IllegalArgumentException("coordinates is null");}
        if(adiacenze == null){throw new IllegalArgumentException("adiacenze is null");}
        if(cityPerLocation == null){throw new IllegalArgumentException("cityPerLocation is null");}

        Map<Integer, Location> locationById = new HashMap<>();
        for(Map.Entry<Integer,int[]> entry: coordinates.entrySet()){
            int idLogico = entry.getKey();
            int[] xy = entry.getValue();

            if(locationById.containsKey(idLogico)){throw new IllegalArgumentException("id duplicato");}
            if(xy == null || xy.length != 2){throw new IllegalArgumentException("coordinate non valide");}
            Location loc = gameSetUpService.creaLocation(xy[0], xy[1]);
            locationById.put(idLogico, loc);
        }
        for(int[] edge: adiacenze){
            if(edge == null || edge.length != 2){throw new IllegalArgumentException("adiacenza non valida");}

            Location a = locationById.get(edge[0]);
            Location b = locationById.get(edge[1]);

            if(a == null || b == null){throw new IllegalArgumentException("adiacenza con id inesistente");}
            a.addAdiacente(b);
        }

        for(Map.Entry<Integer,City> entry: cityPerLocation.entrySet()){
            Location loc = locationById.get(entry.getKey());
            City city = entry.getValue();

            if(loc == null){throw new IllegalArgumentException("city non valide");}
            if(city == null){throw new IllegalArgumentException("city is null");}
            loc.addCity(city);
        }
        return new ArrayList<>(locationById.values());
    }

    public List<Location> creaMappaGioco(List<Player> players) {
        if(players == null){throw new IllegalArgumentException("players is null");}

        Map<Integer, City> cityPerLocation = new HashMap<>();
        assegnaCittaAiNodi(players, cityPerLocation);
        return creaMappaBase(mappa.getCoordinate(), mappa.getAdiacenze(), cityPerLocation);
    }
    private void assegnaCittaAiNodi(List<Player> players, Map<Integer,City> cityPerLocation){
        if(players == null){throw new IllegalArgumentException("players is null");}
        if(cityPerLocation == null){throw new IllegalArgumentException("cityPerLocation is null");}

        for(Player player: players){
            if(player == null){throw new IllegalArgumentException("player is null");}
            for(City city: player.getTerritorio().getCities()){
                cityPerLocation.put(mappa.getNodoDellaCitta(city.getName()), city);
            }
        }
    }
}
