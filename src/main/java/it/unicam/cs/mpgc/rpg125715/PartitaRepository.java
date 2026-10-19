package it.unicam.cs.mpgc.rpg125715;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Persistenza delle partite. Il resto del gioco dipende solo da questa interfaccia:
 * per salvare in un altro formato (JSON, XML, database...) basta una nuova implementazione.
 */
public interface PartitaRepository {

    /** Salva la partita nel file indicato. */
    void salva(Game game, Path file) throws IOException;

    /** Legge una partita salvata in precedenza. */
    Game carica(Path file) throws IOException;

    /** Estensione dei file prodotti, con il punto (per esempio ".rpg"). */
    String estensione();
}