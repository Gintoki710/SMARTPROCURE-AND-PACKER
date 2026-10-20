package service;

import dsa.binpacking.BinPackingSolver;
import dsa.huffman.HuffmanCompressor;
import dsa.knapsack.KnapsackSolver;
import dsa.stack.CartAction;
import dsa.stack.CartHistoryManager;
import dsa.tree.AVLTree;
import model.Box;
import model.Item;
import model.OrderInvoice;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * High-level service orchestrating Catalog AVL Tree, Cart History Stacks,
 * Knapsack Optimization, Carton Bin Packing, and Huffman Compression.
 */
public class ProcurementService {
    private AVLTree catalogTree;
    private Map<String, Item> catalogLookup; // Fast secondary key lookup
    private CartHistoryManager cartHistory;
    private double defaultBoxCapacity; // in kg (e.g. 10.0 kg)
    private double courierFeePerBox;   // in ₹ (e.g. ₹150.0)

    public ProcurementService() {
        this.catalogTree = new AVLTree();
        this.catalogLookup = new HashMap<>();
        this.cartHistory = new CartHistoryManager();
        this.defaultBoxCapacity = 10.0;
        this.courierFeePerBox = 150.0;

        // Load default preset: College Tech Fest & Hackathon
        loadPreset("TECH_FEST");
    }

    public synchronized void loadPreset(String presetKey) {
        catalogTree.clear();
        catalogLookup.clear();

        List<Item> initialItems = new ArrayList<>();

        if ("KIRANA_WHOLESALE".equalsIgnoreCase(presetKey)) {
            // Wholesale Kirana & Grocery Catalog (at least 2-3 products per filter category)
            // 1. Grains & Staples (3 items)
            addItemToCatalog(new Item("K01", "Basmati Rice 10kg", "Grains", 1200.0, 85, 10.0));
            addItemToCatalog(new Item("K02", "Wheat Flour Chakki 5kg", "Grains", 260.0, 70, 5.0));
            addItemToCatalog(new Item("K03", "Sona Masoori Rice 5kg", "Grains", 390.0, 78, 5.0));

            // 2. Cooking Oils & Ghee (3 items)
            addItemToCatalog(new Item("K04", "Refined Sunflower Oil 5L", "Oils", 780.0, 90, 4.6));
            addItemToCatalog(new Item("K05", "Pure Desi Cow Ghee 1L", "Oils", 650.0, 88, 1.1));
            addItemToCatalog(new Item("K06", "Cold-Pressed Mustard Oil 2L", "Oils", 380.0, 82, 1.9));

            // 3. Pulses & Lentils (3 items)
            addItemToCatalog(new Item("K07", "Toor Dal Premium 2kg", "Pulses", 310.0, 75, 2.0));
            addItemToCatalog(new Item("K08", "Moong Dal Washed 2kg", "Pulses", 280.0, 74, 2.0));
            addItemToCatalog(new Item("K09", "Kabuli Chana Special 2kg", "Pulses", 260.0, 72, 2.0));

            // 4. Pantry Essentials & Spices (3 items)
            addItemToCatalog(new Item("K10", "Refined Sugar 5kg", "Pantry", 240.0, 60, 5.0));
            addItemToCatalog(new Item("K11", "Iodized Salt (Pack of 5)", "Pantry", 125.0, 50, 5.0));
            addItemToCatalog(new Item("K12", "Turmeric & Garam Masala Pack", "Pantry", 290.0, 76, 0.8));

            // 5. Beverages & Refreshments (3 items)
            addItemToCatalog(new Item("K13", "Organic Assam Tea 1kg", "Beverages", 450.0, 80, 1.0));
            addItemToCatalog(new Item("K14", "Filter Coffee Blend 500g", "Beverages", 480.0, 78, 0.6));
            addItemToCatalog(new Item("K15", "Green Tea Detox Box (100 bags)", "Beverages", 520.0, 82, 0.5));

            // 6. Cleaning & Hygiene (3 items)
            addItemToCatalog(new Item("K16", "Concentrated Detergent 4kg", "Cleaning", 540.0, 65, 4.0));
            addItemToCatalog(new Item("K17", "Antibacterial Dish Gel 2L", "Cleaning", 320.0, 55, 2.2));
            addItemToCatalog(new Item("K18", "Surface Disinfectant Liquid 5L", "Cleaning", 490.0, 70, 5.1));

            // 7. Snacks & Dry Fruits (3 items)
            addItemToCatalog(new Item("K19", "Dry Fruits Royal Combo 1kg", "Snacks", 1100.0, 95, 1.1));
            addItemToCatalog(new Item("K20", "Roasted Cashew & Almonds 500g", "Snacks", 640.0, 88, 0.6));
            addItemToCatalog(new Item("K21", "Diet Multigrain Cookies Box", "Snacks", 220.0, 62, 0.9));
        } else {
            // Default: College Tech Fest & Event Supplies (at least 2-3 products per filter category)
            // 1. Hardware (3 items)
            addItemToCatalog(new Item("T01", "Raspberry Pi 5 Kit", "Hardware", 8500.0, 95, 0.7));
            addItemToCatalog(new Item("T02", "Arduino Mega Lab Bundles", "Hardware", 2400.0, 88, 1.2));
            addItemToCatalog(new Item("T03", "Precision Sensor Starter Kit", "Hardware", 1400.0, 76, 0.9));

            // 2. Electrical (3 items)
            addItemToCatalog(new Item("T04", "Heavy Duty Extension Spool 25m", "Electrical", 1650.0, 84, 4.5));
            addItemToCatalog(new Item("T05", "Industrial Surge Strip 10-Port", "Electrical", 1250.0, 78, 1.8));
            addItemToCatalog(new Item("T06", "Step-Down Power Converter 500W", "Electrical", 2100.0, 82, 3.2));

            // 3. Networking (3 items)
            addItemToCatalog(new Item("T07", "Cat6 Gigabit Ethernet Reel 100m", "Networking", 1950.0, 80, 5.1));
            addItemToCatalog(new Item("T08", "Gigabit Managed 16-Port Switch", "Networking", 3800.0, 88, 2.4));
            addItemToCatalog(new Item("T09", "Dual-Band Mesh WiFi 6 Router", "Networking", 2600.0, 82, 1.3));

            // 4. Displays & AV (3 items)
            addItemToCatalog(new Item("T10", "RGB LED Matrix Display Board", "Displays", 2800.0, 78, 2.3));
            addItemToCatalog(new Item("T11", "Ultra-Wide IPS Referee Monitor", "Displays", 6500.0, 89, 4.2));
            addItemToCatalog(new Item("T12", "Full HD Portable Projector", "Displays", 9800.0, 92, 3.8));

            // 5. Robotics & Automation (2 items)
            addItemToCatalog(new Item("T13", "Autonomous Rover Robot Kit", "Robotics", 4800.0, 90, 2.1));
            addItemToCatalog(new Item("T14", "Quadcopter Drone Training Rig", "Robotics", 5600.0, 93, 1.9));

            // 6. Tools & Lab Equipment (2 items)
            addItemToCatalog(new Item("T15", "Smart Soldering Workstation", "Tools", 3200.0, 86, 2.6));
            addItemToCatalog(new Item("T16", "Digital Oscilloscope 50MHz", "Tools", 7200.0, 91, 2.8));

            // 7. Supplies & Staging (3 items)
            addItemToCatalog(new Item("T17", "Rolling Whiteboard Easel", "Supplies", 3500.0, 74, 7.5));
            addItemToCatalog(new Item("T18", "Gaffer Tape Heavy Duty (Pack 4)", "Supplies", 720.0, 62, 1.4));
            addItemToCatalog(new Item("T19", "Hackathon Lanyard Badges (100pk)", "Supplies", 950.0, 68, 1.8));
        }

        // Initialize cart with default diverse items so visualizer and cartons are populated
        List<Item> all = catalogTree.inOrderList();
        if (all.size() >= 5) {
            initialItems.add(all.get(0));
            initialItems.add(all.get(1));
            initialItems.add(all.get(2));
            initialItems.add(all.get(3));
            if (all.size() >= 6) {
                initialItems.add(all.get(5));
            }
        }
        cartHistory.reset(initialItems);
    }

