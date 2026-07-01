package it.unicam.cs.mpgc.rpg125715;

public class Ribellione {
    private boolean attiva;
    private int turniAttiva;
    private boolean pacificazione;

    public Ribellione() {
        this.attiva = true;
        this.turniAttiva = 0;
        pacificazione = false;
    }

    public boolean isAttiva() {return attiva;}
    public int getTurniAttiva() {return turniAttiva;}

    //PACIFICAZIONE (NECESSARIO UN TURNO)
    public boolean isPacificazioneProgrammata(){return pacificazione;}
    public void programmaPacificazione(){
        if(attiva){
            pacificazione = true;
        }
    }
    public void annullaPacificazione(){pacificazione = false;}

    public void setAttiva(){
        if(!attiva){
            attiva = true;
            turniAttiva = 0;
            pacificazione = false;
        }
    }
    public void nextTurn(){
        if(attiva){turniAttiva++;}
    }

    public void pacifica(){
        attiva = false;
        turniAttiva = 0;
    }

    public boolean applicaMalusOro(){return attiva && turniAttiva >= 2;}
}
