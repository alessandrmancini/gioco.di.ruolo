package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BotDecisionService {

    private static final int ORO_MINIMO_RECLUTAMENTO = 3;
    private static final int UNITA_PER_SVILUPPO = 4;
    private static final int PESO_PASSA = 1;
    private final ConstructionService constructionService;
    private final NemiciService nemiciService = new NemiciService();
    private final StrategiaLeader strategia = new StrategiaLeader(nemiciService);
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

        BotDecision difesaCitta = decidiDifesaCitta(bot);
        if(difesaCitta != null){return difesaCitta;}

        BotDecision pacifica = decidiPacificazione(bot);
        if(pacifica != null){return pacifica;}

        //PRIORITA' DI LEADER: contrattacco (Federico), guardia della capitale (Cesare), specializzazioni da fare subito
        BotDecision contrattacco = decidiContrattacco(bot);
        if(contrattacco != null){return contrattacco;}

        BotDecision ritorno = decidiRitornoGuardia(bot);
        if(ritorno != null){return ritorno;}

        BotDecision strategico = decidiSviluppoPrioritario(bot);
        if(strategico != null){return strategico;}

        //SCELTA PESATA TRA LE AZIONI POSSIBILI
        int[] pesi = pesiLeader(bot.getLeader());
        List<BotDecision> candidate = new ArrayList<>();
        List<Integer> pesiCandidate = new ArrayList<>();

        //Giulio Cesare alterna turni di sviluppo e turni di guerra (gli altri leader fanno entrambi)
        boolean sviluppo = strategia.ammetteSviluppo(bot);
        boolean guerra = strategia.ammetteGuerra(bot);

        if(sviluppo){
            BotDecision build = decidiCostruzione(bot);
            if(build != null){
                //se non ha ancora una città sviluppata costruire è più probabile (ma non obbligatorio)
                int peso = haCittaSviluppata(bot) ? pesi[0] : pesi[0] * 2;
                aggiungi(candidate, pesiCandidate, build, peso);
            }
            aggiungi(candidate, pesiCandidate, decidiSpecializzazione(bot), pesi[1]);
        }
        aggiungi(candidate, pesiCandidate, decidiReclutamento(bot), pesi[2]);
        if(sviluppo){
            aggiungi(candidate, pesiCandidate, decidiNuovoEsercito(bot), pesi[3]);
        }

        if(guerra){
            BotDecision caccia = decidiCaccia(bot);
            if(caccia != null){
                aggiungi(candidate, pesiCandidate, caccia, pesi[4]+pesi[5]+bonusCaccia(bot.getLeader()));
            }
            else{
                aggiungi(candidate, pesiCandidate, decidiAttacco(game, bot), pesi[4]);
                aggiungi(candidate, pesiCandidate, decidiMovimento(bot), pesi[5]);
            }
        }
        aggiungi(candidate, pesiCandidate, new BotDecision(BotActionType.PASS, null, null, null), PESO_PASSA);

        return scegliPesata(candidate, pesiCandidate);
    }

    //peso in più dell'azione "caccia ai nemici" rispetto a un normale attacco/movimento
    private int bonusCaccia(LeaderType leader){
        return switch (leader){
            case ALESSANDRO_MAGNO, ANNIBALE, GIULIO_CESARE, REGINA_ELISABETTA, GIOVANNA_D_ARCO, FEDERICO_BARBAROSSA,
                 QIN_SHI_HUANG, ATTILA -> 6;
        };
    }

    //quanto rischia il leader: attacca un esercito solo se (attacco - difesa) non è più basso di questo valore
    private int margineMinimo(LeaderType leader){
        return switch (leader){
            case ALESSANDRO_MAGNO, GIOVANNA_D_ARCO, REGINA_ELISABETTA, FEDERICO_BARBAROSSA, QIN_SHI_HUANG,
                 GIULIO_CESARE, ANNIBALE, ATTILA -> -3;
        };
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
        if(bot.getLeader() == LeaderType.FEDERICO_BARBAROSSA){
            List<City> diConfine = new ArrayList<>();
            for(City c : libere){
                if(strategia.eCittaDiConfine(bot,c.getLocation())){diConfine.add(c);}
            }
            if(diConfine.isEmpty()){return new BotDecision(BotActionType.NEW_ARMY, null, null, diConfine.get(random.nextInt(diConfine.size())));}
        }
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
                if(nemico != null && score < margineMinimo(bot.getLeader())){continue;}

                if(!strategia.attaccoConsentito(bot, a , l, nemico)){continue;}
                score = strategia.punteggioAttacco(bot,a,l,nemico,score);
                if(score > miglioreScore){
                    miglioreScore = score;
                    migliore = new BotDecision(BotActionType.ATTACK, a.getPosizione(), l, null);
                }
            }
        }
        return migliore;
    }

    private BotDecision decidiCaccia(Player bot){
        if(!nemiciService.haNemiciDaSconfiggere(bot)){return null;}
        java.util.Map<Location,Integer> distanza = nemiciService.distanzaDaiNemici(bot);

        List<BotDecision> migliori = new ArrayList<>();
        int migliorDistanza = Integer.MAX_VALUE;
        int miglioreScore = Integer.MIN_VALUE;

        for(Army a : bot.getEserciti()){
            if(!puoAgire(bot, a)){continue;}
            Location loc = a.getPosizione();
            Integer distanzaAttuale = distanza.get(loc);
            if(distanzaAttuale == null){continue;}

            for(Location l : loc.getAdiacenti()){
                Integer d = distanza.get(l);
                if(d == null || d>=distanzaAttuale){continue;}
                if(l.hasArmyPlayer(bot)){continue;}
                if(!strategia.mossaConsentita(bot,a,l,distanza)){continue;}

                boolean daConquistare = l.hasCity() && l.getCity().getOwner() != null && l.getCity().getOwner() != bot;
                Army nemico = null;
                for(Army x : l.getArmies()){
                    if(x!= null && x.getOwner() != bot){nemico = x;}
                }
                boolean eAttacco = nemico != null || daConquistare;

                if(eAttacco && bot.getTurniGiocati() == 0){continue;}
                if(nemico != null && a.getAttaccoTotale() - nemico.getDifesaTotale() < margineMinimo(bot.getLeader())){continue;}
                if(eAttacco && !strategia.attaccoConsentito(bot, a, l, nemico)){continue;}

                //a pari distanza si preferisce l'attacco; poi decide la strategia del leader
                int score = eAttacco
                        ? 1000 + strategia.punteggioAttacco(bot, a, l, nemico, strategia.margineAttacco(bot, a, l, nemico))
                        : strategia.preferenzaMovimento(bot, a, l);
                if(d<migliorDistanza || (d == migliorDistanza && score > miglioreScore)){
                    migliorDistanza = d;
                    miglioreScore = score;
                    migliori.clear();
                }
                if(d == migliorDistanza && score == miglioreScore){
                    BotActionType tipo = eAttacco ? BotActionType.ATTACK : BotActionType.MOVE;
                    migliori.add(new BotDecision(tipo, loc, l, null));
                }
            }
        }
        if(migliori.isEmpty()){return null;}
        return migliori.get(random.nextInt(migliori.size()));
    }

    //FEDERICO (contrattacco): subito dopo aver respinto un attacco, attacca l'esercito che l'ha attaccato
    private BotDecision decidiContrattacco(Player bot){
        if(bot.getLeader() != LeaderType.FEDERICO_BARBAROSSA){return null;}
        Army bersaglio = bot.getAttaccanteSconfitto();
        if(bersaglio == null){return null;}
        bot.setAttaccanteSconfitto(null);      //si prova una sola volta
        if(bot.getTurniGiocati() == 0){return null;}
        if(bersaglio.isVuoto() || !bersaglio.getOwner().getEserciti().contains(bersaglio)){return null;}

        Location destinazione = bersaglio.getPosizione();
        if(destinazione == null || destinazione.hasArmyPlayer(bot)){return null;}
        for(Army a : bot.getEserciti()){
            if(a.isVuoto() || !a.isPuoMuovere() || a.getPosizione() == null){continue;}
            if(!a.getPosizione().isAdiacente(destinazione)){continue;}
            if(a.getAttaccoTotale() - bersaglio.getDifesaTotale() < margineMinimo(bot.getLeader())){continue;}
            return new BotDecision(BotActionType.ATTACK, a.getPosizione(), destinazione, null);
        }
        return null;
    }

    //GIULIO CESARE (capitale coperta): se l'esercito di guardia è lontano dalla capitale lo riporta indietro
    private BotDecision decidiRitornoGuardia(Player bot){
        if(bot.getLeader() != LeaderType.GIULIO_CESARE || bot.numeroEserciti() < 2){return null;}
        City capitale = bot.getCapitale();
        if(capitale == null || capitale.getLocation() == null){return null;}

        Army guardia = strategia.guardiaCapitale(bot);
        if(guardia == null || !guardia.isPuoMuovere() || guardia.isVuoto()){return null;}
        java.util.Map<Location,Integer> distanza = nemiciService.distanzeDa(List.of(capitale.getLocation()));
        Integer attuale = distanza.get(guardia.getPosizione());
        if(attuale == null || attuale <= 1){return null;}

        List<Location> passi = new ArrayList<>();
        for(Location l : guardia.getPosizione().getAdiacenti()){
            Integer d = distanza.get(l);
            if(d != null && d < attuale && !l.hasArmyPlayer(bot) && !l.hasEnemyArmiesFor(bot)){passi.add(l);}
        }
        if(passi.isEmpty()){return null;}
        return new BotDecision(BotActionType.MOVE, guardia.getPosizione(), passi.get(random.nextInt(passi.size())), null);
    }

    //SVILUPPO PRIORITARIO: le specializzazioni che il leader vuole fare appena può
    private BotDecision decidiSviluppoPrioritario(Player bot){
        CitySpecialization voluta = null;
        for(CitySpecialization s : strategia.specializzazioniPrioritarie(bot.getLeader())){
            if(!haSpecializzazione(bot, s)){
                voluta = s;
                break;
            }
        }
        if(voluta == null){return null;}

        City capitale = bot.getCapitale();
        //prima serve almeno una città sviluppata: si porta la capitale ad avamposto
        if(!haCittaSviluppata(bot)){
            if(capitale != null && bot.getOro() >= constructionService.costoMiglioramento(capitale)){
                return new BotDecision(BotActionType.BUILD, null, null, capitale);
            }
            return null;
        }
        if(bot.getOro() < ConstructionService.COSTO_SPECIALIZZAZIONE){return null;}

        City scelta = null;
        if(strategia.specializzaInCapitale(bot.getLeader()) && capitale != null
                && !capitale.isAccampamento() && capitale.getSpecialization() == CitySpecialization.NESSUNA){
            scelta = capitale;
        }
        else{
            scelta = scegliCittaPerSpecializzazione(bot, false);
        }
        if(scelta == null){return null;}
        return new BotDecision(BotActionType.SPECIALIZE, null, null, scelta, voluta);
    }

    //RECLUTAMENTO: ordine con cui provare i tipi di unità (il primo che si può pagare)
    public UnitType[] ordineReclutamento(Player bot){
        UnitType[] standard = {UnitType.SPECIALI, UnitType.ELEFANTI, UnitType.ASSEDIO, UnitType.CAVALLERIA, UnitType.FANTERIA};
        if(strategia.preferisceAssedio(bot)){
            //Attila: contro città di livello alto prima l'assedio
            return new UnitType[]{UnitType.ASSEDIO, UnitType.SPECIALI, UnitType.CAVALLERIA, UnitType.FANTERIA};
        }
        return standard;
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
                int score = punteggioNodo(bot, l)*100+strategia.preferenzaMovimento(bot,a,l);
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
        return a != null && a.getPosizione() != null && !a.isVuoto() && a.isPuoMuovere()
                && !eGuardiaCapitale(bot, a) && !strategia.eGuardia(bot, a);
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

    private boolean haNemicoVicino(Player bot, Location loc){
        for(Location l : loc.getAdiacenti()){
            if(l.hasEnemyArmiesFor(bot)){return true;}
        }
        return false;
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
        int migliorePunteggio = Integer.MIN_VALUE;

        boolean frontiera = bot.getLeader() == LeaderType.ALESSANDRO_MAGNO && nemiciService.haNemiciDaSconfiggere(bot);
        java.util.Map<Location, Integer> distanzaNemici = frontiera ? nemiciService.distanzaDaiNemici(bot) : null;
        for(City c : bot.getTerritorio().getCities()){
            if(c.isMetropoli()){continue;}
            if(c.isCitta() && bot.getLeader().bloccaMetropoli() && bot.getCapitale() != c){continue;}
            if(bot.getOro()<constructionService.costoMiglioramento(c)){continue;}
            int punteggio = punteggioCitta(bot,c);
            if(frontiera){
                Integer d = c.getLocation() == null ? null : distanzaNemici.get(c.getLocation());
                punteggio = (bot.getCapitale() == c ? -100 : 0) - (d == null ? 500 : d);
            }
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