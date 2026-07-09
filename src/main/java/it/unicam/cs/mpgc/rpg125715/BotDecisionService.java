package it.unicam.cs.mpgc.rpg125715;

public class BotDecisionService {

    public BotDecision decide(Game game, Player bot){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il player non è un bot");}

        City capitale = bot.getCapitale();
        Location locationCapitale = bot.getCapitale().getLocation();
        if(bot.getOro() > 0 && bot.hasCapitale()){
            if(capitale.getLevel() != CityLevel.ACCAMPAMENTO && locationCapitale.hasArmyPlayer(bot)){
                return new BotDecision(BotActionType.RECRUIT, capitale);
            }


        }
        return new  BotDecision(BotActionType.PASS, null);
    }
}
