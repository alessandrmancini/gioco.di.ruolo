package it.unicam.cs.mpgc.rpg125715;

public class BotDecisionService {

    public BotDecision decide(Game game, Player bot){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il player non è un bot");}

        if(bot.getOro() > 0 && bot.hasCapitale()){return new BotDecision(BotActionType.RECRUIT, bot.getCapitale());}
        return new  BotDecision(BotActionType.PASS, null);
    }
}
