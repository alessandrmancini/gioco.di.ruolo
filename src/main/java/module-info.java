module it.unicam.cs.mpgc.rpg125715 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens it.unicam.cs.mpgc.rpg125715 to javafx.fxml;
    exports it.unicam.cs.mpgc.rpg125715;
}