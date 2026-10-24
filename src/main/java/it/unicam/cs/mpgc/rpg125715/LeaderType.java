package it.unicam.cs.mpgc.rpg125715;

public enum LeaderType {
    ALESSANDRO_MAGNO,
    ANNIBALE,
    ATTILA,
    GIULIO_CESARE,
    REGINA_ELISABETTA,
    GIOVANNA_D_ARCO,
    FEDERICO_BARBAROSSA,
    QIN_SHI_HUANG;

    public String getDisplayName(){
        return switch (this){
            case ALESSANDRO_MAGNO -> "Alessandro Magno";
            case ANNIBALE -> "Annibale";
            case ATTILA -> "Attila";
            case GIULIO_CESARE -> "Giulio Cesare";
            case QIN_SHI_HUANG -> "Qin Shi Huang";
            case FEDERICO_BARBAROSSA ->  "Federico Barbarossa";
            case GIOVANNA_D_ARCO ->   "Giovanna D'Arco";
            case REGINA_ELISABETTA -> "Regina Elisabetta";
        };
    }

    //ALESSANDRO MAGNO
    public boolean haMobilitaMaggiore(){return this == ALESSANDRO_MAGNO;}
    public boolean rischioRibellioneLontananza(){return this == ALESSANDRO_MAGNO;}

    //GIULIO CESARE
    public boolean maggioriProbabilitaDadi(){return this == GIULIO_CESARE;}
    public boolean haCostoReclutamentoMaggiore(UnitType unit){
        return this == GIULIO_CESARE && unit != UnitType.SPECIALI;
    }

    //QIN SHI HUANG
    public boolean haCapitaleInizialeSviluppata(){return this == QIN_SHI_HUANG;}
    public boolean minoriProbabilitaDadi(){return this == QIN_SHI_HUANG;}

    //ANNIBALE
    public boolean puoReclutare(UnitType unit){
        if(unit == null){return false;}
        return switch (unit){
            case FANTERIA, CAVALLERIA, SPECIALI,ASSEDIO -> true;
            case ELEFANTI -> this == ANNIBALE;
        };
    }
    public boolean mobilitaRidottaConElefanti(){return this == ANNIBALE;}

    //ATTILA
    public int getBonusAttacco(){
        return switch (this){
            case ATTILA -> 2;
            case REGINA_ELISABETTA -> -1;
            default -> 0;
        };
    }
    public boolean bloccaMetropoli(){return this == ATTILA;}

    //REGINA ELISABETTA (sopra e sotto)

    //GIOVANNA D'ARCO
    public boolean aumentaProbabilitaInsurrezioniNemiche(){return this == GIOVANNA_D_ARCO;}
    public int getBonusCommercio(){
        return switch (this){
            case REGINA_ELISABETTA -> 1;
            case GIOVANNA_D_ARCO -> -1;
            default -> 0;
        };
    }

    //FEDERICO BARBAROSSA
    public int getBonusDifesa(){
        if(this == FEDERICO_BARBAROSSA){return 3;}
        return 0;
    }
    public boolean haInstabilitaImperiale(){return this == FEDERICO_BARBAROSSA;}

}