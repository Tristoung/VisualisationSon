package org.example.visualisationsons;
import javax.sound.sampled.*;

public class SinusoidalSoundGenerator {

    public static void playSoundFromFinalTab(double[][] tabFinal) {
        try {
            // Paramètres audio
            final int SAMPLE_RATE = 44100; // Fréquence d'échantillonnage (Hz)
            int samplesPerBuffer = tabFinal[0].length; // Taille d'un buffer
            int totalSamples = tabFinal.length * samplesPerBuffer; // Total des échantillons
            byte[] audioData = new byte[totalSamples]; // Tableau final (PCM 8 bits)

            // Concaténer les buffers en un seul tableau unidimensionnel
            int index = 0;
            for (double[] buffer : tabFinal) {
                for (double sample : buffer) {
                    // Normaliser et convertir en 8 bits PCM (-127 à 127)
                    audioData[index++] = (byte) Math.max(-127, Math.min(127, sample * 127));
                }
            }

            // Configurer le format audio
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // Ouvrir et démarrer une ligne audio
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            // Écrire les données concaténées
            line.write(audioData, 0, audioData.length);

            // Terminer et fermer la ligne
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
