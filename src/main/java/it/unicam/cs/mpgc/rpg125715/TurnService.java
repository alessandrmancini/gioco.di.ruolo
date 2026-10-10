package it.unicam.cs.mpgc.rpg125715;

public class TurnService {

    public static final int MAX_ROUND = 10;
    private static final int ORO_BASE = 5;
    private static final int BONUS_COMMERCIALE = 2;
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
        player.aggiungiOro(ORO_BASE+bonusCittaCommerciale(player));
    }

    private int bonusCittaCommerciale(Player player){
        int bonus = 0;
        for(City city : player.getTerritorio().getCities()){
            if(city.getSpecialization() == CitySpecialization.COMMERCIALE){
                bonus += Math.max(0, BONUS_COMMERCIALE+player.getLeader().getBonusCommercio());
            }
        }
        return bonus;
    }

}
