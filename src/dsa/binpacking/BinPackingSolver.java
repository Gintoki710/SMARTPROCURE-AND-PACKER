package dsa.binpacking;

import model.Box;
import model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Solves the Warehouse Carton Packing problem (Module 4 Bin Packing).
 * Implements First Fit Decreasing (FFD), Best Fit Decreasing (BFD), Next Fit, and Worst Fit Decreasing.
 */
public class BinPackingSolver {

    public static class PackingResult {
        private String algorithmName;
        private List<Box> boxes;
        private int boxCount;
        private double totalPackedWeight;
        private double averageUtilization;
        private double courierCost;
        private String theoreticalRatio;

        public PackingResult(String algorithmName, List<Box> boxes, double courierFeePerBox, String theoreticalRatio) {
            this.algorithmName = algorithmName;
            this.boxes = boxes;
            this.boxCount = boxes.size();
            this.theoreticalRatio = theoreticalRatio;
            this.courierCost = Math.round(this.boxCount * courierFeePerBox * 100.0) / 100.0;

            double weightSum = 0.0;
            double utilSum = 0.0;
            for (Box b : boxes) {
                weightSum += b.getCurrentWeight();
                utilSum += b.getUtilizationPercentage();
            }
            this.totalPackedWeight = Math.round(weightSum * 1000.0) / 1000.0;
            this.averageUtilization = boxes.isEmpty() ? 0.0 : Math.round((utilSum / boxes.size()) * 10.0) / 10.0;
        }

        public String getAlgorithmName() { return algorithmName; }
        public List<Box> getBoxes() { return boxes; }
        public int getBoxCount() { return boxCount; }
        public double getTotalPackedWeight() { return totalPackedWeight; }
        public double getAverageUtilization() { return averageUtilization; }
        public double getCourierCost() { return courierCost; }
        public String getTheoreticalRatio() { return theoreticalRatio; }
    }

    public static class ComparisonResult {
        private PackingResult ffd;
        private PackingResult bfd;
        private PackingResult nextFit;
        private PackingResult worstFit;
        private PackingResult optimalChoice;
        private double savingsVersusNaive;

        public ComparisonResult(PackingResult ffd, PackingResult bfd, PackingResult nextFit, PackingResult worstFit) {
            this.ffd = ffd;
            this.bfd = bfd;
            this.nextFit = nextFit;
            this.worstFit = worstFit;

            // Pick the best (minimum box count, ties broken by highest average utilization)
            List<PackingResult> all = List.of(ffd, bfd, worstFit, nextFit);
            PackingResult best = ffd;
            for (PackingResult p : all) {
                if (p.getBoxCount() < best.getBoxCount() ||
                   (p.getBoxCount() == best.getBoxCount() && p.getAverageUtilization() > best.getAverageUtilization())) {
                    best = p;
                }
            }
            this.optimalChoice = best;
            this.savingsVersusNaive = Math.max(0.0, nextFit.getCourierCost() - best.getCourierCost());
        }

        public PackingResult getFfd() { return ffd; }
        public PackingResult getBfd() { return bfd; }
        public PackingResult getNextFit() { return nextFit; }
        public PackingResult getWorstFit() { return worstFit; }
        public PackingResult getOptimalChoice() { return optimalChoice; }
        public double getSavingsVersusNaive() { return savingsVersusNaive; }
    }

