package it.unicam.cs.mpgc.rpg125715;

import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * VIEW: disegna la mappa di gioco sopra l'immagine di sfondo (mappa_colorata.jpeg).
 * Le coordinate delle Location sono in pixel dell'immagine originale: la vista le scala
 * in base allo spazio disponibile (adattamento automatico) e allo zoom scelto dall'utente.
 * Non modifica il modello: notifica i click tramite il callback.
 */
public class MapView extends Pane {

    /** Immagine di sfondo, da mettere in src/main/resources/it/unicam/cs/mpgc/rpg125715/ */
    private static final String MAP_RESOURCE = "mappa_colorata.jpeg";
    private static final double DEFAULT_WIDTH = 1170;
    private static final double DEFAULT_HEIGHT = 642;
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 3.0;

    /** Dove scrivere il nome della città rispetto al nodo: L = sinistra, R = destra, A = sopra (default: sotto). */
    private static final Map<String, Character> LATO_NOME = Map.ofEntries(
            Map.entry("Madrid", 'L'),
            Map.entry("Tariff", 'L'),
            Map.entry("Rennes", 'L'),
            Map.entry("Dublino", 'L'),
            Map.entry("Cagliari", 'L'),
            Map.entry("Edimburgo", 'R'),
            Map.entry("Vienna", 'R'),
            Map.entry("Roma", 'R'),
            Map.entry("Stoccolma", 'R'),
            Map.entry("Amsterdam", 'A'),
            Map.entry("Copenaghen", 'A'),
            Map.entry("Pella", 'A')
    );

