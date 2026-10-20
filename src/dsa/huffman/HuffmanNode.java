package dsa.huffman;

/**
 * Node for Huffman Coding tree (Module 4 / Experiment 14).
 * Implements Comparable to maintain Min-Priority Queue ordering by frequency.
 */
public class HuffmanNode implements Comparable<HuffmanNode> {
    private Character character;
    private int frequency;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode(Character character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
        this.character = null; // Internal composite node
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public Character getCharacter() { return character; }
    public int getFrequency() { return frequency; }
    public HuffmanNode getLeft() { return left; }
    public HuffmanNode getRight() { return right; }

    @Override
    public int compareTo(HuffmanNode other) {
        int diff = Integer.compare(this.frequency, other.frequency);
        if (diff != 0) return diff;
        // Tie breaker for deterministic tree generation
        if (this.character != null && other.character != null) {
            return Character.compare(this.character, other.character);
        }
        return 0;
    }
}

