package it.unicam.cs.mpgc.rpg125715;

public class BotTurnService {

    private final BotDecisionService botDecisionService;
    private final TurnService turnService;

    public BotTurnService(BotDecisionService botDecisionService, TurnService turnService) {
        if(botDecisionService == null){throw new IllegalArgumentException("botDecisionService null");}
        if(turnService == null){throw new IllegalArgumentException("turnService null");}
        this.botDecisionService = botDecisionService;
        this.turnService = turnService;
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

    private String eseguiReclutamento(Player bot, City sourceCity){
        if(sourceCity == null){return bot.getName() + " non può reclutare: città sorgente assente";}
        if(!bot.getTerritorio().getCities().contains(sourceCity)){return bot.getName() + " non può reclutare in "+ sourceCity.getName();}
        if(bot.getOro()<1){return bot.getName() + " non ha oro sufficiente per reclutare";}
        return bot.getName() + " prova a reclutare in " +  sourceCity.getName();
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
