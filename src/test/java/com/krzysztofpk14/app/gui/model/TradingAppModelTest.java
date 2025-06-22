package com.krzysztofpk14.app.gui.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.strategy.StrategyManager;
import com.krzysztofpk14.app.gui.util.ApiCommunicationLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the TradingAppModel class.
 */
public class TradingAppModelTest {
    
    private TradingAppModel model;
    
    // Simple test double for BossaApiClient
    static class TestBossaApiClient extends BossaApiClient { 
        // Empty implementation for testing
    }
    
    // Simple test double for StrategyManager
    static class TestStrategyManager extends StrategyManager {
        public TestStrategyManager() {
            super(new TestBossaApiClient());
        }
    }
      @BeforeEach
    public void setUp() {
        model = new TradingAppModel();
        // We don't need to set the client or strategy manager for most tests
    }
    
    @Test
    public void testGettersAndSetters() {
        // Create test doubles
        TestBossaApiClient client = new TestBossaApiClient();
        TestStrategyManager strategyManager = new TestStrategyManager();
        
        // Test client getter/setter
        model.setClient(client);
        assertSame(client, model.getClient(), "Client getter should return the set client");
        
        // Test strategy manager getter/setter
        model.setStrategyManager(strategyManager);
        assertSame(strategyManager, model.getStrategyManager(), "StrategyManager getter should return the set manager");
        
        // Set client to null and verify
        model.setClient(null);
        assertNull(model.getClient(), "Client getter should return null after setting to null");
        
        // Set strategy manager to null and verify
        model.setStrategyManager(null);
        assertNull(model.getStrategyManager(), "StrategyManager getter should return null after setting to null");
    }
    
    @Test
    public void testCollectionsInitialization() {
        // Test that collections are properly initialized (not null)
        assertNotNull(model.getMarketData(), "Market data collection should not be null");
        assertNotNull(model.getOrders(), "Orders collection should not be null");
        assertNotNull(model.getStrategies(), "Strategies collection should not be null");
        assertNotNull(model.getAvailableSymbols(), "Available symbols should not be null");
        assertNotNull(model.getPriceData(), "Price data map should not be null");
    }
    
    @Test
    public void testCollectionsAreEmpty() {
        // Test that collections are initially empty
        assertEquals(0, model.getMarketData().size(), "Market data collection should be empty initially");
        assertEquals(0, model.getOrders().size(), "Orders collection should be empty initially");
        assertEquals(0, model.getStrategies().size(), "Strategies collection should be empty initially");
        
        // Available symbols might be null initially until set
        assertNotNull(model.getAvailableSymbols(), "Available symbols should not be null");
        assertEquals(0, model.getPriceData().size(), "Price data map should be empty initially");
    }
    
    @Test
    public void testSetAvailableSymbols() {
        // Given
        List<String> symbols = Arrays.asList("KGHM", "PKO", "PKN", "PZU");
        
        // When
        model.setAvailableSymbols(symbols);
        
        // Then
        assertEquals(symbols, model.getAvailableSymbols(), "Available symbols getter should return set symbols");
        assertSame(model.getAvailableSymbols(), model.availableSymbolsProperty().get(), 
                "Available symbols property getter should return the same list as the direct getter");
    }
    
