package it.unicam.cs.mpgc.rpg125715;

public record BotDecision(BotActionType actionType, City sourceCity) {
    public BotDecision {
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
    }
}
