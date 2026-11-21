package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test delle battaglie. Il dado è sostituito da un DiceRoller finto che restituisce
 * sempre lo stesso moltiplicatore, così il risultato è prevedibile.
 */
public class BattleServiceTest {

    @Test
    void costruttore_dadoNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new BattleService(null));
    }

    @Test
    void combatti_esercitoNull_lanciaEccezione() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 1);

        assertThrows(IllegalArgumentException.class, () -> battaglia.combatti(null, difensore, null));
    }

    @Test
    void combatti_attaccanteVuoto_lanciaEccezione() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 0);
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 1);

        assertThrows(IllegalArgumentException.class, () -> battaglia.combatti(attaccante, difensore, null));
    }

    @Test
    void combatti_attaccanteMoltoPiuForte_vinceEDistruggeDifensore() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 4); // attacco 4 x 2 = 8
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 1);          // difesa 1 x 2 = 2

        BattleResult result = battaglia.combatti(attaccante, difensore, null);

        assertTrue(result.haVintoAttaccante());
        assertEquals(8, result.totaleAttaccante());
        assertEquals(2, result.totaleDifensore());
        // il nemico ha almeno il doppio del totale: lo sconfitto perde tutte le unità
        assertTrue(difensore.isVuoto());
        // il vincitore perde il 15% delle unità: round(4 * 0.15) = 1
        assertEquals(3, attaccante.getNumeroUnita());
    }

    @Test
    void combatti_pareggio_vinceDifensore() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 1);
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 1);

        BattleResult result = battaglia.combatti(attaccante, difensore, null);

        assertFalse(result.haVintoAttaccante());
        assertSame(difensore, result.vincitore());
        assertSame(attaccante, result.sconfitto());
    }

    @Test
    void combatti_sconfittoConRapportoBasso_perdeAlmenoUnaUnita() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 2); // 4
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 2);          // 4 -> vince il difensore

        BattleResult result = battaglia.combatti(attaccante, difensore, null);

        assertEquals(1, result.perditeAttaccante());
        assertEquals(1, attaccante.getNumeroUnita());
    }

    @Test
    void combatti_dadoDiversoPerOgniLato_moltiplicaITotali() {
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 2); // 4
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 2);          // 4
        Player giocatoreAttaccante = attaccante.getOwner();
        BattleService battaglia = new BattleService(p -> p == giocatoreAttaccante ? 1.5 : 0.8);

        BattleResult result = battaglia.combatti(attaccante, difensore, null);

        assertEquals(6, result.totaleAttaccante()); // 4 x 1.5
        assertEquals(3, result.totaleDifensore());  // 4 x 0.8 = 3.2 -> 3
        assertTrue(result.haVintoAttaccante());
    }

    @Test
    void combatti_attilaInAttacco_riceveBonusPiuDue() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ATTILA, 1);  // 2 + 2
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 1); // 2

        BattleResult result = battaglia.combatti(attaccante, difensore, null);

        assertEquals(4, result.totaleAttaccante());
        assertTrue(result.haVintoAttaccante());
    }

    @Test
    void combatti_federicoInDifesa_riceveBonusPiuTre() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 2);  // 4
        Army difensore = creaEsercito(2, LeaderType.FEDERICO_BARBAROSSA, 1); // 2 + 3

        BattleResult result = battaglia.combatti(attaccante, difensore, null);

        assertEquals(5, result.totaleDifensore());
        assertFalse(result.haVintoAttaccante());
    }

    @Test
    void combatti_unitaAssedioControCitta_aggiungonoBonus() {
        BattleService battaglia = new BattleService(p -> 1.0);
        Army attaccante = creaEsercito(1, LeaderType.ALESSANDRO_MAGNO, 0);
        attaccante.addUnit(UnitFactory.creaUnita(UnitType.ASSEDIO, LeaderType.ALESSANDRO_MAGNO));
        attaccante.addUnit(UnitFactory.creaUnita(UnitType.ASSEDIO, LeaderType.ALESSANDRO_MAGNO));
        Army difensore = creaEsercito(2, LeaderType.ANNIBALE, 1);
        City city = new City("Città assediata");
        city.upgrade(); // Avamposto
        city.upgrade(); // Città

        BattleResult result = battaglia.combatti(attaccante, difensore, city);

        // attacco 2 x 2 = 4, più 2 punti per ogni unità d'assedio contro una Città
        assertEquals(8, result.totaleAttaccante());
    }

    @Test
    void calcolaBonusDifesaTerritorio_livelliESpecializzazione_restituisceBonusCorretto() {
        City city = new City("Fortezza");
        assertEquals(0, BattleService.calcolaBonusDifesaTerritorio(null));
        assertEquals(0, BattleService.calcolaBonusDifesaTerritorio(city)); // Accampamento

        city.upgrade();
        assertEquals(1, BattleService.calcolaBonusDifesaTerritorio(city)); // Avamposto
        city.upgrade();
        assertEquals(2, BattleService.calcolaBonusDifesaTerritorio(city)); // Città
        city.upgrade();
        assertEquals(4, BattleService.calcolaBonusDifesaTerritorio(city)); // Metropoli

        city.setSpecialization(CitySpecialization.MILITARE);
        assertEquals(6, BattleService.calcolaBonusDifesaTerritorio(city)); // Metropoli militare
    }

    /** Crea un esercito con il numero indicato di fanterie (attacco 2, difesa 2) in un nodo libero. */
    private Army creaEsercito(int id, LeaderType leader, int fanterie) {
        Territory territorio = new Territory(id, "Territorio " + id);
        Player player = new Player(id, "Giocatore " + id, leader, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);
        Army army = new Army(id, player, new Location(id, 0, 0));
        for (int i = 0; i < fanterie; i++) {
            army.addUnit(UnitFactory.creaUnita(UnitType.FANTERIA, leader));
        }
        return army;
    }
}
