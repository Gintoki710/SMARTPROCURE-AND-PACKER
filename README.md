# DSA Project: Smart Procurement Budgeter & Warehouse Box Packer
> **A Comprehensive DSA Capstone Project**  
> *Combining 0/1 Knapsack (DP), Bin Packing (Approximation), Self-Balancing AVL Trees, Stack ADT (Undo/Redo), and Huffman Coding (Greedy Compression)*

---

## 📌 1. The Genuine Real-World Problem
Small shopkeepers, wholesale kirana merchants, and college fest organizers face a dual logistical bottleneck whenever procuring inventory:
1. **Financial Optimization**: How to allocate a strict cash budget (e.g. ₹10,000) across competing supplies of varying prices and utility/profit scores to achieve maximum overall value without exceeding cash on hand.
2. **Physical Packing & Courier Economy**: Once items are purchased, they must be shipped in standard warehouse cartons (e.g., max 10 kg each). Poor packing leads to partially empty boxes and ballooning courier freight expenses (couriers bill per carton).
3. **Auditability & Digital Archive**: Orders must be searched rapidly by price range, edited with full undo/redo transaction safety, and compressed for bandwidth-efficient archival transmission over low-speed networks.

---

## 🚀 2. How the Syllabus Algorithms Work Together

```
                  ┌──────────────────────────────────────────────┐
                  │       Catalog AVL Tree (Exp 11 & 12)         │
                  │   O(log N) Search & O(K + log N) Range Query │
                  └──────────────────────┬───────────────────────┘
                                         │ Filter Candidate Items
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │      0/1 Knapsack Problem (Module 5/Exp 15)  │
                  │      Dynamic Programming Maximizes Utility   │
                  └──────────────────────┬───────────────────────┘
                                         │ Optimal Cart Generated
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │         Cart Stack ADT (Exp 1)               │
                  │     Undo & Redo State Transaction History    │
                  └──────────────────────┬───────────────────────┘
                                         │ Finalized Items & Weights
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │     Warehouse Bin Packing (Module 4)         │
                  │  First Fit Decreasing (FFD) Minimizes Boxes  │
                  └──────────────────────┬───────────────────────┘
                                         │ Packed Shipping Cartons
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │      Huffman Coding (Module 4 / Exp 14)      │
                  │   Optimal Prefix Code Compresses Invoices    │
                  └──────────────────────────────────────────────┘
```

| Algorithm / Data Structure | Syllabus Reference | Real-World Role in Project | Complexity |
| :--- | :--- | :--- | :--- |
| **0/1 Knapsack (Dynamic Programming)** | Module 5 / Exp 15 | Selects the optimal set of supplies maximizing total resale/utility score under cash budget limit. | Time: $O(n \cdot B)$, Space: $O(n \cdot B)$ |
| **Bin Packing (FFD / BFD / NF / WFD)** | Module 4 | Packs items by weight into minimum standard shipping cartons ($\le 10$ kg) to minimize per-box courier fees. | Time: $O(n \log n + n \cdot m)$ |
| **AVL Tree (Self-Balancing BST)** | Module 2 / Exp 11 & 12 | Stores catalog keyed by price with $|h_L - h_R| \le 1$. Enables pruned in-order price-range queries. | Query: $O(K + \log n)$, Insert: $O(\log n)$ |
| **Huffman Coding (Greedy Min-Heap)** | Module 4 / Exp 14 | Builds variable-length prefix tree to compress printable invoice text by 40-50% with lossless decoding. | Tree Build: $O(k \log k)$ |
| **Custom Stack ADT (LIFO)** | Module 1 / Exp 1 | Maintains Undo & Redo transaction history stacks for cart mutations with Top-of-Stack (TOS) pointers. | Push/Pop: $O(1)$ |

---

## 🛠️ 3. Technology Stack
- **Language**: Java 26 Native (Standard Library `com.sun.net.httpserver.HttpServer`).
- **Dependencies**: **Zero external JARs / Maven plugins required**. Runs everywhere standard Java is installed.
- **Frontend**: Classy, responsive Single Page Application (SPA) with CSS Glassmorphism, interactive SVG/DOM AVL Tree visualizer, live Knapsack DP decision matrix, 3D shipping carton representations, and Huffman bitstream monitors.

---

## ⚡ 4. How to Run

### Option A: Using Windows Batch Script (Fastest)
Double-click `build_and_run.bat` or run:
```cmd
build_and_run.bat
```

### Option B: Using PowerShell
```powershell
.\build_and_run.ps1
```

### Option C: Manual Compilation & Execution
```bash
# 1. Compile Java files
javac -d bin (Get-ChildItem -Path src -Recurse -Filter *.java).FullName

# 2. Run the HTTP Server
java -cp bin server.HttpServerApp 8080
```
Open **[http://localhost:8080](http://localhost:8080)** in any modern browser!

---

## 🎓 5. Viva Defense & Lab Evaluation Q&A

### Q1: Why can't we use a Greedy approach for 0/1 Knapsack?
> **Answer**: In Fractional Knapsack, items can be split continuously, so picking items in descending order of utility-to-price ratio ($v_i/c_i$) achieves global optimality ($O(n \log n)$). However, in 0/1 Knapsack, items are discrete (all-or-nothing). A greedy choice can leave large unusable budget gaps, missing a combination of slightly lower-ratio items that sum up closer to the budget. Dynamic Programming solves this by evaluating optimal substructure and overlapping subproblems via the recurrence $dp[i][w] = \max(dp[i-1][w], dp[i-1][w - c_i] + v_i)$.

### Q2: What is the approximation guarantee of First Fit Decreasing (FFD) in Bin Packing?
> **Answer**: Bin Packing is NP-hard. FFD sorts items in descending order of weight and places each item in the first box with sufficient remaining capacity. Johnson (1973) proved that FFD uses at most:
> $$\text{FFD}(I) \le \frac{11}{9}\text{OPT}(I) + \frac{6}{9}$$
> This guarantees that FFD uses within ~22% of the theoretical absolute minimum bins, saving significant courier dispatch charges over naive Next Fit ($2 \cdot \text{OPT}$).

### Q3: How does the AVL Tree maintain $O(\log n)$ height during dynamic catalog insertions?
> **Answer**: The AVL tree checks the balance factor $\text{BF}(node) = \text{height}(left) - \text{height}(right)$ upon backtracking from an insertion. If $|\text{BF}| > 1$, it executes one of four rotations:
> 1. **Left-Left (LL)**: Single Right Rotation.
> 2. **Right-Right (RR)**: Single Left Rotation.
> 3. **Left-Right (LR)**: Left Rotate left child, then Right Rotate parent.
> 4. **Right-Left (RL)**: Right Rotate right child, then Left Rotate parent.

### Q4: Why is Huffman Coding guaranteed to be lossless and prefix-free?
> **Answer**: Since all data characters reside strictly at the leaf nodes of the binary tree, no leaf can be an ancestor of another leaf. Consequently, no assigned binary code can be a prefix of any other code. Decoding is unambiguous: beginning at the root, each '0' traverses left and '1' traverses right until a leaf is encountered.

