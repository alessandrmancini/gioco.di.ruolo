package it.unicam.cs.mpgc.rpg125715;

/** Gli 8 leader del gioco con le loro abilità e debolezze. */
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
    /** Alessandro: +1 movimento (non ancora usato nel gioco). */
    public boolean haMobilitaMaggiore(){return this == ALESSANDRO_MAGNO;}
    /** Alessandro: le città lontane senza guarnigione possono ribellarsi (non ancora usato nel gioco). */
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
    /** Annibale: eserciti con elefanti più lenti (non ancora usato nel gioco). */
    public boolean mobilitaRidottaConElefanti(){return this == ANNIBALE;}

    //ATTILA
    /** Bonus al totale nelle battaglie in attacco: Attila +2, Elisabetta -1. */
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
    /** Oro in più (o in meno) prodotto dalle città commerciali: Elisabetta +1, Giovanna -1. */
    public int getBonusCommercio(){
        return switch (this){
            case REGINA_ELISABETTA -> 1;
            case GIOVANNA_D_ARCO -> -1;
            default -> 0;
        };
    }

    //FEDERICO BARBAROSSA
    /** Bonus al totale nelle battaglie in difesa: Federico +3. */
    public int getBonusDifesa(){
        if(this == FEDERICO_BARBAROSSA){return 3;}
        return 0;
    }
    /** Federico: le città conquistate impiegano un turno in più a stabilizzarsi (non ancora usato nel gioco). */
    public boolean haInstabilitaImperiale(){return this == FEDERICO_BARBAROSSA;}

}