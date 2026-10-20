package dsa.stack;

import model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages Cart state history using Undo and Redo Custom Stacks (Module 1 / Experiment 1).
 */
public class CartHistoryManager {
    private CustomStack<CartAction> undoStack;
    private CustomStack<CartAction> redoStack;
    private List<Item> currentCart;

    public CartHistoryManager() {
        this.undoStack = new CustomStack<>();
        this.redoStack = new CustomStack<>();
        this.currentCart = new ArrayList<>();
    }

    public List<Item> getCurrentCart() {
        return Collections.unmodifiableList(currentCart);
    }

    /**
     * Executes a new action, pushing it to the Undo Stack and clearing the Redo Stack.
     */
    public synchronized void execute(CartAction.Type type, String description, List<Item> newItems) {
        CartAction action = new CartAction(type, description, this.currentCart, newItems);
        undoStack.push(action);
        redoStack.clear(); // Any new action invalidates existing redo history
        this.currentCart = new ArrayList<>(newItems);
    }

    /**
     * Undoes the most recent action by popping from Undo Stack and pushing to Redo Stack.
     */
    public synchronized boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        CartAction action = undoStack.pop();
        redoStack.push(action);
        this.currentCart = new ArrayList<>(action.getPreviousItems());
        return true;
    }

    /**
     * Redoes the last undone action by popping from Redo Stack and pushing to Undo Stack.
     */
    public synchronized boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        CartAction action = redoStack.pop();
        undoStack.push(action);
        this.currentCart = new ArrayList<>(action.getNextItems());
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    public int getRedoCount() {
        return redoStack.size();
    }

    public List<CartAction> getUndoStackSnapshot() {
        return undoStack.toListTopToBottom();
    }

    public List<CartAction> getRedoStackSnapshot() {
        return redoStack.toListTopToBottom();
    }

    public synchronized void reset(List<Item> initialItems) {
        undoStack.clear();
        redoStack.clear();
        this.currentCart = new ArrayList<>(initialItems);
    }
}

