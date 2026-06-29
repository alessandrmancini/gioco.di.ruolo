package it.unicam.cs.mpgc.rpg125715;

public class UnitFactory {

    public static Unit creaUnita(UnitType type, LeaderType leader) {
        if (type == null) {
            throw new IllegalArgumentException("il tipo di unità non può essere nullo");
        }
        if (leader == null) {
            throw new IllegalArgumentException("il leader non può essere nullo");
        }
        if (!leader.puoReclutare(type)) {
            throw new IllegalArgumentException("il leader " + leader + " non può reclutare l'unità " + type);
        }

        return switch (type) {
            case FANTERIA -> new Unit(UnitType.FANTERIA, 2, 2, 2);
            case CAVALLERIA -> new Unit(UnitType.CAVALLERIA, 3, 1, 3);
            case ASSEDIO -> new Unit(UnitType.ASSEDIO, 2, 1, 4);
            case ELEFANTI -> new Unit(UnitType.ELEFANTI, 4, 3, 5);
            case SPECIALI -> creaUnitaSpeciale(leader);
        };
    }

    private static Unit creaUnitaSpeciale(LeaderType leader) {
        return switch (leader) {
            case ALESSANDRO_MAGNO -> new Unit(UnitType.SPECIALI, 5, 3, 6);
            case ANNIBALE -> new Unit(UnitType.SPECIALI, 4, 4, 6);
            case ATTILA -> new Unit(UnitType.SPECIALI, 6, 2, 6);
            case GIULIO_CESARE, QIN_SHI_HUANG -> new Unit(UnitType.SPECIALI, 4, 5, 6);
            case REGINA_ELISABETTA -> new Unit(UnitType.SPECIALI, 3, 4, 5);
            case GIOVANNA_D_ARCO -> new Unit(UnitType.SPECIALI, 5, 2, 5);
            case FEDERICO_BARBAROSSA -> new Unit(UnitType.SPECIALI, 3, 6, 6);
        };
    }
}