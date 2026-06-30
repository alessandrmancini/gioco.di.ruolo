package it.unicam.cs.mpgc.rpg125715;

public enum CityType {

    //QIN SHI HUANG
    Xianyang("Xianyang", LeaderType.QIN_SHI_HUANG, true),
    Hong_Kong("Hong Kong", LeaderType.QIN_SHI_HUANG, false),
    Delhi("Delhi", LeaderType.QIN_SHI_HUANG, false),
    Tokyo("Tokyo", LeaderType.QIN_SHI_HUANG, false),

    //ATTILA
    Mosca("Mosca", LeaderType.ATTILA, true),
    Astana("Astana", LeaderType.ATTILA, false),
    Bratsk("Bratsk", LeaderType.ATTILA, false),
    Stoccolma("Stoccolma", LeaderType.ATTILA, false),

    //ALESSANDRO MAGNO
    Pella("Pella", LeaderType.ALESSANDRO_MAGNO, true),
    Creta("Creta", LeaderType.ALESSANDRO_MAGNO, false),
    Ankara("Ankara", LeaderType.ALESSANDRO_MAGNO, false),
    Babilonia("Babilonia", LeaderType.ALESSANDRO_MAGNO, false),
    Gedda("Gedda", LeaderType.ALESSANDRO_MAGNO, false),

    //GIULIO CESARE
    Roma("Roma", LeaderType.GIULIO_CESARE, true),
    Vienna("Vienna", LeaderType.GIULIO_CESARE, false),
    Cagliari("Cagliari", LeaderType.GIULIO_CESARE, false),

    //GIOVANNA D'ARCO
    Parigi("Parigi", LeaderType.GIOVANNA_D_ARCO, true),
    Marsiglia("Marsiglia", LeaderType.GIOVANNA_D_ARCO, false),
    Amsterdam("Amsterdam", LeaderType.GIOVANNA_D_ARCO, false),
    Rennes("Rennes", LeaderType.GIOVANNA_D_ARCO, false),

    //ELISABETTA
    Londra("Londra", LeaderType.REGINA_ELISABETTA, true),
    Edimburgo("Edimburgo", LeaderType.REGINA_ELISABETTA, false),
    Dublino("Dublino", LeaderType.REGINA_ELISABETTA, false),
    Reykjavik("Reykjavik", LeaderType.REGINA_ELISABETTA, false),

    //FEDERICO BARBAROSSA
    Berlino("Berlino", LeaderType.FEDERICO_BARBAROSSA, true),
    Varsavia("Varsavia", LeaderType.FEDERICO_BARBAROSSA, false),
    Copenaghen("Copenaghen", LeaderType.FEDERICO_BARBAROSSA, false),

    //ANNIBALE
    Cartagine("Cartagine", LeaderType.ANNIBALE, true),
    Il_Cairo("Il Cairo", LeaderType.ANNIBALE, false),
    Rabat("Rabat", LeaderType.ANNIBALE, false),
    Madrid("Madrid", LeaderType.ANNIBALE, false),
    Tariff("Tariff", LeaderType.ANNIBALE, false);


    private final String displayName;
    private final LeaderType leaderType;
    private final boolean capital;
    CityType(String displayName, LeaderType leaderType, boolean capital) {
        this.displayName = displayName;
        this.leaderType = leaderType;
        this.capital = capital;
    }
    //NOME CITTA
    public String getDisplayName() {
        return displayName;
    }
    //A CHE LEADER APPARTIENE
    public LeaderType getStartLeaderType() {
        return leaderType;
    }
    //TRUE SE CAPITALE
    public boolean isCapital() {
        return capital;
    }
}
