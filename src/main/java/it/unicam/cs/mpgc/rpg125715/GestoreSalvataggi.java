package it.unicam.cs.mpgc.rpg125715;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * VISTA della persistenza: chiede all'utente il file (finestra di scelta) e usa il PartitaRepository
 * per salvare o caricare. Il formato del file dipende solo dal repository ricevuto.
 */
public class GestoreSalvataggi {

    /** Partita letta da file, con il nome del file da mostrare nel registro. */
    public record PartitaCaricata(Game game, String nomeFile) {}

    private final PartitaRepository repository;

    public GestoreSalvataggi(PartitaRepository repository) {
        if(repository == null){throw new IllegalArgumentException("repository null");}
        this.repository = repository;
    }

    /** Chiede dove salvare e salva; restituisce il percorso usato, oppure null se l'utente annulla. */
    public Path salva(Game game, Window finestra) throws IOException {
        FileChooser scelta = creaFileChooser("Salva partita");
        scelta.setInitialFileName("partita" + repository.estensione());
        File file = scelta.showSaveDialog(finestra);
        if(file == null){return null;}

        Path percorso = file.toPath();
        if(!percorso.getFileName().toString().endsWith(repository.estensione())){
            percorso = percorso.resolveSibling(percorso.getFileName() + repository.estensione());
        }
        repository.salva(game, percorso);
        return percorso;
    }

    /** Chiede quale file aprire e lo carica; restituisce null se l'utente annulla. */
    public PartitaCaricata carica(Window finestra) throws IOException {
        File file = creaFileChooser("Carica partita").showOpenDialog(finestra);
        if(file == null){return null;}
        return new PartitaCaricata(repository.carica(file.toPath()), file.getName());
    }

    private FileChooser creaFileChooser(String titolo) {
        FileChooser scelta = new FileChooser();
        scelta.setTitle(titolo);
        scelta.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Partite salvate (*" + repository.estensione() + ")", "*" + repository.estensione()));
        return scelta;
    }
}