    public synchronized void addItemToCatalog(Item item) {
        catalogTree.insert(item);
        catalogLookup.put(item.getId(), item);
    }

    public synchronized boolean removeItemFromCatalog(String itemId) {
        Item item = catalogLookup.get(itemId);
        if (item != null) {
            boolean removed = catalogTree.delete(itemId, item.getPrice());
            if (removed) {
                catalogLookup.remove(itemId);
                return true;
            }
        }
        return false;
    }

    public List<Item> getCatalogInOrder() {
        return catalogTree.inOrderList();
    }

    /**
     * AVL Tree Price-Range Query
     */
    public List<Item> filterCatalogByPriceRange(double minPrice, double maxPrice) {
        return catalogTree.rangeSearch(minPrice, maxPrice);
    }

    public Map<String, Object> getCatalogTreeHierarchy() {
        return catalogTree.toVisualHierarchy();
    }

    public int getCatalogSize() {
        return catalogTree.size();
    }

    public int getCatalogTreeHeight() {
        return catalogTree.getTreeHeight();
    }

    // --- Cart Management with Stack ---

    public synchronized List<Item> getCurrentCart() {
        return cartHistory.getCurrentCart();
    }

    public synchronized void addItemToCart(String itemId) {
        Item item = catalogLookup.get(itemId);
        if (item != null) {
            List<Item> newCart = new ArrayList<>(cartHistory.getCurrentCart());
            newCart.add(item);
            cartHistory.execute(CartAction.Type.ADD_ITEM, "Added '" + item.getName() + "' to cart", newCart);
        }
    }

