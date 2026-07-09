package it.unicam.cs.mpgc.rpg125715;

public class BotTurnService {

    private final BotDecisionService botDecisionService;
    private final TurnService turnService;
    private final RecruitmentService recruitmentService;
    private final MovementService movementService;

    public BotTurnService(BotDecisionService botDecisionService, TurnService turnService, RecruitmentService recruitmentService, MovementService movementService) {
        if(botDecisionService == null){throw new IllegalArgumentException("botDecisionService null");}
        if(turnService == null){throw new IllegalArgumentException("turnService null");}
        if(recruitmentService == null){throw new IllegalArgumentException("recruitmentService null");}
        if(movementService == null){throw new IllegalArgumentException("movementService null");}
        this.botDecisionService = botDecisionService;
        this.turnService = turnService;
        this.recruitmentService = recruitmentService;
        this.movementService = movementService;
    }

    public String eseguiTurnoBot(Game game){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(game.isGameOver()){return "la partita è finita";}

        Player currentPlayer = game.getCurrentPlayer();
        if(currentPlayer == null){throw new IllegalArgumentException("currentPlayer null");}
        if(currentPlayer.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il giocatore corrente non è un bot");}

        String esitoTotale = "";
        int maxAzioni = 5;
        int azioniEseguite = 0;

        do{
            BotDecision decision = botDecisionService.decide(game, currentPlayer);

            if(decision.actionType() == BotActionType.PASS){
                if(esitoTotale.isBlank()){esitoTotale = currentPlayer.getName()+" passa il turno";}
                break;
            }

            String esito = eseguiDecisione(game, currentPlayer, decision);
            if(!esitoTotale.isBlank()){esitoTotale += "\n";}
            esitoTotale += esito;
            azioniEseguite++;

            if(game.isGameOver()){break;}
        }while(azioniEseguite < maxAzioni);

        if(!game.isGameOver()){turnService.fineTurno(game);}
        return esitoTotale;
    }

    private String eseguiDecisione(Game game, Player bot, BotDecision decision){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(decision == null){throw new IllegalArgumentException("decision null");}

        return switch (decision.actionType()){
            case RECRUIT -> eseguiReclutamento(bot, decision.city());
            case ATTACK -> eseguiAttacco(bot, decision.sourceLocation(), decision.targetLocation());
            case MOVE -> eseguiMovimento(bot, decision.sourceLocation(), decision.targetLocation());
            case PASS -> bot.getName() + " passa il turno.";
        };
    }

    //RECLUTAMENTO
    private String eseguiReclutamento(Player bot, City sourceCity){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(sourceCity == null){return bot.getName() + " non può reclutare: città sorgente assente";}
        if(!bot.getTerritorio().getCities().contains(sourceCity)){return bot.getName() + " non può reclutare in "+ sourceCity.getName();}

        Army armyInCity = trovaEsercitoNellaCitta(bot, sourceCity);
        if(armyInCity == null){return bot.getName() + " non ha un esercito nella città " + sourceCity.getName();}

        UnitType[] tipoDaReclutare = scegliTipoDaReclutare();
        for(UnitType unitType : tipoDaReclutare){
            try{
                Unit unita = recruitmentService.reclutaUnita(bot, sourceCity, armyInCity, unitType);
                return bot.getName() + " recluta " + unita.getType() + " in "+ sourceCity.getName();
            } catch (IllegalArgumentException e) {}
        }
        return bot.getName() + " non può reclutare in "+ sourceCity.getName();
    }

    private Army trovaEsercitoNellaCitta(Player bot, City city){
        Location loc = city.getLocation();
        if(loc == null){return null;}

        for(Army army : bot.getEserciti()){
            if(army != null && army.getPosizione() == loc){return army;}
        }
        return null;
    }

    private UnitType[] scegliTipoDaReclutare(){
        return new UnitType[]{
                UnitType.SPECIALI,UnitType.ELEFANTI,UnitType.ASSEDIO,
                UnitType.CAVALLERIA, UnitType.FANTERIA
        };
    }
    //ATTACCO
    private String eseguiAttacco(Player bot, Location start, Location end){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(start == null || end == null){return bot.getName() + " non può attaccare: partenza o destinazione assente";}
        if(!start.isAdiacente(end)){return bot.getName() + " non può attaccare: posizioni non adiacenti";}

        Army army = trovaEsercitoInPosizione(bot, start);
        if(army == null){return bot.getName() + " non può attaccare: nessun esercito trovato in posizione "+start.getId();}
        if(!army.isPuoMuovere()){return bot.getName() + " non può attaccare: l'esercito ha già mosso";}

        try{
            movementService.muovi(army, end);
            return bot.getName() + " prova ad attaccare partendo da posizione " +  start.getId() + " verso "+end.getId();
        }catch(IllegalArgumentException e){return bot.getName()+" non può attaccare: "+e.getMessage();}

    }
    private Army trovaEsercitoInPosizione(Player bot, Location start){
        if(bot == null||start == null){return null;}
        for(Army army : bot.getEserciti()){
            if(army != null && army.getPosizione() == start){return army;}
        }
        return null;
    }

    private String eseguiMovimento(Player bot, Location start, Location end){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(start == null || end == null){return bot.getName() + " non può muoversi: partenza o destinazione assente";}
        return bot.getName() + " prova a muoversi da posizione " +  start.getId();
    }
}
