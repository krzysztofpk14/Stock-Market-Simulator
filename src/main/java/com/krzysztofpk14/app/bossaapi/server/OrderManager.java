package com.krzysztofpk14.app.bossaapi.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

/**
 * Manager obsługujący zlecenia.
 */
public class OrderManager {
    private final Map<String, OrderInfo> activeOrders = new ConcurrentHashMap<>();
    private final Map<String, OrderInfo> orderHistory = new ConcurrentHashMap<>();
    private final AtomicInteger orderCounter = new AtomicInteger(1000);
    private final List<Consumer<ExecutionReport>> executionListeners = new CopyOnWriteArrayList<>();
    private MarketDataManager marketDataManager;
    
    /**
     * Inner class to store order information with execution state
     */
    private static class OrderInfo {
        private final OrderRequest order;
        private final String orderId;
        private final String username;
        private String orderStatus;
        private double price;
        private int quantity;
        private int executedQuantity;
        private String side; // "1" for Buy, "2" for Sell
        private List<ExecutionReport> reports = new ArrayList<>();
        
        public OrderInfo(OrderRequest order, String orderId, String username) {
            this.order = order;
            this.orderId = orderId;
            this.username = username;
            this.orderStatus = ExecutionReport.NEW_ORDER;
            
            // Parse order details
            if (order.getOrderType() == OrderRequest.MARKET) {
                this.price = 0.0; // Market orders are executed at current market price
            } else if (order.getOrderType() == OrderRequest.LIMIT) {
                try {
                    this.price = Double.parseDouble(order.getPrice());
                } catch (NumberFormatException e) {
                    this.price = 0.0;
                }
            }

            // Set quantity, default to 0 if parsing fails
            try {
                this.quantity = Integer.parseInt(order.getOrderQuantity().getQuantity());
            } catch (NumberFormatException e) {
                this.quantity = 0;
            }
            
            this.executedQuantity = 0;
            this.side = order.getSide();
        }
        
        public boolean isComplete() {
            return executedQuantity >= quantity;
        }
        
        public int getRemainingQuantity() {
            return quantity - executedQuantity;
        }
        
        public void addExecutedQuantity(int executed) {
            this.executedQuantity += executed;
            
            if (this.executedQuantity >= this.quantity) {
                this.orderStatus = ExecutionReport.DONE;
            } else {
                this.orderStatus = ExecutionReport.ACTIVE;
            }
        }
    }

    /**
     * Sets the MarketDataManager to monitor prices.
     *
     * @param marketDataManager The market data manager
     */
    public void setMarketDataManager(MarketDataManager marketDataManager) {
        this.marketDataManager = marketDataManager;
        
        // Register as a market data listener to check if orders should be executed
        if (marketDataManager != null) {
            marketDataManager.registerMarketDataListener(this::checkOrdersForExecution);
        }
    }

    /**
     * Przetwarza nowe zlecenie.
     * 
     * @param order Zlecenie do przetworzenia
     * @param username Nazwa użytkownika składającego zlecenie
     * @return Raport wykonania
     */
    public ExecutionReport processOrder(OrderRequest order, String username) {
        // Generuj unikalny ID zlecenia w systemie
        String orderId = generateOrderId();
        
        // Create and store order information
        OrderInfo orderInfo = new OrderInfo(order, orderId, username);
        
        // Utwórz raport wykonania (NEW)
        ExecutionReport report = createExecutionReport(order, orderId, username, ExecutionReport.NEW, orderInfo.orderStatus);
        orderInfo.reports.add(report);

        activeOrders.put(orderId, orderInfo);
        
        // Notyfikuj obserwatorów
        notifyExecutionListeners(report);
        
        return report;
    }

