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
import java.util.ArrayList;
import java.util.List;

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
        File selectedFile = fileChooser.showOpenDialog(stage);      // selectedFile = path vers l'image choisie

        if (selectedFile != null) {
            try {
                inputImage = processImage(selectedFile);
                imageView.setImage(inputImage);

            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }

    private Image processImage (File inputFile) {
        // fonction qui prend en argument un path vers une image et qui renvoie une image javafx modifiée correctement

        String imagePath = inputFile.getAbsolutePath();
        Mat inputMat = Imgcodecs.imread(imagePath);
        // Redimmensionner en 64x64
        Imgproc.resize(inputMat, inputMat, new Size(64, 64));

        // Convertir en gris
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        // Réduire la résolution à 4 bits (16 niveaux de gris)
        for (int i = 0; i < grayMat.rows(); i++) {
            for (int j = 0; j < grayMat.cols(); j++) {              // on parcourt la matrice de pixels
                double[] pixel = grayMat.get(i, j);                 // pour chaque pixel (pixel est un tableau de double contenant qu'une seule valeur)
                if (pixel != null) {
                    // Réduction à 4 bits : 0 à 255 -> 0 à 15
                    int reducedValue = (int) (pixel[0] / 16) * 16;  // On divise par 16 puis on multiplie pour revenir à un nombre allant de 0 à 255 qui soit divisible par 16
                    grayMat.put(i, j, reducedValue);
                }
            }
        }

        List<int[]> columnList = new ArrayList<>();             // on crée une liste de colonnes

        for (int col = 0; col < grayMat.cols(); col++) {        // pour chaque colonne
            int[] column = new int[grayMat.rows()];             // on crée une liste de pixel représentant une colonne
            for (int row = 0; row < grayMat.rows(); row++) {    // puis, pour chaque pixel de la colonne (pour chaque row de la colonne)
                double[] pixel = grayMat.get(row, col);         // on prend la valeur du pixel (valeur étant un multiple de 16 allant de 0 à 240)
                if (pixel != null) {                            // protection
                    int reducedValue = (int) (pixel[0] / 16);   // on récupère le niveau de gris allant de 0 à 15 en divisant la valeur du pixel par 16
                    column[grayMat.rows() - 1 - row] = reducedValue;       // on ajoute le niveau de gris au tableau (je remplis le tableau dans l'autre sens pour avoir une lecture de bas en haut de l'image)
                }
            }
            columnList.add(column);                             // on ajoute la colonne à la liste de colonnes
        }

        // DEBUG ça sert a rien
        for (int i = 0; i < columnList.size(); i++) {
            String temp = "";
            for (int j = 0; j < columnList.get(i).length; j++) {
                int n = columnList.get(i)[j];
                if (n < 10) {
                    temp += columnList.get(i)[j] + ", ";
                } else {
                    temp += columnList.get(i)[j] + ",";
                }
            }
            System.out.println(temp);
        }
        // FIN DEBUG qui sert à rien

        inputImage = matToImage(grayMat);       // reconvertir la matrice en image compréhensible par javafx

        return inputImage;
    }

// ########################### CONVERTISSEURS OPENCV <-> JAVAFX #####################################


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