package it.unicam.cs.mpgc.rpg125715;

public class BotDecisionService {

    public BotDecision decide(Game game, Player bot){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il player non è un bot");}

        Territory territory = bot.getTerritorio();

        //DECISIONE RECLUTAMENTO
        for(City c : territory.getCities()){
            Location locationCity = c.getLocation();
            if(bot.getOro() > 0){
                if(c.getLevel() != CityLevel.ACCAMPAMENTO && locationCity.hasArmyPlayer(bot)){
                    return new BotDecision(BotActionType.RECRUIT, null, null, c);
                }
            }
        }

        //DECISIONE ATTACCO
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

        //DECISIONE MOVIMENTO
        for(Army a : bot.getEserciti()){
            if(a == null || a.getPosizione() == null){continue;}

            Location loc = a.getPosizione();
            Location best = null;
            int bestScore = -1;

            for(Location l : loc.getAdiacenti()){
                if(l == null ||l.hasArmy()){continue;}

                int score = 0;

                for(Location l2 : l.getAdiacenti()){
                    if(l2 == null){continue;}

                    if(l2.hasArmy()){
                        boolean nemicoVicino = false;
                        for(Army a2 : l2.getArmies()){
                            if(a2 != null && a2.getOwner() != bot){
                                nemicoVicino = true;
                                break;
                            }
                        }

                        if(nemicoVicino){
                            score = 2;
                            break;
                        }
                    }
                    if(l2.hasCity() && l2.getCity() != null && l2.getCity().getOwner()!=bot){
                        score = Math.max(score, 1);
                    }
                }
                if(score > bestScore){
                    bestScore = score;
                    best = l;
                }
            }
            if(best != null){return new BotDecision(BotActionType.MOVE, loc, best, null);}
        }

        //DECISIONE PASSA TURNO
        return new  BotDecision(BotActionType.PASS, null, null, null);
    }
}
