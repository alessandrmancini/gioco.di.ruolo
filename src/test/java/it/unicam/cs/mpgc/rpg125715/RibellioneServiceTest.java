package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test delle ribellioni. Il Random è sostituito da uno "truccato" che restituisce sempre
 * lo stesso numero, così si decide se la ribellione scoppia o no.
 */
public class RibellioneServiceTest {

    /** nextInt restituisce sempre 0: il tiro vale 1, quindi la ribellione scoppia sempre. */
    private static final Random SEMPRE_RIBELLIONE = new Random() {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    };

    /** nextInt restituisce sempre il massimo: il tiro vale 100, quindi la ribellione non scoppia mai. */
    private static final Random MAI_RIBELLIONE = new Random() {
        @Override
        public int nextInt(int bound) {
            return bound - 1;
        }
    };

    private Player player;
    private Player avversario;
    private City pella;
    private Location nodoPella;
    private Game game;

    @BeforeEach
    void setUp() {
        player = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        avversario = creaPlayer(2, LeaderType.ANNIBALE);

        nodoPella = new Location(1, 0, 0);
        pella = new City("Pella");
        nodoPella.addCity(pella);
        player.getTerritorio().addCity(pella);
        player.getTerritorio().nominaCapitale();

        game = new Game(List.of(player, avversario), List.of(nodoPella));
    }

    @Test
    void costruttore_argomentiNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new RibellioneService(null));
        assertThrows(IllegalArgumentException.class, () -> new RibellioneService(new ConquestService(), null));
    }

    @Test
    void calcolaProbabilita_cittaNonSviluppataSenzaEsercitoNeMilitare_45PerCento() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);

        // +20 nessuna guarnigione, +10 tutte accampamenti, +15 nessuna città militare
        assertEquals(45, service.calcolaProbabilita(player, game));
        assertEquals(3, service.causeAttive(player, game).size());
    }

    @Test
    void calcolaProbabilita_cittaSviluppataMilitareConEsercito_zero() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);
        pella.upgrade();
        pella.setSpecialization(CitySpecialization.MILITARE);
        new Army(1, player, nodoPella);

        assertEquals(0, service.calcolaProbabilita(player, game));
    }

    @Test
    void calcolaProbabilita_giovannaConEsercitoVicino_aggiunge20PerCento() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);
        Player giovanna = creaPlayer(3, LeaderType.GIOVANNA_D_ARCO);
        Location vicino = new Location(2, 1, 0);
        vicino.addAdiacente(nodoPella);
        new Army(3, giovanna, vicino);
        Game partita = new Game(List.of(player, giovanna), List.of(nodoPella, vicino));

        assertTrue(service.subisceEffettoLeaderNemico(player, partita));
        assertEquals(65, service.calcolaProbabilita(player, partita));
    }

    @Test
    void aggiornaRibellioni_tiroFavorevoleDopoIlPrimoTurno_scoppiaRibellione() {
        RibellioneService service = new RibellioneService(new ConquestService(), SEMPRE_RIBELLIONE);
        player.incrementaTurniGiocati();

        service.aggiornaRibellioni(player, game);

        assertTrue(pella.hasRibellione());
        assertFalse(game.prelevaEventi().isEmpty());
    }

    @Test
    void aggiornaRibellioni_primoTurnoDelGiocatore_nessunaRibellione() {
        RibellioneService service = new RibellioneService(new ConquestService(), SEMPRE_RIBELLIONE);

        service.aggiornaRibellioni(player, game);

        assertFalse(pella.hasRibellione());
    }

    @Test
    void aggiornaRibellioni_tiroSfavorevole_nessunaRibellione() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);
        player.incrementaTurniGiocati();

        service.aggiornaRibellioni(player, game);

        assertFalse(pella.hasRibellione());
    }

    @Test
    void aggiornaRibellioni_presidioDiQuattroUnita_sedaLaRibellione() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);
        pella.attivaRibellione();
        Army presidio = new Army(1, player, nodoPella);
        for (int i = 0; i < Ribellione.UNITA_PER_PACIFICARE; i++) {
            presidio.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, player.getLeader()));
        }

        service.aggiornaRibellioni(player, game);

        assertFalse(pella.hasRibellione());
    }

    @Test
    void aggiornaRibellioni_ribellioneSenzaPresidio_contaUnTurno() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);
        pella.attivaRibellione();

        service.aggiornaRibellioni(player, game);

        assertEquals(1, pella.getRibellione().getTurniAttiva());
        assertSame(player, pella.getOwner());
    }

    @Test
    void aggiornaRibellioni_ribellioneDurataTroppo_cittaDiventaIndipendente() {
        RibellioneService service = new RibellioneService(new ConquestService(), MAI_RIBELLIONE);
        aggiungiSecondaCitta();
        pella.attivaRibellione();
        for (int i = 0; i < Ribellione.TURNI_PER_PERDITA - 1; i++) {
            pella.getRibellione().nextTurn();
        }

        service.aggiornaRibellioni(player, game);

        assertNull(pella.getOwner());
        assertSame(player, pella.getExProprietario());
        assertFalse(player.getTerritorio().cercaCity(pella));
    }

    private Player creaPlayer(int id, LeaderType leader) {
        Territory territorio = new Territory(id, "Territorio " + id);
        Player p = new Player(id, "Giocatore " + id, leader, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(p);
        return p;
    }

    /** Seconda città del giocatore, così perdere Pella non lo rende sconfitto. */
    private void aggiungiSecondaCitta() {
        City creta = new City("Creta");
        new Location(9, 5, 5).addCity(creta);
        player.getTerritorio().addCity(creta);
    }
}
