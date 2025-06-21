package com.krzysztofpk14.app.gui.model;

import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for order.
 */
public class OrderModel {
    private final StringProperty orderId = new SimpleStringProperty();
    private final StringProperty symbol = new SimpleStringProperty();
    private final StringProperty side = new SimpleStringProperty();
    private final StringProperty price = new SimpleStringProperty();
    private final StringProperty quantity = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty execType = new SimpleStringProperty();
    private final StringProperty time = new SimpleStringProperty();
    
    public OrderModel(String orderId, String symbol, String side, String price,
                      String quantity, String status, String execType, String time) {
        setOrderId(orderId);
        setSymbol(symbol);
        
        if ("1".equals(side)) {
            setSide("BUY");
        } else if ("2".equals(side)) {
            setSide("SELL");
        } else {
            setSide(side);
        }                 

        setPrice(price);
        setQuantity(quantity);
        setStatus(convertOrderStatus(status));
        setExecType(convertExecutionType(execType));
        setTime(time);
    }
    

    /**
     * Converts numeric order status to human-readable string.
     */
    private String convertOrderStatus(String status) {
        if (status == null) return "Unknown";
        
        switch (status) {
            case ExecutionReport.NEW_ORDER: return "New Order";
            case ExecutionReport.ACTIVE: return "Active";
            case ExecutionReport.DONE: return "Done";
            case ExecutionReport.CANCELED: return "Canceled";
            case ExecutionReport.REJECTED_ORDER: return "Order Rejected";
            case ExecutionReport.ARCHIVED: return "Archived";
            case ExecutionReport.DURING_MODIFICATION: return "During modification";
            case ExecutionReport.ORDER_DURING_CANCELATION: return "Canceling";
            default: return status;
        }
    }

    /**
     * Converts numeric execution type to human-readable string.
     */
    private String convertExecutionType(String execType) {
        if (execType == null) return "Unknown";
        
        switch (execType) {
            case ExecutionReport.NEW: return "New";
            case ExecutionReport.TRANSACTION: return "Trade";
            case ExecutionReport.CANCELING: return "Canceled";
            case ExecutionReport.MODIFICATION: return "Modified";
            case ExecutionReport.DURING_CANCELATION: return "Canceling";
            case ExecutionReport.REJECTED: return "Rejected";
            case ExecutionReport.ORDER_STATUS: return "Order Status";
            default: return execType;
        }
    }

    // Getters and setters
    public String getOrderId() {
        return orderId.get();
    }
    
    public void setOrderId(String orderId) {
        this.orderId.set(orderId);
    }
    
    public StringProperty orderIdProperty() {
        return orderId;
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
    
    public String getSide() {
        return side.get();
    }
    
    public void setSide(String side) {
        this.side.set(side);
    }
    
    public StringProperty sideProperty() {
        return side;
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
    
    public String getQuantity() {
        return quantity.get();
    }
    
    public void setQuantity(String quantity) {
        this.quantity.set(quantity);
    }
    
    public StringProperty quantityProperty() {
        return quantity;
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(convertOrderStatus(status));
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public String getExecType() {
        return execType.get();
    }
    
    public void setExecType(String execType) {
        this.execType.set(convertExecutionType(execType));
    }
    
    public StringProperty execTypeProperty() {
        return execType;
    }
    
    public String getTime() {
        return time.get();
    }
    
    public void setTime(String time) {
        this.time.set(time);
    }
    
    public StringProperty timeProperty() {
        return time;
    }
}