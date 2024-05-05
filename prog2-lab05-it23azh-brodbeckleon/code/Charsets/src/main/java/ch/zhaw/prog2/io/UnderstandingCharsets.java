package ch.zhaw.prog2.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;


public class UnderstandingCharsets {
    public static void main(String[] args) {


        /* Teilaufgabe a
         * In der Vorlesung haben Sie gelernt, dass Java-Klassen fuer Unicode entworfen wurden.
         * Nun ist Unicode aber nicht der einzige Zeichensatz und Java unterstuetz durchaus Alternativen.
         * Welche Zeichensaetze auf einem System konkret unterstuetzt werden haengt von der Konfiguration
         * des Betriebssystems der JVM ab.
         * Schreiben Sie ein Programm, welches alle Unterstuetzten Zeichensaetze auf der Konsole (System.out) ausgibt,
         * zusammen mit dem Standardzeichensatz.
         * https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html
         */

        System.out.println("Default character set:");
        System.out.println(Charset.defaultCharset());
        System.out.println();


        System.out.println("Available character sets:");
        for (String charset : Charset.availableCharsets().keySet()) {
            System.out.println(charset);
        }


        /* Ende Teilaufgabe a */


        /* Teilaufgabe b
         * Ergänzen Sie die Klasse so, dass sie einzelne Zeichen (also Zeichen für Zeichen) im Standardzeichensatz
         * von der Konsole einliest und in zwei Dateien schreibt einmal im Standardzeichensatz und einmal im
         * Zeichensatz `US-ASCII`.
         * Die Eingabe des Zeichens `q` soll das Program ordentlich beenden.
         * Die Dateien sollen `CharSetEvaluation_Default.txt` und `CharSetEvaluation_ASCII.txt` genannt und
         * werden entweder erzeugt oder, falls sie bereits existieren, geöffnet und der Inhalt überschrieben.
         * Testen Sie Ihr Program mit den folgenden Zeichen: a B c d € f ü _ q
         * Öffnen Sie die Textdateien nach Ausführung des Programs mit einem Texteditor und erklären Sie das Ergebnis.
         * Öffnen Sie die Dateien anschliessend mit einem HEX-Editor und vergleichen Sie.
         */

        String input = "";
        try (DataInputStream din = new DataInputStream(System.in)) {
            char ch;
            do {
                ch = (char) din.readByte();
                input += ch;
            } while (ch != 'q');
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        String path = "code/Charsets/src/main/java/ch/zhaw/prog2/io/";
        String defaultFile = "CharSetEvaluation_Default.txt";
        String asciiFile = "CharSetEvaluation_ASCII.txt";

        HashMap<String, Charset> charsets = new HashMap<>();
        charsets.put(defaultFile, Charset.defaultCharset());
        charsets.put(asciiFile, Charset.forName("US-ASCII"));

        for (String file : charsets.keySet()) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+file), charsets.get(file)))) {
                writer.write(input);
                System.out.println("Datei wurde erfolgreich erstellt und beschrieben in " + charsets.get(file).displayName() + ".");
            } catch (IOException e) {
                System.out.println("Ein Fehler ist aufgetreten:");
                e.printStackTrace();
            }
        }
    }
}

