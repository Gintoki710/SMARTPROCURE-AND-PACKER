package dsa.huffman;

import java.util.*;

/**
 * Implements Huffman Coding compression & decompression for Digital Order Invoices (Module 4).
 * Generates optimal prefix codes, frequency maps, bitstreams, and tree visualization data.
 */
public class HuffmanCompressor {

    public static class CodeEntry {
        private String characterDisplay;
        private char rawChar;
        private int frequency;
        private String huffmanCode;
        private int codeLength;

        public CodeEntry(char rawChar, int frequency, String huffmanCode) {
            this.rawChar = rawChar;
            this.frequency = frequency;
            this.huffmanCode = huffmanCode;
            this.codeLength = huffmanCode.length();
            if (rawChar == ' ') {
                this.characterDisplay = "[SPACE]";
            } else if (rawChar == '\n') {
                this.characterDisplay = "[NEWLINE]";
            } else if (rawChar == '\r') {
                this.characterDisplay = "[CR]";
            } else if (rawChar == '\t') {
                this.characterDisplay = "[TAB]";
            } else {
                this.characterDisplay = String.valueOf(rawChar);
            }
        }

        public String getCharacterDisplay() { return characterDisplay; }
        public char getRawChar() { return rawChar; }
        public int getFrequency() { return frequency; }
        public String getHuffmanCode() { return huffmanCode; }
        public int getCodeLength() { return codeLength; }
    }

    public static class CompressionResult {
        private String originalText;
        private String encodedBitString;
        private String decodedText;
        private boolean isLossless;
        private int originalLengthChars;
        private int originalSizeBits;
        private int originalSizeBytes;
        private int compressedSizeBits;
        private int compressedSizeBytes;
        private double compressionRatio;      // Percentage of space saved
        private double bitsPerCharacter;
        private List<CodeEntry> codeTable;
        private Map<String, Object> treeHierarchy;

        public CompressionResult(String originalText, String encodedBitString, String decodedText,
                                 boolean isLossless, int originalLengthChars, int originalSizeBits,
                                 int originalSizeBytes, int compressedSizeBits, int compressedSizeBytes,
                                 double compressionRatio, double bitsPerCharacter,
                                 List<CodeEntry> codeTable, Map<String, Object> treeHierarchy) {
            this.originalText = originalText;
            this.encodedBitString = encodedBitString;
            this.decodedText = decodedText;
            this.isLossless = isLossless;
            this.originalLengthChars = originalLengthChars;
            this.originalSizeBits = originalSizeBits;
            this.originalSizeBytes = originalSizeBytes;
            this.compressedSizeBits = compressedSizeBits;
            this.compressedSizeBytes = compressedSizeBytes;
            this.compressionRatio = compressionRatio;
            this.bitsPerCharacter = bitsPerCharacter;
            this.codeTable = codeTable;
            this.treeHierarchy = treeHierarchy;
        }

        public String getOriginalText() { return originalText; }
        public String getEncodedBitString() { return encodedBitString; }
        public String getDecodedText() { return decodedText; }
        public boolean isLossless() { return isLossless; }
        public int getOriginalLengthChars() { return originalLengthChars; }
        public int getOriginalSizeBits() { return originalSizeBits; }
        public int getOriginalSizeBytes() { return originalSizeBytes; }
        public int getCompressedSizeBits() { return compressedSizeBits; }
        public int getCompressedSizeBytes() { return compressedSizeBytes; }
        public double getCompressionRatio() { return compressionRatio; }
        public double getBitsPerCharacter() { return bitsPerCharacter; }
        public List<CodeEntry> getCodeTable() { return codeTable; }
        public Map<String, Object> getTreeHierarchy() { return treeHierarchy; }
    }

    /**
     * Compresses text using Huffman Coding and generates full metrics.
     */
    public static CompressionResult compress(String text) {
        if (text == null || text.isEmpty()) {
            return new CompressionResult("", "", "", true, 0, 0, 0, 0, 0, 0, 0,
                    Collections.emptyList(), null);
        }

        // 1. Calculate character frequencies
        Map<Character, Integer> freqMap = new LinkedHashMap<>();
        for (char c : text.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        // 2. Build Min-Priority Queue
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // Edge case: text with only 1 distinct character
        if (pq.size() == 1) {
            HuffmanNode single = pq.poll();
            HuffmanNode root = new HuffmanNode(single.getFrequency(), single, null);
            pq.add(root);
        }

        // 3. Iteratively merge the 2 lowest-frequency trees
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(left.getFrequency() + right.getFrequency(), left, right);
            pq.add(parent);
        }

        HuffmanNode root = pq.peek();

        // 4. Generate prefix codes
        Map<Character, String> codeMap = new HashMap<>();
        generateCodes(root, "", codeMap);

        // 5. Encode text into bitstring
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            encoded.append(codeMap.get(c));
        }
        String bitString = encoded.toString();

