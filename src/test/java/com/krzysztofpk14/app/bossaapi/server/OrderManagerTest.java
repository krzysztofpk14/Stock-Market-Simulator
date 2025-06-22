package com.krzysztofpk14.app.bossaapi.server;

import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class OrderManagerTest {

    private OrderManager orderManager;
    private List<ExecutionReport> receivedReports;
    private MarketDataManager mockMarketDataManager;
    
    @BeforeEach
    public void setUp() {
        mockMarketDataManager = new MockMarketDataManager();
        orderManager = new OrderManager();
        orderManager.setMarketDataManager(mockMarketDataManager);
        receivedReports = new ArrayList<>();
        
        // Register a listener to collect execution reports
        orderManager.registerExecutionListener(this::collectExecutionReport);
    }
    
    @Test
    public void testProcessNewOrder() {
        // Given
        OrderRequest order = createTestLimitOrder("12345", "AAPL", "150.00", "10", OrderRequest.BUY);
        String username = "testuser";
        
        // When
        ExecutionReport report = orderManager.processOrder(order, username);
        
        // Then
        assertNotNull(report);
        assertEquals(order.getClientOrderId(), report.getClientOrderId());
        assertEquals(ExecutionReport.NEW, report.getExecutionType());
        assertEquals(ExecutionReport.NEW_ORDER, report.getOrderStatus());
        assertEquals(order.getSide(), report.getSide());
        assertEquals(order.getOrderType(), report.getOrderType());
        assertEquals(order.getPrice(), report.getPrice());
        assertEquals("AAPL", report.getInstrument().getSymbol());
    }
      // Test to verify that order is stored properly (checking by order count instead of direct retrieval)
    @Test
    public void testOrderStorage() {
        // Given
        assertEquals(0, orderManager.getActiveOrderCount());
        
        // When
        OrderRequest order = createTestLimitOrder("order-1", "MSFT", "250.00", "5", OrderRequest.BUY);
        orderManager.processOrder(order, "testuser");
        
        // Then
        assertEquals(1, orderManager.getActiveOrderCount());
    }
    
    // Test to verify multiple orders are stored correctly
    @Test
    public void testMultipleOrders() {
        // Given
        String username = "multiuser";
        OrderRequest order1 = createTestLimitOrder("user-order-1", "AMZN", "3500.00", "1", OrderRequest.BUY);
        OrderRequest order2 = createTestLimitOrder("user-order-2", "GOOGL", "2800.00", "2", OrderRequest.SELL);
        
        // When
        orderManager.processOrder(order1, username);
        orderManager.processOrder(order2, username);
        
        // Then - verify active order count
        assertEquals(2, orderManager.getActiveOrderCount());
        
        // And verify we received notifications for both
        assertEquals(2, receivedReports.size());
    }
    
    @Test
    public void testOrderExecution() {
        // Given
        // Create a buy limit order with price higher than market price (100.0)
        OrderRequest buyOrder = createTestLimitOrder("exec-order-1", "AAPL", "120.00", "10", OrderRequest.BUY);
        orderManager.processOrder(buyOrder, "testuser");
        
        // Reset received reports to only capture execution
        receivedReports.clear();
        
        // When: simulate price update that will trigger order execution
        MarketDataResponse marketData = new MarketDataResponse();
        marketData.setInstrument(createMarketDataInstrument("AAPL"));
        List<MarketDataResponse.MarketDataGroup> groups = new ArrayList<>();
        MarketDataResponse.MarketDataGroup group = new MarketDataResponse.MarketDataGroup();
        group.setPrice("95.00");  // Price below our limit price, will trigger execution
        groups.add(group);
        marketData.setMarketDataGroups(groups);
        
        // Trigger execution through mocked market data update
        ((MockMarketDataManager)mockMarketDataManager).triggerPriceUpdate(marketData);
        
        // Then: verify we received execution report
        assertEquals(1, receivedReports.size(), "Should receive one execution report");
        ExecutionReport execReport = receivedReports.get(0);
        assertEquals(ExecutionReport.TRANSACTION, execReport.getExecutionType());
        assertEquals("10", execReport.getLastQuantity(), "Last quantity should match order quantity");
    }
    
    @Test
    public void testOrderNotifiers() {
        // Given
        AtomicReference<ExecutionReport> capturedReport = new AtomicReference<>();
        orderManager.registerExecutionListener(capturedReport::set);
        
        // When
        OrderRequest order = createTestLimitOrder("notify-order", "NVDA", "500.00", "5", OrderRequest.SELL);
        orderManager.processOrder(order, "usernotify");
        
        // Then
        assertNotNull(capturedReport.get(), "Listener should have been notified");
        assertEquals("notify-order", capturedReport.get().getClientOrderId(), "Notification contains correct order");
        
        // Cleanup
        orderManager.unregisterExecutionListener(capturedReport::set);
    }
    
    @Test
    public void testGetActiveAndCompletedOrderCount() {
        // Given: Start with no orders
        assertEquals(0, orderManager.getActiveOrderCount(), "Should start with 0 active orders");
        assertEquals(0, orderManager.getCompletedOrderCount(), "Should start with 0 completed orders");
        
        // When: Add some orders
        orderManager.processOrder(createTestLimitOrder("count-1", "AAPL", "150.00", "1", OrderRequest.BUY), "user1");
        orderManager.processOrder(createTestLimitOrder("count-2", "MSFT", "250.00", "2", OrderRequest.BUY), "user1");
        
        // Then: Verify count updated
        assertEquals(2, orderManager.getActiveOrderCount(), "Should have 2 active orders");
        assertEquals(0, orderManager.getCompletedOrderCount(), "Should have 0 completed orders");
        
        // When: Trigger execution to complete orders
        MarketDataResponse marketData = new MarketDataResponse();
        marketData.setInstrument(createMarketDataInstrument("AAPL"));
        List<MarketDataResponse.MarketDataGroup> groups = new ArrayList<>();
        MarketDataResponse.MarketDataGroup group = new MarketDataResponse.MarketDataGroup();
        group.setPrice("100.00");
        groups.add(group);
        marketData.setMarketDataGroups(groups);
        
        // Execute first order
        ((MockMarketDataManager)mockMarketDataManager).triggerPriceUpdate(marketData);
        
        // Update for second order
        marketData.setInstrument(createMarketDataInstrument("MSFT"));
        ((MockMarketDataManager)mockMarketDataManager).triggerPriceUpdate(marketData);
        
        // Then: Verify counts updated
        assertEquals(0, orderManager.getActiveOrderCount(), "Should have 0 active orders after execution");
        assertEquals(2, orderManager.getCompletedOrderCount(), "Should have 2 completed orders after execution");
    }
    
    // Helper methods
    private OrderRequest createTestLimitOrder(String clientOrderId, String symbol, 
                                            String price, String quantity, String side) {
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(clientOrderId);
        order.setSide(side);
        order.setOrderType(OrderRequest.LIMIT);
        order.setPrice(price);
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity(quantity);
        order.setOrderQuantity(orderQty);
        
        return order;
    }
    
    private void collectExecutionReport(ExecutionReport report) {
        receivedReports.add(report);
    }
    
    private MarketDataResponse.Instrument createMarketDataInstrument(String symbol) {
        MarketDataResponse.Instrument instrument = new MarketDataResponse.Instrument();
        instrument.setSymbol(symbol);
        return instrument;
    }
      // Mock classes
    private static class MockMarketDataManager extends MarketDataManager {
        private List<Consumer<MarketDataResponse>> listeners = new ArrayList<>();
        
        // Instead of overriding getCurrentPrice (which doesn't exist in parent),
        // we'll directly trigger the market data listener with our test data
        
        @Override
        public synchronized void registerMarketDataListener(Consumer<MarketDataResponse> listener) {
            super.registerMarketDataListener(listener);
            listeners.add(listener);
        }
        
        public void triggerPriceUpdate(MarketDataResponse marketData) {
            for (Consumer<MarketDataResponse> listener : listeners) {
                listener.accept(marketData);
            }
        }
    }
}
