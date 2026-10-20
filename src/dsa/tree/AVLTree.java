package dsa.tree;

import model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Self-balancing AVL Tree implementation for Product Catalog (Module 2 / Experiments 11 & 12).
 * Enables O(log N) price searches, O(K + log N) price-range filtering, and dynamic balancing.
 */
public class AVLTree {
    private AVLNode root;
    private int nodeCount;

    public AVLTree() {
        this.root = null;
        this.nodeCount = 0;
    }

    private int height(AVLNode node) {
        return (node == null) ? 0 : node.getHeight();
    }

    private int getBalance(AVLNode node) {
        return (node == null) ? 0 : height(node.getLeft()) - height(node.getRight());
    }

    private void updateHeight(AVLNode node) {
        if (node != null) {
            node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
        }
    }

    /**
     * Right rotation (LL case)
     */
    private AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.getLeft();
        AVLNode T2 = x.getRight();

        x.setRight(y);
        y.setLeft(T2);

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    /**
     * Left rotation (RR case)
     */
    private AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.getRight();
        AVLNode T2 = y.getLeft();

        y.setLeft(x);
        x.setRight(T2);

        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * Comparator for two items: primary key is price, secondary is itemId.
     */
    private int compare(Item a, Item b) {
        int cmp = Double.compare(a.getPrice(), b.getPrice());
        if (cmp != 0) return cmp;
        return a.getId().compareTo(b.getId());
    }

    private int compare(double price, String id, Item target) {
        int cmp = Double.compare(price, target.getPrice());
        if (cmp != 0) return cmp;
        return id.compareTo(target.getId());
    }

    /**
     * Inserts an item into the AVL tree and rebalances.
     */
    public void insert(Item item) {
        root = insertRec(root, item);
        nodeCount++;
    }

    private AVLNode insertRec(AVLNode node, Item item) {
        if (node == null) {
            return new AVLNode(item);
        }

        int cmp = compare(item, node.getItem());
        if (cmp < 0) {
            node.setLeft(insertRec(node.getLeft(), item));
        } else {
            node.setRight(insertRec(node.getRight(), item));
        }

        updateHeight(node);

        int balance = getBalance(node);

        // Case 1: Left Left (LL)
        if (balance > 1 && compare(item, node.getLeft().getItem()) < 0) {
            return rightRotate(node);
        }

        // Case 2: Right Right (RR)
        if (balance < -1 && compare(item, node.getRight().getItem()) >= 0) {
            return leftRotate(node);
        }

        // Case 3: Left Right (LR)
        if (balance > 1 && compare(item, node.getLeft().getItem()) >= 0) {
            node.setLeft(leftRotate(node.getLeft()));
            return rightRotate(node);
        }

        // Case 4: Right Left (RL)
        if (balance < -1 && compare(item, node.getRight().getItem()) < 0) {
            node.setRight(rightRotate(node.getRight()));
            return leftRotate(node);
        }

        return node;
    }

    /**
     * Deletes an item by ID and price from the AVL tree.
     */
    public boolean delete(String itemId, double price) {
        int initialCount = nodeCount;
        root = deleteRec(root, itemId, price);
        return nodeCount < initialCount;
    }

    private AVLNode minValueNode(AVLNode node) {
        AVLNode current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }

    private AVLNode deleteRec(AVLNode node, String itemId, double price) {
        if (node == null) return null;

        int cmp = compare(price, itemId, node.getItem());
        if (cmp < 0) {
            node.setLeft(deleteRec(node.getLeft(), itemId, price));
        } else if (cmp > 0) {
            node.setRight(deleteRec(node.getRight(), itemId, price));
        } else {
            // Node found
            nodeCount--;
            if (node.getLeft() == null || node.getRight() == null) {
                AVLNode temp = (node.getLeft() != null) ? node.getLeft() : node.getRight();
                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }
            } else {
                AVLNode successor = minValueNode(node.getRight());
                node.setItem(successor.getItem());
                node.setKey(successor.getKey());
                node.setRight(deleteRec(node.getRight(), successor.getItem().getId(), successor.getItem().getPrice()));
            }
        }

        if (node == null) return null;

        updateHeight(node);
        int balance = getBalance(node);

        // Rebalance
        if (balance > 1 && getBalance(node.getLeft()) >= 0) {
            return rightRotate(node);
        }
        if (balance > 1 && getBalance(node.getLeft()) < 0) {
            node.setLeft(leftRotate(node.getLeft()));
            return rightRotate(node);
        }
        if (balance < -1 && getBalance(node.getRight()) <= 0) {
            return leftRotate(node);
        }
        if (balance < -1 && getBalance(node.getRight()) > 0) {
            node.setRight(rightRotate(node.getRight()));
            return leftRotate(node);
        }

        return node;
    }

    /**
     * Fast price-range filtering using pruned in-order BST traversal.
     * Efficiently retrieves all items in [minPrice, maxPrice] in O(K + log N).
     */
    public List<Item> rangeSearch(double minPrice, double maxPrice) {
        List<Item> result = new ArrayList<>();
        rangeSearchRec(root, minPrice, maxPrice, result);
        return result;
    }

    private void rangeSearchRec(AVLNode node, double minPrice, double maxPrice, List<Item> result) {
        if (node == null) return;

        // If current node's price is greater than minPrice, search left subtree
        if (node.getItem().getPrice() >= minPrice) {
            rangeSearchRec(node.getLeft(), minPrice, maxPrice, result);
        }

        // If current node lies within range, add it
        if (node.getItem().getPrice() >= minPrice && node.getItem().getPrice() <= maxPrice) {
            result.add(node.getItem());
        }

        // If current node's price is less than maxPrice, search right subtree
        if (node.getItem().getPrice() <= maxPrice) {
            rangeSearchRec(node.getRight(), minPrice, maxPrice, result);
        }
    }

    /**
     * In-order traversal yielding catalog items sorted ascending by price.
     */
    public List<Item> inOrderList() {
        List<Item> list = new ArrayList<>();
        inOrderRec(root, list);
        return list;
    }

    private void inOrderRec(AVLNode node, List<Item> list) {
        if (node != null) {
            inOrderRec(node.getLeft(), list);
            list.add(node.getItem());
            inOrderRec(node.getRight(), list);
        }
    }

    public void clear() {
        this.root = null;
        this.nodeCount = 0;
    }

    public int size() {
        return nodeCount;
    }

    public int getTreeHeight() {
        return height(root);
    }

    public AVLNode getRoot() {
        return root;
    }

    /**
     * Converts AVL tree structure to a nested map for visual tree rendering in the browser.
     */
    public Map<String, Object> toVisualHierarchy() {
        if (root == null) return null;
        return nodeToMap(root);
    }

    private Map<String, Object> nodeToMap(AVLNode node) {
        if (node == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("name", node.getItem().getName());
        map.put("id", node.getItem().getId());
        map.put("price", node.getItem().getPrice());
        map.put("utility", node.getItem().getUtility());
        map.put("weight", node.getItem().getWeight());
        map.put("height", node.getHeight());
        map.put("balance", node.getBalanceFactor());

        List<Map<String, Object>> children = new ArrayList<>();
        if (node.getLeft() != null) {
            Map<String, Object> leftMap = nodeToMap(node.getLeft());
            leftMap.put("branch", "L");
            children.add(leftMap);
        }
        if (node.getRight() != null) {
            Map<String, Object> rightMap = nodeToMap(node.getRight());
            rightMap.put("branch", "R");
            children.add(rightMap);
        }
        if (!children.isEmpty()) {
            map.put("children", children);
        }
        return map;
    }
}

