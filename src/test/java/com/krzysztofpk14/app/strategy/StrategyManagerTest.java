package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.TradingAppGUI;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Unit tests for the StrategyManager class.
 */
public class StrategyManagerTest {

    private StrategyManager manager;
    private TestBossaApiClient testClient;
    private TestStrategy testStrategy1;
    private TestStrategy testStrategy2;
    
    /**
     * Test double for BossaApiClient to avoid real API calls
     */
    private static class TestBossaApiClient extends BossaApiClient {
        private Consumer<MarketDataResponse> marketDataHandler;
        private Consumer<ExecutionReport> executionReportHandler;
        
        public TestBossaApiClient() {
            super(); // Call superclass constructor with null params
        }
        
        @Override
        public void registerMarketDataHandler(String key, Consumer<MarketDataResponse> handler) {
            this.marketDataHandler = handler;
        }
        
        @Override
        public void registerExecutionReportHandler(String key, Consumer<ExecutionReport> handler) {
            this.executionReportHandler = handler;
        }
        
        public void simulateMarketData(MarketDataResponse data) {
            if (marketDataHandler != null) {
                marketDataHandler.accept(data);
            }
        }
        
        public void simulateExecutionReport(ExecutionReport report) {
            if (executionReportHandler != null) {
                executionReportHandler.accept(report);
            }
        }
    }
    
    /**
     * Test implementation of InvestmentStrategy
     */
    private static class TestStrategy implements InvestmentStrategy {
        private StrategyParameters parameters;
        private StrategyStatus status = StrategyStatus.INITIALIZED;
        private final StrategyStatistics statistics = new StrategyStatistics();
        private final String name;
        private final String description;
        private boolean marketDataProcessed = false;
        private boolean executionReportProcessed = false;
        private int marketDataCount = 0;
        private int executionReportCount = 0;
        private TradingAppGUI gui;
        
