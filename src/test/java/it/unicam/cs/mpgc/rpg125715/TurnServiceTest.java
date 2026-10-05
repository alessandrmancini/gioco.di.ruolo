package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TurnServiceTest {

    @Test
    void costruttore_ribellioneServiceNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new TurnService(null));
    }

    @Test
    void fineTurno_gameNull_lanciaEccezione() {
        TurnService turnService = new TurnService(new RibellioneService());

        assertThrows(IllegalArgumentException.class, () -> turnService.fineTurno(null));
    }

    @Test
    void fineTurno_gameNonIniziato_lanciaEccezione() {
        TurnService turnService = new TurnService(new RibellioneService());
        Game game = creaGameBase();

        assertThrows(IllegalArgumentException.class, () -> turnService.fineTurno(game));
    }

    @Test
    void fineTurno_gameFinito_nonCambiaTurno() {
        TurnService turnService = new TurnService(new RibellioneService());
        Game game = creaGameBase();
        game.iniziaPartita();
        game.setFinita();

        int turnoPrima = game.getTurno();
        int numeroTurnoPrima = game.getNumeroTurno();
        Player playerCorrentePrima = game.getCurrentPlayer();

        turnService.fineTurno(game);

        assertEquals(turnoPrima, game.getTurno());
        assertEquals(numeroTurnoPrima, game.getNumeroTurno());
        assertSame(playerCorrentePrima, game.getCurrentPlayer());
    }

    @Test
    void fineTurno_conDuePlayerAttivi_passaAlPlayerSuccessivo() {
        TurnService turnService = new TurnService(new RibellioneService());
        Game game = creaGameBase();
        game.iniziaPartita();

        Player primo = game.getCurrentPlayer();
        Player secondo = game.getPlayers().get(1);

        turnService.fineTurno(game);

        assertEquals(2, game.getNumeroTurno());
        assertSame(secondo, game.getCurrentPlayer());
        assertNotSame(primo, game.getCurrentPlayer());
    }

    @Test
    void fineTurno_conUnSoloPlayerAttivo_impostaPartitaFinita() {
        TurnService turnService = new TurnService(new RibellioneService());
        Game game = creaGameBase();
        game.iniziaPartita();

        Player secondo = game.getPlayers().get(1);
        secondo.setSconfitto();

        turnService.fineTurno(game);

        assertTrue(game.isGameOver());
    }

    private Game creaGameBase() {
        Territory territorio1 = new Territory(1, "Territorio 1");
        Territory territorio2 = new Territory(2, "Territorio 2");

        Player player1 = new Player(
                1,
                "Giocatore 1",
                LeaderType.ALESSANDRO_MAGNO,
                10,
                territorio1,
                PlayerKind.HUMAN
        );

        Player player2 = new Player(
                2,
                "Giocatore 2",
                LeaderType.ANNIBALE,
                10,
                territorio2,
                PlayerKind.BOT
        );

        territorio1.setOwner(player1);
        territorio2.setOwner(player2);

        Location l1 = new Location(1, 0, 0);
        Location l2 = new Location(2, 1, 0);

        return new Game(List.of(player1, player2), List.of(l1, l2));
    }
}