package it.unicam.cs.mpgc.rpg125715;

public class BotTurnService {

    private final BotDecisionService botDecisionService;
    private final TurnService turnService;
    private final RecruitmentService recruitmentService;
    private final MovementService movementService;
    private final ConstructionService constructionService;
    private final ArmyService armyService;
    private final ConquestService conquestService;

    private static final int MAX_AZIONI = 5;
    private int azioniEseguite;
    //per animazione spostamenti
    private Location ultimaPartenza;
    private Location ultimaDestinazione;

    public BotTurnService(BotDecisionService botDecisionService, TurnService turnService, RecruitmentService recruitmentService, MovementService movementService, ConstructionService constructionService, ArmyService armyService, ConquestService conquestService) {
        if(botDecisionService == null){throw new IllegalArgumentException("botDecisionService null");}
        if(turnService == null){throw new IllegalArgumentException("turnService null");}
        if(recruitmentService == null){throw new IllegalArgumentException("recruitmentService null");}
        if(movementService == null){throw new IllegalArgumentException("movementService null");}
        if(constructionService == null){throw new IllegalArgumentException("constructionService null");}
        if(armyService == null){throw new IllegalArgumentException("armyService null");}
        if(conquestService == null){throw new IllegalArgumentException("conquestService null");}

        this.botDecisionService = botDecisionService;
        this.turnService = turnService;
        this.recruitmentService = recruitmentService;
        this.movementService = movementService;
        this.constructionService = constructionService;
        this.armyService = armyService;
        this.conquestService = conquestService;
    }

    public void iniziaTurnoBot(){
        azioniEseguite = 0;
        ultimaPartenza = null;
        ultimaDestinazione = null;
    }

    public String eseguiProssimaAzione(Game game){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(game.isGameOver()){return null;}

        Player bot = game.getCurrentPlayer();
        if(bot == null){throw new IllegalArgumentException("current player null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("current player not bot");}

        ultimaPartenza = null;
        ultimaDestinazione = null;

        if(bot.numeroCitta() == 0){
            Location l = conquestService.trovaLocationPerFondazione(bot);
            if(l != null){
                City c = conquestService.fondaCitta(bot, l , "Nuova città di: "+bot.getName());
                return bot.getLeader()+ " fonda "+ c.getName();
            }
        }
        if(azioniEseguite >= MAX_AZIONI){return null;}

        BotDecision decision = botDecisionService.decide(game, bot);
        if(decision.actionType() == BotActionType.PASS){return null;}

        azioniEseguite++;
        return eseguiDecisione(game, bot, decision);
    }

    public void terminaTurno(Game game){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(!game.isGameOver()){turnService.fineTurno(game);}
    }
    public Location getUltimaPartenza(){return ultimaPartenza;}
    public Location getUltimaDestinazione(){return ultimaDestinazione;}

    public String eseguiTurnoBot(Game game){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(game.isGameOver()){return "la partita è finita";}

        Player currentPlayer = game.getCurrentPlayer();
        String esitoTotale = "";

        iniziaTurnoBot();
        String esito = eseguiProssimaAzione(game);
        while(esito != null){
            if(!esitoTotale.isBlank()){esitoTotale += "\n";}
            esitoTotale+=esito;
            if(game.isGameOver()){break;}
            esito = eseguiProssimaAzione(game);
        }
        if(esitoTotale.isBlank()){esitoTotale = currentPlayer.getName()+ " passa il turno";}

        terminaTurno(game);
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
            case NEW_ARMY -> eseguiNuovoEsercito(bot, decision.city());
            case SPECIALIZE -> eseguiSpecializzazione(bot, decision.city(), decision.specialization());
        };
    }

    //NUOVO ESERCITO
    private String eseguiNuovoEsercito(Player bot, City city){
        if(city == null){return bot.getName() + " non può creare un esercito: città assente";}
        try{
            armyService.creaNuovoEsercito(bot, city);
            return bot.getName()+ " crea un nuovo esercito a "+ city.getName();
        }catch (IllegalArgumentException | IllegalStateException e){
            return bot.getName()+ " non può creare un esercito: "+ e.getMessage();
        }
    }

    //RECLUTAMENTO
    private String eseguiReclutamento(Player bot, City sourceCity){
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(sourceCity == null){return bot.getName() + " non può reclutare: città sorgente assente";}
        if(!bot.getTerritorio().getCities().contains(sourceCity)){return bot.getName() + " non può reclutare in "+ sourceCity.getName();}

        Army armyInCity = trovaEsercitoNellaCitta(bot, sourceCity);
        if(armyInCity == null){return bot.getName() + " non ha un esercito nella città " + sourceCity.getName();}

        UnitType[] tipoDaReclutare = botDecisionService.ordineReclutamento(bot);
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
            ultimaPartenza = start;
            ultimaDestinazione = end;
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
            ultimaPartenza = start;
            ultimaDestinazione = end;
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
