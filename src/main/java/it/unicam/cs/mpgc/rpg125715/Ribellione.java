package it.unicam.cs.mpgc.rpg125715;

public class Ribellione {
    private boolean attiva;
    private int turniAttiva;

    public Ribellione() {
        this.attiva = true;
        this.turniAttiva = 0;
    }

    public boolean isAttiva() {return attiva;}
    public int getTurniAttiva() {return turniAttiva;}

    public void setAttiva(){
        if(!attiva){
            attiva = true;
            turniAttiva = 0;
        }
    }
    public void nextTurn(){
        if(attiva){turniAttiva++;}
    }

    public void pacifica(){
        attiva = false;
        turniAttiva = 0;
    }

    public boolean applicaMalusOro(){
        return attiva && turniAttiva >= 2;
    }
}
