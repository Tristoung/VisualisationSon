package org.example.visualisationsons;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.File;

public class VisualisationSonApplication extends Application {
    private ImageView imageView;
    private Button loadButton;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

    // Instancier tous les éléments javafx et ajouter des événements au clic de l'image
    @Override
    public void start(Stage primaryStage) {
        imageView = new ImageView();
        loadButton = new Button("Charger une image");
        loadButton.setOnAction(event -> handleLoadButtonAction(primaryStage));

        imageView.setFitWidth(600);
        imageView.setFitHeight(600);
        imageView.setPreserveRatio(true);

        VBox root = new VBox(10, imageView, loadButton);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Visualisation Son");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Événement au clic du bouton : ouvre un explorateur de fichiers pour ajouter une image
    private void handleLoadButtonAction(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File selectedFile = fileChooser.showOpenDialog(stage); // selectedFile = path vers l'image choisie

        if (selectedFile != null) {
            try {
                Image inputImage = ImageProcessor.processImage(selectedFile);
                imageView.setImage(inputImage);

                new Thread(() -> {
                    ImageProcessor.makeSound(inputImage);
                }).start();
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }
}
