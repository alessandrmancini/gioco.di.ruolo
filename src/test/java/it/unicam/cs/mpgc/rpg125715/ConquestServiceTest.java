package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Test del passaggio delle città: conquista, abbandono per ribellione e fondazione. */
public class ConquestServiceTest {

    private final ConquestService conquestService = new ConquestService();

    @Test
    void conquista_argomentiNull_lanciaEccezione() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);

        assertThrows(IllegalArgumentException.class, () -> conquestService.conquista(null, player));
        assertThrows(IllegalArgumentException.class, () -> conquestService.conquista(new City("Pella"), null));
    }

    @Test
    void conquista_cittaGiaPropria_lanciaEccezione() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        City city = aggiungiCitta(player, "Pella");

        assertThrows(IllegalArgumentException.class, () -> conquestService.conquista(city, player));
    }

    @Test
    void conquista_cittaNemica_passaAlConquistatore() {
        Player difensore = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player conquistatore = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(difensore, "Pella");
        City creta = aggiungiCitta(difensore, "Creta");
        aggiungiCitta(conquistatore, "Cartagine");

        conquestService.conquista(creta, conquistatore);

        assertSame(conquistatore, creta.getOwner());
        assertTrue(conquistatore.getTerritorio().cercaCity(creta));
        assertFalse(difensore.getTerritorio().cercaCity(creta));
        assertEquals(1, difensore.numeroCitta());
        assertEquals(2, conquistatore.numeroCitta());
    }

    @Test
    void conquista_capitale_vecchioProprietarioNominaNuovaCapitale() {
        Player difensore = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player conquistatore = creaPlayer(2, LeaderType.ANNIBALE);
        City pella = aggiungiCitta(difensore, "Pella");
        City creta = aggiungiCitta(difensore, "Creta");
        difensore.getTerritorio().setCapitale(pella);

        conquestService.conquista(pella, conquistatore);

        assertSame(creta, difensore.getCapitale());
    }

    @Test
    void conquista_conquistatoreSenzaCitta_laCittaDiventaSuaCapitale() {
        Player difensore = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player conquistatore = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(difensore, "Pella");
        City creta = aggiungiCitta(difensore, "Creta");

        conquestService.conquista(creta, conquistatore);

        assertSame(creta, conquistatore.getCapitale());
    }

    @Test
    void conquista_ultimaCittaDiUnGiocatoreSenzaEserciti_giocatoreSconfitto() {
        Player difensore = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player conquistatore = creaPlayer(2, LeaderType.ANNIBALE);
        City pella = aggiungiCitta(difensore, "Pella");

        conquestService.conquista(pella, conquistatore);

        assertTrue(difensore.isSconfitto());
        assertNull(difensore.getCapitale());
    }

    @Test
    void conquista_cittaInRibellione_cancellaLaRibellione() {
        Player difensore = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player conquistatore = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(difensore, "Pella");
        City creta = aggiungiCitta(difensore, "Creta");
        creta.attivaRibellione();

        conquestService.conquista(creta, conquistatore);

        assertFalse(creta.hasRibellione());
    }

    @Test
    void abbandona_cittaConProprietario_diventaIndipendenteERicordaExProprietario() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        aggiungiCitta(player, "Pella");
        City creta = aggiungiCitta(player, "Creta");
        creta.attivaRibellione();

        conquestService.abbandona(creta);

        assertNull(creta.getOwner());
        assertSame(player, creta.getExProprietario());
        assertFalse(creta.hasRibellione());
        assertFalse(player.getTerritorio().cercaCity(creta));
    }

    @Test
    void abbandona_cittaGiaIndipendente_nonCambiaNulla() {
        City city = new City("Città libera");

        conquestService.abbandona(city);

        assertNull(city.getOwner());
        assertNull(city.getExProprietario());
    }

    @Test
    void fondaCitta_giocatoreSenzaCittaConEsercitoNelNodo_creaLaCapitale() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Location location = new Location(1, 0, 0);
        new Army(1, player, location);

        City nuova = conquestService.fondaCitta(player, location, "  Nuova Pella  ");

        assertEquals("Nuova Pella", nuova.getName());
        assertSame(nuova, location.getCity());
        assertSame(player, nuova.getOwner());
        assertSame(nuova, player.getCapitale());
    }

    @Test
    void fondaCitta_giocatoreConAncoraCitta_lanciaEccezione() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        aggiungiCitta(player, "Pella");
        Location location = new Location(1, 0, 0);
        new Army(1, player, location);

        assertThrows(IllegalArgumentException.class, () -> conquestService.fondaCitta(player, location, "Nuova"));
    }

    @Test
    void fondaCitta_senzaEsercitoNelNodo_lanciaEccezione() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Location location = new Location(1, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> conquestService.fondaCitta(player, location, "Nuova"));
    }

    @Test
    void trovaLocationPerFondazione_giocatoreSenzaCitta_restituisceIlNodoDelSuoEsercito() {
        Player player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Location location = new Location(1, 0, 0);
        new Army(1, player, location);

        assertSame(location, conquestService.trovaLocationPerFondazione(player));
    }

    private Player creaPlayer(int id, LeaderType leader) {
        Territory territorio = new Territory(id, "Territorio " + id);
        Player player = new Player(id, "Giocatore " + id, leader, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);
        return player;
    }

    private City aggiungiCitta(Player player, String nome) {
        City city = new City(nome);
        player.getTerritorio().addCity(city);
        if (player.getCapitale() == null) {
            player.getTerritorio().nominaCapitale();
        }
        return city;
    }
}
