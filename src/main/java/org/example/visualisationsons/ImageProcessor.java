package org.example.visualisationsons;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {

    public static Image processImage(File inputFile) {
        // Fonction qui prend en argument un path vers une image et qui renvoie une image javafx modifiée correctement
        String imagePath = inputFile.getAbsolutePath();
        Mat inputMat = Imgcodecs.imread(imagePath);

        // Redimensionner en 64x64
        Imgproc.resize(inputMat, inputMat, new Size(64, 64));

        // Convertir en niveaux de gris
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        // Réduire la résolution à 4 bits (16 niveaux de gris)
        reduceGrayScaleResolution(grayMat);

        // Extraire les colonnes
        List<int[]> columnList = extractColumnData(grayMat);

        // DEBUG ça sert à rien
        debugColumnData(columnList);

        // Reconversion en image compréhensible par JavaFX
        return Utils.matToImage(grayMat);
    }

    private static void reduceGrayScaleResolution(Mat grayMat) {
        for (int i = 0; i < grayMat.rows(); i++) {
            for (int j = 0; j < grayMat.cols(); j++) {                  // On parcourt la matrice de pixels
                double[] pixel = grayMat.get(i, j);                     // Pour chaque pixel (pixel est un tableau de double contenant une seule valeur)
                if (pixel != null) {
                    // Réduction à 4 bits : 0 à 255 -> 0 à 15
                    int reducedValue = (int) (pixel[0] / 16) * 16;      // On divise par 16 puis on remultiplie pour revenir à un nombre divisible par 16
                    grayMat.put(i, j, reducedValue);
                }
            }
        }
    }

    private static List<int[]> extractColumnData(Mat grayMat) {
        List<int[]> columnList = new ArrayList<>();                     // On crée une liste de colonnes

        for (int col = 0; col < grayMat.cols(); col++) {                // Pour chaque colonne
            int[] column = new int[grayMat.rows()];                     // On crée une liste de pixels représentant une colonne
            for (int row = 0; row < grayMat.rows(); row++) {            // Puis, pour chaque pixel de la colonne (pour chaque row de la colonne)
                double[] pixel = grayMat.get(row, col);                 // On prend la valeur du pixel
                if (pixel != null) { // Protection
                    int reducedValue = (int) (pixel[0] / 16);           // On récupère le niveau de gris allant de 0 à 15
                    column[grayMat.rows() - 1 - row] = reducedValue;    // Remplissage inverse pour lecture bas-haut
                }
            }
            columnList.add(column); // On ajoute la colonne à la liste de colonnes
        }

        return columnList;
    }

    private static void debugColumnData(List<int[]> columnList) {
        for (int[] ints : columnList) {
            StringBuilder temp = new StringBuilder();
            for (int value : ints) {
                temp.append(value < 10 ? value + ", " : value + ",");
            }
            System.out.println(temp);
        }
    }
}
