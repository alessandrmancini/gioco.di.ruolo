package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Strategie dei bot specifiche di ogni leader: attacco, spostamento e sviluppo.
 * Non sceglie le azioni: risponde alle domande del BotDecisionService
 * ("posso attaccare qui?", "quale mossa preferisci?", "cosa devo specializzare?").
 */
public class StrategiaLeader {

    //Regina Elisabetta: attacca solo se il suo attacco supera la difesa di almeno questo valore
    private static final int MARGINE_COLPO_CALCOLATO = 5;
    //Qin Shi Huang: attacca solo se il suo attacco è almeno il 130% della difesa
    private static final int PERCENTUALE_SUPERIORITA = 200;

    //Giulio Cesare: tiene un esercito di guardia alla capitale solo se ha almeno questo numero di eserciti
    private static final int ESERCITI_MINIMI_GUARDIA = 3;
    //Federico Barbarossa: 1 = una città è "di confine" solo se accanto c'è un esercito nemico; 0 = basta anche una città di un altro giocatore
    private static final int CINTURA_SOLO_CON_ESERCITI_NEMICI = 1;

    private final NemiciService nemiciService;

    public StrategiaLeader(NemiciService nemiciService) {
        if(nemiciService == null){throw new IllegalArgumentException("nemiciService null");}
        this.nemiciService = nemiciService;
    }

    // ================= ATTACCO =================

    /** Vero se il leader accetta di attaccare la destinazione con quell'esercito (nemico = esercito che la difende, può essere null). */
    public boolean attaccoConsentito(Player bot, Army esercito, Location destinazione, Army nemico){
        if(nemico == null){return true;}      //città o nodo senza difensori: si prende senza combattere
        return switch (bot.getLeader()){
            //A3 tenaglia: attacca solo se ci sono almeno due suoi eserciti vicini al bersaglio
            case ALESSANDRO_MAGNO -> bot.numeroEserciti() < 2 || esercitiAdiacentiA(bot, destinazione) >= 2;
            //A2 cuneo d'urto: attacca solo l'esercito più forte (con più elefanti)
            case ANNIBALE -> esercito == esercitoDUrto(bot);
            //A3 colpo calcolato: solo con superiorità netta
            case REGINA_ELISABETTA -> margineAttacco(bot, esercito, destinazione, nemico) >= MARGINE_COLPO_CALCOLATO;
            //A1 superiorità: solo con attacco almeno del 30% più alto della difesa
            case QIN_SHI_HUANG -> attaccoTotale(bot, esercito) * 100 >= difesaBersaglio(destinazione, nemico) * PERCENTUALE_SUPERIORITA;
            default -> true;
        };
    }

    /** Attacco (con bonus del leader) meno difesa del bersaglio: positivo = vantaggio. */
    public int margineAttacco(Player bot, Army esercito, Location destinazione, Army nemico){
        return attaccoTotale(bot, esercito) - difesaBersaglio(destinazione, nemico);
    }

    /** Preferenza tra più attacchi possibili (più alto = meglio); base = margineAttacco. */
    public int punteggioAttacco(Player bot, Army esercito, Location destinazione, Army nemico, int base){
        return switch (bot.getLeader()){
            //A2 il più debole: sceglie la città con meno bonus di difesa
            case ATTILA -> base - 10 * BattleService.calcolaBonusDifesaTerritorio(destinazione.getCity());
            //A2 dado fortunato: preferisce le battaglie equilibrate (i suoi dadi fanno la differenza)
            case GIULIO_CESARE -> nemico == null ? 100 : -Math.abs(base);
            //A3 liberatrice: preferisce le città che erano di un altro giocatore e sono state conquistate
            case GIOVANNA_D_ARCO -> base + (cittaConquistata(bot, destinazione) ? 5 : 0);
            default -> base;
        };
    }

    private int attaccoTotale(Player bot, Army esercito){
        return esercito.getAttaccoTotale() + bot.getLeader().getBonusAttacco();
    }

    private int difesaBersaglio(Location destinazione, Army nemico){
        if(nemico == null){return 0;}
        return nemico.getDifesaTotale() + BattleService.calcolaBonusDifesaTerritorio(destinazione.getCity());
    }

    //eserciti del bot che possono muovere e sono vicini alla destinazione
    private int esercitiAdiacentiA(Player bot, Location destinazione){
        int totale = 0;
        for(Army a : bot.getEserciti()){
            if(!a.isVuoto() && a.isPuoMuovere() && a.getPosizione() != null && a.getPosizione().isAdiacente(destinazione)){totale++;}
        }
        return totale;
    }

