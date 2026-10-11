package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotDecisionService {

    private static final int ORO_MINIMO_RECLUTAMENTO = 3;
    private static final int UNITA_PER_SVILUPPO = 4;
    private static final int PESO_PASSA = 1;
    private final ConstructionService constructionService;
    private final Random random = new Random();

    public BotDecisionService(ConstructionService constructionService) {
        if(constructionService == null){throw new IllegalArgumentException("constructionService null");}
        this.constructionService = constructionService;
    }

    public BotDecision decide(Game game, Player bot){
        if(game == null){throw new IllegalArgumentException("game null");}
        if(bot == null){throw new IllegalArgumentException("bot null");}
        if(bot.getKind() != PlayerKind.BOT){throw new IllegalArgumentException("il player non è un bot");}

        //PRIORITA' ASSOLUTE
        BotDecision difesa = decidiDifesaCapitale(bot);
        if(difesa != null){return difesa;}

        BotDecision pacifica = decidiPacificazione(bot);
        if(pacifica != null){return pacifica;}

        //SCELTA PESATA TRA LE AZIONI POSSIBILI
        int[] pesi = pesiLeader(bot.getLeader());
        List<BotDecision> candidate = new ArrayList<>();
        List<Integer> pesiCandidate = new ArrayList<>();

        BotDecision build = decidiCostruzione(bot);
        if(build != null){
            //se non ha ancora una città sviluppata costruire è più probabile (ma non obbligatorio)
            int peso = haCittaSviluppata(bot) ? pesi[0] : pesi[0] * 2;
            aggiungi(candidate, pesiCandidate, build, peso);
        }
        aggiungi(candidate, pesiCandidate, decidiSpecializzazione(bot), pesi[1]);
        aggiungi(candidate, pesiCandidate, decidiReclutamento(bot), pesi[2]);
        aggiungi(candidate, pesiCandidate, decidiNuovoEsercito(bot), pesi[3]);
        aggiungi(candidate, pesiCandidate, decidiAttacco(game, bot), pesi[4]);
        aggiungi(candidate, pesiCandidate, decidiMovimento(bot), pesi[5]);
        aggiungi(candidate, pesiCandidate, new BotDecision(BotActionType.PASS, null, null, null), PESO_PASSA);

        return scegliPesata(candidate, pesiCandidate);
    }

    //PESI: {costruzione, specializzazione, reclutamento, nuovo esercito, attacco, movimento}
    private int[] pesiLeader(LeaderType leader){
        return switch (leader){
            case ALESSANDRO_MAGNO -> new int[]{2, 2, 3, 4, 6, 4};
            case ANNIBALE -> new int[]{2, 3, 4, 3, 5, 3};
            case ATTILA -> new int[]{1, 1, 3, 3, 7, 4};
            case GIULIO_CESARE -> new int[]{4, 3, 3, 3, 4, 3};
            case REGINA_ELISABETTA -> new int[]{4, 4, 3, 2, 2, 2};
            case GIOVANNA_D_ARCO -> new int[]{3, 3, 3, 3, 4, 5};
            case FEDERICO_BARBAROSSA -> new int[]{4, 3, 5, 3, 2, 2};
            case QIN_SHI_HUANG -> new int[]{5, 3, 4, 3, 3, 3};
        };
    }

    private void aggiungi(List<BotDecision> candidate, List<Integer> pesi, BotDecision d, int peso){
        if(d == null || peso <= 0){return;}
        candidate.add(d);
        pesi.add(peso);
    }

    private BotDecision scegliPesata(List<BotDecision> candidate, List<Integer> pesi){
        int totale = 0;
        for(int p : pesi){totale += p;}
        int tiro = random.nextInt(totale);
        for(int i = 0; i < candidate.size(); i++){
            tiro -= pesi.get(i);
            if(tiro < 0){return candidate.get(i);}
        }
        return candidate.get(candidate.size() - 1);
    }

    //COSTRUZIONE
    private BotDecision decidiCostruzione(Player bot){
        City daMigliorare = scegliCittaDaMigliorare(bot);
        if(daMigliorare == null){return null;}
        return new BotDecision(BotActionType.BUILD, null, null, daMigliorare);
    }

    private BotDecision decidiSpecializzazione(Player bot){
        if(!haCittaSviluppata(bot) || bot.getOro() < ConstructionService.COSTO_SPECIALIZZAZIONE){return null;}

        if(!haCittaMilitare(bot)){
            City c = scegliCittaPerSpecializzazione(bot, false);
            if(c != null){return new BotDecision(BotActionType.SPECIALIZE, null, null, c, CitySpecialization.MILITARE);}
        }
        if(contaUnita(bot) >= UNITA_PER_SVILUPPO){
            CitySpecialization spec = scegliSpecializzazioneSecondaria(bot);
            if(spec != null){
                City c = scegliCittaPerSpecializzazione(bot, true);
                if(c != null){return new BotDecision(BotActionType.SPECIALIZE, null, null, c, spec);}
            }
        }
        return null;
    }

    //RECLUTAMENTO
    private BotDecision decidiReclutamento(Player bot){
        if(bot.getOro() < ORO_MINIMO_RECLUTAMENTO){return null;}
        List<City> possibili = new ArrayList<>();
        for(City c : bot.getTerritorio().getCities()){
            Location loc = c.getLocation();
            if(loc != null && !c.hasRibellione() && c.getLevel() != CityLevel.ACCAMPAMENTO && loc.hasArmyPlayer(bot)){
                possibili.add(c);
            }
        }
        if(possibili.isEmpty()){return null;}
        City scelta = possibili.get(random.nextInt(possibili.size()));
        return new BotDecision(BotActionType.RECRUIT, null, null, scelta);
    }

    //NUOVO ESERCITO
    private BotDecision decidiNuovoEsercito(Player bot){
        if(bot.numeroEserciti() >= ArmyService.MAX_ESERCITI){return null;}
        if(bot.getOro() < ArmyService.costoNuovoEsercito(bot)){return null;}

        List<City> libere = new ArrayList<>();
        for(City c : bot.getTerritorio().getCities()){
            Location loc = c.getLocation();
            if(loc != null && !loc.hasArmy()){libere.add(c);}
        }
        if(libere.isEmpty()){return null;}
        //preferisce la capitale, altrimenti una città a caso
        City scelta = libere.get(random.nextInt(libere.size()));
        for(City c : libere){
            if(c == bot.getCapitale()){scelta = c;}
        }
        return new BotDecision(BotActionType.NEW_ARMY, null, null, scelta);
    }

    //ATTACCO (non nel primo turno)
    private BotDecision decidiAttacco(Game game, Player bot){
        if(bot.getTurniGiocati() == 0){return null;}

        BotDecision migliore = null;
        int miglioreScore = Integer.MIN_VALUE;

        for(Army a : bot.getEserciti()){
            if(!puoAgire(bot, a)){continue;}
            for(Location l : a.getPosizione().getAdiacenti()){
                if(l.hasArmyPlayer(bot)){continue;}
                Army nemico = null;
                for(Army x : l.getArmies()){
                    if(x != null && x.getOwner() != bot){nemico = x;}
                }
                City city = l.getCity();
                boolean cittaNemica = city != null && city.getOwner() != null && city.getOwner() != bot;
                if(nemico == null && !cittaNemica){continue;}

                int difesa = nemico == null ? 0 : nemico.getDifesaTotale();
                if(cittaNemica){difesa += 2;}
                int score = a.getAttaccoTotale() - difesa;
                //non attacca se è chiaramente più debole
                if(nemico != null && score < -3){continue;}
                if(score > miglioreScore){
                    miglioreScore = score;
                    migliore = new BotDecision(BotActionType.ATTACK, a.getPosizione(), l, null);
                }
            }
        }
        return migliore;
    }

    //MOVIMENTO (a caso tra le mosse migliori)
    private BotDecision decidiMovimento(Player bot){
        List<BotDecision> migliori = new ArrayList<>();
        int bestScore = -1;

        for(Army a : bot.getEserciti()){
            if(!puoAgire(bot, a)){continue;}
            Location loc = a.getPosizione();

            for(Location l : loc.getAdiacenti()){
                if(l.hasArmy()){continue;}
                //nel primo turno non si può entrare in una città nemica (sarebbe un attacco)
                if(bot.getTurniGiocati() == 0 && l.hasCity() && l.getCity().getOwner() != null && l.getCity().getOwner() != bot){continue;}
                int score = punteggioNodo(bot, l);
                if(score > bestScore){
                    bestScore = score;
                    migliori.clear();
                }
                if(score == bestScore){
                    migliori.add(new BotDecision(BotActionType.MOVE, loc, l, null));
                }
            }
        }
        if(migliori.isEmpty()){return null;}
        return migliori.get(random.nextInt(migliori.size()));
    }

    private int punteggioNodo(Player bot, Location l){
        int score = 0;
        //una città senza proprietario si conquista senza combattere
        if(l.hasCity() && l.getCity().getOwner() == null && l.getCity().getExProprietario() != bot){score = 3;}
        for(Location l2 : l.getAdiacenti()){
            if(l2.hasEnemyArmiesFor(bot)){score = Math.max(score, 2);}
            else if(l2.hasCity() && l2.getCity().getOwner() != bot){score = Math.max(score, 1);}
        }
        return score;
    }

    private boolean puoAgire(Player bot, Army a){
        return a != null && a.getPosizione() != null && !a.isVuoto() && a.isPuoMuovere() && !eGuardiaCapitale(bot, a);
    }

    //PRIORITA': difesa capitale
    private BotDecision decidiDifesaCapitale(Player bot) {
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return null;}
        Location locCapitale = capitale.getLocation();
        if(!capitaleMinacciata(bot,locCapitale)){return null;}

        if(locCapitale.hasArmyPlayer(bot)){
            if(bot.getOro()>= ORO_MINIMO_RECLUTAMENTO && capitale.getLevel() != CityLevel.ACCAMPAMENTO && !capitale.hasRibellione()){
                return new BotDecision(BotActionType.RECRUIT, null, null, capitale);
            }
            return null;
        }
        for(Army a : bot.getEserciti()){
            if(a == null || a.getPosizione() == null || !a.isPuoMuovere() || a.isVuoto()){continue;}
            if(a.getPosizione().isAdiacente(locCapitale) && !locCapitale.hasEnemyArmiesFor(bot)){
                return new BotDecision(BotActionType.MOVE, a.getPosizione(), locCapitale, null);
            }
        }
        return null;
    }

    //PRIORITA': porta un esercito da 4+ unità su una città in ribellione
    private BotDecision decidiPacificazione(Player bot){
        for(City c : bot.getTerritorio().getCities()){
            if(!c.hasRibellione() || c.getLocation() == null){continue;}
            Location loc = c.getLocation();
            if(loc.hasArmyPlayer(bot) || loc.hasEnemyArmiesFor(bot)){continue;}
            for(Army a : bot.getEserciti()){
                if(a.getPosizione() == null || !a.isPuoMuovere() || a.getNumeroUnita() < Ribellione.UNITA_PER_PACIFICARE){continue;}
                if(a.getPosizione().isAdiacente(loc)){
                    return new BotDecision(BotActionType.MOVE, a.getPosizione(), loc, null);
                }
            }
        }
        return null;
    }

    private boolean capitaleMinacciata(Player bot, Location loc){
        if(loc.hasEnemyArmiesFor(bot)){return true;}
        for(Location l : loc.getAdiacenti()){
            if(l.hasEnemyArmiesFor(bot)){return true;}
        }
        return false;
    }
    private boolean eGuardiaCapitale(Player bot, Army a){
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return false;}
        return a.getPosizione() == capitale.getLocation() && capitaleMinacciata(bot,capitale.getLocation());
    }

    private boolean haCittaSviluppata(Player bot){
        for(City c : bot.getTerritorio().getCities()){
            if(c.getLevel() != CityLevel.ACCAMPAMENTO){return true;}
        }
        return false;
    }
    private boolean haSpecializzazione(Player bot, CitySpecialization specialization){
        for(City c : bot.getTerritorio().getCities()){
            if(c.getSpecialization() == specialization){return true;}
        }
        return false;
    }
    private boolean haCittaMilitare(Player bot){
        return haSpecializzazione(bot,CitySpecialization.MILITARE);
    }
    private int contaUnita(Player bot){
        int totale = 0;
        for(Army a : bot.getEserciti()){
            if(a != null){
                totale += a.getNumeroUnita();
            }
        }
        return totale;
    }
    private boolean haEsercito(Player bot,City c){
        return c.getLocation() != null && c.getLocation().hasArmyPlayer(bot);
    }

    private int punteggioCitta(Player bot,City c){
        int punteggio = 0;
        if(bot.getCapitale() == c){punteggio += 2;}
        if(haEsercito(bot,c)){punteggio += 1;}
        return punteggio;
    }
    private City scegliCittaDaMigliorare(Player bot){
        City migliore = null;
        int migliorePunteggio = -1;
        for(City c : bot.getTerritorio().getCities()){
            if(c.isMetropoli()){continue;}
            if(c.isCitta() && bot.getLeader().bloccaMetropoli() && bot.getCapitale() != c){continue;}
            if(bot.getOro()<constructionService.costoMiglioramento(c)){continue;}
            int punteggio = punteggioCitta(bot,c);
            if(punteggio>migliorePunteggio){
                migliorePunteggio = punteggio;
                migliore = c;
            }
        }
        return migliore;
    }
    private City scegliCittaPerSpecializzazione(Player bot,boolean evitaCapitale){
        City migliore = null;
        int migliorePunteggio = -1;
        for(City c : bot.getTerritorio().getCities()){
            if(c.getLevel() == CityLevel.ACCAMPAMENTO || c.getSpecialization() != CitySpecialization.NESSUNA){continue;}
            int punteggio = evitaCapitale ? (bot.getCapitale() == c ? 0 : 2) + (haEsercito(bot, c) ? 1 : 0) : punteggioCitta(bot,c);
            if(punteggio>migliorePunteggio){
                migliorePunteggio = punteggio;
                migliore = c;
            }
        }
        return migliore;
    }
    private CitySpecialization scegliSpecializzazioneSecondaria(Player bot){
        LeaderType leader = bot.getLeader();
        boolean haAgricola = haSpecializzazione(bot, CitySpecialization.AGRICOLA);
        boolean haCommerciale = haSpecializzazione(bot, CitySpecialization.COMMERCIALE);
        boolean commercioConsentito = leader.getBonusCommercio() >= 0;
        boolean commercioPrioritario = leader.getBonusCommercio() > 0;

        if(commercioPrioritario && !haCommerciale){return CitySpecialization.COMMERCIALE;}
        if(!haAgricola){return CitySpecialization.AGRICOLA;}
        if(commercioConsentito && !haCommerciale){return CitySpecialization.COMMERCIALE;}
        return null;
    }
}