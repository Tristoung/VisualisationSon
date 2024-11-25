package org.example.visualisationsons;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.w3c.dom.ls.LSOutput;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}