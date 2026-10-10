package it.unicam.cs.mpgc.rpg125715;

public class GameInfo {

    private GameInfo(){} //

    public static String nomeLeader(LeaderType leader){
        return switch(leader){
            case ALESSANDRO_MAGNO -> "Alessandro Magno";
            case ANNIBALE -> "Annibale";
            case ATTILA -> "Attila";
            case GIULIO_CESARE -> "Giulio Cesare";
            case REGINA_ELISABETTA -> "Regina Elisabetta";
            case GIOVANNA_D_ARCO ->  "Giovanna d'Arco";
            case FEDERICO_BARBAROSSA -> "Federico Barbarossa";
            case QIN_SHI_HUANG ->  "Qin Shi Huang";
        };
    }
    public static int difficolta(LeaderType leader){
        return switch (leader){
            case QIN_SHI_HUANG -> 1;
            case ATTILA -> 2;
            case REGINA_ELISABETTA -> 3;
            case ALESSANDRO_MAGNO, ANNIBALE -> 4;
            case GIOVANNA_D_ARCO, FEDERICO_BARBAROSSA, GIULIO_CESARE -> 5;
        };
    }
    public static String testoDifficolta(LeaderType leader){
        int d = difficolta(leader);
        String stelle = "";
        for (int i= 1; i <=5; i++){
            stelle += (i<=d) ? "★" : "☆";
        }
        String parola = switch (d){
            case 1 -> "molto facile";
            case 2 -> "facile";
            case 3 -> "media";
            case 4 -> "difficile";
            default -> "molto difficile";
        };
        return "Difficoltà: "+ stelle + " ("+ parola + ")";
    }

    public static String abilita(LeaderType leader){
        return switch (leader){
            case QIN_SHI_HUANG ->
                    "+ La capitale parte già al livello Avamposto.\n"
                            + "- Dadi più ristretti: x0.8 / x1.0 / x1.1.";
            case ATTILA ->
                    "+ +2 all'attacco in tutte le battaglie offensive.\n"
                            + "- Può migliorare a Metropoli solo la capitale.";
            case REGINA_ELISABETTA ->
                    "+ Le città commerciali producono +1 oro in più.\n"
                            + "- -1 all'attacco in tutte le battaglie.";
            case ALESSANDRO_MAGNO ->
                    "+ Eserciti più mobili: +1 movimento (non ancora attivo).\n"
                            + "- I territori lontani senza guarnigione possono ribellarsi (non ancora attivo).";
            case ANNIBALE ->
                    "+ Parte con un elefante e può reclutare Elefanti (solo nelle città militari).\n"
                            + "- Mobilità ridotta con gli elefanti (non ancora attiva).";
            case GIOVANNA_D_ARCO ->
                    "+ Se un suo esercito confina con una città nemica, quella città rischia una ribellione (+20%).\n"
                            + "- Le città commerciali producono 1 oro in meno.";
            case GIULIO_CESARE ->
                    "+ Dadi migliorati: x1.0 / x1.1 / x1.2 / x1.3 / x1.5.\n"
                            + "- Tutte le unità costano +1 oro (escluse le speciali).";
            case FEDERICO_BARBAROSSA ->
                    "+ +3 alla difesa in tutte le battaglie difensive.\n"
                            + "- Dopo una conquista la città ci mette un turno in più a stabilizzarsi (non ancora attivo).";
        };
    }
    public static String riassuntoLeader(LeaderType leader){
        return testoDifficolta(leader)+ "\n" + abilita(leader);
    }

    public static String legenda(){
        String t = "";
        t+="Costruzione (costo in oro)\n";
        t+= "Accampamento -> Avamposto: 3\n";
        t += "  Avamposto -> Città: 4\n";
        t += "  Città -> Metropoli: 6\n";
        t += "  Specializzazione (o cambio): " + ConstructionService.COSTO_SPECIALIZZAZIONE + "\n\n";

        t += "SPECIALIZZAZIONI\n";
        t += "  Militare: +2 difesa nelle battaglie in città, unità -1 oro, evita il rischio ribellione\n";
        t += "            per mancanza di città militari; serve per reclutare gli Elefanti (Annibale).\n";
        t += "  Commerciale: oro in più a ogni turno secondo il livello\n";
        t += "            (Accampamento +1, Avamposto +2, Città +2, Metropoli +3).\n";
        t += "  Agricola: oro in più a ogni turno secondo il livello\n";
        t += "            (Accampamento +1, Avamposto +1, Città +2, Metropoli +2).\n\n";

        t += "BATTAGLIE\n";
        t += "  Perdite: chi perde lascia il 40% delle unità (almeno 1), chi vince il 15%.\n";
        t += "  L'esercito sconfitto si ritira in una tua città vicina o in un nodo libero;\n";
        t += "  se non può, viene distrutto e ricompare in una tua città libera con una sola fanteria.\n\n";


        t += "TRUPPE (costo base; attacco/difesa)\n";
        t += "  Fanteria: 2 oro (2/2)\n";
        t += "  Cavalleria: 3 oro (3/1)\n";
        t += "  Assedio: 4 oro (2/1, bonus contro Città e Metropoli)\n";
        t += "  Speciale: 6 oro (statistiche diverse per ogni leader)\n";
        t += "  Elefanti: 5 oro (4/3, solo Annibale)\n";
        t += "  Sconti: città -1, metropoli -2, specializzazione militare -1 (costo minimo 1).\n";
        t += "  Giulio Cesare paga +1 su tutte le unità tranne le speciali.\n\n";

        t += "QUANDO SI RECLUTA\n";
        t += "  Solo in una tua città dove c'è un tuo esercito, con il territorio senza ribellione.\n";
        t += "  Fanteria, Cavalleria, Assedio: da Avamposto in su.\n";
        t += "  Unità speciali: solo da Città o Metropoli.\n";
        t += "  Elefanti: solo Annibale, in una città militare.\n\n";

        t += "ESERCITI\n";
        t += "  Massimo " + ArmyService.MAX_ESERCITI + " eserciti. Un nuovo esercito costa 2, 4, 8, 16, 32 oro\n";
        t += "  (il costo raddoppia a ogni esercito posseduto) e nasce in una tua città senza\n";
        t += "  un tuo esercito, con 3 fanterie e 1 cavalleria.\n";
        t += "  Nel primo turno di ogni giocatore non si può attaccare.\n\n";

        t += "ENTRATE E RIBELLIONI\n";
        t += "  Ogni turno (dal secondo): 5 oro + i bonus delle città commerciali e agricole.\n";
        t += "  Rischio di ribellione a inizio turno: 35% nessuna città con guarnigione,\n";
        t += "  20% tutte le città sono accampamenti, 25% nessuna città militare,\n";
        t += "  20% leader nemico vicino (Giovanna d'Arco).\n";
        t += "  Con la ribellione non si guadagna oro e non si recluta. Si risolve portando un\n";
        t += "  esercito di almeno 4 unità in una tua città; dopo 2 turni si perde 1 oro a turno\n";
        t += "  e dopo 7 turni si perde la partita.\n\n";

        t += "SENZA CITTA'\n";
        t += "  Se perdi l'ultima città ma hai ancora un esercito, resti in gioco e puoi fondare\n";
        t += "  una nuova città (con il nome che vuoi) nel nodo senza città dove si trova l'esercito.\n\n";


        t += "LEADER (+ passiva, - debolezza)\n";
        for (LeaderType l : LeaderType.values()) {
            t += "\n" + nomeLeader(l) + " - " + testoDifficolta(l) + "\n" + abilita(l) + "\n";
        }
        return t;
    }
}
