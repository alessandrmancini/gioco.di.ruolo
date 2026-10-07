package it.unicam.cs.mpgc.rpg125715;

public class BotDecisionService {

    private static final int ORO_MINIMO_RECLUTAMENTO = 3;
    private static final int UNITA_PER_SVILUPPO = 4;
    private final ConstructionService constructionService;

    public BotDecisionService(ConstructionService constructionService) {
        if(constructionService == null){throw new IllegalArgumentException("constructionService null");}
        this.constructionService = constructionService;
    }
    public BotDecision decide(Game game, Player bot){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il player non è un bot");}

        Territory territory = bot.getTerritorio();

        //DIFESA CAPITALE
        BotDecision difesa = decidiDifesaCapitale(bot);
        if(difesa != null){return difesa;}

        //COSTRUZIONE
            //città sviluppata per reclutamento
        if(!haCittaSviluppata(bot)){
            City daMigliorare = scegliCittaDaMigliorare(bot);
            if(daMigliorare != null){return new BotDecision(BotActionType.BUILD, null,null,daMigliorare);}
        }
            //città militare
        if(haCittaSviluppata(bot) && !haCittaMilitare(bot) && bot.getOro() >= ConstructionService.COSTO_SPECIALIZZAZIONE){
            City daSpecializzare = scegliCittaPerSpecializzazione(bot,false);
            if(daSpecializzare != null){return new BotDecision(BotActionType.SPECIALIZE, null,null,daSpecializzare,CitySpecialization.MILITARE);}
        }
            //agricola-commerciale
        if(contaUnita(bot) >= UNITA_PER_SVILUPPO && bot.getOro() >= ConstructionService.COSTO_SPECIALIZZAZIONE){
            CitySpecialization spec = scegliSpecializzazioneSecondaria(bot);
            if(spec != null){
                City daSpecializzare = scegliCittaPerSpecializzazione(bot,true);
                if(daSpecializzare != null){
                    return new BotDecision(BotActionType.SPECIALIZE, null, null, daSpecializzare,spec);
                }
            }
        }

        //miglioramento esercito
        if(contaUnita(bot) >= UNITA_PER_SVILUPPO){
            City daMigliorare = scegliCittaDaMigliorare(bot);
            if(daMigliorare != null){return new BotDecision(BotActionType.BUILD, null, null, daMigliorare);}
        }

        //DECISIONE RECLUTAMENTO
        if(bot.getOro() >= ORO_MINIMO_RECLUTAMENTO && !territory.hasRibellione()){
            for(City c : territory.getCities()){
                Location locationCity = c.getLocation();
                if(locationCity != null && c.getLevel() != CityLevel.ACCAMPAMENTO && locationCity.hasArmyPlayer(bot)){
                    return new BotDecision(BotActionType.RECRUIT, null, null, c);
                }
            }
        }


        //DECISIONE ATTACCO
        for(Player p : game.getPlayers()){
            if(p == null || p == bot || p.isSconfitto()){continue;}

            Territory territorioNemico = p.getTerritorio();
            if(territorioNemico == null){continue;}

            for(City c : territorioNemico.getCities()){
                if(c == null || c.getLocation() == null){continue;}
                for(Army a : bot.getEserciti()){
                    if(a == null || a.getPosizione() == null || a.isVuoto() || !a.isPuoMuovere() || eGuardiaCapitale(bot,a)){continue;}
                    if(a.getPosizione().isAdiacente(c.getLocation()) && !c.getLocation().hasArmyPlayer(bot)){
                        return new BotDecision(BotActionType.ATTACK, a.getPosizione(), c.getLocation(), null);
                    }
                    for(Army b : p.getEserciti()){
                        if(b == null || b.getPosizione() == null){continue;}
                        if(a.getPosizione().isAdiacente(b.getPosizione()) && !b.getPosizione().hasArmyPlayer(bot)){
                            return new BotDecision(BotActionType.ATTACK, a.getPosizione(), b.getPosizione(), null);
                        }
                    }
                }
            }

        }

        //DECISIONE MOVIMENTO
        for(Army a : bot.getEserciti()){
            if(a == null || a.getPosizione() == null || a.isVuoto() || !a.isPuoMuovere() || eGuardiaCapitale(bot,a)){continue;}

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

    private BotDecision decidiDifesaCapitale(Player bot) {
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return null;}
        Location locCapitale = capitale.getLocation();
        if(!capitaleMinacciata(bot,locCapitale)){return null;}

        if(locCapitale.hasArmyPlayer(bot)){
            if(bot.getOro()>= ORO_MINIMO_RECLUTAMENTO && capitale.getLevel() != CityLevel.ACCAMPAMENTO && !bot.getTerritorio().hasRibellione()){
                return new BotDecision(BotActionType.RECRUIT, null, null, capitale);
            }
            return null;
        }
        for(Army a : bot.getEserciti()){
            if(a == null || a.getPosizione() == null || !a.isPuoMuovere() || a.isVuoto()){continue;}
            if(a.getPosizione().isAdiacente(locCapitale)){
                return new BotDecision(BotActionType.MOVE, a.getPosizione(), locCapitale, null);
            }
        }
        return null;
    }
    private boolean capitaleMinacciata(Player bot, Location loc){
        if(loc.hasEnemyArmiesFor(bot)){return true;}
        for(Location l : loc.getAdiacenti()){
            if(l.hasEnemyArmiesFor(bot)){return true;}
        }
        return false;
    }
    private boolean eGuardiaCapitale(Player bot, Army a){
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return false;}
        return a.getPosizione() == capitale.getLocation() && capitaleMinacciata(bot,capitale.getLocation());
    }

