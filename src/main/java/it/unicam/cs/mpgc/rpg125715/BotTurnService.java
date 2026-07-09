package it.unicam.cs.mpgc.rpg125715;

public class BotTurnService {

    private final BotDecisionService botDecisionService;
    private final TurnService turnService;
    private final RecruitmentService recruitmentService;

    public BotTurnService(BotDecisionService botDecisionService, TurnService turnService, RecruitmentService recruitmentService) {
        if(botDecisionService == null){throw new IllegalArgumentException("botDecisionService null");}
        if(turnService == null){throw new IllegalArgumentException("turnService null");}
        if(recruitmentService == null){throw new IllegalArgumentException("recruitmentService null");}
        this.botDecisionService = botDecisionService;
        this.turnService = turnService;
        this.recruitmentService = recruitmentService;
    }

    public String eseguiTurnoBot(Game game){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(game.isGameOver()){return "la partita è finita";}

        Player currentPlayer = game.getCurrentPlayer();
        if(currentPlayer == null){throw new IllegalArgumentException("currentPlayer null");}
        if(currentPlayer.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il giocatore corrente non è un bot");}

        BotDecision decision = botDecisionService.decide(game, currentPlayer);
        String esito = eseguiDecisione(game,currentPlayer,decision);

        if(!game.isGameOver()){turnService.fineTurno(game);}
        return esito;
    }

    private String eseguiDecisione(Game game, Player bot, BotDecision decision){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(decision == null){throw new IllegalArgumentException("decision null");}

        return switch (decision.actionType()){
            case RECRUIT -> eseguiReclutamento(bot, decision.sourceCity());
            case ATTACK -> eseguiAttacco(bot, decision.sourceCity());
            case MOVE -> eseguiMovimento(bot, decision.sourceCity());
            case PASS -> bot.getName() + " passa il turno.";
        };
    }

    //RECLUTAMENTO
    private String eseguiReclutamento(Player bot, City sourceCity){
        if(sourceCity == null){return bot.getName() + " non può reclutare: città sorgente assente";}
        if(!bot.getTerritorio().getCities().contains(sourceCity)){return bot.getName() + " non può reclutare in "+ sourceCity.getName();}
        
        Army armyInCity = trovaEsercitoNellaCitta(bot, sourceCity);
        if(armyInCity == null){return bot.getName() + " non ha un esercito nella città " + sourceCity.getName();}

        UnitType tipoDaReclutare = scegliTipoDaReclutare(bot, sourceCity);
        if(tipoDaReclutare == null){return bot.getName() + " non può reclutare in " + sourceCity.getName();}
        try{
            Unit unita = recruitmentService.reclutaUnita(bot, sourceCity, armyInCity, tipoDaReclutare);
            return bot.getName() + " recluta " + unita.getType() + " in "+ sourceCity.getName();
        } catch (IllegalArgumentException e){
            return bot.getName() + " non riesce a reclutare in "+ sourceCity.getName()+": "+ e.getMessage();
        }
    }

    private Army trovaEsercitoNellaCitta(Player bot, City city){
        Location loc = city.getLocation();
        if(loc == null){return null;}

        for(Army army : bot.getEserciti()){
            if(army != null && army.getPosizione() == loc){return army;}
        }
        return null;
    }

    private UnitType scegliTipoDaReclutare(Player bot, City city){
        if(bot.getOro()<2){return null;}
        if(bot.getOro()<4){return UnitType.FANTERIA;}
        if(bot.getOro()<6){return UnitType.CAVALLERIA;}
        if(bot.getOro()<7){return UnitType.ASSEDIO;}
        if(bot.getOro()<=8){return UnitType.SPECIALI;}

        return null;
    }

    private String eseguiAttacco(Player bot, City sourceCity){
        if(sourceCity == null){return bot.getName() + " non può attaccare: città sorgente assente";}
        return bot.getName() + " prova ad attaccare partendo da " +  sourceCity.getName();
    }
    private String eseguiMovimento(Player bot, City sourceCity){
        if(sourceCity == null){return bot.getName() + " non può muoversi: città sorgente assente";}
        return bot.getName() + " prova a muoversi da " +  sourceCity.getName();
    }
}
