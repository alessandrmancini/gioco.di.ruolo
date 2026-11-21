package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Test del dado: per ogni leader controlla il moltiplicatore della faccia più bassa e di quella più alta. */
public class DiceServiceTest {

    private static final double DELTA = 0.0001;

    @Test
    void costruttore_randomNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new DiceService(null));
    }

    @Test
    void lanciaDado_playerNull_lanciaEccezione() {
        DiceService dado = new DiceService(new Random());

        assertThrows(IllegalArgumentException.class, () -> dado.lanciaDado(null));
    }

    @Test
    void lanciaDado_leaderNormale_da08A13() {
        assertEquals(0.8, new DiceService(faccia(1)).lanciaDado(creaPlayer(LeaderType.ALESSANDRO_MAGNO)), DELTA);
        assertEquals(1.3, new DiceService(faccia(6)).lanciaDado(creaPlayer(LeaderType.ALESSANDRO_MAGNO)), DELTA);
    }

    @Test
    void lanciaDado_cesare_da10A15() {
        assertEquals(1.0, new DiceService(faccia(1)).lanciaDado(creaPlayer(LeaderType.GIULIO_CESARE)), DELTA);
        assertEquals(1.5, new DiceService(faccia(6)).lanciaDado(creaPlayer(LeaderType.GIULIO_CESARE)), DELTA);
    }

    @Test
    void lanciaDado_qin_da08A11() {
        assertEquals(0.8, new DiceService(faccia(1)).lanciaDado(creaPlayer(LeaderType.QIN_SHI_HUANG)), DELTA);
        assertEquals(1.1, new DiceService(faccia(6)).lanciaDado(creaPlayer(LeaderType.QIN_SHI_HUANG)), DELTA);
    }

    @Test
    void lanciaDado_randomVero_restaSempreNellIntervalloDelLeader() {
        DiceService dado = new DiceService(new Random(42));
        Player player = creaPlayer(LeaderType.ATTILA);

        for (int i = 0; i < 1000; i++) {
            double valore = dado.lanciaDado(player);
            assertTrue(valore >= 0.8 && valore <= 1.3);
        }
    }

    /** Random "truccato" che fa uscire sempre la faccia indicata (da 1 a 6). */
    private Random faccia(int valore) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return valore - 1;
            }
        };
    }

    private Player creaPlayer(LeaderType leader) {
        Territory territorio = new Territory(1, "Territorio");
        Player player = new Player(1, "Giocatore", leader, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);
        return player;
    }
}