        public TestStrategy(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        @Override
        public void initialize(StrategyParameters parameters) {
            this.parameters = parameters;
            this.status = StrategyStatus.INITIALIZED;
        }
        
        @Override
        public void start() {
            this.status = StrategyStatus.RUNNING;
            this.statistics.setStartTime(java.time.LocalDateTime.now());
        }
        
        @Override
        public void stop() {
            this.status = StrategyStatus.STOPPED;
            this.statistics.setEndTime(java.time.LocalDateTime.now());
        }
        
        @Override
        public void onMarketData(MarketDataResponse marketData) {
            this.marketDataProcessed = true;
            this.marketDataCount++;
        }
        
        @Override
        public void onExecutionReport(ExecutionReport report) {
            this.executionReportProcessed = true;
            this.executionReportCount++;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        @Override
        public StrategyStatus getStatus() {
            return status;
        }
        
        @Override
        public String getStatusString() {
            return status.name();
        }
        
        @Override
        public StrategyStatistics getStatistics() {
            return statistics;
        }
        
        @Override
        public void displayStatistics() {
            // Just a stub for testing
        }
        
        @Override
        public String getParametersAsString() {
            return "Test Parameters";
        }
        
        @Override
        public String getInstruments() {
            if (parameters != null && parameters.getInstruments() != null) {
                return String.join(", ", parameters.getInstruments());
            }
            return "No instruments";
        }
        
        @Override
        public void setGui(TradingAppGUI gui) {
            this.gui = gui;
        }
        
        // Test helper methods
        public boolean wasMarketDataProcessed() {
            return marketDataProcessed;
        }
        
        public boolean wasExecutionReportProcessed() {
            return executionReportProcessed;
        }
        
        public int getMarketDataCount() {
            return marketDataCount;
        }
        
        public int getExecutionReportCount() {
            return executionReportCount;
        }
        
        public TradingAppGUI getGui() {
            return gui;
        }
    }
    
    /**
     * Test double for MarketDataResponse
     */
    private static class TestMarketDataResponse extends MarketDataResponse {
        // Simple test implementation
    }
    
    /**
     * Test double for ExecutionReport
     */
    private static class TestExecutionReport extends ExecutionReport {
        // Simple test implementation
    }
    
    @BeforeEach
    public void setUp() {
        testClient = new TestBossaApiClient();
        manager = new StrategyManager(testClient);
        
        testStrategy1 = new TestStrategy("RSI Strategy", "Test RSI Strategy");
        testStrategy2 = new TestStrategy("MA Strategy", "Test Moving Average Strategy");
    }
    
    @Test
    public void testAddStrategy() {
        // Given
        StrategyParameters params = new StrategyParameters();
        params.setInstruments(Arrays.asList("KGHM", "PKO"));
        
        // When
        boolean result = manager.addStrategy(testStrategy1, params);
        
        // Then
        assertTrue(result, "Adding strategy should return true");
        assertEquals(1, manager.getStrategies().size(), "Manager should have one strategy");
        assertSame(testStrategy1, manager.getStrategies().get(0), "Added strategy should be the same instance");
    }
    
    @Test
    public void testAddNullStrategy() {
        // When
        boolean result = manager.addStrategy(null, new StrategyParameters());
        
        // Then
        assertFalse(result, "Adding null strategy should return false");
        assertEquals(0, manager.getStrategies().size(), "Manager should have no strategies");
    }
    
    @Test
    public void testAddStrategyWithNullParameters() {
        // When
        boolean result = manager.addStrategy(testStrategy1, null);
        
        // Then
        assertFalse(result, "Adding strategy with null parameters should return false");
        assertEquals(0, manager.getStrategies().size(), "Manager should have no strategies");
    }
    
    @Test
    public void testStartAllStrategies() {
        // Given
        StrategyParameters params = new StrategyParameters();
        manager.addStrategy(testStrategy1, params);
        manager.addStrategy(testStrategy2, params);
        
        // When
        manager.startAllStrategies();
        
        // Then
        assertEquals(StrategyStatus.RUNNING, testStrategy1.getStatus(), "First strategy should be running");
        assertEquals(StrategyStatus.RUNNING, testStrategy2.getStatus(), "Second strategy should be running");
    }
    
    @Test
    public void testStopAllStrategies() {
        // Given
        StrategyParameters params = new StrategyParameters();
        manager.addStrategy(testStrategy1, params);
        manager.addStrategy(testStrategy2, params);
        manager.startAllStrategies();
        
        // When
        manager.stopAllStrategies();
        
        // Then
        assertEquals(StrategyStatus.STOPPED, testStrategy1.getStatus(), "First strategy should be stopped");
        assertEquals(StrategyStatus.STOPPED, testStrategy2.getStatus(), "Second strategy should be stopped");
    }
    
    @Test
    public void testGetStrategies() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        manager.addStrategy(testStrategy2, new StrategyParameters());
        
        // When
        List<InvestmentStrategy> strategies = manager.getStrategies();
        
        // Then
        assertEquals(2, strategies.size(), "Should return 2 strategies");
        assertTrue(strategies.contains(testStrategy1), "Should contain first strategy");
        assertTrue(strategies.contains(testStrategy2), "Should contain second strategy");
        
        // Verify the list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            strategies.add(new TestStrategy("New", "New"));
        });
    }
    
    @Test
    public void testRemoveStrategy() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        manager.addStrategy(testStrategy2, new StrategyParameters());
        
        // When
        boolean result = manager.removeStrategy("RSI Strategy");
        
        // Then
        assertTrue(result, "Removing existing strategy should return true");
        assertEquals(1, manager.getStrategies().size(), "Should have one strategy left");
        assertSame(testStrategy2, manager.getStrategies().get(0), "Remaining strategy should be the second one");
        assertEquals(StrategyStatus.STOPPED, testStrategy1.getStatus(), "Removed strategy should be stopped");
    }
    
    @Test
    public void testRemoveNonexistentStrategy() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        
        // When
        boolean result = manager.removeStrategy("Nonexistent Strategy");
        
        // Then
        assertFalse(result, "Removing nonexistent strategy should return false");
        assertEquals(1, manager.getStrategies().size(), "Strategy list should be unchanged");
    }
    
    @Test
    public void testDistributeMarketData() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        manager.addStrategy(testStrategy2, new StrategyParameters());
        TestMarketDataResponse marketData = new TestMarketDataResponse();
        
        // When
        testClient.simulateMarketData(marketData);
        
        // Then
        assertTrue(testStrategy1.wasMarketDataProcessed(), "First strategy should process market data");
        assertTrue(testStrategy2.wasMarketDataProcessed(), "Second strategy should process market data");
    }
    
    @Test
    public void testDistributeExecutionReport() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        manager.addStrategy(testStrategy2, new StrategyParameters());
        TestExecutionReport report = new TestExecutionReport();
        
        // When
        testClient.simulateExecutionReport(report);
        
        // Then
        assertTrue(testStrategy1.wasExecutionReportProcessed(), "First strategy should process execution report");
        assertTrue(testStrategy2.wasExecutionReportProcessed(), "Second strategy should process execution report");
    }
    
    @Test
    public void testMultipleMarketDataDistribution() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        TestMarketDataResponse marketData1 = new TestMarketDataResponse();
        TestMarketDataResponse marketData2 = new TestMarketDataResponse();
        
        // When
        testClient.simulateMarketData(marketData1);
        testClient.simulateMarketData(marketData2);
        
        // Then
        assertEquals(2, testStrategy1.getMarketDataCount(), "Strategy should receive both market data updates");
    }
    
    @Test
    public void testMultipleExecutionReportDistribution() {
        // Given
        manager.addStrategy(testStrategy1, new StrategyParameters());
        TestExecutionReport report1 = new TestExecutionReport();
        TestExecutionReport report2 = new TestExecutionReport();
        
        // When
        testClient.simulateExecutionReport(report1);
        testClient.simulateExecutionReport(report2);
        
        // Then
        assertEquals(2, testStrategy1.getExecutionReportCount(), "Strategy should receive both execution reports");
    }
    
    @Test
    public void testSetGui() {
        // Given
        TradingAppGUI gui = new TradingAppGUI();
        manager.addStrategy(testStrategy1, new StrategyParameters());
        manager.addStrategy(testStrategy2, new StrategyParameters());
        
        // When
        manager.setGui(gui);
        
        // Then
        assertSame(gui, testStrategy1.getGui(), "First strategy should have GUI set");
        assertSame(gui, testStrategy2.getGui(), "Second strategy should have GUI set");
    }
    
    @Test
    public void testDisplayStatistics() {
        // This is mostly a coverage test since the method just logs data
        manager.addStrategy(testStrategy1, new StrategyParameters());
        
        // Should not throw an exception
        manager.displayStatistics();
    }
}
