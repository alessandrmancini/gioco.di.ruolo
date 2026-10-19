package it.unicam.cs.mpgc.rpg125715;

public enum VictoryMode {
    /** Si gioca al massimo 30 round, poi vince chi ha più punti. */
    PUNTI("A punti (massimo " + TurnService.MAX_ROUND + " round)",
            "La partita dura al massimo " + TurnService.MAX_ROUND + " round. Alla fine vince chi ha più punti "
                    + "(capitali, città sviluppate, battaglie vinte...). Se resta un solo giocatore vince lui subito."),
    /** Nessun limite di round: vince chi elimina tutti gli altri. */
    CONQUISTA("Conquista totale (nessun limite di round)",
            "Non c'è limite di round: la partita finisce solo quando resta un unico giocatore, che vince.");

    private final String nome;
    private final String descrizione;

    VictoryMode(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public String getNome() {return nome;}
    public String getDescrizione() {return descrizione;}
}