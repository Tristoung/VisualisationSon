module org.example.visualisationsons {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires opencv;
    requires java.desktop;
    requires javafx.media;

    opens org.example.visualisationsons to javafx.fxml;
    exports org.example.visualisationsons;
    exports squelettes.javasound to javafx.graphics;
}