    /**
     * Packs items using First Fit Decreasing (FFD).
     * 1. Sort items descending by weight.
     * 2. For each item, place in the first box that can accommodate it.
     * 3. If no box fits, open a new box.
     */
    public static PackingResult packFFD(List<Item> items, double boxCapacity, double courierFeePerBox) {
        List<Item> sortedItems = new ArrayList<>(items);
        sortedItems.sort((a, b) -> Double.compare(b.getWeight(), a.getWeight()));

        List<Box> boxes = new ArrayList<>();
        int boxCounter = 1;

        for (Item item : sortedItems) {
            boolean placed = false;
            for (Box box : boxes) {
                if (box.canFit(item)) {
                    box.addItem(item);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                Box newBox = new Box(boxCounter++, boxCapacity);
                newBox.addItem(item);
                boxes.add(newBox);
            }
        }

        return new PackingResult("First Fit Decreasing (FFD)", boxes, courierFeePerBox, "Uses at most (11/9)*OPT + 6/9 bins. Highly optimal offline heuristic.");
    }

    /**
     * Packs items using Best Fit Decreasing (BFD).
     * 1. Sort items descending by weight.
     * 2. For each item, place in the box that leaves the least remaining capacity.
     */
    public static PackingResult packBFD(List<Item> items, double boxCapacity, double courierFeePerBox) {
        List<Item> sortedItems = new ArrayList<>(items);
        sortedItems.sort((a, b) -> Double.compare(b.getWeight(), a.getWeight()));

        List<Box> boxes = new ArrayList<>();
        int boxCounter = 1;

        for (Item item : sortedItems) {
            Box bestBox = null;
            double minRemainingAfterPlacement = Double.MAX_VALUE;

            for (Box box : boxes) {
                if (box.canFit(item)) {
                    double remainingAfter = box.getRemainingCapacity() - item.getWeight();
                    if (remainingAfter < minRemainingAfterPlacement) {
                        minRemainingAfterPlacement = remainingAfter;
                        bestBox = box;
                    }
                }
            }

            if (bestBox != null) {
                bestBox.addItem(item);
            } else {
                Box newBox = new Box(boxCounter++, boxCapacity);
                newBox.addItem(item);
                boxes.add(newBox);
            }
        }

        return new PackingResult("Best Fit Decreasing (BFD)", boxes, courierFeePerBox, "Minimizes leftover gap per carton. Tightest fit guarantee.");
    }

    /**
     * Packs items using Next Fit (NF) - does not sort and only checks the latest open box.
     * Illustrates naive/online packing waste versus sorted algorithms.
     */
    public static PackingResult packNextFit(List<Item> items, double boxCapacity, double courierFeePerBox) {
        List<Box> boxes = new ArrayList<>();
        if (items.isEmpty()) {
            return new PackingResult("Next Fit (Online / Naive)", boxes, courierFeePerBox, "Competitive ratio: 2*OPT. Demonstrates why sorting saves courier costs.");
        }

        int boxCounter = 1;
        Box currentBox = new Box(boxCounter++, boxCapacity);
        boxes.add(currentBox);

        for (Item item : items) {
            if (!currentBox.addItem(item)) {
                currentBox = new Box(boxCounter++, boxCapacity);
                currentBox.addItem(item);
                boxes.add(currentBox);
            }
        }

        return new PackingResult("Next Fit (Online / Naive)", boxes, courierFeePerBox, "Competitive ratio: 2*OPT. Demonstrates why sorting saves courier costs.");
    }

    /**
     * Packs items using Worst Fit Decreasing (WFD).
     * Places item into the box with the most remaining capacity (load balancing).
     */
    public static PackingResult packWorstFit(List<Item> items, double boxCapacity, double courierFeePerBox) {
        List<Item> sortedItems = new ArrayList<>(items);
        sortedItems.sort((a, b) -> Double.compare(b.getWeight(), a.getWeight()));

        List<Box> boxes = new ArrayList<>();
        int boxCounter = 1;

        for (Item item : sortedItems) {
            Box worstBox = null;
            double maxRemaining = -1.0;

            for (Box box : boxes) {
                if (box.canFit(item) && box.getRemainingCapacity() > maxRemaining) {
                    maxRemaining = box.getRemainingCapacity();
                    worstBox = box;
                }
            }

            if (worstBox != null) {
                worstBox.addItem(item);
            } else {
                Box newBox = new Box(boxCounter++, boxCapacity);
                newBox.addItem(item);
                boxes.add(newBox);
            }
        }

        return new PackingResult("Worst Fit Decreasing (WFD)", boxes, courierFeePerBox, "Balances carton weights evenly across boxes.");
    }

    /**
     * Compares all bin packing strategies and calculates courier savings.
     */
    public static ComparisonResult compareAll(List<Item> items, double boxCapacity, double courierFeePerBox) {
        PackingResult ffd = packFFD(items, boxCapacity, courierFeePerBox);
        PackingResult bfd = packBFD(items, boxCapacity, courierFeePerBox);
        PackingResult nf = packNextFit(items, boxCapacity, courierFeePerBox);
        PackingResult wf = packWorstFit(items, boxCapacity, courierFeePerBox);

        return new ComparisonResult(ffd, bfd, nf, wf);
    }
}