    public synchronized void removeItemFromCart(int index) {
        List<Item> current = cartHistory.getCurrentCart();
        if (index >= 0 && index < current.size()) {
            List<Item> newCart = new ArrayList<>(current);
            Item removed = newCart.remove(index);
            cartHistory.execute(CartAction.Type.REMOVE_ITEM, "Removed '" + removed.getName() + "' from cart", newCart);
        }
    }

    public synchronized void clearCart() {
        if (!cartHistory.getCurrentCart().isEmpty()) {
            cartHistory.execute(CartAction.Type.CLEAR_CART, "Cleared cart", Collections.emptyList());
        }
    }

    public synchronized boolean undoCart() {
        return cartHistory.undo();
    }

    public synchronized boolean redoCart() {
        return cartHistory.redo();
    }

    public CartHistoryManager getCartHistory() {
        return cartHistory;
    }

    // --- Knapsack Budget Optimization ---

    /**
     * Runs 0/1 Knapsack Optimizer on selected or all catalog items.
     * Optionally automatically updates the cart with undo tracking.
     */
    public synchronized KnapsackSolver.KnapsackResult optimizeBudget(List<String> candidateIds, double budget, boolean applyToCart) {
        List<Item> candidates = new ArrayList<>();
        if (candidateIds != null && !candidateIds.isEmpty()) {
            for (String id : candidateIds) {
                Item it = catalogLookup.get(id);
                if (it != null) candidates.add(it);
            }
        } else {
            // Default: all catalog items
            candidates = catalogTree.inOrderList();
        }

        KnapsackSolver.KnapsackResult result = KnapsackSolver.solve(candidates, budget);

        if (applyToCart && !result.getSelectedItems().isEmpty()) {
            cartHistory.execute(CartAction.Type.REPLACE_CART,
                    String.format("Knapsack DP Applied (Budget: ₹%.0f, %d items)", budget, result.getSelectedItems().size()),
                    result.getSelectedItems());
        }

        return result;
    }

    // --- Warehouse Carton Bin Packing ---

    public BinPackingSolver.ComparisonResult packCurrentCart(Double boxCapacity, Double customCourierFee) {
        double cap = (boxCapacity != null && boxCapacity > 0) ? boxCapacity : defaultBoxCapacity;
        double fee = (customCourierFee != null && customCourierFee >= 0) ? customCourierFee : courierFeePerBox;
        return BinPackingSolver.compareAll(cartHistory.getCurrentCart(), cap, fee);
    }

    // --- Order Checkout & Huffman Compression ---

    public synchronized CheckoutResult checkoutOrder(String customerName, String organization, Double boxCapacity, Double customCourierFee) {
        List<Item> cart = cartHistory.getCurrentCart();
        double cap = (boxCapacity != null && boxCapacity > 0) ? boxCapacity : defaultBoxCapacity;
        double fee = (customCourierFee != null && customCourierFee >= 0) ? customCourierFee : courierFeePerBox;

        // Perform FFD Bin Packing for shipping boxes
        BinPackingSolver.PackingResult packing = BinPackingSolver.packFFD(cart, cap, fee);

        // Generate Invoice
        String invId = "INV-" + (int)(100000 + Math.random() * 900000);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        OrderInvoice invoice = new OrderInvoice(invId, customerName, organization, timestamp, cart, packing.getBoxes(), fee);

        // Compress Raw Invoice Text with Huffman Coding
        HuffmanCompressor.CompressionResult compression = HuffmanCompressor.compress(invoice.getRawInvoiceText());

        return new CheckoutResult(invoice, packing, compression);
    }

    public static class CheckoutResult {
        private OrderInvoice invoice;
        private BinPackingSolver.PackingResult packingResult;
        private HuffmanCompressor.CompressionResult compressionResult;

        public CheckoutResult(OrderInvoice invoice, BinPackingSolver.PackingResult packingResult,
                              HuffmanCompressor.CompressionResult compressionResult) {
            this.invoice = invoice;
            this.packingResult = packingResult;
            this.compressionResult = compressionResult;
        }

        public OrderInvoice getInvoice() { return invoice; }
        public BinPackingSolver.PackingResult getPackingResult() { return packingResult; }
        public HuffmanCompressor.CompressionResult getCompressionResult() { return compressionResult; }
    }

    public double getDefaultBoxCapacity() { return defaultBoxCapacity; }
    public void setDefaultBoxCapacity(double defaultBoxCapacity) { this.defaultBoxCapacity = defaultBoxCapacity; }
    public double getCourierFeePerBox() { return courierFeePerBox; }
    public void setCourierFeePerBox(double courierFeePerBox) { this.courierFeePerBox = courierFeePerBox; }
}

