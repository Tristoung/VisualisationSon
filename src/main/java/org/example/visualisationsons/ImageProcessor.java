package org.example.visualisationsons;

import javafx.scene.image.Image;
import jdk.jshell.execution.Util;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class ImageProcessor {

    /**
     * Process an input image file to generate a sound and return a modified grayscale image.
     */
    public static Image processImage(File inputFile) {
        String imagePath = inputFile.getAbsolutePath();
        Mat inputMat = Imgcodecs.imread(imagePath);

        // Resize to 64x64 and convert to grayscale
        Mat grayMat = preprocessImage(inputMat);

        // Reduce grayscale resolution to 4 bits (16 levels)
        reduceGrayScaleResolution(grayMat);

        // Return the processed image for visualization
        return Utils.matToImage(grayMat);
    }

    public static void makeSound(Image inputImage) {
        Mat inputMat = Utils.imageToMat(inputImage);

        // Precompute sinusoidal waves
        double[][] precalculatedSinusoids = calculateSinusoidalTable();

        // Extract columns of reduced grayscale values
        int[][] grayscaleColumns = extractColumnData(inputMat);

        // Generate the final audio data table
        double[][] audioData = generateAudioData(grayscaleColumns, precalculatedSinusoids);

        // Play the generated sound
        SinusoidalSoundGenerator.saveToWavFile(audioData, "sound.wav");
    }

    /**
     * Resize the image to 64x64 and convert to grayscale.
     */
    private static Mat preprocessImage(Mat inputMat) {
        Imgproc.resize(inputMat, inputMat, new Size(64, 64));
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        return grayMat;
    }

    /**
     * Reduce the grayscale resolution to 4 bits (16 levels).
     */
    private static void reduceGrayScaleResolution(Mat grayMat) {
        for (int i = 0; i < grayMat.rows(); i++) {
            for (int j = 0; j < grayMat.cols(); j++) {
                double[] pixel = grayMat.get(i, j);
                if (pixel != null) {
                    int reducedValue = (int) (pixel[0] / 16) * 16;
                    grayMat.put(i, j, reducedValue);
                }
            }
        }
    }

    /**
     * Generate the final audio data table by mixing the sinusoidal waves
     * based on the grayscale column data.
     */
    private static double[][] generateAudioData(int[][] columns, double[][] precalculatedSinusoids) {
        int numColumns = columns.length;
        int waveLength = precalculatedSinusoids[0].length;

        double[][] audioData = new double[numColumns][waveLength];

        for (int col = 0; col < numColumns; col++) {
            double[] cumulativeWave = new double[waveLength];

            for (int row = 0; row < columns[col].length; row++) {
                int grayLevel = columns[col][row];
                double[] sinusoid = precalculatedSinusoids[row];

                for (int k = 0; k < waveLength; k++) {
                    cumulativeWave[k] += sinusoid[k] * grayLevel;
                }
            }
            audioData[col] = cumulativeWave;
        }

        return audioData;
    }

    /**
     * Precompute sinusoidal waves for all grayscale levels and frequencies.
     */
    public static double[][] calculateSinusoidalTable() {
        int rows = 64; // Number of sinusoidal waves
        final int SAMPLE_RATE = 44100; // Samples per second
        double fMin = 300; // Minimum frequency (Hz)
        double fMax = 3000; // Maximum frequency (Hz)

        double[] frequencies = new double[rows];
        for (int i = 0; i < rows; i++) {
            frequencies[i] = fMin + i * (fMax - fMin) / (rows - 1);
        }

        int samplesPerPeriod = SAMPLE_RATE / rows;
        double[][] sinusoidTable = new double[rows][samplesPerPeriod];

        for (int i = 0; i < rows; i++) {
            double frequency = frequencies[i];
            for (int j = 0; j < samplesPerPeriod; j++) {
                double t = (double) j / SAMPLE_RATE;
                sinusoidTable[i][j] = Math.sin(2 * Math.PI * frequency * t);
            }
        }

        return sinusoidTable;
    }

    /**
     * Extract grayscale data from the image and organize it column by column.
     */
    private static int[][] extractColumnData(Mat grayMat) {
        int[][] columns = new int[grayMat.cols()][grayMat.rows()];

        for (int col = 0; col < grayMat.cols(); col++) {
            for (int row = 0; row < grayMat.rows(); row++) {
                double[] pixel = grayMat.get(row, col);
                if (pixel != null) {
                    int reducedValue = (int) (pixel[0] / 16);
                    columns[col][grayMat.rows() - 1 - row] = reducedValue;
                }
            }
        }

        return columns;
    }

    /**
     * Debug the extracted grayscale column data.
     */
    private static void debugColumnData(int[][] columnArray) {
        for (int[] column : columnArray) {
            System.out.println(arrayToString(column));
        }
    }

    /**
     * Convert an array to a readable string representation.
     */
    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int value : array) {
            sb.append(value).append(", ");
        }
        return sb.toString();
    }
}
