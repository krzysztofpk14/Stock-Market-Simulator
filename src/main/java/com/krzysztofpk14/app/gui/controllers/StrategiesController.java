package com.krzysztofpk14.app.gui.controllers;

import com.krzysztofpk14.app.gui.model.StrategyModel;
import com.krzysztofpk14.app.gui.model.TradingAppModel;
import com.krzysztofpk14.app.strategy.InvestmentStrategy;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Controller for Strategies tab.
 */
public class StrategiesController {
    private TradingAppModel model;
    private TableView<StrategyModel> strategyTable;
    private Button startStopButton;
    private TextArea strategyDetailsArea;
    
    public StrategiesController(TradingAppModel model) {
        this.model = model;
    }
    
    /**
     * Creates the Strategies tab.
     */
    public Tab createTab() {
        Tab tab = new Tab("Strategies");
        tab.setClosable(false);
        
        BorderPane pane = new BorderPane();
        
        // Strategy controls
        HBox controlBox = createControlBox();
        
        // Create a split pane to divide strategy table and details
        SplitPane splitPane = new SplitPane();
        
        // Strategy statistics table
        strategyTable = createStrategyTable();
        
        // Strategy details panel
        VBox detailsPane = createStrategyDetailsPane();
        
        // Add both components to the split pane
        splitPane.getItems().addAll(strategyTable, detailsPane);
        splitPane.setDividerPositions(0.6); // 60% for table, 40% for details
        
        pane.setTop(controlBox);
        pane.setCenter(splitPane);
        tab.setContent(pane);
        
        return tab;
    }
    
    /**
     * Creates the control box.
     */
    private HBox createControlBox() {
        HBox controlBox = new HBox(10);
        controlBox.setPadding(new Insets(10));
        
        startStopButton = new Button("Start Strategies");
        startStopButton.setId("startStopStrategiesButton");
        startStopButton.setDisable(true); // Enabled after client is connected
        startStopButton.setOnAction(e -> toggleStrategies());
        
        Button refreshButton = new Button("Refresh Statistics");
        refreshButton.setOnAction(e -> updateStrategyStatistics());
        
        controlBox.getChildren().addAll(startStopButton, refreshButton);
        
        return controlBox;
    }
    
    /**
     * Creates the strategy table.
     */
    private TableView<StrategyModel> createStrategyTable() {
        TableView<StrategyModel> table = new TableView<>();
        
        TableColumn<StrategyModel, String> nameCol = new TableColumn<>("Strategy");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<StrategyModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        TableColumn<StrategyModel, String> tradesCol = new TableColumn<>("Trades");
        tradesCol.setCellValueFactory(new PropertyValueFactory<>("trades"));
        tradesCol.setPrefWidth(70);
        
        TableColumn<StrategyModel, String> winRateCol = new TableColumn<>("Win Rate");
        winRateCol.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        winRateCol.setPrefWidth(100);
        
        TableColumn<StrategyModel, String> pnlCol = new TableColumn<>("P/L");
        pnlCol.setCellValueFactory(new PropertyValueFactory<>("pnl"));
        pnlCol.setPrefWidth(100);
        
        table.getColumns().addAll(nameCol, statusCol, tradesCol, winRateCol, pnlCol);
        table.setItems(model.getStrategies());
        
        // Add selection listener to show strategy details
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showStrategyDetails(newSelection));
        
