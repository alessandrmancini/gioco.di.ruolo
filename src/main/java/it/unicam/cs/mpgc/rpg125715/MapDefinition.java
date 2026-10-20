package it.unicam.cs.mpgc.rpg125715;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Descrizione della mappa letta da un file di testo: nodi con coordinate,
 * collegamenti tra nodi e nome della città che sta in ogni nodo.
 */
public final class MapDefinition {

    private final Map<Integer, int[]> coordinate;
    private final List<int[]> adiacenze;
    private final Map<String, Integer> nodoPerCitta;

    private MapDefinition(Map<Integer, int[]> coordinate, List<int[]> adiacenze, Map<String, Integer> nodoPerCitta) {
        this.coordinate = coordinate;
        this.adiacenze = adiacenze;
        this.nodoPerCitta = nodoPerCitta;
    }

    /** Legge la mappa da una risorsa del progetto (nella stessa cartella di questa classe). */
    public static MapDefinition daRisorsa(String nomeFile) {
        if(nomeFile == null || nomeFile.isBlank()){throw new IllegalArgumentException("nomeFile non valido");}
        try(InputStream in = MapDefinition.class.getResourceAsStream(nomeFile)){
            if(in == null){throw new IllegalArgumentException("risorsa non trovata: " + nomeFile);}
            return leggi(in);
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public static MapDefinition leggi(InputStream in) throws IOException {
        Map<Integer, int[]> coordinate = new TreeMap<>();
        List<int[]> adiacenze = new ArrayList<>();
        Map<String, Integer> nodoPerCitta = new LinkedHashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String riga;
        int numero = 0;
        while((riga = reader.readLine()) != null){
            numero++;
            riga = riga.strip();
            if(riga.isEmpty() || riga.startsWith("#")){continue;}
            String[] p = riga.split("\\s+", 5);
            try{
                switch (p[0]){
                    case "N" -> {
                        int id = Integer.parseInt(p[1]);
                        if(coordinate.containsKey(id)){throw new IllegalArgumentException("nodo duplicato " + id);}
                        coordinate.put(id, new int[]{Integer.parseInt(p[2]), Integer.parseInt(p[3])});
                        if(p.length > 4){nodoPerCitta.put(p[4], id);}
                    }
                    case "E" -> adiacenze.add(new int[]{Integer.parseInt(p[1]), Integer.parseInt(p[2])});
                    default -> throw new IllegalArgumentException("tipo di riga sconosciuto: " + p[0]);
                }
            }catch(RuntimeException e){
                throw new IllegalArgumentException("mappa non valida alla riga " + numero + ": " + e.getMessage(), e);
            }
        }
        return new MapDefinition(coordinate, adiacenze, nodoPerCitta);
    }

    /** id del nodo -> {x, y}, in ordine di id. */
    public Map<Integer, int[]> getCoordinate() {return new TreeMap<>(coordinate);}

    public List<int[]> getAdiacenze() {return new ArrayList<>(adiacenze);}

    /** Id del nodo in cui si trova la città con questo nome. */
    public int getNodoDellaCitta(String nomeCitta) {
        Integer id = nodoPerCitta.get(nomeCitta);
        if(id == null){throw new IllegalArgumentException("nome non riconosciuto: " + nomeCitta);}
        return id;
    }

    public Map<String, Integer> getNodiDelleCitta() {return Collections.unmodifiableMap(nodoPerCitta);}
}