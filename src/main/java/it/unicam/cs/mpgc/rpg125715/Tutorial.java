package it.unicam.cs.mpgc.rpg125715;

import java.util.List;

/**
 * Testi del tutorial: una pagina per argomento, nell'ordine in cui serve capire il gioco.
 * I numeri (costi, limiti, punti) sono letti dalle costanti del gioco, così il tutorial resta
 * corretto anche se si cambiano i valori.
 */
public final class Tutorial {

    public record Pagina(String titolo, String testo) {}

    private Tutorial() {}

    public static List<Pagina> pagine() {
        return List.of(
                new Pagina("1. Di cosa si tratta",
                        "Conquest Game è un gioco di strategia a turni: ogni giocatore guida un leader storico "
                                + "e tutti gli 8 leader partecipano sempre (quelli che non sono controllati da una persona li gioca il computer).\n\n"
                                + "Ogni leader parte con 3-5 città e un esercito nella sua capitale. Si fa crescere l'impero "
                                + "costruendo, reclutando, muovendo gli eserciti e conquistando le città degli altri.\n\n"
                                + "Il gioco ha due modalità di vittoria, che scegli prima di iniziare (vedi l'ultima pagina)."),

                new Pagina("2. La mappa",
                        "La mappa è fatta di nodi collegati da linee: un esercito può spostarsi solo su un nodo collegato a quello dove si trova.\n\n"
                                + "- Un nodo con una città ha un nome e un livello; il colore indica il proprietario.\n"
                                + "- Clicca un nodo per selezionarlo: a destra, sotto 'Selezione', vedi cosa contiene.\n"
                                + "- Ctrl + rotella del mouse: zoom. Trascinando con il mouse ti sposti sulla mappa.\n"
                                + "- Il pulsante 'Legenda' in alto riassume costi, bonus e regole in ogni momento."),

                new Pagina("3. Il tuo turno",
                        "Quando tocca a te puoi fare, in qualsiasi ordine:\n"
                                + "  • muovere i tuoi eserciti,\n"
                                + "  • reclutare unità, migliorare e specializzare le città,\n"
                                + "  • creare nuovi eserciti.\n\n"
                                + "Poi premi 'Fine turno': giocano gli altri e tornerai a giocare. "
                                + "Un 'round' è un giro completo, cioè quando hanno giocato tutti.\n\n"
                                + "Il tuo primo turno serve a prepararti: non si può attaccare. "
                                + "Dal secondo turno ricevi ogni volta 5 oro più i bonus delle città commerciali e agricole.\n"
                                + "Con 'Annulla spostamento' puoi tornare indietro sull'ultima mossa."),

                new Pagina("4. Eserciti e movimento",
                        "Per muovere un esercito: clicca un tuo esercito, poi un nodo evidenziato. "
                                + "Verde = nodo libero, rosso = nodo con un nemico.\n\n"
                                + "- Ogni esercito si sposta di un nodo per turno.\n"
                                + "- In un nodo possono stare al massimo due eserciti, di giocatori diversi.\n"
                                + "- Entrare in una città senza difensori la conquista.\n"
                                + "- Hai al massimo " + ArmyService.MAX_ESERCITI + " eserciti. Un nuovo esercito (pulsante 'Crea esercito') "
                                + "si crea in una tua città libera e costa 2, 4, 8, 16... oro: il prezzo raddoppia a ogni esercito che hai già."),

                new Pagina("5. Le battaglie",
                        "Se entri in un nodo con un esercito nemico c'è battaglia.\n\n"
                                + "Totale = (attacco o difesa delle unità + bonus) x dado.\n"
                                + "Il dado (1-6) è un moltiplicatore tra x0.8 e x1.3. Vince il totale più alto; a pari vince chi difende.\n\n"
                                + "Bonus: le città danno bonus alla difesa (Avamposto +1, Città +2, Metropoli +4, specializzazione militare +2); "
                                + "le macchine d'assedio attaccano meglio le città; ogni leader ha bonus e malus propri.\n\n"
                                + "Perdite: chi perde lascia il 40% delle unità, chi vince il 15%. "
                                + "L'esercito sconfitto si ritira; se non può, viene distrutto e ricompare in una città libera con una sola fanteria."),

                new Pagina("6. Città, livelli e specializzazioni",
                        "Ogni città ha un livello: Accampamento -> Avamposto -> Città -> Metropoli.\n"
                                + "Costo del miglioramento: 3, 4 e 6 oro. Un accampamento non produce e non può reclutare.\n\n"
                                + "Ogni città può avere una specializzazione (" + ConstructionService.COSTO_SPECIALIZZAZIONE + " oro):\n"
                                + "  • Militare: +2 difesa, unità meno care, protegge dal rischio di ribellione.\n"
                                + "  • Commerciale: più oro a ogni turno.\n"
                                + "  • Agricola: oro in più, un po' meno del commerciale.\n\n"
                                + "Per migliorare o specializzare: seleziona una tua città e usa i pulsanti a destra."),

                new Pagina("7. Reclutare",
                        "Si recluta in una tua città dove c'è un tuo esercito: seleziona la città, scegli l'unità e premi 'Recluta unità'.\n\n"
                                + "  • Fanteria 2 oro (2/2) e Cavalleria 3 oro (3/1): da Avamposto in su.\n"
                                + "  • Assedio 4 oro: forte contro città e metropoli.\n"
                                + "  • Unità speciale 6 oro (diversa per ogni leader): da Città in su.\n"
                                + "  • Elefanti: solo Annibale, in una città militare.\n\n"
                                + "Le città grandi e quelle militari fanno sconti. Il prezzo esatto compare accanto a ogni unità nel menu."),

                new Pagina("8. Oro e ribellioni",
                        "L'oro serve per tutto. Ogni turno ricevi 5 oro più i bonus delle città. Spendi con attenzione: "
                                + "senza oro non puoi reclutare né costruire.\n\n"
                                + "Le città possono ribellarsi se l'impero è poco stabile (città senza eserciti, nessuna città militare...). "
                                + "Sul pannello 'Ribellioni' vedi il rischio del turno dopo e le cause.\n"
                                + "Una città in ribellione non produce e non recluta: per calmarla serve un esercito di almeno 4 unità. "
                                + "Dopo 2 turni costa 1 oro a turno; dopo 4 la città diventa indipendente.\n\n"
                                + "Se perdi tutte le città ma hai ancora un esercito, resti in gioco e puoi fondare una nuova città ('Fonda città')."),

                new Pagina("9. Leader e bot",
                        "Ogni leader ha un'abilità e un punto debole (pannello 'Abilità' a destra, e nella scelta del leader).\n\n"
                                + "I bot giocano con una strategia diversa per ogni leader. All'inizio ogni bot sceglie due nemici "
                                + "(uno dei più vicini e il secondo più vicino) e li attacca finché non sono sconfitti; "
                                + "poi segue la sua strategia abituale. Nel registro in basso vedi cosa fanno.\n\n"
                                + "Il pulsante 'Salta mosse dei bot' accelera i loro turni."),

                new Pagina("10. Come si vince",
                        "Scegli la modalità prima di iniziare la partita:\n\n"
                                + "CONQUISTA TOTALE - nessun limite di round: vince chi resta l'ultimo giocatore.\n\n"
                                + "A PUNTI - massimo " + TurnService.MAX_ROUND + " round. Alla fine vince chi ha più punti:\n"
                                + "  • Capitale iniziale di un leader che controlli: " + PunteggioService.PUNTI_CAPITALE + " punti ciascuna (sono 8 in tutto)\n"
                                + "  • Metropoli: " + PunteggioService.PUNTI_METROPOLI + "   |   Città: " + PunteggioService.PUNTI_CITTA
                                + "   |   Avamposto: " + PunteggioService.PUNTI_AVAMPOSTO + "   |   Accampamento: " + PunteggioService.PUNTI_ACCAMPAMENTO + "\n"
                                + "  • Ogni " + PunteggioService.BATTAGLIE_PER_PUNTO + " battaglie vinte: 1 punto\n"
                                + "  • Più oro di tutti: +" + PunteggioService.BONUS_PIU_ORO + "   |   Più città di tutti: +" + PunteggioService.BONUS_PIU_CITTA + "\n"
                                + "  • Ogni città in ribellione a fine partita: -" + PunteggioService.PENALITA_RIBELLIONE + "\n"
                                + "A pari punti vince chi ha più città, poi più oro. In questa modalità i punti provvisori sono visibili nel pannello dei giocatori. "
                                + "Se resta un solo giocatore la partita finisce subito anche a punti."),

                new Pagina("11. Salvare e consigli",
                        "Con 'Salva partita' (durante il tuo turno) salvi la partita in un file .rpg; con 'Carica partita' la riapri quando vuoi.\n\n"
                                +"Consigli per i primi turni:\n"
                                + "  1. Specializza la capitale (militare o commerciale) e migliorala ad Avamposto.\n"
                                + "  2. Recluta qualche unità prima di muoverti: gli eserciti piccoli perdono.\n"
                                + "  3. Non lasciare le tue città senza esercito vicino ai nemici.\n"
                                + "  4. Attacca solo se il tuo totale è chiaramente più alto di quello del difensore.\n"
                                + "  5. Se un bot è tuo nemico dichiarato, aspettati che arrivi presto: prepara la difesa.\n\n"
                                + "Buon divertimento!"));
    }
}