        return table;
    }
    
    /**
     * Creates the strategy details pane.
     */
    private VBox createStrategyDetailsPane() {
        VBox detailsPane = new VBox(10);
        detailsPane.setPadding(new Insets(10));
        detailsPane.setStyle("-fx-background-color: #f5f5f5;");
        
        Label detailsLabel = new Label("Strategy Details:");
        detailsLabel.setFont(Font.font("System", 14));
        
        strategyDetailsArea = new TextArea();
        strategyDetailsArea.setEditable(false);
        strategyDetailsArea.setWrapText(true);
        strategyDetailsArea.setFont(Font.font("System", 12));
        
        VBox.setVgrow(strategyDetailsArea, Priority.ALWAYS);
        detailsPane.getChildren().addAll(detailsLabel, strategyDetailsArea);
        
        return detailsPane;
    }
    
    /**
     * Shows details for the selected strategy.
     */
    private void showStrategyDetails(StrategyModel strategyModel) {
        if (strategyModel == null) {
            strategyDetailsArea.setText("");
            return;
        }
        
        // Find the actual strategy object that matches this model
        InvestmentStrategy strategy = findStrategyByName(strategyModel.getName());
        
        if (strategy == null) {
            strategyDetailsArea.setText("Strategy details not available.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        
        // Strategy name and description
        details.append("NAME: ").append(strategy.getName()).append("\n\n");
        details.append("DESCRIPTION: ").append(strategy.getDescription()).append("\n\n");
        
        // Instruments
        details.append("INSTRUMENTS:\n");
        details.append("----------------------------------------\n");
        if (strategy.getInstruments() != null && !strategy.getInstruments().isEmpty()) {
            details.append(strategy.getInstruments()).append("\n\n");
        } else {
            details.append("No instruments defined.\n\n");
        }

        // Parameters
        details.append("PARAMETERS:\n");
        details.append("----------------------------------------\n");
        details.append(strategy.getParametersAsString()).append("\n\n");
        
        // Detailed statistics
        details.append("STATISTICS:\n");
        details.append("----------------------------------------\n");
        
        // Add statistics from StrategyStatistics object
        details.append("Status: ").append(strategy.getStatusString()).append("\n");
        details.append("Total Trades: ").append(strategy.getStatistics().getTradeCount()).append("\n");
        details.append("Winning Trades: ").append(strategy.getStatistics().getWinningTrades()).append("\n");
        details.append("Losing Trades: ").append(strategy.getStatistics().getLosingTrades()).append("\n");
        details.append("Win Rate: ").append(String.format("%.1f%%", strategy.getStatistics().getWinRatio() * 100)).append("\n");
        details.append("Total P/L: ").append(String.format("%.2f", strategy.getStatistics().getTotalPnL())).append("\n");
        
        details.append("Total Buys: ").append(strategy.getStatistics().getTotalBuys()).append("\n");
        details.append("Total Sells: ").append(strategy.getStatistics().getTotalSells()).append("\n");
        details.append("Total Buy Volume: ").append(String.format("%.2f", strategy.getStatistics().getTotalBuyVolume())).append("\n");
        details.append("Total Sell Volume: ").append(String.format("%.2f", strategy.getStatistics().getTotalSellVolume())).append("\n");
        details.append("Total Buy Value: ").append(String.format("%.2f", strategy.getStatistics().getTotalBuyValue())).append("\n");
        details.append("Total Sell Value: ").append(String.format("%.2f", strategy.getStatistics().getTotalSellValue())).append("\n");
        
        // Add timing information if available
        if (strategy.getStatistics().getStartTime() != null) {
            details.append("\nStarted: ").append(strategy.getStatistics().getStartTime()).append("\n");
        }
        if (strategy.getStatistics().getEndTime() != null) {
            details.append("Ended: ").append(strategy.getStatistics().getEndTime()).append("\n");
        }
        
        strategyDetailsArea.setText(details.toString());
    }
    
    /**
     * Finds a strategy by its name.
     */
    private InvestmentStrategy findStrategyByName(String name) {
        if (model.getStrategyManager() == null) return null;
        
        for (InvestmentStrategy strategy : model.getStrategyManager().getStrategies()) {
            if (strategy.getName().equals(name)) {
                return strategy;
            }
        }
        return null;
    }
    
    /**
     * Toggles strategies between started and stopped states.
     */
    private void toggleStrategies() {
        if (model.getStrategyManager() == null) return;
        
        if (startStopButton.getText().equals("Start Strategies")) {
            model.getStrategyManager().startAllStrategies();
            startStopButton.setText("Stop Strategies");
        } else {
            model.getStrategyManager().stopAllStrategies();
            startStopButton.setText("Start Strategies");
        }
        
        updateStrategyStatistics();
    }
    
    /**
     * Updates strategy statistics in the table.
     */
    public void updateStrategyStatistics() {
        if (model.getStrategyManager() == null) return;
        
        // Clear existing data
        model.getStrategies().clear();
        
        for (InvestmentStrategy strategy : model.getStrategyManager().getStrategies()) {
            model.getStrategies().add(new StrategyModel(
                strategy.getName(),
                strategy.getStatusString(),
                String.valueOf(strategy.getStatistics().getTradeCount()),
                String.format("%.1f%%", strategy.getStatistics().getWinRatio() * 100),
                String.format("%.2f", strategy.getStatistics().getTotalPnL())
            ));
        }
        
        // Enable start/stop button if strategies exist
        if (startStopButton != null) {
            startStopButton.setDisable(model.getStrategies().isEmpty());
        }
        
        // Update the details view if a strategy is selected
        StrategyModel selectedStrategy = strategyTable.getSelectionModel().getSelectedItem();
        if (selectedStrategy != null) {
            showStrategyDetails(selectedStrategy);
        }
    }
}