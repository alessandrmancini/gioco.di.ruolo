package it.unicam.cs.mpgc.rpg125715;

public class TurnService {

    public static final int MAX_ROUND = 30;
    private static final int ORO_BASE = 5;
    private final RibellioneService ribellioneService;

    public TurnService(RibellioneService ribellioneService) {
        if(ribellioneService == null) {throw new IllegalArgumentException("ribellioneService null");}
        this.ribellioneService = ribellioneService;
    }


    public  void fineTurno(Game game){
        validaGame(game);
        if(game.isGameOver()){return;}
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}

        Player finito = game.getCurrentPlayer();
        liberaCittaPerse(finito, game);
        finito.incrementaTurniGiocati();
        game.incrementaNumeroTurno();

        prossimoPlayer(game);
        if(game.haSoloUnPlayerAttivo()){game.setFinita();return;}

        Player corrente = game.getCurrentPlayer();
        sbloccaEsercitiPlayer(corrente);
        aggiornaRibellioni(corrente, game);
        assegnaOroTurno(corrente, game);

        if(game.haSoloUnPlayerAttivo()){game.setFinita();}
    }

    private void validaGame(Game game){
        if(game == null){throw new IllegalArgumentException("Game is null");}
        if(!game.isIniziato()){throw new IllegalArgumentException("Game never started");}
    }
    private void prossimoPlayer(Game game){
        game.nextPlayer();
    }
    private void sbloccaEsercitiPlayer(Player player){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        for(Army a: player.getEserciti()){
            a.sbloccaMovimento();
        }
    }

    //RIBELLIONI
    private void aggiornaRibellioni(Player player, Game game){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        if(game == null){throw new IllegalArgumentException("Game is null");}

        ribellioneService.aggiornaRibellioni(player, game);
        if(player.isSconfitto() && game.haSoloUnPlayerAttivo()){game.setFinita();}
    }

    private void liberaCittaPerse(Player finito, Game game){
        for(Location l : game.getLocations()){
            if(l.hasCity() && l.getCity().getExProprietario() == finito){
                l.getCity().setExProprietario(null);
            }
        }
    }

    //ORO
    private void assegnaOroTurno(Player player, Game game){
        if(player == null){throw new IllegalArgumentException("Player is null");}
        if(game == null){throw new IllegalArgumentException("Game is null");}

        if(game.getNumeroTurno() <= game.getPlayers().size()){return;}

        int oro = ORO_BASE;
        int cittaConMalus = 0;
        for(City city : player.getTerritorio().getCities()){
            if(city.hasRibellione()){
                if(city.getRibellione().applicaMalusOro()){cittaConMalus++;}
                continue;
            }
            oro += bonusOroCittaConLeader(city, player.getLeader());
        }
        player.aggiungiOro(oro);
        player.spendiOro(Math.min(cittaConMalus, player.getOro()));
    }

    private int bonusOroCities(Player player){
        int bonus = 0;
        for(City city : player.getTerritorio().getCities()){
            bonus += bonusOroCittaConLeader(city, player.getLeader());
        }
        return bonus;
    }

    public static int bonusOroCitta(City city){
        return switch (city.getSpecialization()){
            case COMMERCIALE -> switch (city.getLevel()){
                case ACCAMPAMENTO -> 1;
                case AVAMPOSTO, CITTA -> 2;
                case METROPOLI -> 3;
            };
            case AGRICOLA -> switch (city.getLevel()){
                case ACCAMPAMENTO, AVAMPOSTO -> 1;
                case CITTA, METROPOLI -> 2;
            };
            default -> 0;
        };
    }
    public static int bonusOroCittaConLeader(City city, LeaderType leader){
        int bonus = bonusOroCitta(city);
        if(city.getSpecialization() == CitySpecialization.COMMERCIALE){
            bonus = Math.max(0, bonus + leader.getBonusCommercio());
        }
        return bonus;
    }

}
