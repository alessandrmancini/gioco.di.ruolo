package it.unicam.cs.mpgc.rpg125715;

import javafx.util.StringConverter;

/** VISTA: come mostrare leader, modalità e valori degli enum nelle ComboBox. */
public final class ConvertitoriTesto {

    private ConvertitoriTesto() {}

    /** Mostra il nome del leader (per esempio "Giulio Cesare"). */
    public static StringConverter<LeaderType> leader() {
        return new StringConverter<>() {
            @Override
            public String toString(LeaderType leader) {return leader == null ? "" : GameInfo.nomeLeader(leader);}
            @Override
            public LeaderType fromString(String testo) {return null;}
        };
    }

    /** Mostra il nome della modalità di vittoria. */
    public static StringConverter<VictoryMode> modalita() {
        return new StringConverter<>() {
            @Override
            public String toString(VictoryMode m) {return m == null ? "" : m.getNome();}
            @Override
            public VictoryMode fromString(String testo) {return null;}
        };
    }

    /** "CAVALLERIA" diventa "Cavalleria". */
    public static <T extends Enum<T>> StringConverter<T> enumerato() {
        return new StringConverter<>() {
            @Override
            public String toString(T valore) {return valore == null ? "" : MapView.bello(valore.name());}
            @Override
            public T fromString(String testo) {return null;}
        };
    }
}