    @Test
    public void testAddPriceDataPoint() {
        // Given
        String symbol = "KGHM";
        double price = 150.75;
        long timestamp = System.currentTimeMillis();
        
        // When - add first point
        model.addPriceDataPoint(symbol, price, timestamp);
        
        // Then
        assertTrue(model.getPriceData().containsKey(symbol), "Price data map should contain the symbol");
        assertEquals(1, model.getPriceData().get(symbol).size(), "Symbol should have 1 data point");
        PriceDataPoint point = model.getPriceData().get(symbol).get(0);
        assertEquals(price, point.getPrice(), "Price should match");
        assertEquals(timestamp, point.getTimestamp(), "Timestamp should match");
        assertEquals(0, point.getIndex(), "Index should be 0 for first point");
        
        // When - add second point
        double price2 = 151.25;
        long timestamp2 = timestamp + 1000;
        model.addPriceDataPoint(symbol, price2, timestamp2);
        
        // Then
        assertEquals(2, model.getPriceData().get(symbol).size(), "Symbol should have 2 data points");
        PriceDataPoint point2 = model.getPriceData().get(symbol).get(1);
        assertEquals(price2, point2.getPrice(), "Price should match for second point");
        assertEquals(timestamp2, point2.getTimestamp(), "Timestamp should match for second point");
        assertEquals(1, point2.getIndex(), "Index should be 1 for second point");
    }
    
    @Test
    public void testPriceDataLimit() {
        // Given
        String symbol = "KGHM";
        
        // When - add more than 1000 points
        for (int i = 0; i < 1010; i++) {
            model.addPriceDataPoint(symbol, 100 + i, System.currentTimeMillis() + i);
        }
        
        // Then - should be limited to 1000 points
        assertEquals(1000, model.getPriceData().get(symbol).size(), 
                "Symbol should have max 1000 data points");
        
        // First points should be removed
        PriceDataPoint firstPoint = model.getPriceData().get(symbol).get(0);
        assertEquals(100 + 10, firstPoint.getPrice(), "First point should now be the 11th added");
    }
    
    @Test
    public void testApiLogger() {
        // The API logger should be initialized and accessible
        ApiCommunicationLogger logger = model.getApiLogger();
        assertNotNull(logger, "API logger should not be null");
    }
    
    @Test
    public void testMultipleSymbols() {
        // Add price data for multiple symbols
        model.addPriceDataPoint("KGHM", 150.75, 1000L);
        model.addPriceDataPoint("PKO", 35.50, 1000L);
        model.addPriceDataPoint("PZU", 42.25, 1000L);
        
        // Verify each symbol has its own data
        assertEquals(3, model.getPriceData().size(), "Price data map should have 3 symbols");
        assertEquals(1, model.getPriceData().get("KGHM").size(), "KGHM should have 1 data point");
        assertEquals(1, model.getPriceData().get("PKO").size(), "PKO should have 1 data point");
        assertEquals(1, model.getPriceData().get("PZU").size(), "PZU should have 1 data point");
        
        // Verify correct prices
        assertEquals(150.75, model.getPriceData().get("KGHM").get(0).getPrice(), "KGHM price should match");
        assertEquals(35.50, model.getPriceData().get("PKO").get(0).getPrice(), "PKO price should match");
        assertEquals(42.25, model.getPriceData().get("PZU").get(0).getPrice(), "PZU price should match");
    }
      @Test
    public void testObservableCollections() {
        // Given
        MarketDataModel marketDataModel = new MarketDataModel("KGHM", "150.75", "20250621-14:30:45.123");
        OrderModel orderModel = new OrderModel("1", "KGHM", "Buy", "150.75", "100", "New Order", "New", "20250621-14:30:45.123");
        StrategyModel strategyModel = new StrategyModel("RSI", "RUNNING", "10", "60%", "+500.25");
        
        // When
        model.getMarketData().add(marketDataModel);
        model.getOrders().add(orderModel);
        model.getStrategies().add(strategyModel);
        
        // Then
        assertEquals(1, model.getMarketData().size(), "Market data collection should have 1 item");
        assertEquals(1, model.getOrders().size(), "Orders collection should have 1 item");
        assertEquals(1, model.getStrategies().size(), "Strategies collection should have 1 item");
        
        assertSame(marketDataModel, model.getMarketData().get(0), "Market data item should be the one added");
        assertSame(orderModel, model.getOrders().get(0), "Order item should be the one added");
        assertSame(strategyModel, model.getStrategies().get(0), "Strategy item should be the one added");
    }
}
