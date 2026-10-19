package it.unicam.cs.mpgc.rpg125715;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SerializzazionePartitaRepository implements PartitaRepository {

    private static final String ESTENSIONE = ".rpg";
    private static final String INTESTAZIONE = "CONQUEST-GAME-SAVE";
    private static final int VERSIONE = 1;

    /** Il file può contenere solo classi del gioco e classi base di Java: protegge da file manomessi. */
    private static final String FILTRO = "it.unicam.cs.mpgc.rpg125715.*;java.util.*;java.lang.*;!*";

    @Override
    public String estensione() {return ESTENSIONE;}

    @Override
    public void salva(Game game, Path file) throws IOException {
        if (game == null || file == null) {throw new IllegalArgumentException("game o file null");}
        if (game.isGameOver()) {throw new IllegalStateException("la partita è già finita");}
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeUTF(INTESTAZIONE);
            out.writeInt(VERSIONE);
            out.writeObject(game);
        }
    }

    @Override
    public Game carica(Path file) throws IOException {
        if (file == null) {throw new IllegalArgumentException("file null");}
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(FILTRO));
            if (!INTESTAZIONE.equals(in.readUTF())) {throw new IOException("non è un file di salvataggio del gioco");}
            int versione = in.readInt();
            if (versione != VERSIONE) {throw new IOException("versione del salvataggio non supportata: " + versione);}
            Object letto = in.readObject();
            if (!(letto instanceof Game game)) {throw new IOException("il file non contiene una partita");}
            return game;
        } catch (StreamCorruptedException | InvalidClassException e) {
            throw new IOException("file non valido o salvato con una versione diversa del gioco", e);
        } catch (ClassNotFoundException e) {
            throw new IOException("file di salvataggio non valido", e);
        }
    }
}