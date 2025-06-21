package com.krzysztofpk14.app.gui.model;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.strategy.StrategyManager;
import com.krzysztofpk14.app.gui.util.ApiCommunicationLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for the Trading Application.
 */
public class TradingAppModel {
    private BossaApiClient client;
    private StrategyManager strategyManager;
    private final ApiCommunicationLogger apiLogger = new ApiCommunicationLogger(); 
    
    // Observable collections for UI binding
    private final ObservableList<MarketDataModel> marketData = FXCollections.observableArrayList();
    private final ObservableList<OrderModel> orders = FXCollections.observableArrayList();
    private final ObservableList<StrategyModel> strategies = FXCollections.observableArrayList();
    private final SimpleObjectProperty<List<String>> availableSymbols = 
            new SimpleObjectProperty<>(FXCollections.observableArrayList());
           
    
    // Price data for charting
    private final Map<String, List<PriceDataPoint>> priceData = new HashMap<>();
    
    public BossaApiClient getClient() {
        return client;
    }
    
    public void setClient(BossaApiClient client) {
        this.client = client;
    }
    
    public StrategyManager getStrategyManager() {
        return strategyManager;
    }
    
    public void setStrategyManager(StrategyManager strategyManager) {
        this.strategyManager = strategyManager;
    }
    
    public ObservableList<MarketDataModel> getMarketData() {
        return marketData;
    }
    
    public ObservableList<OrderModel> getOrders() {
        return orders;
    }
    
    public ObservableList<StrategyModel> getStrategies() {
        return strategies;
    }
    
    public List<String> getAvailableSymbols() {
        return availableSymbols.get();
    }
    
    public SimpleObjectProperty<List<String>> availableSymbolsProperty() {
        return availableSymbols;
    }
    
    public void setAvailableSymbols(List<String> symbols) {
        this.availableSymbols.set(FXCollections.observableArrayList(symbols));
    }
    
    public Map<String, List<PriceDataPoint>> getPriceData() {
        return priceData;
    }
    
    /**
     * Add price data point for a symbol.
     */
    public void addPriceDataPoint(String symbol, double price, long timestamp) {
        if (!priceData.containsKey(symbol)) {
            priceData.put(symbol, FXCollections.observableArrayList());
        }
        
        List<PriceDataPoint> symbolData = priceData.get(symbol);

        // Use static counter for index if needed
        int index = symbolData.size();
        symbolData.add(new PriceDataPoint(timestamp, price, index));
        
        // Keep a limited number of points
        if (symbolData.size() > 1000) {
            symbolData.remove(0);
        }
    }

    /**
     * Gets the API communication logger.
     * 
     * @return The API logger
     */
    public ApiCommunicationLogger getApiLogger() {
        return apiLogger;
    }
}