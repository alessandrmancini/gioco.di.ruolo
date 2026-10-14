package it.unicam.cs.mpgc.rpg125715;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
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
    private final Random random;

    public NemiciService(){
        this(new Random());
    }

    public NemiciService(Random random){
        if(random == null){throw new IllegalArgumentException("random null");}
        this.random = random;
    }

    //ASSEGNAZIONE
    public void assegnaNemici(List<Player> players){
        if(players == null){throw new IllegalArgumentException("players null");}
        for(Player bot : players){
            if(bot == null || bot.getKind() != PlayerKind.BOT){continue;}

            Map<Player, Integer> distanze = distanzeGiocatori(bot, players);
            Player vicino = scegliVicino(distanze);
            if(vicino != null){bot.addNemico(vicino);}

            Player secondo = scegliPiuVicinoRimasto(distanze, vicino);
            if(secondo != null){bot.addNemico(secondo);}
        }
    }

    //distanza tra il bot e ogni altro giocatore (minimo tra due città dei due giocatori)
    private Map<Player, Integer> distanzeGiocatori(Player bot, List<Player> players){
        Map<Location, Integer> distanza = distanzeDa(posizioniCitta(bot));
        Map<Player, Integer> distanzaGiocatori = new HashMap<>();
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
    private Player scegliVicino(Map<Player, Integer> distanze){
        int minima = Integer.MAX_VALUE;
        for(int d : distanze.values()){minima = Math.min(minima, d);}

        List<Player> vicini = new ArrayList<>();
        for(Map.Entry<Player, Integer> e : distanze.entrySet()){
            if(e.getValue() <= minima + MARGINE_VICINATO){vicini.add(e.getKey());}
        }
        if(vicini.isEmpty()){return null;}
        return vicini.get(random.nextInt(vicini.size()));
    }

    //secondo nemico: il giocatore più vicino tra quelli rimasti (a pari distanza a caso)
    private Player scegliPiuVicinoRimasto(Map<Player, Integer> distanze, Player giaScelto){
        int minima = Integer.MAX_VALUE;
        for(Map.Entry<Player, Integer> e : distanze.entrySet()){
            if(e.getKey() != giaScelto){minima = Math.min(minima, e.getValue());}
        }
        List<Player> candidati = new ArrayList<>();
        for(Map.Entry<Player, Integer> e : distanze.entrySet()){
            if(e.getKey() != giaScelto && e.getValue() == minima){candidati.add(e.getKey());}
        }
        if(candidati.isEmpty()){return null;}
        return candidati.get(random.nextInt(candidati.size()));
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
        Map<Location, Integer> distanza = new HashMap<>();
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