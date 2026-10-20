package dsa.knapsack;

import model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Solves the 0/1 Knapsack Problem using Dynamic Programming (Module 5 / Experiment 15).
 * Also computes Greedy heuristic for theoretical comparison in DSA evaluation.
 */
public class KnapsackSolver {

    public static class KnapsackResult {
        private List<Item> selectedItems;
        private int totalUtility;
        private double totalCost;
        private double remainingBudget;
        private double totalWeight;
        private long computationTimeNanos;
        
        // Comparison with Greedy Heuristic (viva / syllabus demonstration)
        private List<Item> greedyItems;
        private int greedyUtility;
        private double greedyCost;
        private String optimalityExplanation;

        // DP Table snapshot for visualization
        private List<DpTableSampleRow> dpSampleRows;

        public KnapsackResult(List<Item> selectedItems, int totalUtility, double totalCost,
                              double remainingBudget, double totalWeight, long computationTimeNanos,
                              List<Item> greedyItems, int greedyUtility, double greedyCost,
                              String optimalityExplanation, List<DpTableSampleRow> dpSampleRows) {
            this.selectedItems = selectedItems;
            this.totalUtility = totalUtility;
            this.totalCost = totalCost;
            this.remainingBudget = remainingBudget;
            this.totalWeight = totalWeight;
            this.computationTimeNanos = computationTimeNanos;
            this.greedyItems = greedyItems;
            this.greedyUtility = greedyUtility;
            this.greedyCost = greedyCost;
            this.optimalityExplanation = optimalityExplanation;
            this.dpSampleRows = dpSampleRows;
        }

        public List<Item> getSelectedItems() { return selectedItems; }
        public int getTotalUtility() { return totalUtility; }
        public double getTotalCost() { return totalCost; }
        public double getRemainingBudget() { return remainingBudget; }
        public double getTotalWeight() { return totalWeight; }
        public long getComputationTimeNanos() { return computationTimeNanos; }
        public List<Item> getGreedyItems() { return greedyItems; }
        public int getGreedyUtility() { return greedyUtility; }
        public double getGreedyCost() { return greedyCost; }
        public String getOptimalityExplanation() { return optimalityExplanation; }
        public List<DpTableSampleRow> getDpSampleRows() { return dpSampleRows; }
    }

    public static class DpTableSampleRow {
        private String itemName;
        private int cost;
        private int utility;
        private List<Integer> valuesAtKeyBudgets;

        public DpTableSampleRow(String itemName, int cost, int utility, List<Integer> valuesAtKeyBudgets) {
            this.itemName = itemName;
            this.cost = cost;
            this.utility = utility;
            this.valuesAtKeyBudgets = valuesAtKeyBudgets;
        }

        public String getItemName() { return itemName; }
        public int getCost() { return cost; }
        public int getUtility() { return utility; }
        public List<Integer> getValuesAtKeyBudgets() { return valuesAtKeyBudgets; }
    }

    /**
     * Solves 0/1 Knapsack with DP given candidate items and max budget.
     */
    public static KnapsackResult solve(List<Item> candidateItems, double budget) {
        long startTime = System.nanoTime();

        if (candidateItems == null || candidateItems.isEmpty() || budget <= 0) {
            return new KnapsackResult(Collections.emptyList(), 0, 0, budget, 0, 0,
                    Collections.emptyList(), 0, 0, "No items or zero budget provided.", Collections.emptyList());
        }

        // Determine price scaling factor: round prices to integer units (e.g. ₹1)
        int budgetInt = (int) Math.round(budget);
        if (budgetInt <= 0) budgetInt = 1;

        int n = candidateItems.size();
        int[] costs = new int[n];
        int[] values = new int[n];

        for (int i = 0; i < n; i++) {
            Item item = candidateItems.get(i);
            costs[i] = Math.max(1, (int) Math.round(item.getPrice()));
            values[i] = item.getUtility();
        }

        // DP Table: dp[i][w] = max value achievable with first i items and budget w
        int[][] dp = new int[n + 1][budgetInt + 1];

        for (int i = 1; i <= n; i++) {
            int c = costs[i - 1];
            int v = values[i - 1];
            for (int w = 0; w <= budgetInt; w++) {
                if (c <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - c] + v);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Backtracking to find exact selected items
        List<Item> selectedItems = new ArrayList<>();
        int w = budgetInt;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Item chosen = candidateItems.get(i - 1);
                selectedItems.add(chosen);
                w -= costs[i - 1];
            }
        }
        Collections.reverse(selectedItems);

        double totalCost = 0.0;
        int totalUtility = 0;
        double totalWeight = 0.0;
        for (Item item : selectedItems) {
            totalCost += item.getPrice();
            totalUtility += item.getUtility();
            totalWeight += item.getWeight();
        }
        totalCost = Math.round(totalCost * 100.0) / 100.0;
        totalWeight = Math.round(totalWeight * 1000.0) / 1000.0;
        double remainingBudget = Math.round((budget - totalCost) * 100.0) / 100.0;

        // Greedy Comparison (sorting by utility / cost ratio)
        List<Item> greedyCandidates = new ArrayList<>(candidateItems);
        greedyCandidates.sort((a, b) -> Double.compare(b.getUtilityPerPrice(), a.getUtilityPerPrice()));
        List<Item> greedySelected = new ArrayList<>();
        double greedyCost = 0.0;
        int greedyUtility = 0;
        for (Item item : greedyCandidates) {
            if (greedyCost + item.getPrice() <= budget + 1e-4) {
                greedySelected.add(item);
                greedyCost += item.getPrice();
                greedyUtility += item.getUtility();
            }
        }
        greedyCost = Math.round(greedyCost * 100.0) / 100.0;

        String explanation;
        if (totalUtility > greedyUtility) {
            explanation = String.format("Dynamic Programming achieved %d utility points, beating Greedy heuristic (%d points) by %d points (+%.1f%%). This mathematically proves the necessity of 0/1 Knapsack DP over simple greedy selection for discrete items.",
                    totalUtility, greedyUtility, (totalUtility - greedyUtility),
                    greedyUtility > 0 ? ((double)(totalUtility - greedyUtility) / greedyUtility) * 100.0 : 0.0);
        } else if (totalUtility == greedyUtility) {
            explanation = String.format("Dynamic Programming matched the greedy selection at %d utility points. DP guarantees the global optimum even when greedy heuristics risk sub-optimality.", totalUtility);
        } else {
            explanation = "Optimal solution determined via DP state recurrence.";
        }

        // Generate sample rows for DP table visualizer (sample at 6 key budget intervals: 0%, 20%, 40%, 60%, 80%, 100%)
        List<DpTableSampleRow> sampleRows = new ArrayList<>();
        int[] keyBudgets = new int[]{
                0,
                (int) (budgetInt * 0.20),
                (int) (budgetInt * 0.40),
                (int) (budgetInt * 0.60),
                (int) (budgetInt * 0.80),
                budgetInt
        };

        for (int i = 1; i <= Math.min(n, 12); i++) {
            Item item = candidateItems.get(i - 1);
            List<Integer> valuesAtKeys = new ArrayList<>();
            for (int kb : keyBudgets) {
                valuesAtKeys.add(dp[i][kb]);
            }
            sampleRows.add(new DpTableSampleRow(item.getName(), costs[i - 1], values[i - 1], valuesAtKeys));
        }

        long elapsedNanos = System.nanoTime() - startTime;

        return new KnapsackResult(selectedItems, totalUtility, totalCost, remainingBudget, totalWeight,
                elapsedNanos, greedySelected, greedyUtility, greedyCost, explanation, sampleRows);
    }
}

