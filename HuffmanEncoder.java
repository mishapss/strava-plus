import java.io.File;       
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import  java.util.PriorityQueue;

//TODO: PRIORITYQUEUE ANGUCKEN WIE MAN ES BENUTZEN KANN

public class HuffmanEncoder {

    public static void GPXreader(String fileName) throws IOException {
        File xmlFile = new File(fileName);
        PriorityQueue<Integer> pq = new PriorityQueue<>();
                
        if (!xmlFile.exists()) {
            System.out.println("Die Datei muss existieren");
            return;
        }

        String content = Files.readString(Path.of(fileName));
        char[] alleZeichen = content.toCharArray();

        Set<Character> einzigartigeCharacters = content.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toSet());
        
        for (char c : einzigartigeCharacters) {
            double anzahlBuchstaben = content.chars()
                                            .filter(ch -> ch == c)
                                            .count();
            System.out.println("Char: " + c);
            System.out.println("anzahlBuchstaben: " + anzahlBuchstaben);
            double häufigkeit = anzahlBuchstaben/content.length();
            System.out.println("Häufigkeit: " + häufigkeit);

            //elemente in die Warteschlange einfügen
            pq.add(c);

        }
    }
}