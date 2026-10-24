package it.unicam.cs.mpgc.rpg125715;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Punto di accesso alle strategie dei leader: sceglie la LeaderStrategy giusta per ogni bot.
 * Per aggiungere un leader: nuova classe che estende StrategiaBase e una riga in registra(LeaderType, LeaderStrategy).
 */
public class StrategiaLeader {

    private final Map<LeaderType, LeaderStrategy> strategie = new EnumMap<>(LeaderType.class);
    private final StrategiaBase neutra;

    public StrategiaLeader(NemiciService nemiciService) {
        if(nemiciService == null){throw new IllegalArgumentException("nemiciService null");}
        this.neutra = new StrategiaBase(nemiciService) {
            @Override
            public int[] pesiAzioni() {return new int[]{3, 3, 3, 3, 3, 3};}
        };
        registra(LeaderType.ALESSANDRO_MAGNO, new StrategiaAlessandro(nemiciService));
        registra(LeaderType.ANNIBALE, new StrategiaAnnibale(nemiciService));
        registra(LeaderType.ATTILA, new StrategiaAttila(nemiciService));
        registra(LeaderType.GIULIO_CESARE, new StrategiaCesare(nemiciService));
        registra(LeaderType.REGINA_ELISABETTA, new StrategiaElisabetta(nemiciService));
        registra(LeaderType.GIOVANNA_D_ARCO, new StrategiaGiovanna(nemiciService));
        registra(LeaderType.FEDERICO_BARBAROSSA, new StrategiaFederico(nemiciService));
        registra(LeaderType.QIN_SHI_HUANG, new StrategiaQin(nemiciService));
    }

    public void registra(LeaderType leader, LeaderStrategy strategia) {
        if(leader == null || strategia == null){throw new IllegalArgumentException("leader o strategia null");}
        strategie.put(leader, strategia);
    }

    /** Strategia del leader (se non ne ha una registrata, una strategia neutra). */
    public LeaderStrategy per(LeaderType leader) {
        return strategie.getOrDefault(leader, neutra);
    }

    public LeaderStrategy per(Player bot) {return per(bot.getLeader());}

    /** Attacco (con bonus del leader) meno difesa del bersaglio: positivo = vantaggio. */
    public int margineAttacco(Player bot, Army esercito, Location destinazione, Army nemico) {
        return StrategiaBase.margineAttacco(bot, esercito, destinazione, nemico);
    }

    /** Vero se il leader accetta di attaccare la destinazione (senza difensori si prende sempre). */
    public boolean attaccoConsentito(Player bot, Army esercito, Location destinazione, Army nemico) {
        if(nemico == null){return true;}
        return per(bot).attaccoConsentito(bot, esercito, destinazione, nemico);
    }

    /** Una città è "di confine" se accanto c'è un esercito nemico. */
    public boolean eCittaDiConfine(Player bot, Location l) {
        return StrategiaBase.eCittaDiConfine(bot, l);
    }
}