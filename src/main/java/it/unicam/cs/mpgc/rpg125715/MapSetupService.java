package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSetupService {

    private static final String MAPPA_PREEDEFINITA = "mappa.txt";

    private final GameSetUpService gameSetUpService;
    private final MapDefinition mappa;

    private void assegnaCoordinateBase(Map<Integer, int[]> coordinates){
        if(coordinates == null){throw new IllegalArgumentException("coordinates is null");}

        // OVEST EUROPA / NORD AFRICA
        coordinates.put(1, new int[]{106, 406});   // Rabat
        coordinates.put(2, new int[]{229, 379});   // Cartagine
        coordinates.put(3, new int[]{226, 348});   // Cagliari
        coordinates.put(4, new int[]{253, 329});   // Roma
        coordinates.put(5, new int[]{205, 313});   // Marsiglia
        coordinates.put(6, new int[]{192, 267});   // Parigi
        coordinates.put(7, new int[]{135, 341});   // Madrid
        coordinates.put(8, new int[]{261, 289});   // Vienna
        coordinates.put(9, new int[]{248, 245});   // Berlino
        coordinates.put(10, new int[]{301, 244});  // Varsavia
        coordinates.put(11, new int[]{248, 211});  // Copenaghen
        coordinates.put(12, new int[]{205, 238});  // Amsterdam
        coordinates.put(13, new int[]{156, 278});  // Rennes
        coordinates.put(14, new int[]{171, 243});  // Londra
        coordinates.put(15, new int[]{121, 231});  // Dublino
        coordinates.put(16, new int[]{161, 213});  // Edimburgo
        coordinates.put(17, new int[]{68, 143});   // Reykjavik
        coordinates.put(42, new int[]{119, 373});  // Tariff

        // NORD EUROPA
        coordinates.put(41, new int[]{282, 179});  // Stoccolma

        // BALCANI / MEDIO ORIENTE
        coordinates.put(18, new int[]{333, 333});  // Pella
        coordinates.put(19, new int[]{335, 384});  // Creta
        coordinates.put(20, new int[]{397, 350});  // Ankara
        coordinates.put(21, new int[]{494, 405});  // Babilonia
        coordinates.put(22, new int[]{449, 493});  // Gedda
        coordinates.put(23, new int[]{365, 426});  // Il Cairo

        // URALI / ASIA CENTRALE
        coordinates.put(24, new int[]{414, 215});  // Mosca
        coordinates.put(25, new int[]{616, 307});  // Astana
        coordinates.put(26, new int[]{813, 161});  // Bratsk

        // INDIA / CINA / EST ASIA
        coordinates.put(28, new int[]{714, 467});  // Delhi
        coordinates.put(29, new int[]{861, 348});  // Xianyang
        coordinates.put(30, new int[]{955, 482});  // Hong Kong
        coordinates.put(31, new int[]{1085, 384}); // Tokyo

        // nodi intermedi (senza città)
        coordinates.put(27, new int[]{555, 280});
        coordinates.put(34, new int[]{490, 240});
        coordinates.put(35, new int[]{445, 378});
        coordinates.put(36, new int[]{566, 432});
        coordinates.put(37, new int[]{640, 452});
        coordinates.put(39, new int[]{893, 400});
        coordinates.put(43, new int[]{837, 255});  // tra Bratsk e Xianyang
        coordinates.put(44, new int[]{229, 321});  // tra Roma e Marsiglia
        coordinates.put(45, new int[]{370, 203});  // tra Mosca e Stoccolma (1)
        coordinates.put(46, new int[]{326, 191});  // tra Mosca e Stoccolma (2)
        coordinates.put(47, new int[]{407, 459});  // tra Il Cairo e Gedda
        coordinates.put(48, new int[]{227, 363});  // tra Cartagine e Cagliari
        coordinates.put(49, new int[]{160, 305});  // tra Parigi e Madrid (collegato a Rennes)
    }

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
