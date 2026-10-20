package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dsa.binpacking.BinPackingSolver;
import dsa.huffman.HuffmanCompressor;
import dsa.knapsack.KnapsackSolver;
import model.Item;
import service.ProcurementService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Embedded HTTP Server for Smart Procurement Budgeter & Warehouse Box Packer.
 * Runs on standard Java with zero external dependencies.
 */
public class HttpServerApp {
    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private final ProcurementService service;
    private final Path webRootDir;
    private HttpServer server;

    public HttpServerApp(int port, Path webRootDir) {
        this.port = port;
        this.webRootDir = webRootDir;
        this.service = new ProcurementService();
    }

    public void start() throws IOException {
        int activePort = port;
        while (activePort < port + 10) {
            try {
                server = HttpServer.create(new InetSocketAddress(activePort), 0);
                break;
            } catch (IOException e) {
                activePort++;
            }
        }

        if (server == null) {
            throw new IOException("Could not bind HTTP server to any port in range " + port + "-" + (port + 10));
        }

        // Register API handlers
        server.createContext("/api/status", this::handleStatus);
        server.createContext("/api/presets/load", this::handleLoadPreset);
        server.createContext("/api/catalog/tree", this::handleCatalogTree);
        server.createContext("/api/catalog/item", this::handleCatalogItem);
        server.createContext("/api/catalog", this::handleCatalog);
        server.createContext("/api/cart/add", this::handleCartAdd);
        server.createContext("/api/cart/remove", this::handleCartRemove);
        server.createContext("/api/cart/clear", this::handleCartClear);
        server.createContext("/api/cart/undo", this::handleCartUndo);
        server.createContext("/api/cart/redo", this::handleCartRedo);
        server.createContext("/api/cart", this::handleCartGet);
        server.createContext("/api/knapsack", this::handleKnapsack);
        server.createContext("/api/binpack", this::handleBinPack);
        server.createContext("/api/checkout", this::handleCheckout);
        server.createContext("/api/huffman/custom", this::handleCustomHuffman);

        // Static files handler (serves HTML, CSS, JS)
        server.createContext("/", this::handleStaticFiles);

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("==================================================================");
        System.out.println("  SMART PROCUREMENT BUDGETER & WAREHOUSE BOX PACKER SERVER");
        System.out.println("  DSA Laboratory Capstone System (Java + Web Dashboard)");
        System.out.println("==================================================================");
        System.out.println("  URL: http://localhost:" + activePort);
        System.out.println("  Algorithms Active:");
        System.out.println("    - Module 5 / Exp 15: 0/1 Knapsack DP + Backtracking");
        System.out.println("    - Module 4: Bin Packing (FFD, BFD, Next Fit, Worst Fit)");
        System.out.println("    - Module 4 / Exp 14: Huffman Coding (Compress & Lossless Decode)");
        System.out.println("    - Module 2 / Exp 11 & 12: AVL Tree (Self-balancing BST & Range Query)");
        System.out.println("    - Module 1 / Exp 1: Stack ADT (Cart History Undo / Redo)");
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ==========================================
    // API HANDLERS
    // ==========================================

    private void handleStatus(HttpExchange exchange) throws IOException {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("catalogCount", service.getCatalogSize());
        status.put("catalogTreeHeight", service.getCatalogTreeHeight());
        status.put("cartItemCount", service.getCurrentCart().size());
        status.put("boxCapacity", service.getDefaultBoxCapacity());
        status.put("courierFeePerBox", service.getCourierFeePerBox());
        sendJsonResponse(exchange, 200, status);
    }

    private void handleLoadPreset(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
        String preset = (String) req.getOrDefault("preset", "TECH_FEST");
        service.loadPreset(preset);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("preset", preset);
        res.put("catalog", service.getCatalogInOrder());
        res.put("cart", service.getCurrentCart());
        sendJsonResponse(exchange, 200, res);
    }

    private void handleCatalog(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
            List<Item> items;
            if (query.containsKey("minPrice") || query.containsKey("maxPrice")) {
                double minPrice = query.containsKey("minPrice") ? Double.parseDouble(query.get("minPrice")) : 0.0;
                double maxPrice = query.containsKey("maxPrice") ? Double.parseDouble(query.get("maxPrice")) : Double.MAX_VALUE;
                items = service.filterCatalogByPriceRange(minPrice, maxPrice);
            } else {
                items = service.getCatalogInOrder();
            }

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("items", items);
            res.put("count", items.size());
            res.put("treeHeight", service.getCatalogTreeHeight());
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCatalogTree(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("tree", service.getCatalogTreeHierarchy());
            res.put("nodeCount", service.getCatalogSize());
            res.put("height", service.getCatalogTreeHeight());
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCatalogItem(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            String id = (String) req.getOrDefault("id", "CUST-" + (int)(100 + Math.random() * 900));
            String name = (String) req.getOrDefault("name", "Custom Item");
            String category = (String) req.getOrDefault("category", "General");
            double price = ((Number) req.getOrDefault("price", 100.0)).doubleValue();
            int utility = ((Number) req.getOrDefault("utility", 50)).intValue();
            double weight = ((Number) req.getOrDefault("weight", 1.0)).doubleValue();

            Item item = new Item(id, name, category, price, utility, weight);
            service.addItemToCatalog(item);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", true);
            res.put("item", item);
            res.put("catalogCount", service.getCatalogSize());
            sendJsonResponse(exchange, 201, res);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            String id = (String) req.get("id");
            boolean ok = service.removeItemFromCatalog(id);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", ok);
            res.put("catalogCount", service.getCatalogSize());
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCartGet(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 200, buildCartResponseData());
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCartAdd(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            String itemId = (String) req.get("itemId");
            service.addItemToCart(itemId);
            sendJsonResponse(exchange, 200, buildCartResponseData());
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCartRemove(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            int index = ((Number) req.getOrDefault("index", 0)).intValue();
            service.removeItemFromCart(index);
            sendJsonResponse(exchange, 200, buildCartResponseData());
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCartClear(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            service.clearCart();
            sendJsonResponse(exchange, 200, buildCartResponseData());
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCartUndo(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            boolean success = service.undoCart();
            Map<String, Object> res = buildCartResponseData();
            res.put("actionExecuted", success);
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCartRedo(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            boolean success = service.redoCart();
            Map<String, Object> res = buildCartResponseData();
            res.put("actionExecuted", success);
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private Map<String, Object> buildCartResponseData() {
        List<Item> cart = service.getCurrentCart();
        double cost = 0.0;
        int utility = 0;
        double weight = 0.0;
        for (Item it : cart) {
            cost += it.getPrice();
            utility += it.getUtility();
            weight += it.getWeight();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", cart);
        data.put("totalCost", Math.round(cost * 100.0) / 100.0);
        data.put("totalUtility", utility);
        data.put("totalWeight", Math.round(weight * 1000.0) / 1000.0);
        data.put("itemCount", cart.size());
        data.put("canUndo", service.getCartHistory().canUndo());
        data.put("canRedo", service.getCartHistory().canRedo());
        data.put("undoCount", service.getCartHistory().getUndoCount());
        data.put("redoCount", service.getCartHistory().getRedoCount());
        data.put("undoStack", service.getCartHistory().getUndoStackSnapshot());
        data.put("redoStack", service.getCartHistory().getRedoStackSnapshot());
        return data;
    }

    @SuppressWarnings("unchecked")
    private void handleKnapsack(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            double budget = ((Number) req.getOrDefault("budget", 10000.0)).doubleValue();
            boolean applyToCart = Boolean.TRUE.equals(req.get("applyToCart"));

            List<String> candidateIds = null;
            if (req.containsKey("candidateIds") && req.get("candidateIds") instanceof List) {
                candidateIds = (List<String>) req.get("candidateIds");
            }

            KnapsackSolver.KnapsackResult result = service.optimizeBudget(candidateIds, budget, applyToCart);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("result", result);
            res.put("cart", buildCartResponseData());
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleBinPack(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            Double capacity = null;
            Double fee = null;
            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> query = parseQueryParams(exchange.getRequestURI().getRawQuery());
                if (query.containsKey("capacity")) {
                    try { capacity = Double.parseDouble(query.get("capacity")); } catch (Exception ignored) {}
                }
                if (query.containsKey("fee")) {
                    try { fee = Double.parseDouble(query.get("fee")); } catch (Exception ignored) {}
                }
            } else {
                Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
                if (req.containsKey("boxCapacity")) capacity = ((Number) req.get("boxCapacity")).doubleValue();
                if (req.containsKey("courierFee")) fee = ((Number) req.get("courierFee")).doubleValue();
            }

            BinPackingSolver.ComparisonResult comparison = service.packCurrentCart(capacity, fee);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("comparison", comparison);
            res.put("cartSummary", buildCartResponseData());
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCheckout(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            String customerName = (String) req.getOrDefault("customerName", "Valued Client");
            String organization = (String) req.getOrDefault("organization", "Procurement Operations");
            Double capacity = req.containsKey("boxCapacity") ? ((Number) req.get("boxCapacity")).doubleValue() : null;
            Double fee = req.containsKey("courierFee") ? ((Number) req.get("courierFee")).doubleValue() : null;

            ProcurementService.CheckoutResult checkout = service.checkoutOrder(customerName, organization, capacity, fee);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("invoice", checkout.getInvoice());
            res.put("packing", checkout.getPackingResult());
            res.put("compression", checkout.getCompressionResult());
            sendJsonResponse(exchange, 200, res);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleCustomHuffman(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> req = SimpleJson.parseObject(readRequestBody(exchange));
            String text = (String) req.getOrDefault("text", "Smart Procurement DSA Project");
            HuffmanCompressor.CompressionResult result = HuffmanCompressor.compress(text);
            sendJsonResponse(exchange, 200, result);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    // ==========================================
    // STATIC FILE SERVER
    // ==========================================

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Prevent directory traversal attacks
        Path resolved = webRootDir.resolve(path.substring(1)).normalize();
        if (!resolved.startsWith(webRootDir) || !Files.exists(resolved) || Files.isDirectory(resolved)) {
            // Serve 404 or fallback to index.html for SPA
            resolved = webRootDir.resolve("index.html");
            if (!Files.exists(resolved)) {
                String notFound = "<h1>404 Not Found</h1>";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }
        }

        String mimeType = getMimeType(resolved.toString());
        byte[] bytes = Files.readAllBytes(resolved);

        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = SimpleJson.stringify(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        Map<String, String> err = Collections.singletonMap("error", "Method Not Allowed");
        sendJsonResponse(exchange, 405, err);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) return Collections.emptyMap();
        Map<String, String> map = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    // ==========================================
    // MAIN ENTRYPOINT
    // ==========================================

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Web directory resolution relative to project root
        Path webDir = Paths.get("web");
        if (!Files.exists(webDir)) {
            // Check fallback for current directory
            Path parentWeb = Paths.get("../web");
            if (Files.exists(parentWeb)) {
                webDir = parentWeb;
            }
        }

        try {
            HttpServerApp app = new HttpServerApp(port, webDir.toAbsolutePath());
            app.start();
            // Keep main thread alive so the HTTP server process stays active
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Fatal Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
