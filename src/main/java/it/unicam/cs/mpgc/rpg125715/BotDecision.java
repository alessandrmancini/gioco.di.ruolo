package it.unicam.cs.mpgc.rpg125715;

public record BotDecision(BotActionType actionType,Location sourceLocation, Location targetLocation, City city) {
    public BotDecision {
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
    }
}
