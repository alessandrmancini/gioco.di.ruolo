package it.unicam.cs.mpgc.rpg125715;

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
