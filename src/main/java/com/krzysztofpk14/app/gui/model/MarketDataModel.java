package com.krzysztofpk14.app.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for market data.
 */
public class MarketDataModel {
    private final StringProperty symbol = new SimpleStringProperty();
    private final StringProperty price = new SimpleStringProperty();
    private final StringProperty timestamp = new SimpleStringProperty();
    
    public MarketDataModel(String symbol, String price, String timestamp) {
        setSymbol(symbol);
        setPrice(price);
        setTimestamp(timestamp);
    }
    
    public String getSymbol() {
        return symbol.get();
    }
    
    public void setSymbol(String symbol) {
        this.symbol.set(symbol);
    }
    
    public StringProperty symbolProperty() {
        return symbol;
    }
    
    public String getPrice() {
        return price.get();
    }
    
    public void setPrice(String price) {
        this.price.set(price);
    }
    
    public StringProperty priceProperty() {
        return price;
    }
    
    public String getTimestamp() {
        return timestamp.get();
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp.set(timestamp);
    }
    
    public StringProperty timestampProperty() {
        return timestamp;
    }
}