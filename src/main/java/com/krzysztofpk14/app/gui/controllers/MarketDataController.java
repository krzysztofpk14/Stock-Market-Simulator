package com.krzysztofpk14.app.gui.controllers;

import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.model.MarketDataModel;
import com.krzysztofpk14.app.gui.model.TradingAppModel;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Controller for Market Data tab.
 */
public class MarketDataController {
    private TradingAppModel model;
    private TableView<MarketDataModel> marketDataTable;
    
    public MarketDataController(TradingAppModel model) {
        this.model = model;
    }
    
    /**
     * Creates the Market Data tab.
     */
    public Tab createTab() {
        Tab tab = new Tab("Market Data");
        tab.setClosable(false);
        
        marketDataTable = createMarketDataTable();
        
        // Market data control panel
        HBox controlBox = new HBox(10);
        controlBox.setPadding(new Insets(10));
        
        ComboBox<String> symbolCombo = new ComboBox<>();
        symbolCombo.setId("chartSymbolComboBox");
        symbolCombo.setPrefWidth(120);
        symbolCombo.setPromptText("Select Symbol");
        
        // Bind available symbols
        model.availableSymbolsProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                symbolCombo.setItems(FXCollections.observableArrayList(newVal));
                if (!newVal.isEmpty()) {
                    symbolCombo.setValue(newVal.get(0));
                }
            }
        });
        
        Button subscribeButton = new Button("Subscribe");
        subscribeButton.setOnAction(e -> {
            String symbol = symbolCombo.getValue();
            if (symbol != null && !symbol.isEmpty() && model.getClient() != null) {
                // Request subscription using the model's client
                subscribeToMarketData(symbol);
            }
        });
        
        controlBox.getChildren().addAll(new Label("Symbol:"), symbolCombo, subscribeButton);
        
        // Add components to a layout
        BorderPane pane = new BorderPane();
        pane.setTop(controlBox);
        pane.setCenter(marketDataTable);
        
        tab.setContent(pane);
        return tab;
    }
    
    /**
     * Creates the market data table.
     */
    private TableView<MarketDataModel> createMarketDataTable() {
        TableView<MarketDataModel> table = new TableView<>();
        
        TableColumn<MarketDataModel, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        symbolCol.setPrefWidth(100);
        
        TableColumn<MarketDataModel, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        priceCol.setPrefWidth(100);
        
        TableColumn<MarketDataModel, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
        timestampCol.setPrefWidth(200);
        
        table.getColumns().addAll(symbolCol, priceCol, timestampCol);
        table.setItems(model.getMarketData());
        
        return table;
    }
    
    /**
     * Updates the market data display with new data.
     */
    public void updateMarketData(MarketDataResponse data) {
        if (data.getInstrument() == null || data.getMarketDataGroups() == null || 
            data.getMarketDataGroups().isEmpty()) {
            return;
        }
        
        String symbol = data.getInstrument().getSymbol();
        String price = data.getMarketDataGroups().get(0).getPrice();
        String timestamp = data.getMarketDataGroups().get(0).getTime();
        
        if (price == null) return;
        
        // Update or add market data entry
        boolean found = false;
        for (MarketDataModel entry : model.getMarketData()) {
            if (entry.getSymbol().equals(symbol)) {
                entry.setPrice(price);
                entry.setTimestamp(timestamp);
                found = true;
                break;
            }
        }
        
        if (!found) {
            model.getMarketData().add(new MarketDataModel(symbol, price, timestamp));
        }
        
        // Update price data in the model
        model.addPriceDataPoint(symbol, Double.parseDouble(price), System.currentTimeMillis());
    }
    
    /**
     * Subscribes to market data for a symbol.
     */
    private void subscribeToMarketData(String symbol) {
        if (model.getClient() != null) {
            // This would typically call a service method
            // For now we'll just create a request directly
            com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest request = 
                    new com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest();
            
            request.setRequestId(java.util.UUID.randomUUID().toString());
            request.setSubscriptionRequestType("1"); // Subscribe
            request.addInstrument(symbol);
            
            try {
                model.getClient().subscribeMarketData(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}