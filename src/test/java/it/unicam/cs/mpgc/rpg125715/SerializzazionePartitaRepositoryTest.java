package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Test del salvataggio e del caricamento delle partite su file ".rpg". */
public class SerializzazionePartitaRepositoryTest {

    private final PartitaRepository repository = new SerializzazionePartitaRepository();

    @Test
    void estensione_restituisceRpg() {
        assertEquals(".rpg", repository.estensione());
    }

    @Test
    void salvaECarica_partitaInCorso_ritornaLoStessoStato() throws IOException {
        Game originale = creaGame();
        originale.iniziaPartita();
        originale.incrementaNumeroTurno();
        Path file = fileTemporaneo();

        repository.salva(originale, file);
        Game caricato = repository.carica(file);

        assertNotSame(originale, caricato);
        assertTrue(caricato.isIniziato());
        assertEquals(originale.getNumeroTurno(), caricato.getNumeroTurno());
        assertEquals(2, caricato.getPlayers().size());

        Player alessandro = caricato.getPlayers().get(0);
        assertEquals("Giocatore 1", alessandro.getName());
        assertEquals(LeaderType.ALESSANDRO_MAGNO, alessandro.getLeader());
        assertEquals(15, alessandro.getOro());
        assertEquals("Pella", alessandro.getCapitale().getName());
        assertEquals(1, alessandro.numeroEserciti());
        assertEquals(2, alessandro.getEserciti().get(0).getNumeroUnita());
    }

    @Test
    void salvaECarica_riferimentiTraOggetti_restanoCollegati() throws IOException {
        Path file = fileTemporaneo();

        repository.salva(creaGame(), file);
        Game caricato = repository.carica(file);

        Player alessandro = caricato.getPlayers().get(0);
        City pella = alessandro.getCapitale();
        Army army = alessandro.getEserciti().get(0);
        Location nodo = caricato.getLocationById(1);
        // dopo il caricamento gli oggetti puntano ancora l'uno all'altro (non sono copie separate)
        assertSame(alessandro, pella.getOwner());
        assertSame(nodo, pella.getLocation());
        assertSame(nodo, army.getPosizione());
        assertTrue(nodo.hasArmyPlayer(alessandro));
    }

    @Test
    void salva_partitaFinita_lanciaEccezione() {
        Game game = creaGame();
        game.setFinita();

        assertThrows(IllegalStateException.class, () -> repository.salva(game, fileTemporaneo()));
    }

    @Test
    void salva_argomentiNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> repository.salva(null, fileTemporaneo()));
        assertThrows(IllegalArgumentException.class, () -> repository.salva(creaGame(), null));
    }

    @Test
    void carica_fileDiTesto_lanciaIOException() throws IOException {
        Path file = fileTemporaneo();
        Files.writeString(file, "questo non è un salvataggio");

        assertThrows(IOException.class, () -> repository.carica(file));
    }

    @Test
    void carica_intestazioneSbagliata_lanciaIOException() throws IOException {
        Path file = fileTemporaneo();
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeUTF("ALTRO-GIOCO");
            out.writeInt(1);
        }

        assertThrows(IOException.class, () -> repository.carica(file));
    }

    @Test
    void carica_versioneDiversa_lanciaIOException() throws IOException {
        Path file = fileTemporaneo();
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeUTF("CONQUEST-GAME-SAVE");
            out.writeInt(99);
        }

        assertThrows(IOException.class, () -> repository.carica(file));
    }

    /** Partita con due giocatori, una città ciascuno e un esercito di due fanterie per Alessandro. */
    private Game creaGame() {
        Location nodo1 = new Location(1, 0, 0);
        Location nodo2 = new Location(2, 1, 0);
        nodo1.addAdiacente(nodo2);

        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO, "Pella", nodo1);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE, "Cartagine", nodo2);
        alessandro.setOro(15);

        Army army = new Army(1, alessandro, nodo1);
        army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, LeaderType.ALESSANDRO_MAGNO));
        army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, LeaderType.ALESSANDRO_MAGNO));

        return new Game(List.of(alessandro, annibale), List.of(nodo1, nodo2));
    }

    private Player creaPlayer(int id, LeaderType leader, String nomeCitta, Location location) {
        Territory territorio = new Territory(id, "Territorio " + id);
        Player player = new Player(id, "Giocatore " + id, leader, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);
        City city = new City(nomeCitta);
        location.addCity(city);
        territorio.addCity(city);
        territorio.nominaCapitale();
        return player;
    }

    /** File temporaneo che viene cancellato alla fine dei test. */
    private Path fileTemporaneo() throws IOException {
        Path file = Files.createTempFile("partita-test", ".rpg");
        file.toFile().deleteOnExit();
        return file;
    }
}
