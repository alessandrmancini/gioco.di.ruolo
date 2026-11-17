package it.unicam.cs.mpgc.rpg125715;

/**
 * Crea e collega tra loro tutti i servizi del gioco (unico punto in cui si usano i "new" dei servizi).
 * Se ne crea uno nuovo per ogni partita, così i contatori degli id ripartono da capo.
 */
public class ServiziPartita {

    private final IdGenerator idGenerator;
    private final ArmyService armyService;
    private final ConquestService conquestService;
    private final MovementService movementService;
    private final RecruitmentService recruitmentService;
    private final ConstructionService constructionService;
    private final RibellioneService ribellioneService;
    private final TurnService turnService;
    private final BotTurnService botTurnService;
    private final GameInitializationService gameInitializationService;

    public ServiziPartita() {
        this.idGenerator = new IdGenerator();
        DiceService diceService = new DiceService();
        BattleService battleService = new BattleService(diceService);
        this.armyService = new ArmyService(idGenerator);
        this.conquestService = new ConquestService();
        this.movementService = new MovementService(battleService, armyService, conquestService);
        this.recruitmentService = new RecruitmentService();
        this.constructionService = new ConstructionService();
        this.ribellioneService = new RibellioneService(conquestService);
        this.turnService = new TurnService(ribellioneService);
        BotDecisionService botDecisionService = new BotDecisionService(constructionService);
        this.botTurnService = new BotTurnService(botDecisionService, turnService, recruitmentService, movementService, constructionService, armyService, conquestService);
        GameSetUpService gameSetUpService = new GameSetUpService(idGenerator);
        MapSetupService mapSetupService = new MapSetupService(gameSetUpService);
        this.gameInitializationService = new GameInitializationService(gameSetUpService, mapSetupService, armyService);
    }

    /** Servizi per una partita caricata da file: gli id nuovi partono dopo quelli già usati nella partita. */
    public static ServiziPartita perPartitaCaricata(Game game) {
        if(game == null){throw new IllegalArgumentException("game null");}
        ServiziPartita servizi = new ServiziPartita();
        servizi.idGenerator.allineaA(game);
        return servizi;
    }

    public ArmyService armyService() {return armyService;}
    public ConquestService conquestService() {return conquestService;}
    public MovementService movementService() {return movementService;}
    public RecruitmentService recruitmentService() {return recruitmentService;}
    public ConstructionService constructionService() {return constructionService;}
    public RibellioneService ribellioneService() {return ribellioneService;}
    public TurnService turnService() {return turnService;}
    public BotTurnService botTurnService() {return botTurnService;}
    public GameInitializationService gameInitializationService() {return gameInitializationService;}
}
