package com.krzysztofpk14.app.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for strategy statistics.
 */
public class StrategyModel {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty trades = new SimpleStringProperty();
    private final StringProperty winRate = new SimpleStringProperty();
    private final StringProperty pnl = new SimpleStringProperty();
    
    public StrategyModel(String name, String status, String trades, String winRate, String pnl) {
        setName(name);
        setStatus(status);
        setTrades(trades);
        setWinRate(winRate);
        setPnl(pnl);
    }
    
    // Getters and setters
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public String getTrades() {
        return trades.get();
    }
    
    public void setTrades(String trades) {
        this.trades.set(trades);
    }
    
    public StringProperty tradesProperty() {
        return trades;
    }
    
    public String getWinRate() {
        return winRate.get();
    }
    
    public void setWinRate(String winRate) {
        this.winRate.set(winRate);
    }
    
    public StringProperty winRateProperty() {
        return winRate;
    }
    
    public String getPnl() {
        return pnl.get();
    }
    
    public void setPnl(String pnl) {
        this.pnl.set(pnl);
    }
    
    public StringProperty pnlProperty() {
        return pnl;
    }
}