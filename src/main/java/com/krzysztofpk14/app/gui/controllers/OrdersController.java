package com.krzysztofpk14.app.gui.controllers;

import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.gui.BossaApiService;
import com.krzysztofpk14.app.gui.model.OrderModel;
import com.krzysztofpk14.app.gui.model.TradingAppModel;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Controller for Orders tab.
 */
public class OrdersController {
    private TradingAppModel model;
    private BossaApiService apiService;
    private TableView<OrderModel> ordersTable;
    
    public OrdersController(TradingAppModel model) {
        this.model = model;
    }

    /**
     * Sets the API service for sending orders.
     */
    public void setApiService(BossaApiService apiService) {
        this.apiService = apiService;
    }
    
    /**
     * Creates the Orders tab.
     */
    public Tab createTab() {
        Tab tab = new Tab("Orders");
        tab.setClosable(false);
        
        BorderPane pane = new BorderPane();
        
        // Order entry form
        GridPane formPane = createOrderForm();
        
        // Orders table
        ordersTable = createOrdersTable();
        
        pane.setTop(formPane);
        pane.setCenter(ordersTable);
        tab.setContent(pane);
        return tab;
    }
    
    /**
     * Creates the order entry form.
     */
    private GridPane createOrderForm() {
        GridPane formPane = new GridPane();
        formPane.setPadding(new Insets(10));
        formPane.setHgap(10);
        formPane.setVgap(10);
        
        ComboBox<String> symbolCombo = new ComboBox<>();
        symbolCombo.setId("symbolComboBox");
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
        
        ComboBox<String> sideCombo = new ComboBox<>(
                FXCollections.observableArrayList("Buy", "Sell"));
        sideCombo.setId("sideComboBox");
        sideCombo.setValue("Buy");
        
        TextField quantityField = new TextField("1");
        quantityField.setId("quantityField");
        quantityField.setPrefWidth(80);
        
        TextField priceField = new TextField("100.00");
        priceField.setId("priceField");
        priceField.setPrefWidth(80);
        
        Button sendButton = new Button("Send Order");
        sendButton.setOnAction(e -> {
            try {
                // Get form values
                String symbol = symbolCombo.getValue();
                String side = sideCombo.getValue();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());
                
                // Create and send order
                OrderRequest order = createOrderRequest(symbol, side, quantity, price);

                // Add to orders table immediately to show pending state
                String orderId = order.getClientOrderId();
                model.getOrders().add(0, new OrderModel(
                    orderId,
                    symbol,
                    side,
                    String.format("%.2f", price),
                    String.valueOf(quantity),
                    "PENDING", // Initial status
                    "NEW",     // Initial exec type
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                ));

                apiService.sendOrder(order);

                
            } catch (NumberFormatException ex) {
                // Show error alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Invalid input");
                alert.setContentText("Please enter valid numeric values for quantity and price.");
                alert.showAndWait();
            } catch (Exception ex) {
                // Show error alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Order Error");
                alert.setHeaderText("Failed to send order");
                alert.setContentText("Error: " + ex.getMessage());
                alert.showAndWait();
            }
        });
        
        formPane.add(new Label("Symbol:"), 0, 0);
        formPane.add(symbolCombo, 1, 0);
        formPane.add(new Label("Side:"), 2, 0);
        formPane.add(sideCombo, 3, 0);
        formPane.add(new Label("Quantity:"), 0, 1);
        formPane.add(quantityField, 1, 1);
        formPane.add(new Label("Price:"), 2, 1);
        formPane.add(priceField, 3, 1);
        formPane.add(sendButton, 4, 1);
        
        return formPane;
    }
    
    /**
     * Creates the orders table.
     */
    private TableView<OrderModel> createOrdersTable() {
        TableView<OrderModel> table = new TableView<>();
        
        TableColumn<OrderModel, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(cellData -> cellData.getValue().orderIdProperty());
        orderIdCol.setPrefWidth(100);
        
        TableColumn<OrderModel, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        symbolCol.setPrefWidth(80);
        
        TableColumn<OrderModel, String> sideCol = new TableColumn<>("Side");
        sideCol.setCellValueFactory(cellData -> cellData.getValue().sideProperty());
        sideCol.setPrefWidth(60);
        
        TableColumn<OrderModel, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        priceCol.setPrefWidth(80);
        
        TableColumn<OrderModel, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        qtyCol.setPrefWidth(80);
        
        TableColumn<OrderModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(80);
        
        TableColumn<OrderModel, String> execTypeCol = new TableColumn<>("Exec Type");
        execTypeCol.setCellValueFactory(cellData -> cellData.getValue().execTypeProperty());
        execTypeCol.setPrefWidth(80);
        
        TableColumn<OrderModel, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        timeCol.setPrefWidth(140);
        
        table.getColumns().addAll(orderIdCol, symbolCol, sideCol, priceCol, 
                qtyCol, statusCol, execTypeCol, timeCol);
        table.setItems(model.getOrders());
        
        return table;
    }
    
    /**
     * Creates an order request from form inputs.
     */
    private OrderRequest createOrderRequest(String symbol, String side, int quantity, double price) {
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(UUID.randomUUID().toString());
        
        // Set side (BUY/SELL)
        if ("Buy".equals(side)) {
            order.setSide(OrderRequest.BUY);
        } else {
            order.setSide(OrderRequest.SELL);
        }
        
        // Set order type and time in force
        order.setOrderType(OrderRequest.LIMIT);
        order.setTimeInForce(OrderRequest.DAY);
        order.setPrice(String.format("%.2f", price));
        
        // Set instrument
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        // Set quantity
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity(String.valueOf(quantity));
        order.setOrderQuantity(orderQty);
        
        return order;
    }
    
    /**
     * Adds an execution report to the orders table.
     */
    public void addExecutionReport(ExecutionReport report) {
    // Old code
    //     if (report == null || report.getInstrument() == null) return;
        
    //     model.getOrders().add(0, new OrderModel(
    //         report.getClientOrderId(),
    //         report.getInstrument().getSymbol(),
    //         report.getSide(),
    //         report.getPrice(),
    //         report.getOrderQuantity() != null ? report.getOrderQuantity().getQuantity() : "0",
    //         report.getOrderStatus(),
    //         report.getExecutionType(),
    //         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
    //     ));
    // }

    // New code
        if (report == null || report.getInstrument() == null) return;
        
        String orderId = report.getClientOrderId();
        boolean updated = false;
        
        // Try to find and update existing order in the table
        for (OrderModel existingOrder : model.getOrders()) {
            if (orderId.equals(existingOrder.getOrderId())) {
                // Update existing order entry
                existingOrder.setStatus(report.getOrderStatus());
                existingOrder.setExecType(report.getExecutionType());
                existingOrder.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                
                // Update other fields if needed
                if (report.getPrice() != null) {
                    existingOrder.setPrice(report.getPrice());
                }
                if (report.getOrderQuantity() != null && report.getOrderQuantity().getQuantity() != null) {
                    existingOrder.setQuantity(report.getOrderQuantity().getQuantity());
                }
                
                updated = true;
                break;
            }
        }
        
        // If not found (e.g., for orders not initiated through the UI), add as new entry
        if (!updated) {
            model.getOrders().add(0, new OrderModel(
                report.getClientOrderId(),
                report.getInstrument().getSymbol(),
                report.getSide(),
                report.getPrice(),
                report.getOrderQuantity() != null ? report.getOrderQuantity().getQuantity() : "0",
                report.getOrderStatus(),
                report.getExecutionType(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
            ));
        }
    }
}