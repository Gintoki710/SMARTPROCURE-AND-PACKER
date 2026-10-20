package dsa.tree;

import model.Item;

/**
 * Node for self-balancing AVL Tree (Module 2 / Experiments 11 & 12).
 * Stores catalog item keyed by price.
 */
public class AVLNode {
    private Item item;
    private double key; // price
    private int height;
    private AVLNode left;
    private AVLNode right;

    public AVLNode(Item item) {
        this.item = item;
        this.key = item.getPrice();
        this.height = 1;
        this.left = null;
        this.right = null;
    }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public double getKey() { return key; }
    public void setKey(double key) { this.key = key; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public AVLNode getLeft() { return left; }
    public void setLeft(AVLNode left) { this.left = left; }

    public AVLNode getRight() { return right; }
    public void setRight(AVLNode right) { this.right = right; }

    public int getBalanceFactor() {
        int leftH = (left != null) ? left.getHeight() : 0;
        int rightH = (right != null) ? right.getHeight() : 0;
        return leftH - rightH;
    }
}

