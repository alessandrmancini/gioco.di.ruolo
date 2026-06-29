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

    public int nextId(EntityType type) {
        if(type == null){throw new IllegalArgumentException("entity type null");}
        return counters.get(type).getAndIncrement();
    }
}