    /**
     * Checks all active orders against current market price for execution
     * 
     * @param marketData The market data response to check against
     */
    private void checkOrdersForExecution(MarketDataResponse marketData) {
        if (marketData == null || 
            marketData.getInstrument() == null || 
            marketData.getMarketDataGroups() == null || 
            marketData.getMarketDataGroups().isEmpty()) {
            return;
        }
        
        String symbol = marketData.getInstrument().getSymbol();
        String priceStr = marketData.getMarketDataGroups().get(0).getPrice();
        
        if (priceStr == null) return;
        
        double currentPrice;
        try {
            currentPrice = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            return;
        }
        
        // Check all active orders for the given symbol
        for (OrderInfo orderInfo : new ArrayList<>(activeOrders.values())) {
            try {
                // Skip orders for different symbols
                String orderSymbol = orderInfo.order.getInstrument().getSymbol();
                if (!symbol.equals(orderSymbol)) continue;
                
                // Skip fully executed orders
                if (orderInfo.isComplete()) continue;
                
                // Check if the order can be executed at current price
                boolean shouldExecute = false;
                
                
                if (orderInfo.order.getOrderType().equals(OrderRequest.MARKET)) {
                    // Market orders execute immediately at current price
                    shouldExecute = true;
                } 
                else if (orderInfo.order.getOrderType().equals(OrderRequest.LIMIT)) {
                    // Limit orders check against the specified price
                    if (orderInfo.side.equals(OrderRequest.BUY) && currentPrice <= orderInfo.price) {
                        // For buy orders, execute if current price <= order price
                        shouldExecute = true;
                    } else if (orderInfo.side.equals(OrderRequest.SELL) && currentPrice >= orderInfo.price) {
                        // For sell orders, execute if current price >= order price
                        shouldExecute = true;
                    }

                }
                
                // Execute the order if conditions are met
                if (shouldExecute) {
                    executeOrder(orderInfo, currentPrice);
                }
            } catch (Exception e) {
                System.err.println("Error checking order for execution: " + e.getMessage());
            }
        }
    }

    /**
     * Executes an order at the given price
     * 
     * @param orderInfo The order information
     * @param executionPrice The price to execute at
     */
    private void executeOrder(OrderInfo orderInfo, double executionPrice) {
        // Calculate quantity to execute (for simplicity, executing full remaining quantity)
        int execQuantity = orderInfo.getRemainingQuantity();
        
        // Update order information
        orderInfo.addExecutedQuantity(execQuantity);
        
        // Create execution report
        ExecutionReport report = createExecutionReport(
            orderInfo.order, 
            orderInfo.orderId, 
            orderInfo.username,
            ExecutionReport.TRANSACTION,  // Execution type for trade
            orderInfo.orderStatus         // New status (ACTIVE or DONE)
        );
        
        // Set executed price and quantity
        report.setPrice(String.format("%.2f", executionPrice));

        report.setLastPrice(String.format("%.2f", executionPrice));
        
        // Set cumulative quantity
        ExecutionReport.OrderQuantity cumQty = new ExecutionReport.OrderQuantity();
        cumQty.setQuantity(String.valueOf(orderInfo.executedQuantity));
        report.setCumulativeQuantity(String.valueOf(orderInfo.executedQuantity));
        
        // Set executed quantity for this fill
        ExecutionReport.OrderQuantity lastQty = new ExecutionReport.OrderQuantity();
        lastQty.setQuantity(String.valueOf(execQuantity));
        report.setLastQuantity(String.valueOf(execQuantity));
        
        // Add to order reports
        orderInfo.reports.add(report);
        
        // If order is complete, move to history
        if (orderInfo.isComplete()) {
            activeOrders.remove(orderInfo.orderId);
            orderHistory.put(orderInfo.orderId, orderInfo);
        }
        
        // Notify listeners
        notifyExecutionListeners(report);
        System.out.println("Order executed: " + orderInfo.order.getClientOrderId() + 
                           ", Quantity: " + execQuantity + 
                           ", Price: " + executionPrice + 
                           ", Status: " + orderInfo.orderStatus);
    }
    
    /**
     * Generuje unikalny identyfikator zlecenia.
     * 
     * @return Unikalny identyfikator
     */
    private String generateOrderId() {
        return "ORD" + orderCounter.incrementAndGet();
    }
    
