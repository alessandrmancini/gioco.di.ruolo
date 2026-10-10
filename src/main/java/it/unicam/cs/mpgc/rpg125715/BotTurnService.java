package it.unicam.cs.mpgc.rpg125715;

public class BotTurnService {

    private final BotDecisionService botDecisionService;
    private final TurnService turnService;
    private final RecruitmentService recruitmentService;
    private final MovementService movementService;
    private final ConstructionService constructionService;

    public BotTurnService(BotDecisionService botDecisionService, TurnService turnService, RecruitmentService recruitmentService, MovementService movementService, ConstructionService constructionService) {
        if(botDecisionService == null){throw new IllegalArgumentException("botDecisionService null");}
        if(turnService == null){throw new IllegalArgumentException("turnService null");}
        if(recruitmentService == null){throw new IllegalArgumentException("recruitmentService null");}
        if(movementService == null){throw new IllegalArgumentException("movementService null");}
        if(constructionService == null){throw new IllegalArgumentException("constructionService null");}
        this.botDecisionService = botDecisionService;
        this.turnService = turnService;
        this.recruitmentService = recruitmentService;
        this.movementService = movementService;
        this.constructionService = constructionService;
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
            case BUILD -> eseguiMiglioramento(bot, decision.city());
            case SPECIALIZE -> eseguiSpecializzazione(bot, decision.city(), decision.specialization());
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
        if(army == null){return bot.getName() + " non può attaccare: nessun esercito trovato a "+nomeLuogo(start);}
        if(!army.isPuoMuovere()){return bot.getName() + " non può attaccare: l'esercito ha già mosso";}

        try{
            BattleResult r = movementService.muovi(army,end);
            String testo = bot.getName()+ " attacca "+ nomeLuogo(end)+ " partendo da "+ nomeLuogo(start);
            if(r!= null){testo += r.haVintoAttaccante() ? " e vince la battaglia" : " e perde la battaglia";}
            return testo;
        }catch(IllegalArgumentException | IllegalStateException e){return bot.getName()+" non può attaccare: "+e.getMessage();}

    }
    private Army trovaEsercitoInPosizione(Player bot, Location start){
        if(bot == null||start == null){return null;}
        for(Army army : bot.getEserciti()){
            if(army != null && army.getPosizione() == start){return army;}
        }
        return null;
    }

    //MOVIMENTO
    private String eseguiMovimento(Player bot, Location start, Location end){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(start == null || end == null){return bot.getName() + " non può muoversi: partenza o destinazione assente";}

        Army army = trovaEsercitoInPosizione(bot, start);
        if(army == null){return bot.getName()+ " non può muoversi: nessun esercito a "+nomeLuogo(start);}
        if(!army.isPuoMuovere()){return bot.getName()+ " non può muoversi, ha già mosso";}

        try{
            movementService.muovi(army, end);
            return bot.getName() + " muove l'esercito da " +  nomeLuogo(start)+ " a "+ nomeLuogo(end);
        }catch(IllegalArgumentException | IllegalStateException e){
            return bot.getName() + " non può muoversi: " + e.getMessage();
        }
    }

    //CITTA
    private String eseguiMiglioramento(Player bot, City city){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(city == null){throw new IllegalArgumentException("city null");}
        try{
            int costo = constructionService.costoMiglioramento(city);
            constructionService.miglioraCitta(bot, city);
            return bot.getName() + " migliora " + city.getName();
        }catch (IllegalArgumentException | IllegalStateException e){
            return bot.getName()+ " non può migliorare "+ city.getName()+ ": "+e.getMessage();
        }
    }
    private String eseguiSpecializzazione(Player bot, City city, CitySpecialization specialization){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(city == null || specialization == null){throw new IllegalArgumentException("città o specializzazione assente");}
        try{
            constructionService.impostaSpecializzazione(bot,city,specialization);
            return bot.getName()+ " specializza "+ city.getName()+ " in "+ specialization;
        }catch (IllegalArgumentException | IllegalStateException e){
            return bot.getName() + " non può specializzare "+ city.getName()+ ": "+e.getMessage();
        }
    }
    private String nomeLuogo(Location l){
        if(l != null && l.hasCity()){return l.getCity().getName();}
        return "un territorio senza città";
    }
}
