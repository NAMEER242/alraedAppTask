module n242.alraed.appTask {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires transitive java.desktop;
    requires jdk.crypto.ec;
    requires jdk.crypto.cryptoki;

    opens n242.alraed.appTask to javafx.fxml;
    exports n242.alraed.appTask;
    opens n242.alraed.appTask.secondaryView to javafx.fxml;
    exports n242.alraed.appTask.secondaryView;
}