        // 6. Decode bitstring back to text to verify lossless reconstruction
        String decoded = decode(root, bitString);
        boolean lossless = text.equals(decoded);

        // 7. Calculate compression statistics
        int originalChars = text.length();
        int originalBits = originalChars * 8; // Standard ASCII/UTF-8 single byte representation
        int originalBytes = originalChars;
        int compressedBits = bitString.length();
        int compressedBytes = (int) Math.ceil((double) compressedBits / 8.0);
        double spaceSavings = originalBits > 0 ? ((double) (originalBits - compressedBits) / originalBits) * 100.0 : 0.0;
        spaceSavings = Math.round(spaceSavings * 10.0) / 10.0;
        double bpc = originalChars > 0 ? (double) compressedBits / originalChars : 0.0;
        bpc = Math.round(bpc * 100.0) / 100.0;

        // 8. Build code table sorted by frequency descending
        List<CodeEntry> codeTable = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            codeTable.add(new CodeEntry(entry.getKey(), entry.getValue(), codeMap.get(entry.getKey())));
        }
        codeTable.sort((a, b) -> Integer.compare(b.getFrequency(), a.getFrequency()));

        // 9. Generate visual tree hierarchy (limit depth/nodes for clean UI display)
        Map<String, Object> visualTree = buildTreeHierarchy(root, "");

        return new CompressionResult(text, bitString, decoded, lossless, originalChars,
                originalBits, originalBytes, compressedBits, compressedBytes,
                spaceSavings, bpc, codeTable, visualTree);
    }

    private static void generateCodes(HuffmanNode node, String prefix, Map<Character, String> codeMap) {
        if (node == null) return;

        if (node.isLeaf()) {
            // If tree had single character
            codeMap.put(node.getCharacter(), prefix.isEmpty() ? "0" : prefix);
            return;
        }

        generateCodes(node.getLeft(), prefix + "0", codeMap);
        generateCodes(node.getRight(), prefix + "1", codeMap);
    }

    /**
     * Decodes the bitstring back to plain text using the Huffman Tree.
     */
    public static String decode(HuffmanNode root, String bitString) {
        if (root == null || bitString == null || bitString.isEmpty()) return "";

        StringBuilder decoded = new StringBuilder();
        HuffmanNode current = root;

        // Special case for single-node root
        if (root.isLeaf()) {
            for (int i = 0; i < bitString.length(); i++) {
                decoded.append(root.getCharacter());
            }
            return decoded.toString();
        }

        for (int i = 0; i < bitString.length(); i++) {
            char bit = bitString.charAt(i);
            current = (bit == '0') ? current.getLeft() : current.getRight();

            if (current != null && current.isLeaf()) {
                decoded.append(current.getCharacter());
                current = root; // Reset to root for next character
            }
        }

        return decoded.toString();
    }

    private static Map<String, Object> buildTreeHierarchy(HuffmanNode node, String path) {
        if (node == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("frequency", node.getFrequency());
        map.put("path", path);

        if (node.isLeaf()) {
            char c = node.getCharacter();
            String display = (c == ' ') ? "[SPACE]" : (c == '\n') ? "[NL]" : String.valueOf(c);
            map.put("character", display);
            map.put("isLeaf", true);
        } else {
            map.put("character", "•");
            map.put("isLeaf", false);
            List<Map<String, Object>> children = new ArrayList<>();
            if (node.getLeft() != null) {
                Map<String, Object> l = buildTreeHierarchy(node.getLeft(), path + "0");
                l.put("bit", "0");
                children.add(l);
            }
            if (node.getRight() != null) {
                Map<String, Object> r = buildTreeHierarchy(node.getRight(), path + "1");
                r.put("bit", "1");
                children.add(r);
            }
            map.put("children", children);
        }

        return map;
    }
}

