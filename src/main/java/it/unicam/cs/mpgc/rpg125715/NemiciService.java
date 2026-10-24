package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Assegna a ogni bot, a inizio partita, due nemici da sconfiggere:
 * uno tra i giocatori vicini (a caso) e il giocatore più vicino tra quelli rimasti.
 */
public class NemiciService {

    public static final int NUMERO_NEMICI = 2;
    //sono "vicini" i giocatori a distanza minima + questo margine
    private static final int MARGINE_VICINATO = 1;
    private static final int MAX_INSEGUITORI = 2;
    private final Random random;

    public NemiciService(){
        this(new Random());
    }

    public NemiciService(Random random){
        if(random == null){throw new IllegalArgumentException("random null");}
        this.random = random;
    }


    //ASSEGNAZIONE
    //ASSEGNAZIONE
    public void assegnaNemici(List<Player> players){
        if(players == null){throw new IllegalArgumentException("players null");}
        //quante volte ogni giocatore è già stato scelto come nemico: serve a non far ricadere tutti i bot sugli stessi giocatori
        Map<Player, Integer> inseguitori = new LinkedHashMap<>();
        //l'ordine è casuale così nessun leader sceglie sempre per primo
        List<Player> ordine = new ArrayList<>(players);
        Collections.shuffle(ordine, random);

        for(Player bot : ordine){
            if(bot == null || bot.getKind() != PlayerKind.BOT){continue;}

            Map<Player, Integer> distanze = distanzeGiocatori(bot, players);
            Player vicino = scegliVicino(distanze, inseguitori);
            if(vicino != null){
                bot.addNemico(vicino);
                inseguitori.merge(vicino, 1, Integer::sum);
            }

            Player secondo = scegliPiuVicinoRimasto(distanze, vicino, inseguitori);
            if(secondo != null){
                bot.addNemico(secondo);
                inseguitori.merge(secondo, 1, Integer::sum);
            }
        }
    }

    //distanza tra il bot e ogni altro giocatore (minimo tra due città dei due giocatori)
    private Map<Player, Integer> distanzeGiocatori(Player bot, List<Player> players){
        Map<Location, Integer> distanza = distanzeDa(posizioniCitta(bot));
        Map<Player, Integer> distanzaGiocatori = new LinkedHashMap<>();
        for(Player p : players){
            if(p == bot){continue;}
            int d = Integer.MAX_VALUE;
            for(Location l : posizioniCitta(p)){
                Integer dl = distanza.get(l);
                if(dl != null){d = Math.min(d, dl);}
            }
            if(d != Integer.MAX_VALUE){distanzaGiocatori.put(p, d);}
        }
        return distanzaGiocatori;
    }

    //primo nemico: uno a caso tra i giocatori vicini (distanza minima + margine)
    private Player scegliVicino(Map<Player, Integer> distanze, Map<Player, Integer> inseguitori){
        int minima = Integer.MAX_VALUE;
        for(int d : distanze.values()){minima = Math.min(minima, d);}

        List<Player> vicini = new ArrayList<>();
        for(Map.Entry<Player, Integer> e : distanze.entrySet()){
            if(e.getValue() <= minima + MARGINE_VICINATO && inseguitori.getOrDefault(e.getKey(),0)<MAX_INSEGUITORI){
                vicini.add(e.getKey());
            }
        }
        if(vicini.isEmpty()){return scegliPiuVicino(distanze, null ,inseguitori);}
        return scegliTraMenoScelti(vicini, inseguitori);
    }

    //secondo nemico: il giocatore più vicino tra quelli rimasti (a pari distanza a caso)
    private Player scegliPiuVicinoRimasto(Map<Player, Integer> distanza, Player giaScelto, Map<Player, Integer> inseguitori){
        return scegliPiuVicino(distanza, giaScelto, inseguitori);
    }
    //se tutti sono già al massimo si ignora il limite
    private Player scegliPiuVicino(Map<Player, Integer> distanze, Player escluso, Map<Player, Integer> inseguitori){
        for(boolean conLimite : new boolean[]{true, false}){
            int minima = Integer.MAX_VALUE;
            for(Map.Entry<Player, Integer> e : distanze.entrySet()){
                if(e.getKey() == escluso){continue;}
                if(conLimite && inseguitori.getOrDefault(e.getKey(), 0) >= MAX_INSEGUITORI){continue;}
                minima = Math.min(minima, e.getValue());
            }
            List<Player> candidati = new ArrayList<>();
            for(Map.Entry<Player, Integer> e : distanze.entrySet()){
                if(e.getKey() == escluso || e.getValue() != minima){continue;}
                if(conLimite && inseguitori.getOrDefault(e.getKey(), 0) >= MAX_INSEGUITORI){continue;}
                candidati.add(e.getKey());
            }
            if(!candidati.isEmpty()){return scegliTraMenoScelti(candidati, inseguitori);}
        }
        return null;
    }

    private Player scegliTraMenoScelti(List<Player> candidati, Map<Player, Integer> inseguitori){
        if(candidati.isEmpty()){return null;}
        int minimo = Integer.MAX_VALUE;
        for(Player p : candidati){minimo = Math.min(minimo, inseguitori.getOrDefault(p, 0));}
        List<Player> scelti = new ArrayList<>();
        for(Player p : candidati){
            if(inseguitori.getOrDefault(p, 0) == minimo){scelti.add(p);}
        }
        return scelti.get(random.nextInt(scelti.size()));
    }


    //NEMICI ANCORA DA SCONFIGGERE
    public boolean haNemiciDaSconfiggere(Player bot){
        return bot != null && !bot.getNemiciDaSconfiggere().isEmpty();
    }

    //distanza di ogni nodo dalla città o dall'esercito più vicino di un nemico ancora in gioco
    public Map<Location, Integer> distanzaDaiNemici(Player bot){
        List<Location> obiettivi = new ArrayList<>();
        for(Player nemico : bot.getNemiciDaSconfiggere()){
            obiettivi.addAll(posizioniCitta(nemico));
            for(Army a : nemico.getEserciti()){
                if(a.getPosizione() != null){obiettivi.add(a.getPosizione());}
            }
        }
        return distanzeDa(obiettivi);
    }

    //MAPPA
    private List<Location> posizioniCitta(Player p){
        List<Location> posizioni = new ArrayList<>();
        for(City c : p.getTerritorio().getCities()){
            if(c.getLocation() != null){posizioni.add(c.getLocation());}
        }
        return posizioni;
    }

    //ricerca in ampiezza: numero minimo di collegamenti da uno qualsiasi dei nodi di partenza
    public Map<Location, Integer> distanzeDa(Collection<Location> partenze){
        Map<Location, Integer> distanza = new LinkedHashMap<>();
        Deque<Location> coda = new ArrayDeque<>();
        for(Location l : partenze){
            if(l != null && !distanza.containsKey(l)){
                distanza.put(l, 0);
                coda.add(l);
            }
        }
        while(!coda.isEmpty()){
            Location corrente = coda.poll();
            for(Location vicino : corrente.getAdiacenti()){
                if(!distanza.containsKey(vicino)){
                    distanza.put(vicino, distanza.get(corrente) + 1);
                    coda.add(vicino);
                }
            }
        }
        return distanza;
    }
}