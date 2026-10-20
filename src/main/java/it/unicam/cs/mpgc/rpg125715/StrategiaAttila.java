package it.unicam.cs.mpgc.rpg125715;

public class StrategiaAttila extends StrategiaBase {

    public StrategiaAttila(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{1, 1, 3, 3, 7, 4};}

    //il più debole: sceglie la città con meno bonus di difesa
    @Override
    public int punteggioAttacco(Player bot, Army esercito, Location destinazione, Army nemico, int base) {
        return base - 10 * BattleService.calcolaBonusDifesaTerritorio(destinazione.getCity());
    }

    // preferisce i nodi accanto a un altro suo esercito
    @Override
    public int preferenzaMovimento(Player bot, Army esercito, Location destinazione) {
        return alleatiAccanto(bot, esercito, destinazione);
    }

    //assedio: se punta a città di livello alto recluta prima le macchine d'assedio
    @Override
    public boolean preferisceAssedio(Player bot) {
        for(Player nemico : bot.getNemiciDaSconfiggere()){
            for(City c : nemico.getTerritorio().getCities()){
                if(c.isSviluppata()){return true;}
            }
        }
        return false;
    }
}