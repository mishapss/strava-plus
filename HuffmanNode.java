public class HuffmanNode implements Comparable<HuffmanNode> { //definiert, wie die objekte HuffmanNode miteinander verglichen werden müssen
    char character;
    double frequency;
    HuffmanNode left;
    HuffmanNode right;

    ////konstruktor für die blätter
    public HuffmanNode(char character, double frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;  //blatt ist nicht sortiert
        this.right = null; //blatt ist nicht sortiert
    }

    
    public HuffmanNode(double frequency, HuffmanNode left, HuffmanNode right) {
        this.character = '\0'; //Platzhalter, gibt´s kein Zeichen
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return Double.compare(this.frequency, other.frequency);
    }
}

