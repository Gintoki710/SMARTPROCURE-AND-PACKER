package model;

import java.util.List;

/**
 * Represents the finalized order and printable digital invoice.
 * Generates structured text that will be compressed via Huffman Coding.
 */
public class OrderInvoice {
    private String invoiceNumber;
    private String customerName;
    private String organization;
    private String timestamp;
    private List<Item> items;
    private List<Box> boxes;
    private double itemsTotalCost;
    private double totalWeight;
    private int totalUtility;
    private double courierFeePerBox;
    private double totalCourierFee;
    private double grandTotal;
    private String rawInvoiceText;

    public OrderInvoice(String invoiceNumber, String customerName, String organization, String timestamp,
                        List<Item> items, List<Box> boxes, double courierFeePerBox) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.organization = organization;
        this.timestamp = timestamp;
        this.items = items;
        this.boxes = boxes;
        this.courierFeePerBox = courierFeePerBox;

        // Calculate totals
        double cost = 0.0;
        double weight = 0.0;
        int utility = 0;
        for (Item item : items) {
            cost += item.getPrice();
            weight += item.getWeight();
            utility += item.getUtility();
        }
        this.itemsTotalCost = Math.round(cost * 100.0) / 100.0;
        this.totalWeight = Math.round(weight * 1000.0) / 1000.0;
        this.totalUtility = utility;
        this.totalCourierFee = Math.round(boxes.size() * courierFeePerBox * 100.0) / 100.0;
        this.grandTotal = Math.round((this.itemsTotalCost + this.totalCourierFee) * 100.0) / 100.0;

        this.rawInvoiceText = generateRawInvoiceText();
    }

    private String generateRawInvoiceText() {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("         SMART PROCUREMENT & WAREHOUSE DISPATCH             \n");
        sb.append("                 DIGITAL ORDER INVOICE                      \n");
        sb.append("============================================================\n");
        sb.append("Invoice No : ").append(invoiceNumber).append("\n");
        sb.append("Date & Time: ").append(timestamp).append("\n");
        sb.append("Customer   : ").append(customerName).append("\n");
        sb.append("Org/Purpose: ").append(organization).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-4s %-24s %-10s %-8s %-8s\n", "No.", "Item Description", "Price(INR)", "Util(pt)", "Weight"));
        sb.append("------------------------------------------------------------\n");

        int idx = 1;
        for (Item item : items) {
            sb.append(String.format("%-4d %-24s %-10.2f %-8d %-7.2fkg\n",
                    idx++,
                    item.getName().length() > 24 ? item.getName().substring(0, 21) + "..." : item.getName(),
                    item.getPrice(),
                    item.getUtility(),
                    item.getWeight()));
        }

        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("Procurement Item Cost:  INR %.2f\n", itemsTotalCost));
        sb.append(String.format("Total Value / Utility:  %d points\n", totalUtility));
        sb.append(String.format("Total Physical Weight:  %.2f kg\n", totalWeight));
        sb.append("------------------------------------------------------------\n");
        sb.append("WAREHOUSE CARTON PACKAGING BREAKDOWN:\n");
        for (Box b : boxes) {
            sb.append(String.format(" * Box #%d: %.2f/%.2f kg (%.1f%% full) - %d items packed\n",
                    b.getBoxId(), b.getCurrentWeight(), b.getMaxCapacity(), b.getUtilizationPercentage(), b.getItems().size()));
        }
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("Total Cartons Used   :  %d standard boxes\n", boxes.size()));
        sb.append(String.format("Courier Rate / Carton:  INR %.2f\n", courierFeePerBox));
        sb.append(String.format("Shipping Courier Fee :  INR %.2f\n", totalCourierFee));
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("FINAL PAYABLE AMOUNT :  INR %.2f\n", grandTotal));
        sb.append("============================================================\n");
        sb.append("  Algorithmic Verification: 0/1 Knapsack & Bin Packing FFD  \n");
        sb.append("  Compressed with Huffman Coding (Experiment 14 / Mod 4)    \n");
        sb.append("============================================================\n");
        return sb.toString();
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public double getItemsTotalCost() {
        return itemsTotalCost;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public int getTotalUtility() {
        return totalUtility;
    }

    public double getCourierFeePerBox() {
        return courierFeePerBox;
    }

    public double getTotalCourierFee() {
        return totalCourierFee;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public String getRawInvoiceText() {
        return rawInvoiceText;
    }
}

