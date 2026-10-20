package it.unicam.cs.mpgc.rpg125715;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private final Map<EntityType, AtomicInteger> counters;

    public IdGenerator() {
        this.counters = new EnumMap<>(EntityType.class);

        for(EntityType type : EntityType.values()) {
            counters.put(type, new AtomicInteger(1));
        }
    }


    public void assicuraMinimo(EntityType type, int prossimo) {
        if(type == null){throw new IllegalArgumentException("entity type null");}
        counters.get(type).accumulateAndGet(prossimo, Math::max);
    }
    public void allineaA(Game game){
        if(game == null){throw new IllegalArgumentException("game is null");}
        for(Player p : game.getPlayers()){
            assicuraMinimo(EntityType.PLAYER, p.getId()+1);
            assicuraMinimo(EntityType.TERRITORY, p.getTerritorio().getId()+1);
            for(Army a : p.getEserciti()){
                assicuraMinimo(EntityType.ARMY, a.getId()+1);
            }
        }
        for(Location l : game.getLocations()){
            assicuraMinimo(EntityType.LOCATION, l.getId()+1);
        }
    }

    public int nextId(EntityType type) {
        if(type == null){throw new IllegalArgumentException("entity type null");}
        return counters.get(type).getAndIncrement();
    }
}