    private final Consumer<Location> onLocationClicked;
    private final Image background;
    private final double baseWidth;
    private final double baseHeight;
    private final DropShadow halo = new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 3, 0.9, 0, 0);

    private double fitScale = 0.8;
    private double zoom = 1.0;

    public MapView(Consumer<Location> onLocationClicked) {
        this.onLocationClicked = Objects.requireNonNull(onLocationClicked);
        getStyleClass().add("map-view");

        Image img = null;
        try (InputStream in = MapView.class.getResourceAsStream(MAP_RESOURCE)) {
            if (in != null) {
                img = new Image(in);
                if (img.isError()) {img = null;}
            }
        } catch (IOException e) {
            img = null;
        }
        this.background = img;
        this.baseWidth = img != null ? img.getWidth() : DEFAULT_WIDTH;
        this.baseHeight = img != null ? img.getHeight() : DEFAULT_HEIGHT;
        applySize();
    }

    // ================= COLORI =================

    /** Stessi colori delle regioni disegnate sull'immagine. */
    public static Color colorOf(LeaderType leader) {
        return switch (leader) {
            case ALESSANDRO_MAGNO -> Color.rgb(154, 217, 234);
            case ANNIBALE -> Color.rgb(254, 242, 0);
            case ATTILA -> Color.rgb(163, 73, 163);
            case GIULIO_CESARE -> Color.rgb(237, 27, 36);
            case REGINA_ELISABETTA -> Color.rgb(59, 124, 43);
            case GIOVANNA_D_ARCO -> Color.rgb(8, 161, 225);
            case FEDERICO_BARBAROSSA -> Color.rgb(200, 254, 34);
            case QIN_SHI_HUANG -> Color.rgb(220, 145, 140);
        };
    }

    // ================= DIMENSIONI E ZOOM =================

    /** Adatta la mappa allo spazio disponibile (da chiamare quando cambia la dimensione del ScrollPane). */
    public void setViewport(double width, double height) {
        if (width <= 0 || height <= 0) {return;}
        fitScale = Math.max(0.3, Math.min((width - 2) / baseWidth, (height - 2) / baseHeight));
        applySize();
    }

    /** Moltiplica lo zoom corrente (1.0 = mappa intera visibile). */
    public void zoomBy(double factor) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * factor));
        applySize();
    }

    private double scale() {return fitScale * zoom;}

    private void applySize() {
        double w = baseWidth * scale();
        double h = baseHeight * scale();
        setMinSize(w, h);
        setPrefSize(w, h);
        setMaxSize(w, h);
    }

    // ================= DISEGNO =================

    public void render(Game game, Location selected, Army selectedArmy, Set<Location> targets) {
        getChildren().clear();
        double s = scale();

        if (background != null) {
            ImageView sfondo = new ImageView(background);
            sfondo.setFitWidth(baseWidth * s);
            sfondo.setFitHeight(baseHeight * s);
            sfondo.setPreserveRatio(false);
            sfondo.setSmooth(true);
            sfondo.setMouseTransparent(true);
            getChildren().add(sfondo);
        }
        if (game == null) {return;}

        Player viewer = game.getCurrentPlayer();

        // collegamenti: ombra scura + tratteggio chiaro, leggibili su qualsiasi colore
        for (Location l : game.getLocations()) {
            for (Location n : l.getAdiacenti()) {
                if (l.getId() < n.getId()) {
                    Line ombra = new Line(px(l), py(l), px(n), py(n));
                    ombra.setStroke(Color.rgb(15, 20, 35, 0.55));
                    ombra.setStrokeWidth(4);
                    ombra.setMouseTransparent(true);

                    Line linea = new Line(px(l), py(l), px(n), py(n));
                    linea.setStroke(Color.WHITE);
                    linea.setStrokeWidth(1.6);
                    linea.getStrokeDashArray().addAll(6.0, 4.0);
                    linea.setMouseTransparent(true);
                    getChildren().addAll(ombra, linea);
                }
            }
        }
        for (Location l : game.getLocations()) {
            getChildren().add(createLocationNode(l, viewer, selected, selectedArmy, targets));
        }
    }

    private Group createLocationNode(Location l, Player viewer, Location selected, Army selectedArmy, Set<Location> targets) {
        double k = sizeFactor();
        double cx = px(l);
        double cy = py(l);
        double radius = radiusOf(l) * k;
        Group g = new Group();
        g.setCursor(Cursor.HAND);

        // destinazioni possibili del movimento
        if (targets.contains(l)) {
            Circle anello = new Circle(cx, cy, radius + 8);
            anello.setFill(Color.TRANSPARENT);
            anello.setStroke(isHostile(l, viewer) ? Color.TOMATO : Color.LIMEGREEN);
            anello.setStrokeWidth(3);
            anello.getStrokeDashArray().addAll(6.0, 4.0);
            g.getChildren().add(anello);
        }

        // nodo (colore del proprietario) con anello esterno
        Color fill = Color.web("#8b93a5");
        if (l.hasCity() && l.getCity().getOwner() != null) {
            fill = colorOf(l.getCity().getOwner().getLeader());
        }
        boolean selezionato = (l == selected);
        Circle anelloEsterno = new Circle(cx, cy, radius + 2.5);
        anelloEsterno.setFill(Color.TRANSPARENT);
        anelloEsterno.setStroke(selezionato ? Color.GOLD : Color.WHITE);
        anelloEsterno.setStrokeWidth(selezionato ? 3.5 : 1.5);
        Circle nodo = new Circle(cx, cy, radius);
        nodo.setFill(fill);
        nodo.setStroke(Color.web("#10162a"));
        nodo.setStrokeWidth(2.5);
        g.getChildren().addAll(anelloEsterno, nodo);

        char lato = 'B';
        if (l.hasCity()) {
            City c = l.getCity();
            g.getChildren().add(testo(String.valueOf(levelNumber(c)), cx, cy, 12 * k, textOn(fill), true, false));

            boolean capitale = c.getOwner() != null && c.getOwner().getCapitale() == c;
            lato = LATO_NOME.getOrDefault(c.getName(), 'B');
            String etichetta = c.getName() + (capitale ? " ★" : "");
            Text nome = testo(etichetta, 0, 0, Math.max(10, 11 * k), Color.WHITE, true, true);
            double w = nome.getLayoutBounds().getWidth();
            double tx;
            double ty;
            switch (lato) {
                case 'L' -> {tx = cx - radius - 5 - w / 2; ty = cy;}
                case 'R' -> {tx = cx + radius + 5 + w / 2; ty = cy;}
                case 'A' -> {tx = cx; ty = cy - radius - 9;}
                default -> {tx = cx; ty = cy + radius + 10;}
            }
            nome.setX(tx - w / 2);
            nome.setY(ty);
            g.getChildren().add(nome);
        }

        // badge degli eserciti: sopra il nodo (sotto se il nome è sopra)
        int n = l.numeroArmy();
        double badgeW = 26;
        double badgeH = 16;
        double gap = 3;
        double start = cx - (n * badgeW + (n - 1) * gap) / 2;
        double by = (lato == 'A') ? cy + radius + 4 : cy - radius - badgeH - 5;
        int slot = 0;
        for (Army a : l.getArmies()) {
            if (a == null) {continue;}
            double bx = start + slot * (badgeW + gap);
            Rectangle r = new Rectangle(bx, by, badgeW, badgeH);
            r.setArcWidth(8);
            r.setArcHeight(8);
            Color colore = colorOf(a.getOwner().getLeader());
            r.setFill(colore);
            if (a == selectedArmy) {
                r.setStroke(Color.GOLD);
                r.setStrokeWidth(3);
            } else {
                r.setStroke(a.isPuoMuovere() ? Color.WHITE : Color.web("#4b5563"));
                r.setStrokeWidth(1.5);
            }
            g.getChildren().add(r);
            g.getChildren().add(testo(String.valueOf(a.getNumeroUnita()), bx + badgeW / 2, by + badgeH / 2, 11, textOn(colore), true, false));
            slot++;
        }

        Tooltip.install(g, new Tooltip(descrizione(l)));
        g.setOnMouseClicked(e -> onLocationClicked.accept(l));
        return g;
    }

    public static String bello(String s){
        if(s == null || s.isBlank()){return "";}
        return s.charAt(0)+ s.substring(1).toLowerCase().replace('_',' ');
    }
    public static String descrizione(Location l) {
        if(l == null){return "Nessuna selezione";}

        String testo = "";
        if(l.hasCity()){
            City c = l.getCity();
            Player proprietario = c.getOwner();
            testo+=c.getName();
            if(proprietario!=null && proprietario.getCapitale() == c){
                testo+=" (capitale)";
            }
            testo+= "\nLivello: "+ bello(c.getLevel().name());
            testo+= "\nSpecializzazione: "+ bello(c.getSpecialization().name());
            testo+= "\nProprietario: " + (proprietario != null ? proprietario.getName() : "nessuno");
        }
        else {
            testo += "Territorio libero (posizione "+l.getId()+")";
        }
        for(Army a : l.getArmies()){
            if(a == null){continue;}
            testo += "\n\nEsercito di "+a.getOwner().getName() + " - "+ a.getNumeroUnita()+ " unità";
            for(UnitType tipo : UnitType.values()){
                int quante = a.getNumeroUnitaPerTipo(tipo);
                if(quante > 0){testo += "\n "+ bello(tipo.name())+ " x"+ quante;}
            }
            if(!a.isPuoMuovere()){testo += "\n(ha già mosso)";}
        }
        return testo;
    }

    // ================= SUPPORTO =================

    private boolean isHostile(Location l, Player viewer) {
        if (l.hasEnemyArmiesFor(viewer)) {return true;}
        return l.hasCity() && l.getCity().getOwner() != null && l.getCity().getOwner() != viewer;
    }

    /** Raggio base del nodo in base al livello della città (i nodi senza città sono piccoli). */
    private double radiusOf(Location l) {
        if (!l.hasCity()) {return 5;}
        return switch (l.getCity().getLevel()) {
            case ACCAMPAMENTO -> 11;
            case AVAMPOSTO -> 13;
            case CITTA -> 15;
            case METROPOLI -> 18;
        };
    }

    private int levelNumber(City c) {
        return switch (c.getLevel()) {
            case ACCAMPAMENTO -> 0;
            case AVAMPOSTO -> 1;
            case CITTA -> 2;
            case METROPOLI -> 3;
        };
    }

    /** I nodi crescono un po' con lo zoom, ma non in proporzione diretta. */
    private double sizeFactor() {
        return Math.max(0.85, Math.min(1.6, 0.6 + scale() * 0.5));
    }

    /** Nero su colori chiari, bianco su colori scuri. */
    private Color textOn(Color sfondo) {
        double luminanza = 0.299 * sfondo.getRed() + 0.587 * sfondo.getGreen() + 0.114 * sfondo.getBlue();
        return luminanza > 0.55 ? Color.BLACK : Color.WHITE;
    }

    /** Crea un testo centrato in (cx, cy); con haloEffect aggiunge un contorno scuro per leggerlo sulla mappa. */
    private Text testo(String s, double cx, double cy, double size, Color color, boolean bold, boolean haloEffect) {
        Text t = new Text(s);
        t.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        t.setFill(color);
        t.setTextOrigin(VPos.CENTER);
        if (haloEffect) {t.setEffect(halo);}
        t.setX(cx - t.getLayoutBounds().getWidth() / 2);
        t.setY(cy);
        return t;
    }

    private double px(Location l) {return l.getX() * scale();}
    private double py(Location l) {return l.getY() * scale();}
}