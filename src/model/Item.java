package model;

/**
 * Represents a procurement candidate item in the catalog.
 * Includes financial cost (for Knapsack), utility/profit score,
 * and physical weight (for Carton Bin Packing).
 */
public class Item {
    private String id;
    private String name;
    private String category;
    private double price;       // Cost in ₹
    private int utility;        // Importance / Resale / Profit value (points)
    private double weight;      // Physical weight in kg

    public Item(String id, String name, String category, double price, int utility, double weight) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.utility = utility;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getUtility() {
        return utility;
    }

    public double getWeight() {
        return weight;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Utility-to-price ratio used in Greedy Knapsack comparison.
     */
    public double getUtilityPerPrice() {
        return price > 0 ? (double) utility / price : 0;
    }

    @Override
    public String toString() {
        return String.format("%s (₹%.2f, %d pts, %.2f kg)", name, price, utility, weight);
    }
}

