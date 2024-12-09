package org.example.visualisationsons;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

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

        // Précalcul des sinusoidales sans pondération
        double[][] precalc_sin_tab = calculTableauSinusoidales();

        // Extraire les colonnes
        int[][] columnTab = extractColumnData(grayMat);

        double[][] periode_tab = new double[64][689];

        double[][] tab_final = new double[64][689];

        for (int col = 0; col < columnTab.length; col++) {
            for (int row = 0; row < columnTab[col].length; row++) {
                double[] precalc_i = precalc_sin_tab[row];
                int gl = columnTab[col][row];

                // Multiplier chaque élément de precalc_i par gl
                double[] result = new double[precalc_i.length];
                for (int k = 0; k < precalc_i.length; k++) {
                    result[k] = precalc_i[k] * gl;
                }

                // Stocker le résultat dans periode_tab
                periode_tab[col] = result;
            }
            // Initialisation du tableau final
            double[] tab_pour_un_buffer = new double[periode_tab[0].length]; // Longueur basée sur un tableau individuel

            // Additionner chaque tableau dans periode_tab
            for (double[] currentArray : periode_tab) {
                for (int i = 0; i < currentArray.length; i++) {
                    tab_pour_un_buffer[i] += currentArray[i]; // Addition position par position
                }
            }

            tab_final[col] = tab_pour_un_buffer;
        }

        for (double[] column : tab_final) { // Pour chaque colonne dans le tableau 2D
            StringBuilder temp = new StringBuilder();
            for (double value : column) { // Pour chaque valeur dans la colonne
                temp.append(value + ",");
            }
            System.out.println(temp); // Affiche la représentation de la colonne
        }
        SinusoidalSoundGenerator.playSoundFromFinalTab(tab_final);



        // DEBUG ça sert à rien
//        debugColumnData(columnTab);

        // Reconversion en image compréhensible par JavaFX
        return Utils.matToImage(grayMat);
    }

    public static double[][] calculTableauSinusoidales() {
        int rows = 64; // Nombre d'ondes sinusoïdales
        final int F_ECH = 44100; // Échantillons par seconde
        double fMin = 300; // Fréquence minimale (en Hz)
        double fMax = 3000; // Fréquence maximale (en Hz)

        // Tableau des fréquences (f0 à f63)
        double[] f_tab = new double[rows];
        for (int i = 0; i < rows; i++) {
            f_tab[i] = fMin + i * (fMax - fMin) / (rows - 1);
        }

        // Taille d'un tableau correspondant à une période (1/64e de seconde)
        int samplesPerPeriod = F_ECH / rows;

        // Tableau 2D pour stocker les ondes sinusoïdales
        double[][] sinusoidales_selon_frequence = new double[rows][samplesPerPeriod];

        // Générer les ondes sinusoïdales
        for (int i = 0; i < rows; i++) {
            double frequency = f_tab[i]; // Fréquence pour cette onde

            // Créer une seule période de l'onde sinusoïdale
            for (int j = 0; j < samplesPerPeriod; j++) {
                double t = (double) j / F_ECH; // Temps correspondant pour l'échantillon
                sinusoidales_selon_frequence[i][j] = Math.sin(2 * Math.PI * frequency * t); // Calcul de l'onde sinusoïdale
            }
        }

        // Retourner le tableau 2D
        return sinusoidales_selon_frequence;
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

    private static int[][] extractColumnData(Mat grayMat) {
        // Initialiser un tableau 2D pour stocker les données de chaque colonne
        int[][] columnArray = new int[grayMat.cols()][grayMat.rows()];

        for (int col = 0; col < grayMat.cols(); col++) {                        // Pour chaque colonne
            for (int row = 0; row < grayMat.rows(); row++) {                    // Pour chaque pixel de la colonne (par ligne)
                double[] pixel = grayMat.get(row, col);                         // Récupère la valeur du pixel
                if (pixel != null) {                                            // Protection pour éviter les valeurs nulles
                    int reducedValue = (int) (pixel[0] / 16);                   // Réduction du niveau de gris à une valeur entre 0 et 15
                    columnArray[col][grayMat.rows() - 1 - row] = reducedValue;  // Remplissage inverse pour lecture bas-haut
                }
            }
        }

        return columnArray; // Retourne le tableau 2D
    }


    private static void debugColumnData(int[][] columnArray) {
        for (int[] column : columnArray) { // Pour chaque colonne dans le tableau 2D
            StringBuilder temp = new StringBuilder();
            for (int value : column) { // Pour chaque valeur dans la colonne
                temp.append(value < 10 ? value + ", " : value + ",");
            }
            System.out.println(temp); // Affiche la représentation de la colonne
        }
    }

}
