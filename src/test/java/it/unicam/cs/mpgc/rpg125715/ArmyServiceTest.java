package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Test della creazione, distruzione e ricomparsa degli eserciti. */
public class ArmyServiceTest {

    private ArmyService armyService;
    private Player player;
    private City capitale;
    private Location nodoCapitale;

    @BeforeEach
    void setUp() {
        armyService = new ArmyService(new IdGenerator());

        Territory territorio = new Territory(1, "Territorio 1");
        player = new Player(1, "Giocatore 1", LeaderType.ALESSANDRO_MAGNO, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);

        nodoCapitale = new Location(1, 0, 0);
        capitale = new City("Pella");
        nodoCapitale.addCity(capitale);
        territorio.addCity(capitale);
        territorio.nominaCapitale();
    }

    @Test
    void costruttore_idGeneratorNull_lanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> new ArmyService(null));
    }

    @Test
    void costoNuovoEsercito_raddoppiaPerOgniEsercitoPosseduto() {
        assertEquals(0, ArmyService.costoNuovoEsercito(player)); // nessun esercito: gratis
        armyService.creaEsercito(player, new Location(2, 1, 0));
        assertEquals(2, ArmyService.costoNuovoEsercito(player));
        armyService.creaEsercito(player, new Location(3, 2, 0));
        assertEquals(4, ArmyService.costoNuovoEsercito(player));
    }

    @Test
    void creaEsercito_dueEserciti_ricevonoIdDiversi() {
        Army primo = armyService.creaEsercito(player, new Location(2, 1, 0));
        Army secondo = armyService.creaEsercito(player, new Location(3, 2, 0));

        assertNotEquals(primo.getId(), secondo.getId());
        assertEquals(2, player.numeroEserciti());
    }

    @Test
    void creaNuovoEsercito_inCittaPropriaLibera_creaTreFanterieEUnaCavalleria() {
        Army army = armyService.creaNuovoEsercito(player, capitale);

        assertSame(nodoCapitale, army.getPosizione());
        assertEquals(3, army.getNumeroUnitaPerTipo(UnitType.FANTERIA));
        assertEquals(1, army.getNumeroUnitaPerTipo(UnitType.CAVALLERIA));
        assertEquals(10, player.getOro()); // il primo esercito è gratis
    }

    @Test
    void creaNuovoEsercito_secondoEsercito_pagaIlCosto() {
        armyService.creaEsercito(player, new Location(2, 1, 0));

        armyService.creaNuovoEsercito(player, capitale);

        assertEquals(8, player.getOro()); // 10 - 2
    }

    @Test
    void creaNuovoEsercito_cittaGiaOccupataDaUnTuoEsercito_lanciaEccezione() {
        armyService.creaEsercito(player, nodoCapitale);

        assertThrows(IllegalArgumentException.class, () -> armyService.creaNuovoEsercito(player, capitale));
    }

    @Test
    void creaNuovoEsercito_cittaDiUnAltroGiocatore_lanciaEccezione() {
        City altra = new City("Cartagine");

        assertThrows(IllegalArgumentException.class, () -> armyService.creaNuovoEsercito(player, altra));
    }

    @Test
    void creaNuovoEsercito_oroInsufficiente_lanciaEccezione() {
        armyService.creaEsercito(player, new Location(2, 1, 0));
        player.setOro(1);

        assertThrows(IllegalArgumentException.class, () -> armyService.creaNuovoEsercito(player, capitale));
    }

    @Test
    void distruggiEsercito_toglieLEsercitoDalGiocatoreEDalNodo() {
        Location nodo = new Location(2, 1, 0);
        Army army = armyService.creaEsercito(player, nodo);

        armyService.distruggiEsercito(army);

        assertEquals(0, player.numeroEserciti());
        assertFalse(nodo.hasArmy());
    }

    @Test
    void distruggiERespawn_capitaleLibera_ricompareInCapitaleBloccato() {
        Army vecchio = armyService.creaEsercito(player, new Location(2, 1, 0));

        Army nuovo = armyService.distruggiERespawn(vecchio);

        assertNotNull(nuovo);
        assertSame(nodoCapitale, nuovo.getPosizione());
        assertEquals(1, nuovo.getNumeroUnitaPerTipo(UnitType.FANTERIA));
        assertEquals(1, nuovo.getNumeroUnitaPerTipo(UnitType.SPECIALI));
        assertFalse(nuovo.isPuoMuovere());
        assertEquals(1, player.numeroEserciti());
    }
}
