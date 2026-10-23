import java.io.File;       
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;
import  java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;

//TODO: PRIORITYQUEUE ANGUCKEN WIE MAN ES BENUTZEN KANN

public class HuffmanEncoder {

    public static void GPXreader(String fileName) throws IOException {
        File xmlFile = new File(fileName);
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        //Comparator<CharPriority> comparator = Comparator.comparingDouble(CharPriority::häufigkeit);
        //Queue<CharPriority> queue = new PriorityQueue<>(comparator);
                        
        if (!xmlFile.exists()) {
            System.out.println("Die Datei muss existieren");
            return;
        }

        String content = Files.readString(Path.of(fileName));
        Set<Character> einzigartigeCharacters = content.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toSet());
        
        

        for (char c : einzigartigeCharacters) {
            double anzahlBuchstaben = content.chars()
                                            .filter(ch -> ch == c)
                                            .count();
            

            //elemente in die Warteschlange einfügen
            int code = (int) c;


            //CharPriority charPriorityListe = new CharPriority(code, häufigkeit);
            queue.offer(new HuffmanNode(c, code));//annordung der blätter
        }
        // gibt die richtige Reihenfolge aus, aufsteigend
        while (queue.size() > 1) {
            HuffmanNode links = queue.poll();
            HuffmanNode rechts = queue.poll();

            double kombiniertHäufigkeit = links.frequency + rechts.frequency;
            HuffmanNode elternNode = new HuffmanNode('\0', kombiniertHäufigkeit);

            elternNode.left = links;
            elternNode.right = rechts;

            queue.offer(elternNode);
        }        

        HuffmanNode wurzel = queue.poll();
        Map<Character, String> codes = generateCodes(wurzel);

        System.out.println("\n--- Huffman-Codes ---");
        long gesamtBits = 0;
        for (var entry : codes.entrySet()) {
            System.out.printf("Zeichen '%c' -> %s%n", entry.getKey(), entry.getValue()); //entry.getKey() = zeichen, entry.getValue() = Code
            int bitsLentgh = entry.getValue().length(); //länge eines zeichen

            long anzahlBuchstaben = content.chars().filter(ch -> ch == entry.getKey()).count();

            gesamtBits += anzahlBuchstaben * bitsLentgh;
        }

        System.out.println("GesamtspeicherBedarf in Bits: " + gesamtBits);
        System.out.println("GesamtspeicherBedarf in Bytes: " + gesamtBits / 8);

    }

    public static void printTree(HuffmanNode node, String indent) {
        if (node == null) {
            return;
        }

        if (node.character != '\0') {
            System.out.println(indent + "Blatt -> Zeichen: '" + node.character + "' (Häufigkeit: " + node.frequency + ")");
        } else {
            System.out.println(indent + "Knoten (Häufigkeit: " + node.frequency + ")");
        }

        printTree(node.left, indent + " ");
        printTree(node.right, indent + " ");
    }

    public static Map<Character, String> generateCodes(HuffmanNode root) {
        Map<Character, String> huffmanCodes = new HashMap<>();
        generateCodesRecursive(root, "", huffmanCodes);
        return huffmanCodes;
    }

    public static void generateCodesRecursive(HuffmanNode node, String currentNode, Map<Character, String> huffmanCodes) {
        if (node == null) {
            return;
        }

        if (node.character != '\0') {
            huffmanCodes.put(node.character, currentNode);
            return;
        }

        generateCodesRecursive(node.left, currentNode + "0", huffmanCodes);
        generateCodesRecursive(node.right, currentNode + "1", huffmanCodes);
    }
   
}