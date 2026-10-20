package dsa.stack;

import model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a discrete command / action performed on the cart.
 * Stores previous and subsequent snapshots to enable reversible undo/redo.
 */
public class CartAction {
    public enum Type {
        ADD_ITEM,
        REMOVE_ITEM,
        REPLACE_CART,
        CLEAR_CART
    }

    private Type type;
    private String description;
    private List<Item> previousItems;
    private List<Item> nextItems;
    private long timestamp;

    public CartAction(Type type, String description, List<Item> previousItems, List<Item> nextItems) {
        this.type = type;
        this.description = description;
        this.previousItems = new ArrayList<>(previousItems);
        this.nextItems = new ArrayList<>(nextItems);
        this.timestamp = System.currentTimeMillis();
    }

    public Type getType() { return type; }
    public String getDescription() { return description; }
    public List<Item> getPreviousItems() { return Collections.unmodifiableList(previousItems); }
    public List<Item> getNextItems() { return Collections.unmodifiableList(nextItems); }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d -> %d items)", type, description, previousItems.size(), nextItems.size());
    }
}

