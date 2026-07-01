package it.unicam.cs.mpgc.rpg125715;

public class TurnService {

    public  void fineTurno(Game game){
        validaGame(game);
        prossimoPlayer(game);
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}
        Player corrente = game.getCurrentPlayer();
        sbloccaEsercitiPlayer(game,corrente);
        assegnaOroTurno(corrente);
        aggiornaRibellioni(corrente, game);
    }

    private void validaGame(Game game){
        if(game == null){throw new IllegalArgumentException("Game is null");}
        if(!game.isIniziato()){throw new IllegalArgumentException("Game never started");}
        if(game.isGameOver()){return;}
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}
    }
    private void prossimoPlayer(Game game){
        game.nextPlayer();
    }
    private void sbloccaEsercitiPlayer(Game game,Player player){
        for(Army a: player.getEserciti()){
            a.sbloccaMovimento();
        }
    }
    private void assegnaOroTurno(Player player){
        player.aggiungiOro(5);
    }
    private void aggiornaRibellioni(Player player, Game game){

    }
}
