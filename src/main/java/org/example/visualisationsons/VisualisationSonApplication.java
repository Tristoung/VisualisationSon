package org.example.visualisationsons;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;

public class VisualisationSonApplication extends Application {
    private ImageView imageView;
    private Button loadButton;
    private Image inputImage;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

    // instancier tous les elements javafx et ajouter des evenements au clic de l'image
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
        primaryStage.setTitle("Lego Simple");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // evenement au clic du bouton : ouvre un explorateur de fichier pour ajouter une image
    private void handleLoadButtonAction(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                processImage(selectedFile);

            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }

    private Image processImage (File inputFile) {
        String imagePath = inputFile.getAbsolutePath();
        Mat inputMat = Imgcodecs.imread(imagePath);
        // Redimmensionner en 64x64
        Imgproc.resize(inputMat, inputMat, new Size(64, 64));

        // Convertir en gris
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        // Réduire la résolution à 4 bits (16 niveaux de gris)
        for (int i = 0; i < grayMat.rows(); i++) {
            for (int j = 0; j < grayMat.cols(); j++) {
                double[] pixel = grayMat.get(i, j);
                if (pixel != null) {
                    // Réduction à 4 bits : 0 à 255 -> 0 à 15
                    int reducedValue = (int) (pixel[0] / 16) * 16; // On divise par 16 puis on multiplie pour revenir à un niveau discret
                    grayMat.put(i, j, reducedValue);
                }
            }
        }

        inputImage = matToImage(grayMat);
        imageView.setImage(inputImage);

        return inputImage;
    }


    // Convertir l'image JavaFX en matrice OpenCV (Mat)
    private Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];
        image.getPixelReader().getPixels(0, 0, width, height, javafx.scene.image.PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);
        return mat;
    }

    // Convertir une matrice OpenCV en image JavaFX
    private Image matToImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

}