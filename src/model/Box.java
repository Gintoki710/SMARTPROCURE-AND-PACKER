package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a standard shipping carton in the warehouse.
 * Has a maximum physical weight capacity (e.g. 10 kg).
 */
public class Box {
    private int boxId;
    private double maxCapacity; // in kg
    private double currentWeight; // in kg
    private List<Item> items;

    public Box(int boxId, double maxCapacity) {
        this.boxId = boxId;
        this.maxCapacity = maxCapacity;
        this.currentWeight = 0.0;
        this.items = new ArrayList<>();
    }

    public boolean canFit(Item item) {
        return (currentWeight + item.getWeight()) <= (maxCapacity + 1e-6);
    }

    public boolean addItem(Item item) {
        if (canFit(item)) {
            items.add(item);
            currentWeight += item.getWeight();
            // Round to 3 decimal places to avoid floating point precision artifacts
            currentWeight = Math.round(currentWeight * 1000.0) / 1000.0;
            return true;
        }
        return false;
    }

    public int getBoxId() {
        return boxId;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public double getRemainingCapacity() {
        double rem = maxCapacity - currentWeight;
        return rem < 0 ? 0.0 : Math.round(rem * 1000.0) / 1000.0;
    }

    public double getUtilizationPercentage() {
        if (maxCapacity <= 0) return 0.0;
        double pct = (currentWeight / maxCapacity) * 100.0;
        return Math.min(100.0, Math.round(pct * 10.0) / 10.0);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        return String.format("Box #%d [%.2f/%.2f kg - %.1f%% used, %d items]",
                boxId, currentWeight, maxCapacity, getUtilizationPercentage(), items.size());
    }
}

