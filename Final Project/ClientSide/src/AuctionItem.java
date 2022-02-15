/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

import java.util.ArrayList;

public class AuctionItem {

    private final double buyNow;
    private final String itemName;
    private final String itemDescription;
    private double minPrice;
    private String status;
    private String highestBidderName;
    private final ArrayList<String> history;

    public AuctionItem (double buyNow, String itemName, String itemDescription, double minPrice) {
        this.buyNow = buyNow;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.minPrice = minPrice;
        this.status = "Item is available!";
        this.highestBidderName = "none";
        this.history = new ArrayList<>();
    }

    public double getBuyNow() { return buyNow; }

    public String getItemName() { return itemName; }

    public String getItemDescription() { return this.itemDescription; }

    public double getMinPrice() { return minPrice; }

    public String getStatus() { return this.status; }

    public String getHighestBidderName() { return highestBidderName; }

    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }

    public void setStatus(String status) { this.status = status; }

    public void setHighestBidderName(String highestBidderName) { this.highestBidderName = highestBidderName; }

} 