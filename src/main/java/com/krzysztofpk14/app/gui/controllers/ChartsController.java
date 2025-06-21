package com.krzysztofpk14.app.gui.controllers;

import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.model.TradingAppModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Charts tab.
 */
public class ChartsController {
    private TradingAppModel model;
    private LineChart<Number, Number> priceChart;
    private Map<String, XYChart.Series<Number, Number>> priceSeries = new HashMap<>();
    private int dataPointIndex = 0;
    private ComboBox<String> symbolComboBox;
    private ObservableList<String> availableSymbols = FXCollections.observableArrayList();
    private String currentSymbol = "ALL"; // "ALL" represents showing all symbols
    private Slider timeRangeSlider;
    private Label rangeLabel;
    private final int MAX_DATA_POINTS = 1000; // Maximum number of data points to store
    private final int DEFAULT_VISIBLE_POINTS = 100; // Default number of points to display
    
    public ChartsController(TradingAppModel model) {
        this.model = model;
    }
    
    /**
     * Creates the Charts tab.
     */
    public Tab createTab() {
        Tab tab = new Tab("Charts");
        tab.setClosable(false);
        
        BorderPane pane = new BorderPane();


        // Create control panel with symbol selector
        HBox controlPanel = createControlPanel();
        
        priceChart = createPriceChart();

        pane.setTop(controlPanel);
        pane.setCenter(priceChart);
        
        tab.setContent(pane);
        return tab;
    }

    /**
     * Creates the control panel with symbol selector.
     */
    private HBox createControlPanel() {
        HBox controlPanel = new HBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        
        // Create label
        Label symbolLabel = new Label("Symbol:");
        
        // Create combo box with "ALL" option
        availableSymbols.add("ALL");
        symbolComboBox = new ComboBox<>(availableSymbols);
        symbolComboBox.setValue("ALL");
        
        // Add listener for symbol selection
        symbolComboBox.setOnAction(e -> {
            String selected = symbolComboBox.getValue();
            if (selected != null) {
                currentSymbol = selected;
                updateChartVisibility(selected);
            }
        });
        
        controlPanel.getChildren().addAll(symbolLabel, symbolComboBox);
        
        return controlPanel;
    }

    /**
     * Updates which series are visible based on selected symbol.
     */
    private void updateChartVisibility(String selectedSymbol) {
        // Clear the chart
        priceChart.getData().clear();
        
        // Add back the appropriate series
        if ("ALL".equals(selectedSymbol)) {
            // Show all series
            for (XYChart.Series<Number, Number> series : priceSeries.values()) {
                if (!priceChart.getData().contains(series)) {
                    priceChart.getData().add(series);
                }
            }
        } else {
            // Show only the selected symbol
            XYChart.Series<Number, Number> series = priceSeries.get(selectedSymbol);
            if (series != null) {
                priceChart.getData().add(series);
            }
        }
    }
    
    /**
     * Creates the price chart.
     */
    private LineChart<Number, Number> createPriceChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                // Just show index numbers on X-axis
                return String.valueOf(object.intValue());
            }

            @Override
            public Number fromString(String string) {
                return Integer.parseInt(string);
            }
        });
        xAxis.setLabel("Time");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Price");
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Price Chart");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        
        return chart;
    }
    
    /**
     * Updates the chart with new market data.
     */
    public void updateChart(MarketDataResponse data) {
        if (data.getInstrument() == null || data.getMarketDataGroups() == null || 
            data.getMarketDataGroups().isEmpty()) {
            return;
        }
        
        String symbol = data.getInstrument().getSymbol();
        String priceStr = data.getMarketDataGroups().get(0).getPrice();
        
        if (priceStr == null) return;
        
        double price = Double.parseDouble(priceStr);

        // Update available symbols list if needed
        if (!availableSymbols.contains(symbol)) {
            availableSymbols.add(symbol);
        }
        
        if (!priceSeries.containsKey(symbol)) {
            // Create new series for this symbol
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(symbol);
            priceSeries.put(symbol, series);

            // Add to chart only if this symbol should be visible
            if ("ALL".equals(currentSymbol) || symbol.equals(currentSymbol)) {
                priceChart.getData().add(series);
            }
        }
        
        // Add data to the series
        XYChart.Series<Number, Number> series = priceSeries.get(symbol);
        
        // Keep only a limited number of points for performance
        if (series.getData().size() > 100) {
            series.getData().remove(0);
        }
        
        series.getData().add(new XYChart.Data<>(dataPointIndex++, price));
    }

    /**
     * Updates the chart when new securities are available.
     */
    public void updateAvailableSecurities(ObservableList<String> securities) {
        // Remove existing symbols except "ALL"
        availableSymbols.clear();
        availableSymbols.add("ALL");
        
        // Add all securities
        availableSymbols.addAll(securities);
    }
}