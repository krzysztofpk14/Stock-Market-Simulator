package com.krzysztofpk14.app.gui;

import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.controllers.ChartsController;
import com.krzysztofpk14.app.gui.controllers.MarketDataController;
import com.krzysztofpk14.app.gui.controllers.OrdersController;
import com.krzysztofpk14.app.gui.controllers.StrategiesController;
import com.krzysztofpk14.app.gui.controllers.DebugController;
import com.krzysztofpk14.app.gui.model.TradingAppModel;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

public class TradingAppGUI extends Application {
    private TradingAppModel model;
    public BossaApiService apiService;
    
    // Controllers
    private MarketDataController marketDataController;
    private OrdersController ordersController;
    private ChartsController chartsController;
    private StrategiesController strategiesController;
    
    // UI Components
    private TabPane tabPane;
    private Label statusLabel;
    
    // Background services
    private ScheduledExecutorService scheduledExecutorService;
    
    @Override
    public void start(Stage primaryStage) {
        configureLogging();
        
        // Initialize model
        model = new TradingAppModel();
        
        // Create UI
        BorderPane root = new BorderPane();
        tabPane = new TabPane();
        
        // Initialize controllers
        marketDataController = new MarketDataController(model);
        ordersController = new OrdersController(model);
        chartsController = new ChartsController(model);
        strategiesController = new StrategiesController(model);
        DebugController debugController = new DebugController(model);
        
        // Create tabs
        Tab marketDataTab = marketDataController.createTab();
        Tab ordersTab = ordersController.createTab();
        Tab chartsTab = chartsController.createTab();
        Tab strategyTab = strategiesController.createTab();
        Tab debugTab = debugController.createTab();
        
        tabPane.getTabs().addAll(marketDataTab, ordersTab, chartsTab, strategyTab, debugTab);
        
        // Create control panel and status bar
        HBox controlPanel = createControlPanel();
        HBox statusBar = createStatusBar();
        
        root.setTop(controlPanel);
        root.setCenter(tabPane);
        root.setBottom(statusBar);
        
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Trading Application");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize API service
        apiService = new BossaApiService(model);
        apiService.setMarketDataHandler(this::handleMarketData);
        apiService.setExecutionReportHandler(this::handleExecutionReport);
        apiService.setStatusUpdateHandler(this::updateStatus);
        apiService.initialize();
        ordersController.setApiService(apiService);
        model.getStrategyManager().setGui(this);

        
        // Setup scheduled updates
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(
                this::updateStatistics, 5, 5, TimeUnit.SECONDS);
    }
    
    private void configureLogging() {
        try {
            LogManager.getLogManager().readConfiguration(
                    getClass().getResourceAsStream("/logging.properties"));
        } catch (Exception e) {
            System.err.println("Cannot configure logging: " + e.getMessage());
        }
    }
    
    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f0;");
        
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> shutdown());
        
        panel.getChildren().addAll(new Label("Trading Application"), 
                new Pane(), quitButton);
        HBox.setHgrow(panel.getChildren().get(1), Priority.ALWAYS);
        
        return panel;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        
        statusLabel = new Label("Initializing...");
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    private void handleMarketData(MarketDataResponse data) {
        if (data == null) return;
        
        Platform.runLater(() -> {
            marketDataController.updateMarketData(data);
            chartsController.updateChart(data);
        });
    }
    
    private void handleExecutionReport(ExecutionReport report) {
        if (report == null) return;
        
        Platform.runLater(() -> {
            ordersController.addExecutionReport(report);
        });
    }
    
    private void updateStatus(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
        });
    }
    
    private void updateStatistics() {
        Platform.runLater(() -> {
            strategiesController.updateStrategyStatistics();
        });
    }
    
    private void shutdown() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        
        apiService.shutdown();
        Platform.exit();
    }
    
    @Override
    public void stop() {
        shutdown();
    }
}