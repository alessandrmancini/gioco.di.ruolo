package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del calcolo dei punti della modalità a punti.
 * Nei test entrambi i giocatori hanno 0 oro, così il bonus "più oro" non viene assegnato
 * (tranne nel test dedicato).
 */
public class PunteggioServiceTest {

    @Test
    void classifica_gameNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> PunteggioService.classifica(null));
    }

    @Test
    void classifica_capitaleIniziale_vale5Punti() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Roma", 0);      // Roma è una capitale iniziale (Accampamento)
        aggiungiCitta(annibale, "Paesino", 0);

        PunteggioService.Punteggio punteggio = punteggioDi(alessandro, creaGame(alessandro, annibale));

        assertEquals(1, punteggio.capitali());
        // 5 per la capitale + 2 di bonus "più città" (pari merito con annibale)
        assertEquals(7, punteggio.totale());
    }

    @Test
    void classifica_livelliDelleCitta_sommaIPuntiDiOgniLivello() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Avamposto", 1);
        aggiungiCitta(alessandro, "Città", 2);
        aggiungiCitta(alessandro, "Metropoli", 3);
        aggiungiCitta(annibale, "Paesino", 0);

        PunteggioService.Punteggio punteggio = punteggioDi(alessandro, creaGame(alessandro, annibale));

        assertEquals(1, punteggio.avamposti());
        assertEquals(1, punteggio.citta());
        assertEquals(1, punteggio.metropoli());
        // 1 + 2 + 3 + 2 di bonus "più città"
        assertEquals(8, punteggio.totale());
    }

    @Test
    void classifica_cittaInRibellione_tolgonoUnPunto() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Città", 2).attivaRibellione();
        aggiungiCitta(annibale, "Paesino", 0);

        PunteggioService.Punteggio punteggio = punteggioDi(alessandro, creaGame(alessandro, annibale));

        assertEquals(1, punteggio.ribellioni());
        // 2 (Città) - 1 (ribellione) + 2 (più città)
        assertEquals(3, punteggio.totale());
    }

    @Test
    void classifica_battaglieVinte_unPuntoOgniDue() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Paesino A", 0);
        aggiungiCitta(annibale, "Paesino B", 0);
        for (int i = 0; i < 5; i++) {
            alessandro.incrementaBattaglieVinte();
        }

        PunteggioService.Punteggio punteggio = punteggioDi(alessandro, creaGame(alessandro, annibale));

        // 5 / 2 = 2 punti, più 2 di bonus "più città"
        assertEquals(4, punteggio.totale());
    }

    @Test
    void classifica_giocatoreConPiuOro_riceveIlBonus() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Paesino A", 0);
        aggiungiCitta(annibale, "Paesino B", 0);
        alessandro.setOro(20);
        annibale.setOro(5);
        Game game = creaGame(alessandro, annibale);

        assertTrue(punteggioDi(alessandro, game).piuOro());
        assertFalse(punteggioDi(annibale, game).piuOro());
        assertEquals(PunteggioService.BONUS_PIU_ORO + PunteggioService.BONUS_PIU_CITTA, punteggioDi(alessandro, game).totale());
    }

    @Test
    void classifica_ordinataDalPunteggioPiuAlto() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Paesino", 0);
        aggiungiCitta(annibale, "Metropoli", 3);

        List<PunteggioService.Punteggio> classifica = PunteggioService.classifica(creaGame(alessandro, annibale));

        assertSame(annibale, classifica.get(0).player());
        assertSame(alessandro, classifica.get(1).player());
    }

    @Test
    void classifica_giocatoreSconfitto_inFondoConZeroPunti() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Roma", 3);
        aggiungiCitta(annibale, "Paesino", 0);
        alessandro.setSconfitto();

        List<PunteggioService.Punteggio> classifica = PunteggioService.classifica(creaGame(alessandro, annibale));

        assertSame(alessandro, classifica.get(1).player());
        assertEquals(0, classifica.get(1).totale());
    }

    @Test
    void punti_giocatoreDellaPartita_restituisceIlSuoTotale() {
        Player alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        Player annibale = creaPlayer(2, LeaderType.ANNIBALE);
        aggiungiCitta(alessandro, "Roma", 0);
        aggiungiCitta(annibale, "Paesino", 0);

        assertEquals(7, PunteggioService.punti(creaGame(alessandro, annibale), alessandro));
    }

    private Player creaPlayer(int id, LeaderType leader) {
        Territory territorio = new Territory(id, "Territorio " + id);
        Player player = new Player(id, "Giocatore " + id, leader, 0, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);
        return player;
    }

    /** Aggiunge una città portata al livello indicato (0 = Accampamento ... 3 = Metropoli). */
    private City aggiungiCitta(Player player, String nome, int livelli) {
        City city = new City(nome);
        for (int i = 0; i < livelli; i++) {
            city.upgrade();
        }
        player.getTerritorio().addCity(city);
        return city;
    }

    private Game creaGame(Player... players) {
        return new Game(List.of(players), List.of(new Location(1, 0, 0)));
    }

    private PunteggioService.Punteggio punteggioDi(Player player, Game game) {
        for (PunteggioService.Punteggio p : PunteggioService.classifica(game)) {
            if (p.player() == player) {
                return p;
            }
        }
        throw new AssertionError("giocatore non presente in classifica");
    }
}
