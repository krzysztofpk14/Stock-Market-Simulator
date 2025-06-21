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

// package com.krzysztofpk14.app.gui.controllers;

// import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
// import com.krzysztofpk14.app.gui.model.TradingAppModel;
// import com.krzysztofpk14.app.gui.model.PriceDataPoint;

// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.geometry.Insets;
// import javafx.scene.chart.LineChart;
// import javafx.scene.chart.NumberAxis;
// import javafx.scene.chart.XYChart;
// import javafx.scene.control.*;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.Priority;
// import javafx.scene.layout.VBox;
// import javafx.util.StringConverter;

// import java.util.*;

// /**
//  * Controller for Charts tab.
//  */
// public class ChartsController {
//     private TradingAppModel model;
//     private LineChart<Number, Number> priceChart;
//     private Map<String, List<PriceDataPoint>> historicalData = new HashMap<>();
//     private Map<String, XYChart.Series<Number, Number>> displayedSeries = new HashMap<>();
//     private int dataPointIndex = 0;
//     private ComboBox<String> symbolComboBox;
//     private ObservableList<String> availableSymbols = FXCollections.observableArrayList();
//     private String currentSymbol = "ALL"; // "ALL" represents showing all symbols
//     private Slider timeRangeSlider;
//     private Label rangeLabel;
//     private final int MAX_DATA_POINTS = 1000; // Maximum number of data points to store
//     private final int DEFAULT_VISIBLE_POINTS = 100; // Default number of points to display
//     private boolean liveUpdates = true;

//     public ChartsController(TradingAppModel model) {
//         this.model = model;
//     }
//     /**
//      * Creates the Charts tab.
//      */
//     public Tab createTab() {
//         Tab tab = new Tab("Charts");
//         tab.setClosable(false);
        
//         BorderPane pane = new BorderPane();

//         // Create control panel with symbol selector and slider
//         VBox controlPanel = new VBox(10);
//         controlPanel.setPadding(new Insets(10));
        
//         HBox symbolPanel = createSymbolPanel();
//         HBox sliderPanel = createSliderPanel();
//         HBox buttonPanel = createButtonPanel();
        
//         controlPanel.getChildren().addAll(symbolPanel, sliderPanel, buttonPanel);
        
//         priceChart = createPriceChart();

//         pane.setTop(controlPanel);
//         pane.setCenter(priceChart);
        
//         tab.setContent(pane);
//         return tab;
//     }

//     /**
//      * Creates the symbol selection panel.
//      */
//     private HBox createSymbolPanel() {
//         HBox symbolPanel = new HBox(10);
//         symbolPanel.setPadding(new Insets(0, 0, 5, 0));
        
//         // Create label
//         Label symbolLabel = new Label("Symbol:");
        
//         // Create combo box with "ALL" option
//         availableSymbols.add("ALL");
//         symbolComboBox = new ComboBox<>(availableSymbols);
//         symbolComboBox.setValue("ALL");
        
//         // Add listener for symbol selection
//         symbolComboBox.setOnAction(e -> {
//             String selected = symbolComboBox.getValue();
//             if (selected != null) {
//                 currentSymbol = selected;
//                 updateChartRange((int) timeRangeSlider.getValue());
//             }
//         });
        
//         symbolPanel.getChildren().addAll(symbolLabel, symbolComboBox);
        
//         return symbolPanel;
//     }
    
//     /**
//      * Creates the slider panel for historical data navigation.
//      */
//     private HBox createSliderPanel() {
//         HBox sliderPanel = new HBox(10);
//         sliderPanel.setPadding(new Insets(0, 0, 5, 0));
        
//         // Create time range slider
//         Label sliderLabel = new Label("Time Range:");
        
//         timeRangeSlider = new Slider();
//         timeRangeSlider.setMin(10); // Minimum 10 data points
//         timeRangeSlider.setMax(DEFAULT_VISIBLE_POINTS);
//         timeRangeSlider.setValue(DEFAULT_VISIBLE_POINTS);
//         timeRangeSlider.setShowTickLabels(true);
//         timeRangeSlider.setShowTickMarks(true);
//         timeRangeSlider.setMajorTickUnit(50);
//         timeRangeSlider.setBlockIncrement(10);
//         HBox.setHgrow(timeRangeSlider, Priority.ALWAYS);
        
//         rangeLabel = new Label("Showing last " + DEFAULT_VISIBLE_POINTS + " data points");
        
//         // Add listener to slider value
//         timeRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
//             int visiblePoints = newVal.intValue();
//             rangeLabel.setText("Showing last " + visiblePoints + " data points");
//             updateChartRange(visiblePoints);
//         });
        
//         sliderPanel.getChildren().addAll(sliderLabel, timeRangeSlider, rangeLabel);
        
//         return sliderPanel;
//     }

//     /**
//      * Creates button panel with additional controls
//      */
//     private HBox createButtonPanel() {
//         HBox buttonPanel = new HBox(10);
//         buttonPanel.setPadding(new Insets(0, 0, 5, 0));
        
//         // Create live updates checkbox
//         CheckBox liveUpdatesCheckBox = new CheckBox("Live Updates");
//         liveUpdatesCheckBox.setSelected(true);
//         liveUpdatesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
//             liveUpdates = newVal;
//             if (liveUpdates) {
//                 // When live updates are re-enabled, show the most recent data
//                 updateChartRange((int) timeRangeSlider.getValue());
//             }
//         });
        
//         // Create zoom buttons
//         Button zoomInButton = new Button("+");
//         zoomInButton.setOnAction(e -> {
//             double currentValue = timeRangeSlider.getValue();
//             timeRangeSlider.setValue(Math.max(10, currentValue * 0.75));
//         });
        
