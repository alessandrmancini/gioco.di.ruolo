package it.unicam.cs.mpgc.rpg125715;

/**
 * Azione scelta da un bot. I campi che non servono per quell'azione sono null
 * (per esempio un MOVE usa solo partenza e destinazione).
 */
public record BotDecision(BotActionType actionType,Location sourceLocation, Location targetLocation, City city, CitySpecialization specialization) {
    public BotDecision {
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
    }
    public BotDecision(BotActionType actionType, Location sourceLocation, Location targetLocation,City city){
        this(actionType,sourceLocation,targetLocation,city,null);
    }
}
