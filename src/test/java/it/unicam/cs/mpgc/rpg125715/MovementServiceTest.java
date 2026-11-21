package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del movimento degli eserciti su una piccola mappa di tre nodi in fila:
 * nodo1 - nodo2 - nodo3. Il dado restituisce sempre 1.0.
 */
public class MovementServiceTest {

    private MovementService movementService;
    private Location nodo1;
    private Location nodo2;
    private Location nodo3;
    private Player alessandro;
    private Player annibale;

    @BeforeEach
    void setUp() {
        movementService = new MovementService(
                new BattleService(p -> 1.0),
                new ArmyService(new IdGenerator()),
                new ConquestService());

        nodo1 = new Location(1, 0, 0);
        nodo2 = new Location(2, 1, 0);
        nodo3 = new Location(3, 2, 0);
        nodo1.addAdiacente(nodo2);
        nodo2.addAdiacente(nodo3);

        alessandro = creaPlayer(1, LeaderType.ALESSANDRO_MAGNO);
        annibale = creaPlayer(2, LeaderType.ANNIBALE);
    }

    @Test
    void costruttore_serviceNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovementService(null, new ArmyService(new IdGenerator()), new ConquestService()));
    }

    @Test
    void muovi_versoNodoAdiacenteLibero_spostaEBloccaLEsercito() {
        Army army = creaEsercito(1, alessandro, nodo1, 2);

        BattleResult result = movementService.muovi(army, nodo2);

        assertNull(result);
        assertSame(nodo2, army.getPosizione());
        assertTrue(nodo2.hasArmyPlayer(alessandro));
        assertFalse(nodo1.hasArmy());
        assertFalse(army.isPuoMuovere());
    }

    @Test
    void muovi_versoNodoNonAdiacente_lanciaEccezione() {
        Army army = creaEsercito(1, alessandro, nodo1, 2);

        assertThrows(IllegalArgumentException.class, () -> movementService.muovi(army, nodo3));
    }

    @Test
    void muovi_esercitoGiaMosso_lanciaEccezione() {
        Army army = creaEsercito(1, alessandro, nodo1, 2);
        movementService.muovi(army, nodo2);

        assertThrows(IllegalArgumentException.class, () -> movementService.muovi(army, nodo3));
    }

    @Test
    void muovi_esercitoSenzaUnita_lanciaEccezione() {
        Army army = creaEsercito(1, alessandro, nodo1, 0);

        assertThrows(IllegalArgumentException.class, () -> movementService.muovi(army, nodo2));
    }

    @Test
    void muovi_attaccoNelPrimoTurno_lanciaEccezione() {
        Army army = creaEsercito(1, alessandro, nodo1, 2);
        creaEsercito(2, annibale, nodo2, 1);
        // alessandro non ha ancora giocato nessun turno

        assertThrows(IllegalArgumentException.class, () -> movementService.muovi(army, nodo2));
    }

    @Test
    void muovi_versoCittaNemicaIndifesa_conquistaLaCitta() {
        alessandro.incrementaTurniGiocati();
        City cartagine = aggiungiCitta(annibale, "Cartagine", nodo2);
        aggiungiCitta(annibale, "Rabat", nodo3);
        Army army = creaEsercito(1, alessandro, nodo1, 2);

        BattleResult result = movementService.muovi(army, nodo2);

        assertNull(result);
        assertSame(alessandro, cartagine.getOwner());
    }

    @Test
    void muovi_controEsercitoPiuDebole_vinceEConquistaLaCitta() {
        alessandro.incrementaTurniGiocati();
        City cartagine = aggiungiCitta(annibale, "Cartagine", nodo2);
        Army attaccante = creaEsercito(1, alessandro, nodo1, 4); // attacco 8
        creaEsercito(2, annibale, nodo2, 1);                     // difesa 2

        BattleResult result = movementService.muovi(attaccante, nodo2);

        assertNotNull(result);
        assertTrue(result.haVintoAttaccante());
        assertSame(alessandro, cartagine.getOwner());
        assertEquals(1, alessandro.getBattaglieVinte());
        // annibale ha perso l'unica città e l'unico esercito
        assertTrue(annibale.isSconfitto());
    }

    @Test
    void muovi_controEsercitoUgualmenteForte_attaccantePerdeESiRitira() {
        alessandro.incrementaTurniGiocati();
        Army attaccante = creaEsercito(1, alessandro, nodo1, 3); // attacco 6
        Army difensore = creaEsercito(2, annibale, nodo2, 3);    // difesa 6 -> a parità vince chi difende

        BattleResult result = movementService.muovi(attaccante, nodo2);

        assertFalse(result.haVintoAttaccante());
        // si ritira nell'unico nodo vicino libero e senza città
        assertSame(nodo1, attaccante.getPosizione());
        assertEquals(2, attaccante.getNumeroUnita());
        assertSame(nodo2, difensore.getPosizione());
        assertSame(attaccante, annibale.getAttaccanteSconfitto());
    }

    @Test
    void annullaMovimento_dopoUnoSpostamento_riportaIndietroESbloccaLEsercito() {
        Army army = creaEsercito(1, alessandro, nodo1, 2);
        movementService.muovi(army, nodo2);

        movementService.annullaMovimento(army, nodo1);

        assertSame(nodo1, army.getPosizione());
        assertFalse(nodo2.hasArmy());
        assertTrue(army.isPuoMuovere());
    }

    @Test
    void annullaMovimento_esercitoNonMosso_lanciaEccezione() {
        Army army = creaEsercito(1, alessandro, nodo1, 2);

        assertThrows(IllegalArgumentException.class, () -> movementService.annullaMovimento(army, nodo1));
    }

    @Test
    void getRitirata_cittaPropriaVicinaLibera_haLaPrecedenza() {
        aggiungiCitta(alessandro, "Pella", nodo3);

        assertSame(nodo3, movementService.getRitirata(nodo2, alessandro));
    }

    @Test
    void isAttacco_cittaDiUnAltroGiocatore_restituisceVero() {
        aggiungiCitta(annibale, "Cartagine", nodo2);

        assertTrue(movementService.isAttacco(nodo2, alessandro));
        assertFalse(movementService.isAttacco(nodo3, alessandro));
    }

    private Player creaPlayer(int id, LeaderType leader) {
        Territory territorio = new Territory(id, "Territorio " + id);
        Player player = new Player(id, "Giocatore " + id, leader, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);
        return player;
    }

    private Army creaEsercito(int id, Player owner, Location posizione, int fanterie) {
        Army army = new Army(id, owner, posizione);
        for (int i = 0; i < fanterie; i++) {
            army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, owner.getLeader()));
        }
        return army;
    }

    private City aggiungiCitta(Player player, String nome, Location location) {
        City city = new City(nome);
        location.addCity(city);
        player.getTerritorio().addCity(city);
        if (player.getCapitale() == null) {
            player.getTerritorio().nominaCapitale();
        }
        return city;
    }
}
