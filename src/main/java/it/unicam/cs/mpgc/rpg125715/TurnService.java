package it.unicam.cs.mpgc.rpg125715;

public class TurnService {

    public static final int MAX_ROUND = 30;
    private static final int ORO_BASE = 5;
    private final RibellioneService ribellioneService;

    public TurnService(RibellioneService ribellioneService) {
        if(ribellioneService == null) {throw new IllegalArgumentException("ribellioneService null");}
        this.ribellioneService = ribellioneService;
    }


    public  void fineTurno(Game game){
        validaGame(game);
        if(game.isGameOver()){return;}
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}

        game.getCurrentPlayer().incrementaTurniGiocati();
        game.incrementaNumeroTurno();

        prossimoPlayer(game);
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}

        Player corrente = game.getCurrentPlayer();
        sbloccaEsercitiPlayer(corrente);
        aggiornaRibellioni(corrente, game);
        assegnaOroTurno(corrente, game);

        if(game.haSoloUnPlayerAttivo()){game.setFinita();}
    }

    private void validaGame(Game game){
        if(game == null){throw new IllegalArgumentException("Game is null");}
        if(!game.isIniziato()){throw new IllegalArgumentException("Game never started");}
    }
    private void prossimoPlayer(Game game){
        game.nextPlayer();
    }
    private void sbloccaEsercitiPlayer(Player player){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        for(Army a: player.getEserciti()){
            a.sbloccaMovimento();
        }
    }

    //RIBELLIONI
    private void aggiornaRibellioni(Player player, Game game){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        if(game == null){throw new IllegalArgumentException("Game is null");}

        boolean giaInRibellione = player.getTerritorio().hasRibellione();
        ribellioneService.aggiornaRibellioneTurno(player.getTerritorio());
        if(!giaInRibellione && !player.isSconfitto() && player.getTurniGiocati()>0){
            ribellioneService.verificaNuovaRibellione(player.getTerritorio(), subisceEffettoLeaderNemico(player, game));
        }
        if(player.isSconfitto() && game.haSoloUnPlayerAttivo()){game.setFinita();}
    }
    private boolean subisceEffettoLeaderNemico(Player player, Game game){
        for(Player altro : game.getPlayers()){
            if(altro == player || altro.isSconfitto() || !altro.getLeader().aumentaProbabilitaInsurrezioniNemiche()){continue;}
            for(Army army: altro.getEserciti()){
                for(City city : player.getTerritorio().getCities()){
                    Location l = city.getLocation();
                    if(l != null && army.getPosizione() != null && l.isAdiacente(army.getPosizione())){return true;}
                }
            }
        }
        return false;
    }

    private void assegnaOroTurno(Player player, Game game){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        if(game == null){throw new IllegalArgumentException("Game is null");}

        if(game.getNumeroTurno() <= game.getPlayers().size()){return;}

        Territory territory = player.getTerritorio();
        if(!ribellioneService.territorioProduceOro(territory)){
            if(ribellioneService.applicaMalusOro(territory) && player.getOro()>0){
                player.spendiOro(1);
            }
            return;
        }
        player.aggiungiOro(ORO_BASE+bonusOroCities(player));
    }

    private int bonusOroCities(Player player){
        int bonus = 0;
        for(City city : player.getTerritorio().getCities()){
            bonus += bonusOroCittaConLeader(city, player.getLeader());
        }
        return bonus;
    }

    public static int bonusOroCitta(City city){
        return switch (city.getSpecialization()){
            case COMMERCIALE -> switch (city.getLevel()){
                case ACCAMPAMENTO -> 1;
                case AVAMPOSTO, CITTA -> 2;
                case METROPOLI -> 3;
            };
            case AGRICOLA -> switch (city.getLevel()){
                case ACCAMPAMENTO, AVAMPOSTO -> 1;
                case CITTA, METROPOLI -> 2;
            };
            default -> 0;
        };
    }
    public static int bonusOroCittaConLeader(City city, LeaderType leader){
        int bonus = bonusOroCitta(city);
        if(city.getSpecialization() == CitySpecialization.COMMERCIALE){
            bonus = Math.max(0, bonus + leader.getBonusCommercio());
        }
        return bonus;
    }

}
