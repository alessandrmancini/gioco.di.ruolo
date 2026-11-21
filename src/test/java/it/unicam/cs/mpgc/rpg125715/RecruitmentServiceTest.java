package it.unicam.cs.mpgc.rpg125715;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Test del reclutamento: costi, città che possono reclutare e controlli prima di pagare. */
public class RecruitmentServiceTest {

    private RecruitmentService recruitmentService;
    private Player player;
    private City city;
    private Army army;

    @BeforeEach
    void setUp() {
        recruitmentService = new RecruitmentService();

        Territory territorio = new Territory(1, "Territorio 1");
        player = new Player(1, "Giocatore 1", LeaderType.ALESSANDRO_MAGNO, 10, territorio, PlayerKind.HUMAN);
        territorio.setOwner(player);

        Location location = new Location(1, 0, 0);
        city = new City("Pella");
        city.upgrade(); // Avamposto: può reclutare fanteria, cavalleria e assedio
        location.addCity(city);
        territorio.addCity(city);

        army = new Army(1, player, location);
    }

    @Test
    void costoUnita_fanteriaInAccampamento_costoBase() {
        assertEquals(2, recruitmentService.costoUnita(UnitType.FANTERIA, new City("Accampamento"), LeaderType.ALESSANDRO_MAGNO));
    }

    @Test
    void costoUnita_cesare_paga1InPiuTranneLeSpeciali() {
        City citta = creaCitta(2); // Città: -1
        assertEquals(4, recruitmentService.costoUnita(UnitType.CAVALLERIA, new City("Accampamento"), LeaderType.GIULIO_CESARE)); // 3 + 1
        assertEquals(5, recruitmentService.costoUnita(UnitType.SPECIALI, citta, LeaderType.GIULIO_CESARE));                     // 6 - 1
    }

    @Test
    void costoUnita_metropoliMilitare_scontoMaiSottoLaMetaDelCostoBase() {
        City metropoli = creaCitta(3);
        metropoli.setSpecialization(CitySpecialization.MILITARE);

        assertEquals(3, recruitmentService.costoUnita(UnitType.SPECIALI, metropoli, LeaderType.ALESSANDRO_MAGNO)); // 6 - 2 - 1
        assertEquals(1, recruitmentService.costoUnita(UnitType.FANTERIA, metropoli, LeaderType.ALESSANDRO_MAGNO)); // minimo (2+1)/2 = 1
    }

    @Test
    void puoReclutareDaCitta_accampamento_nonPuoReclutareNiente() {
        assertFalse(recruitmentService.puoReclutareDaCitta(new City("Accampamento"), UnitType.FANTERIA, LeaderType.ALESSANDRO_MAGNO));
    }

    @Test
    void puoReclutareDaCitta_speciali_soloDaCittaOMetropoli() {
        assertFalse(recruitmentService.puoReclutareDaCitta(creaCitta(1), UnitType.SPECIALI, LeaderType.ALESSANDRO_MAGNO));
        assertTrue(recruitmentService.puoReclutareDaCitta(creaCitta(2), UnitType.SPECIALI, LeaderType.ALESSANDRO_MAGNO));
    }

    @Test
    void puoReclutareDaCitta_elefanti_soloAnnibaleInCittaMilitare() {
        City militare = creaCitta(1);
        militare.setSpecialization(CitySpecialization.MILITARE);

        assertTrue(recruitmentService.puoReclutareDaCitta(militare, UnitType.ELEFANTI, LeaderType.ANNIBALE));
        assertFalse(recruitmentService.puoReclutareDaCitta(militare, UnitType.ELEFANTI, LeaderType.ALESSANDRO_MAGNO));
        assertFalse(recruitmentService.puoReclutareDaCitta(creaCitta(1), UnitType.ELEFANTI, LeaderType.ANNIBALE));
    }

    @Test
    void reclutaUnita_condizioniValide_pagaEAggiungeLUnita() {
        Unit unita = recruitmentService.reclutaUnita(player, city, army, UnitType.FANTERIA);

        assertEquals(UnitType.FANTERIA, unita.type());
        assertEquals(8, player.getOro());
        assertEquals(1, army.getNumeroUnita());
    }

    @Test
    void reclutaUnita_oroInsufficiente_lanciaEccezioneENonPaga() {
        player.setOro(1);

        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.reclutaUnita(player, city, army, UnitType.FANTERIA));
        assertEquals(1, player.getOro());
        assertTrue(army.isVuoto());
    }

    @Test
    void reclutaUnita_cittaInRibellione_lanciaEccezione() {
        city.attivaRibellione();

        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.reclutaUnita(player, city, army, UnitType.FANTERIA));
    }

    @Test
    void reclutaUnita_esercitoFuoriDallaCitta_lanciaEccezione() {
        Location altroNodo = new Location(2, 1, 0);
        Army lontano = new Army(2, player, altroNodo); // esercito del giocatore, ma in un altro nodo

        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.reclutaUnita(player, city, lontano, UnitType.FANTERIA));
    }

    @Test
    void reclutaUnita_tipoNonReclutabileInQuestaCitta_lanciaEccezione() {
        // Avamposto: le unità speciali richiedono almeno una Città
        assertThrows(IllegalArgumentException.class,
                () -> recruitmentService.reclutaUnita(player, city, army, UnitType.SPECIALI));
    }

    /** Città non assegnata a nessuno, portata al livello indicato (1 = Avamposto, 2 = Città, 3 = Metropoli). */
    private City creaCitta(int livelli) {
        City c = new City("Città di prova");
        for (int i = 0; i < livelli; i++) {
            c.upgrade();
        }
        return c;
    }
}