//         Button zoomOutButton = new Button("-");
//         zoomOutButton.setOnAction(e -> {
//             double currentValue = timeRangeSlider.getValue();
//             timeRangeSlider.setValue(Math.min(timeRangeSlider.getMax(), currentValue * 1.25));
//         });
        
//         Button resetButton = new Button("Reset");
//         resetButton.setOnAction(e -> {
//             timeRangeSlider.setValue(DEFAULT_VISIBLE_POINTS);
//         });
        
//         Label zoomLabel = new Label("Zoom:");
        
//         buttonPanel.getChildren().addAll(liveUpdatesCheckBox, zoomLabel, 
//                                         zoomInButton, zoomOutButton, resetButton);
        
//         return buttonPanel;
//     }
    
//     /**
//      * Creates the price chart.
//      */
//     private LineChart<Number, Number> createPriceChart() {
//         NumberAxis xAxis = new NumberAxis();
//         xAxis.setForceZeroInRange(false);
//         xAxis.setTickLabelFormatter(new StringConverter<Number>() {
//             @Override
//             public String toString(Number object) {
//                 // Just show index numbers on X-axis
//                 return String.valueOf(object.intValue());
//             }

//             @Override
//             public Number fromString(String string) {
//                 return Integer.parseInt(string);
//             }
//         });
//         xAxis.setLabel("Time");
        
//         NumberAxis yAxis = new NumberAxis();
//         yAxis.setLabel("Price");
//         yAxis.setAutoRanging(true);
//         yAxis.setForceZeroInRange(false);
        
//         LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
//         chart.setTitle("Price Chart");
//         chart.setAnimated(false);
//         chart.setCreateSymbols(true); // Symbols help identify data points
//         chart.setLegendVisible(true);
        
//         return chart;
//     }
    
//    /**
//      * Updates the chart with new market data.
//      */
//     public void updateChart(MarketDataResponse data) {
//         if (data.getInstrument() == null || data.getMarketDataGroups() == null || 
//             data.getMarketDataGroups().isEmpty()) {
//             return;
//         }
        
//         String symbol = data.getInstrument().getSymbol();
//         String priceStr = data.getMarketDataGroups().get(0).getPrice();
        
//         if (priceStr == null) return;
        
//         double price = Double.parseDouble(priceStr);
//         long timestamp = System.currentTimeMillis();

//         // Update available symbols list if needed
//         if (!availableSymbols.contains(symbol)) {
//             availableSymbols.add(symbol);
//         }
        
//         // Store in historical data
//         if (!historicalData.containsKey(symbol)) {
//             historicalData.put(symbol, new ArrayList<>());
//         }
        
//         List<PriceDataPoint> symbolData = historicalData.get(symbol);
//         symbolData.add(new PriceDataPoint(timestamp, price, dataPointIndex));
        
//         // Also add to model's price data (if you want to use it elsewhere)
//         model.addPriceDataPoint(symbol, price, timestamp);
        
//         // Limit historical data size
//         if (symbolData.size() > MAX_DATA_POINTS) {
//             symbolData.remove(0);
//         }
        
//         // Update slider maximum if needed
//         int maxDataSize = historicalData.values().stream()
//                 .mapToInt(List::size)
//                 .max()
//                 .orElse(DEFAULT_VISIBLE_POINTS);
                
//         if (maxDataSize > (int)timeRangeSlider.getMax()) {
//             timeRangeSlider.setMax(maxDataSize);
//         }
        
//         dataPointIndex++;
        
//         // Update chart if live updates are enabled
//         if (liveUpdates) {
//             updateChartRange((int) timeRangeSlider.getValue());
//         }
//     }

    
//     /**
//      * Updates the chart to show the selected range of historical data.
//      */
//     private void updateChartRange(int visiblePoints) {
//         // Clear the chart
//         priceChart.getData().clear();
//         displayedSeries.clear();
        
//         // Show data based on selected symbol
//         if ("ALL".equals(currentSymbol)) {
//             // Show all symbols
//             for (String symbol : historicalData.keySet()) {
//                 addSymbolToChart(symbol, visiblePoints);
//             }
//         } else {
//             // Show only selected symbol
//             addSymbolToChart(currentSymbol, visiblePoints);
//         }
//     }
    
//     /**
//      * Adds a symbol's data to the chart with the specified visible range.
//      */
//     private void addSymbolToChart(String symbol, int visiblePoints) {
//         List<PriceDataPoint> symbolData = historicalData.get(symbol);
//         if (symbolData == null || symbolData.isEmpty()) {
//             return;
//         }
        
//         XYChart.Series<Number, Number> series = new XYChart.Series<>();
//         series.setName(symbol);
        
//         // Determine data range to display
//         int startIndex = Math.max(0, symbolData.size() - visiblePoints);
//         List<PriceDataPoint> visibleData = symbolData.subList(startIndex, symbolData.size());
        
//         // Add data points to series
//         int index = 0;
//         for (PriceDataPoint point : visibleData) {
//             series.getData().add(new XYChart.Data<>(index++, point.getPrice()));
//         }
        
//         // Add series to chart
//         displayedSeries.put(symbol, series);
//         priceChart.getData().add(series);
//     }

//     /**
//      * Updates the chart when new securities are available.
//      */
//     public void updateAvailableSecurities(ObservableList<String> securities) {
//         // Remove existing symbols except "ALL"
//         availableSymbols.clear();
//         availableSymbols.add("ALL");
        
//         // Add all securities
//         availableSymbols.addAll(securities);
//     }
// }