package it.unicam.cs.mpgc.rpg125715;

public class TurnService {

    private final RibellioneService ribellioneService;

    public TurnService(RibellioneService ribellioneService) {
        if(ribellioneService == null) {throw new IllegalArgumentException("ribellioneService null");}
        this.ribellioneService = ribellioneService;
    }


    public  void fineTurno(Game game){
        validaGame(game);
        if(game.isGameOver()){return;}
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}

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
    private void aggiornaRibellioni(Player player, Game game){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        if(game == null){throw new IllegalArgumentException("Game is null");}

        ribellioneService.aggiornaRibellioneTurno(player.getTerritorio());
        if(player.isSconfitto() && game.haSoloUnPlayerAttivo()){game.setFinita();}
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
        player.aggiungiOro(5);
    }

}
