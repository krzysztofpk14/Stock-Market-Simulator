package com.krzysztofpk14.app.gui.model;

// /**
//  * Model for price data point.
//  */
// public class PriceDataPoint {
//     private final long timestamp;
//     private final double price;
//     private final int index;
    
//     public PriceDataPoint(long timestamp, double price) {
//         this.timestamp = timestamp;
//         this.price = price;
//     }
    
//     public long getTimestamp() {
//         return timestamp;
//     }
    
//     public double getPrice() {
//         return price;
//     }
// }



/**
 * Model for storing price data points with timestamps
 */
public class PriceDataPoint {
    private final long timestamp;
    private final double price;
    private final int index;
    
    public PriceDataPoint(long timestamp, double price, int index) {
        this.timestamp = timestamp;
        this.price = price;
        this.index = index;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public double getPrice() {
        return price;
    }
    
    public int getIndex() {
        return index;
    }
}