    /**
     * Tworzy raport wykonania dla zlecenia.
     * 
     * @param order Zlecenie
     * @param orderId ID zlecenia w systemie
     * @param username Nazwa użytkownika
     * @param execType Typ wykonania
     * @param orderStatus Status zlecenia
     * @return Raport wykonania
     */
    private ExecutionReport createExecutionReport(
            OrderRequest order, 
            String orderId, 
            String username, 
            String execType, 
            String orderStatus) {
        
        ExecutionReport report = new ExecutionReport();
        
        // Wypełnij podstawowe pola
        report.setReportId(UUID.randomUUID().toString().substring(0, 8));
        report.setOrderId(orderId);
        report.setClientOrderId(order.getClientOrderId());
        report.setExecutionType(execType);
        report.setOrderStatus(orderStatus);
        report.setSide(order.getSide());
        report.setOrderType(order.getOrderType());
        report.setPrice(order.getPrice());
        // report.setUsername(username);
        
        // Dodaj instrument
        ExecutionReport.Instrument instrument = new ExecutionReport.Instrument();
        instrument.setSymbol(order.getInstrument().getSymbol());
        instrument.setId(order.getInstrument().getId());
        instrument.setIdSource(order.getInstrument().getIdSource());
        report.setInstrument(instrument);
        
        // Dodaj ilość
        if (order.getOrderQuantity() != null) {
            ExecutionReport.OrderQuantity quantity = new ExecutionReport.OrderQuantity();
            quantity.setQuantity(order.getOrderQuantity().getQuantity());
            report.setOrderQuantity(quantity);
        }
        
        // Dodaj czas transakcji
        report.setTransactionTime(getCurrentTimeFormatted());
        
        return report;
    }

    /**
     * Zwraca aktualny czas w formacie FIXML.
     * 
     * @return Sformatowany czas
     */
    private String getCurrentTimeFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
    }
    
    /**
     * Rejestruje obserwatora raportów wykonania.
     * 
     * @param listener Funkcja odbierająca raporty wykonania
     */
    public synchronized void registerExecutionListener(Consumer<ExecutionReport> listener) {
        executionListeners.add(listener);
    }
    
    /**
     * Wyrejestrowuje obserwatora raportów wykonania.
     * 
     * @param listener Funkcja odbierająca raporty wykonania
     */
    public synchronized void unregisterExecutionListener(Consumer<ExecutionReport> listener) {
        executionListeners.remove(listener);
    }
    
    /**
     * Powiadamia wszystkich obserwatorów o nowym raporcie wykonania.
     * 
     * @param report Raport wykonania
     */
    private void notifyExecutionListeners(ExecutionReport report) {
        for (Consumer<ExecutionReport> listener : executionListeners) {
            try {
                listener.accept(report);
            } catch (Exception e) {
                System.err.println("Błąd podczas notyfikacji obserwatora: " + e.getMessage());
            }
        }
    }

    /**
     * Gets the number of active orders.
     * 
     * @return The number of active orders
     */
    public int getActiveOrderCount() {
        return activeOrders.size();
    }

    /**
     * Gets the number of completed orders.
     * 
     * @return The number of completed orders
     */
    public int getCompletedOrderCount() {
        return orderHistory.size();
    }
    
    // /**
    //  * Symuluje wykonanie zlecenia (fill).
    //  * 
    //  * @param clientOrderId ID zlecenia klienta
    //  * @param price Cena wykonania
    //  * @param quantity Ilość wykonana
    //  * @param username Nazwa użytkownika
    //  * @return Raport wykonania
    //  */
    // public ExecutionReport simulateOrderExecution(String clientOrderId, String price, String quantity, String username) {
    //     // Znajdź zlecenie
    //     OrderRequest order = null;
    //     String orderId = null;
        
    //     for (Map.Entry<String, OrderRequest> entry : orders.entrySet()) {
    //         if (entry.getValue().getClientOrderId().equals(clientOrderId)) {
    //             order = entry.getValue();
    //             orderId = entry.getKey();
    //             break;
    //         }
    //     }
        
    //     if (order == null) {
    //         return null;
    //     }
        
    //     // Utwórz raport wykonania
    //     ExecutionReport report = createExecutionReport(order, orderId, username);
    //     report.setExecutionType(ExecutionReport.NEW);
    //     report.setOrderStatus(ExecutionReport.NEW_ORDER);
    //     report.setPrice(price);
        
    //     // Dodaj ilość wykonania
    //     ExecutionReport.OrderQuantity orderQty = new ExecutionReport.OrderQuantity();
    //     orderQty.setQuantity(quantity);
    //     report.setOrderQuantity(orderQty);
        
    //     // Notyfikuj obserwatorów
    //     notifyExecutionListeners(report);
        
    //     return report;
    // }
}