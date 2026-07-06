package it.unicam.cs.mpgc.rpg125715;


import java.util.ArrayList;
import java.util.List;

public class GameSetUpService {
        private final IdGenerator idGenerator;

        public GameSetUpService(IdGenerator idGenerator) {
            if(idGenerator == null){throw new IllegalArgumentException("idGenerator null");}
            this.idGenerator = idGenerator;
        }

        public record StartingProfile(String territoryName, String capitalName){}

        public StartingProfile getStartingProfile(LeaderType leader){
                if(leader == null){throw new IllegalArgumentException("leader is null");}
                return switch (leader){
                    case GIULIO_CESARE -> new StartingProfile("Italia", "Roma");
                    case ALESSANDRO_MAGNO -> new StartingProfile("Balcani", "Pella");
                    case ANNIBALE -> new StartingProfile("Nord Africa", "Cartagine");
                    case REGINA_ELISABETTA -> new StartingProfile("Inghilterra", "Londra");
                    case ATTILA -> new StartingProfile("Urali", "Mosca");
                    case GIOVANNA_D_ARCO -> new StartingProfile("Francia", "Parigi");
                    case FEDERICO_BARBAROSSA -> new StartingProfile("Germania", "Berlino");
                    case QIN_SHI_HUANG -> new StartingProfile("Cina", "Xianyang");
                };
        }

        public Territory creaTerritorioPerLeader(LeaderType leader){
            StartingProfile profile = getStartingProfile(leader);

            return new Territory(idGenerator.nextId(EntityType.TERRITORY), profile.territoryName());
        }

        public Player creaPlayerIniziale(String nome, LeaderType leader, int denari){
            if(nome == null || nome.isBlank()){throw new IllegalArgumentException("nome is null");}
            if(leader == null){throw new IllegalArgumentException("leader is null");}
            if(denari<0){throw new IllegalArgumentException("denari is negative");}

            Territory territory = creaTerritorioPerLeader(leader);
            Player player = new Player(idGenerator.nextId(EntityType.PLAYER),nome, leader, denari, territory);

            territory.setOwner(player);

            List<City> cities = creaCitiesForLeader(leader, player);

            for(City city : cities){
                territory.addCity(city);
            }

            City capitale = trovaCapitale(cities, leader);
            territory.setCapitale(capitale);
            return player;
        }

        public Location creaLocation(int x, int y){
            return new Location(idGenerator.nextId(EntityType.LOCATION), x, y);
        }

        public City creaCity(CityType cityType){
            if(cityType == null){throw new IllegalArgumentException("cityType is null");}

            City c = new City(cityType.getDisplayName());
            if(cityType.getStartLeaderType().haCapitaleInizialeSviluppata() && cityType.isCapital()){
                c.upgrade();
            }
            return c;
        }

        public List<CityType> getCityTypesForLeader(LeaderType leader){
            if(leader == null){throw new IllegalArgumentException("leader is null");}
            List<CityType> cityTypes = new ArrayList<>();
            for(CityType city : CityType.values()){
                if(city.getStartLeaderType()== leader){
                    cityTypes.add(city);
                }
            }
            return cityTypes;
        }
        public CityType getCapitaleForLeader(LeaderType leader){
            if(leader == null){throw new IllegalArgumentException("leader is null");}
            for(CityType city : CityType.values()){
                if(city.getStartLeaderType()== leader && city.isCapital()){return city;}
            }
            throw new IllegalArgumentException("nessuna capitale trovata");
        }

        public List<City> creaCitiesForLeader(LeaderType leader, Player owner){
            if(leader == null){throw new IllegalArgumentException("leader is null");}
            if(owner == null){throw new IllegalArgumentException("owner is null");}
            if(owner.getLeader() != leader){throw new IllegalArgumentException("leader mismatch");}

            List<City> cities = new ArrayList<>();
            for(CityType city : getCityTypesForLeader(leader)){
                City c = creaCity(city);
                c.setOwner(owner);
                cities.add(c);
            }
            return cities;
        }

        public City trovaCapitale(List<City> cities, LeaderType leader){
            if(cities == null || cities.isEmpty()){throw new IllegalArgumentException("cities is null or empty");}
            if(leader == null){throw new IllegalArgumentException("leader is null");}

            String capitalName = getCapitaleForLeader(leader).getDisplayName();
            for(City city : cities){
                if(city.getName().equals(capitalName)){
                    return city;
                }
            }
            throw new IllegalArgumentException("nessuna capitale trovata");
        }

}