    private boolean haCittaSviluppata(Player bot){
        for(City c : bot.getTerritorio().getCities()){
            if(c.getLevel() != CityLevel.ACCAMPAMENTO){return true;}
        }
        return false;
    }
    private boolean haSpecializzazione(Player bot, CitySpecialization specialization){
        for(City c : bot.getTerritorio().getCities()){
            if(c.getSpecialization() == specialization){return true;}
        }
        return false;
    }
    private boolean haCittaMilitare(Player bot){
        return haSpecializzazione(bot,CitySpecialization.MILITARE);
    }
    private int contaUnita(Player bot){
        int totale = 0;
        for(Army a : bot.getEserciti()){
            if(a != null){
                totale += a.getNumeroUnita();
            }
        }
        return totale;
    }
    private boolean haEsercito(Player bot,City c){
        return c.getLocation() != null && c.getLocation().hasArmyPlayer(bot);
    }

    private int punteggioCitta(Player bot,City c){
        int punteggio = 0;
        if(bot.getCapitale() == c){punteggio += 2;}
        if(haEsercito(bot,c)){punteggio += 1;}
        return punteggio;
    }
    private City scegliCittaDaMigliorare(Player bot){
        City migliore = null;
        int migliorePunteggio = -1;
        for(City c : bot.getTerritorio().getCities()){
            if(c.isMetropoli()){continue;}
            if(c.isCitta() && bot.getLeader().bloccaMetropoli() && bot.getCapitale() != c){continue;}
            if(bot.getOro()<constructionService.costoMiglioramento(c)){continue;}
            int punteggio = punteggioCitta(bot,c);
            if(punteggio>migliorePunteggio){
                migliorePunteggio = punteggio;
                migliore = c;
            }
        }
        return migliore;
    }
    private City scegliCittaPerSpecializzazione(Player bot,boolean evitaCapitale){
        City migliore = null;
        int migliorePunteggio = -1;
        for(City c : bot.getTerritorio().getCities()){
            if(c.getLevel() == CityLevel.ACCAMPAMENTO || c.getSpecialization() != CitySpecialization.NESSUNA){continue;}
            int punteggio = evitaCapitale ? (bot.getCapitale() == c ? 0 : 2) + (haEsercito(bot, c) ? 1 : 0) : punteggioCitta(bot,c);
            if(punteggio>migliorePunteggio){
                migliorePunteggio = punteggio;
                migliore = c;
            }
        }
        return migliore;
    }
    private CitySpecialization scegliSpecializzazioneSecondaria(Player bot){
        LeaderType leader = bot.getLeader();
        boolean haAgricola = haSpecializzazione(bot, CitySpecialization.AGRICOLA);
        boolean haCommerciale = haSpecializzazione(bot, CitySpecialization.COMMERCIALE);
        boolean commercioConsentito = leader.getBonusCommercio() >= 0;
        boolean commercioPrioritario = leader.getBonusCommercio() > 0;

        if(commercioPrioritario && !haCommerciale){return CitySpecialization.COMMERCIALE;}
        if(!haAgricola){return CitySpecialization.AGRICOLA;}
        if(commercioConsentito && !haCommerciale){return CitySpecialization.COMMERCIALE;}
        return null;
    }
}
