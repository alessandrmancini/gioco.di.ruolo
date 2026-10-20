package it.unicam.cs.mpgc.rpg125715;

public class StrategiaAlessandro extends StrategiaBase {

    public StrategiaAlessandro(NemiciService nemiciService) {super(nemiciService);}

    @Override
    public int[] pesiAzioni() {return new int[]{2, 2, 3, 4, 6, 4};}

    //tenaglia: attacca solo se ci sono almeno due suoi eserciti vicini al bersaglio
    @Override
    public boolean attaccoConsentito(Player bot, Army esercito, Location destinazione, Army nemico) {
        return bot.numeroEserciti() < 2 || esercitiAdiacentiA(bot, destinazione) >= 2;
    }

    //metropoli di frontiera
    @Override
    public boolean sviluppoDiFrontiera() {return true;}

    //città in fila: preferisce i nodi con una città da conquistare
    @Override
    public int preferenzaMovimento(Player bot, Army esercito, Location destinazione) {
        return destinazione.hasCity() && destinazione.getCity().getOwner() != bot ? 3 : 0;
    }
}