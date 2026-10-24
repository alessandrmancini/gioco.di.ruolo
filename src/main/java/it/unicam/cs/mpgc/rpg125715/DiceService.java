package it.unicam.cs.mpgc.rpg125715;


import java.util.Random;

public class DiceService implements DiceRoller{
    private final Random random;

    public DiceService() {
        this(new Random());
    }

    public DiceService(Random random) {
        if(random == null){throw new IllegalArgumentException("Random cannot be null.");}
        this.random = new Random();
    }

    @Override
    public double lanciaDado(Player p){
        if(p == null){throw new IllegalArgumentException("il player non può essere null");}

        LeaderType leader = p.getLeader();
        if(leader.maggioriProbabilitaDadi()){
            return switch (random.nextInt(6)+1){
                case 1,2 -> 1.0;
                case 3 -> 1.1;
                case 4 -> 1.2;
                case 5 -> 1.3;
                case 6 -> 1.5;
                default -> 0;
            };
        }
        else if(leader.minoriProbabilitaDadi()){
            return switch (random.nextInt(6)+1){
              case 1,2 -> 0.8;
              case 3,4 -> 1.0;
              case 5,6 -> 1.1;
              default -> 0;
            };
        }
        else{
            return switch (random.nextInt(6)+1){
                case 1 -> 0.8;
                case 2 -> 0.9;
                case 3 -> 1.0;
                case 4 -> 1.1;
                case 5 -> 1.2;
                case 6 -> 1.3;
                default -> 0;
            };
        }

    }

}
