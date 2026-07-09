package it.unicam.cs.mpgc.rpg125715;

public class BotDecisionService {

    public BotDecision decide(Game game, Player bot){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il player non è un bot");}


        Territory territory = bot.getTerritorio();
        for(City c : territory.getCities()){
            Location locationCity = c.getLocation();
            if(bot.getOro() > 0){
                if(c.getLevel() != CityLevel.ACCAMPAMENTO && locationCity.hasArmyPlayer(bot)){
                    return new BotDecision(BotActionType.RECRUIT, null, null, c);
                }
            }
        }

        for(Player p : game.getPlayers()){
            if(p == null || p == bot){continue;}

            Territory territorioNemico = p.getTerritorio();
            if(territorioNemico == null){continue;}

            for(City c : territorioNemico.getCities()){
                if(c == null || c.getLocation() == null){continue;}
                for(Army a :bot.getEserciti()){
                    if(a == null || a.getPosizione() == null){continue;}
                    if(a.getPosizione().isAdiacente(c.getLocation())){
                        return new BotDecision(BotActionType.ATTACK, a.getPosizione(), c.getLocation(), null);
                    }
                    for(Army b : p.getEserciti()){
                        if(b == null || b.getPosizione() == null){continue;}
                        if(a.getPosizione().isAdiacente(b.getPosizione())){
                            return new BotDecision(BotActionType.ATTACK, a.getPosizione(), b.getPosizione(), null);
                        }
                    }
                }
            }

        }
        return new  BotDecision(BotActionType.PASS, null, null, null);
    }
}
