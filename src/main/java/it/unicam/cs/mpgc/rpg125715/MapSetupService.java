package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSetupService {

    private final GameSetUpService gameSetUpService;

    public MapSetupService(GameSetUpService gameSetUpService) {
        if(gameSetUpService == null){throw new IllegalArgumentException("GameSetUpService is null");}
        this.gameSetUpService = gameSetUpService;
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

    public List<Location> creaMappaGioco(List<Player> players){
        if(players == null){throw new IllegalArgumentException("players is null");}

        Map<Integer, int[]> coordinates = new HashMap<>();
        List<int[]> adiacenze = new ArrayList<>();
        Map<Integer,City> cityPerLocation = new HashMap<>();

        assegnaCoordinateBase(coordinates);
        assegnaAdiacenzaBase(adiacenze);
        assegnaCittaAiNodi(players, cityPerLocation);
        return creaMappaBase(coordinates, adiacenze, cityPerLocation);
    }

    private void assegnaCoordinateBase(Map<Integer, int[]> coordinates){
        if(coordinates == null){throw new IllegalArgumentException("coordinates is null");}

        //OVEST EUROPE / NORD AFRICA
        coordinates.put(1, new int[]{120,260});   //Rabat
        coordinates.put(2, new int[]{220,250});   //Cartagine
        coordinates.put(3, new int[]{320, 220});  // Cagliari
        coordinates.put(4, new int[]{360, 180});  // Roma
        coordinates.put(5, new int[]{300, 120});  // Marsiglia
        coordinates.put(6, new int[]{220, 110});  // Parigi
        coordinates.put(7, new int[]{160, 180});  // Madrid
        coordinates.put(8, new int[]{430, 120});  // Vienna
        coordinates.put(9, new int[]{500, 90});   // Berlino
        coordinates.put(10, new int[]{580, 110}); // Varsavia
        coordinates.put(11, new int[]{470, 40});  // Copenaghen
        coordinates.put(12, new int[]{350, 40});  // Amsterdam
        coordinates.put(13, new int[]{250, 60});  // Rennes
        coordinates.put(14, new int[]{140, 20});  // Londra
        coordinates.put(15, new int[]{80, 40});   // Dublino
        coordinates.put(16, new int[]{200, 10});  // Edimburgo
        coordinates.put(17, new int[]{20, 0});  // Reykjavik

        // BALCANI / MEDIO ORIENTE
        coordinates.put(18, new int[]{470, 200}); // Pella
        coordinates.put(19, new int[]{420, 260}); // Creta
        coordinates.put(20, new int[]{560, 200}); // Ankara
        coordinates.put(21, new int[]{700, 230}); // Babilonia
        coordinates.put(22, new int[]{650, 320}); // Gedda
        coordinates.put(23, new int[]{470, 330}); // Il Cairo

        // URALI / ASIA CENTRALE
        coordinates.put(24, new int[]{700, 70});  // Mosca
        coordinates.put(25, new int[]{880, 110}); // Astana
        coordinates.put(26, new int[]{1040, 70}); // Bratsk
        coordinates.put(27, new int[]{860, 220}); // nodo intermedio area Attila

        // INDIA / CINA / EST ASIA
        coordinates.put(28, new int[]{980, 300});  // Delhi
        coordinates.put(29, new int[]{1120, 260}); // Xianyang
        coordinates.put(30, new int[]{1230, 360}); // Hong Kong
        coordinates.put(31, new int[]{1360, 330}); // Tokyo

        // nodi intermedi utili
        coordinates.put(32, new int[]{260, 180});
        coordinates.put(33, new int[]{390, 150});
        coordinates.put(34, new int[]{540, 150});
        coordinates.put(35, new int[]{610, 180});
        coordinates.put(36, new int[]{760, 180});
        coordinates.put(37, new int[]{820, 260});
        coordinates.put(38, new int[]{920, 260});
        coordinates.put(39, new int[]{1080, 320});
        coordinates.put(40, new int[]{1180, 320});
    }
    private void assegnaAdiacenzaBase(List<int[]> adiacenze){
        if(adiacenze == null){throw new IllegalArgumentException("adiacenze is null");}
        // Ovest
        adiacenze.add(new int[]{17,16});
        adiacenze.add(new int[]{16,14});
        adiacenze.add(new int[]{14,15});
        adiacenze.add(new int[]{14, 12});
        adiacenze.add(new int[]{12, 6});
        adiacenze.add(new int[]{6, 13});
        adiacenze.add(new int[]{6, 5});
        adiacenze.add(new int[]{5, 4});
        adiacenze.add(new int[]{4, 3});
        adiacenze.add(new int[]{6, 7});
        adiacenze.add(new int[]{7, 1});
        adiacenze.add(new int[]{1, 2});
        adiacenze.add(new int[]{2, 3});

        // Centro Europa
        adiacenze.add(new int[]{4, 8});
        adiacenze.add(new int[]{8, 9});
        adiacenze.add(new int[]{9, 10});
        adiacenze.add(new int[]{9, 11});
        adiacenze.add(new int[]{8, 18});
        adiacenze.add(new int[]{18, 19});
        adiacenze.add(new int[]{18, 20});

        // Sud-est / Medio Oriente
        adiacenze.add(new int[]{20, 35});
        adiacenze.add(new int[]{35, 21});
        adiacenze.add(new int[]{21, 22});
        adiacenze.add(new int[]{22, 23});
        adiacenze.add(new int[]{23, 2});
        adiacenze.add(new int[]{23, 19});

        // Urali
        adiacenze.add(new int[]{10, 24});
        adiacenze.add(new int[]{24, 34});
        adiacenze.add(new int[]{34, 27});
        adiacenze.add(new int[]{27, 25});
        adiacenze.add(new int[]{25, 26});

        // Cina / India
        adiacenze.add(new int[]{21, 36});
        adiacenze.add(new int[]{36, 37});
        adiacenze.add(new int[]{37, 28});
        adiacenze.add(new int[]{28, 38});
        adiacenze.add(new int[]{38, 29});
        adiacenze.add(new int[]{29, 39});
        adiacenze.add(new int[]{39, 40});
        adiacenze.add(new int[]{40, 30});
        adiacenze.add(new int[]{30, 31});

        // collegamenti alternativi interni
        adiacenze.add(new int[]{3, 32});
        adiacenze.add(new int[]{32, 4});
        adiacenze.add(new int[]{4, 33});
        adiacenze.add(new int[]{33, 8});
        adiacenze.add(new int[]{20, 24});
    }
    private void assegnaCittaAiNodi(List<Player> players, Map<Integer,City> cityPerLocation){
        if(players == null){throw new IllegalArgumentException("players is null");}
        if(cityPerLocation == null){throw new IllegalArgumentException("cityPerLocation is null");}

        for(Player player: players){
            if(player == null){throw new IllegalArgumentException("player is null");}
            for(City city: player.getTerritorio().getCities()){
                int locationId = trovaLocationPerNomeCitta(city.getName());
                cityPerLocation.put(locationId,city);
            }
        }
    }

    private int trovaLocationPerNomeCitta(String nome){
        if(nome == null){throw new IllegalArgumentException("nome is null");}
        return switch (nome){
          case "Rabat" -> 1;
          case "Cartagine" -> 2;
            case "Cagliari" -> 3;
            case "Roma" -> 4;
            case "Marsiglia" -> 5;
            case "Parigi" -> 6;
            case "Madrid" -> 7;
            case "Vienna" -> 8;
            case "Berlino" -> 9;
            case "Varsavia" -> 10;
            case "Copenaghen" -> 11;
            case "Amsterdam" -> 12;
            case "Rennes" -> 13;
            case "Londra" -> 14;
            case "Dublino" -> 15;
            case "Edimburgo" -> 16;
            case "Reykjavik" -> 17;
            case "Pella" -> 18;
            case "Creta" -> 19;
            case "Ankara" -> 20;
            case "Babilonia" -> 21;
            case "Gedda" -> 22;
            case "Il Cairo" -> 23;
            case "Mosca" -> 24;
            case "Astana" -> 25;
            case "Bratsk" -> 26;
            case "Delhi" -> 28;
            case "Xianyang" -> 29;
            case "Hong Kong" -> 30;
            case "Tokyo" -> 31;
            default -> throw new IllegalArgumentException("nome non riconosciuto");
        };
    }
}