    //più elefanti, poi più unità, poi più attacco
    public Army esercitoDUrto(Player bot){
        Army migliore = null;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto()){continue;}
            if(migliore == null || confrontaForza(a, migliore, true) > 0){migliore = a;}
        }
        return migliore;
    }

    public Army esercitoPiuForte(Player bot){
        Army migliore = null;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto()){continue;}
            if(migliore == null || confrontaForza(a, migliore, false) > 0){migliore = a;}
        }
        return migliore;
    }

    private int confrontaForza(Army a, Army b, boolean contaElefanti){
        if(contaElefanti){
            int c = Integer.compare(a.getNumeroUnitaPerTipo(UnitType.ELEFANTI), b.getNumeroUnitaPerTipo(UnitType.ELEFANTI));
            if(c != 0){return c;}
        }
        int c = Integer.compare(a.getNumeroUnita(), b.getNumeroUnita());
        if(c != 0){return c;}
        return Integer.compare(a.getAttaccoTotale(), b.getAttaccoTotale());
    }

    //città ora di un giocatore ma che all'inizio era di un altro
    private boolean cittaConquistata(Player bot, Location l){
        if(!l.hasCity()){return false;}
        City c = l.getCity();
        return c.getOwner() != null && c.getOwner() != bot
                && c.getProprietarioIniziale() != null && c.getProprietarioIniziale() != c.getOwner();
    }

    // ================= SPOSTAMENTO =================

    /** Esercito che il leader tiene fermo (a presidio): non va a muoversi né ad attaccare. */
    public boolean eGuardia(Player bot, Army esercito){
        return switch (bot.getLeader()){
            //S3 capitale sempre coperta: l'esercito più vicino alla capitale resta a un passo da essa
            case GIULIO_CESARE -> bot.numeroEserciti() >= ESERCITI_MINIMI_GUARDIA && esercito == guardiaCapitale(bot) && distanzaDallaCapitale(bot, esercito) <= 1;
            //S2 cintura: un esercito resta in ogni città di confine (ma almeno uno resta libero)
            case FEDERICO_BARBAROSSA -> guardiaDiConfine(bot, esercito);
            default -> false;
        };
    }

    //Cesare: l'esercito più vicino alla capitale (a pari distanza il più debole)
    public Army guardiaCapitale(Player bot){
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return null;}
        Map<Location, Integer> distanza = nemiciService.distanzeDa(List.of(capitale.getLocation()));
        Army migliore = null;
        int migliorDistanza = Integer.MAX_VALUE;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto() || a.getPosizione() == null){continue;}
            Integer d = distanza.get(a.getPosizione());
            if(d == null){continue;}
            if(d < migliorDistanza || (d == migliorDistanza && a.getNumeroUnita() < migliore.getNumeroUnita())){
                migliorDistanza = d;
                migliore = a;
            }
        }
        return migliore;
    }

    public int distanzaDallaCapitale(Player bot, Army esercito){
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null || esercito.getPosizione() == null){return Integer.MAX_VALUE;}
        Integer d = nemiciService.distanzeDa(List.of(capitale.getLocation())).get(esercito.getPosizione());
        return d == null ? Integer.MAX_VALUE : d;
    }

    //Federico: una città è di confine se accanto c'è una città di un altro giocatore o un esercito nemico
    public boolean eCittaDiConfine(Player bot, Location l){
        for(Location vicina : l.getAdiacenti()){
            if(CINTURA_SOLO_CON_ESERCITI_NEMICI == 0
                    && vicina.hasCity() && vicina.getCity().getOwner() != null && vicina.getCity().getOwner() != bot){return true;}
            if(vicina.hasEnemyArmiesFor(bot)){return true;}
        }
        return false;
    }

    private boolean guardiaDiConfine(Player bot, Army esercito){
        if(!staSuCittaDiConfine(bot, esercito)){return false;}
        List<Army> guardie = new ArrayList<>();
        int eserciti = 0;
        for(Army a : bot.getEserciti()){
            if(a.isVuoto()){continue;}
            eserciti++;
            if(staSuCittaDiConfine(bot, a)){guardie.add(a);}
        }
        //se tutti gli eserciti sarebbero di guardia, il più forte resta libero
        if(guardie.size() >= eserciti){
            Army libero = null;
            for(Army a : guardie){
                if(libero == null || confrontaForza(a, libero, false) > 0){libero = a;}
            }
            return esercito != libero;
        }
        return true;
    }

    private boolean staSuCittaDiConfine(Player bot, Army a){
        Location l = a.getPosizione();
        return l != null && l.hasCity() && l.getCity().getOwner() == bot && eCittaDiConfine(bot, l);
    }

    /** Preferenza tra più spostamenti equivalenti (più alto = meglio). */
    public int preferenzaMovimento(Player bot, Army esercito, Location destinazione){
        return switch (bot.getLeader()){
            //S2 città in fila: preferisce i nodi con una città da conquistare
            case ALESSANDRO_MAGNO -> destinazione.hasCity() && destinazione.getCity().getOwner() != bot ? 3 : 0;
            //S2 valichi: preferisce i nodi con pochi collegamenti
            case ANNIBALE -> -destinazione.getAdiacenti().size();
            //S2 orda unica: preferisce i nodi accanto a un altro suo esercito
            case ATTILA -> alleatiAccanto(bot, esercito, destinazione);
            //S3 passo a passo: tra i passi equivalenti sceglie quello meno esposto ai nemici
            case QIN_SHI_HUANG -> -nemiciAccanto(bot, destinazione);
            default -> 0;
        };
    }

    /** Giovanna S3 stendardo: l'esercito più forte guida, gli altri non lo superano verso il bersaglio. */
    public boolean mossaConsentita(Player bot, Army esercito, Location destinazione, Map<Location, Integer> distanza){
        if(bot.getLeader() != LeaderType.GIOVANNA_D_ARCO){return true;}
        Army guida = esercitoPiuForte(bot);
        if(guida == null || guida == esercito || guida.getPosizione() == null){return true;}
        Integer distanzaGuida = distanza.get(guida.getPosizione());
        Integer distanzaDestinazione = distanza.get(destinazione);
        if(distanzaGuida == null || distanzaDestinazione == null){return true;}
        return distanzaDestinazione >= distanzaGuida;
    }

    private int alleatiAccanto(Player bot, Army esercito, Location destinazione){
        int totale = 0;
        for(Army a : bot.getEserciti()){
            if(a != esercito && !a.isVuoto() && a.getPosizione() != null && a.getPosizione().isAdiacente(destinazione)){totale++;}
        }
        return totale;
    }

    private int nemiciAccanto(Player bot, Location destinazione){
        int totale = 0;
        for(Location l : destinazione.getAdiacenti()){
            if(l.hasEnemyArmiesFor(bot)){totale++;}
        }
        return totale;
    }

    // ================= SVILUPPO =================

    /** Specializzazioni da fare appena possibile, nell'ordine (vuoto = nessuna priorità). */
    public List<CitySpecialization> specializzazioniPrioritarie(LeaderType leader){
        return switch (leader){
            case ANNIBALE, FEDERICO_BARBAROSSA -> List.of(CitySpecialization.MILITARE);
            case REGINA_ELISABETTA, QIN_SHI_HUANG -> List.of(CitySpecialization.COMMERCIALE, CitySpecialization.AGRICOLA);
            case GIULIO_CESARE -> List.of(CitySpecialization.COMMERCIALE);
            case GIOVANNA_D_ARCO -> List.of(CitySpecialization.AGRICOLA);
            default -> List.of();
        };
    }

    /** Federico: la prima specializzazione va fatta nella capitale. */
    public boolean specializzaInCapitale(LeaderType leader){
        return leader == LeaderType.FEDERICO_BARBAROSSA;
    }

    /** Giulio Cesare alterna: turno di sviluppo (pari) e turno di guerra (dispari). Gli altri leader sempre entrambi. */
    public boolean ammetteSviluppo(Player bot){
        return bot.getLeader() != LeaderType.GIULIO_CESARE || bot.getTurniGiocati() % 2 == 0;
    }

    public boolean ammetteGuerra(Player bot){
        return bot.getLeader() != LeaderType.GIULIO_CESARE || bot.getTurniGiocati() % 2 == 1;
    }

    // Attila: se punta a città di livello alto recluta prima le macchine d'assedio.
    public boolean preferisceAssedio(Player bot){
        if(bot.getLeader() != LeaderType.ATTILA){return false;}
        for(Player nemico : bot.getNemiciDaSconfiggere()){
            for(City c : nemico.getTerritorio().getCities()){
                if(c.isSviluppata()){return true;}
            }
        }
        return false;
    }
}