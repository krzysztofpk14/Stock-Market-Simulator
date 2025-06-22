package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.TradingAppGUI;

import java.util.Arrays;

/**
 * Unit tests for strategies implementing InvestmentStrategy.
 * This tests a sample implementation, not using Mockito.
 */
public class InvestmentStrategyTest {

    private TestInvestmentStrategy strategy;
    private StrategyParameters parameters;
    
    /**
     * Custom test implementation of InvestmentStrategy for testing purposes.
     */
    private static class TestInvestmentStrategy implements InvestmentStrategy {
        private StrategyParameters parameters;
        private StrategyStatus status = StrategyStatus.INITIALIZED;
        private final StrategyStatistics statistics = new StrategyStatistics();
        private boolean initializeCalled = false;
        private boolean startCalled = false;
        private boolean stopCalled = false;
        private boolean marketDataProcessed = false;
        private boolean executionReportProcessed = false;
        private TradingAppGUI gui;
        
        @Override
        public void initialize(StrategyParameters parameters) {
            this.parameters = parameters;
            this.initializeCalled = true;
            this.status = StrategyStatus.INITIALIZED;
        }
        
        @Override
        public void start() {
            this.startCalled = true;
            this.status = StrategyStatus.RUNNING;
            this.statistics.setStartTime(java.time.LocalDateTime.now());
        }
        
        @Override
        public void stop() {
            this.stopCalled = true;
            this.status = StrategyStatus.STOPPED;
            this.statistics.setEndTime(java.time.LocalDateTime.now());
        }
        
        @Override
        public void onMarketData(MarketDataResponse marketData) {
            this.marketDataProcessed = true;
        }
        
        @Override
        public void onExecutionReport(ExecutionReport report) {
            this.executionReportProcessed = true;
        }
        
        @Override
        public String getName() {
            return "Test Strategy";
        }
        
        @Override
        public String getDescription() {
            return "Test Strategy Description";
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
        
        // Helpers for testing
        public boolean wasInitializeCalled() {
            return initializeCalled;
        }
        
        public boolean wasStartCalled() {
            return startCalled;
        }
        
        public boolean wasStopCalled() {
            return stopCalled;
        }
        
        public boolean wasMarketDataProcessed() {
            return marketDataProcessed;
        }
        
        public boolean wasExecutionReportProcessed() {
            return executionReportProcessed;
        }
        
        public TradingAppGUI getGui() {
            return gui;
        }
        
        public StrategyParameters getParameters() {
            return parameters;
        }
    }
    
    @BeforeEach
    public void setUp() {
        strategy = new TestInvestmentStrategy();
        parameters = new StrategyParameters();
        parameters.setInstruments(Arrays.asList("KGHM", "PKO"));
        parameters.setParam("period", 14);
        parameters.setParam("threshold", 0.5);
    }
    
    @Test
    public void testLifecycle() {
        // Test initialization
        strategy.initialize(parameters);
        assertTrue(strategy.wasInitializeCalled(), "Initialize should be called");
        assertEquals(StrategyStatus.INITIALIZED, strategy.getStatus(), "Status should be INITIALIZED");
        assertSame(parameters, strategy.getParameters(), "Parameters should be stored");
        
        // Test start
        strategy.start();
        assertTrue(strategy.wasStartCalled(), "Start should be called");
        assertEquals(StrategyStatus.RUNNING, strategy.getStatus(), "Status should be RUNNING");
        assertNotNull(strategy.getStatistics().getStartTime(), "Start time should be set");
        
        // Test stop
        strategy.stop();
        assertTrue(strategy.wasStopCalled(), "Stop should be called");
        assertEquals(StrategyStatus.STOPPED, strategy.getStatus(), "Status should be STOPPED");
        assertNotNull(strategy.getStatistics().getEndTime(), "End time should be set");
    }
    
    @Test
    public void testMarketDataHandling() {
        // Given
        MarketDataResponse data = new MarketDataResponse();
        
        // When
        strategy.onMarketData(data);
        
        // Then
        assertTrue(strategy.wasMarketDataProcessed(), "Market data should be processed");
    }
    
    @Test
    public void testExecutionReportHandling() {
        // Given
        ExecutionReport report = new ExecutionReport();
        
        // When
        strategy.onExecutionReport(report);
        
        // Then
        assertTrue(strategy.wasExecutionReportProcessed(), "Execution report should be processed");
    }
    
    @Test
    public void testGetters() {
        // When initialized with parameters
        strategy.initialize(parameters);
        
        // Then getters should return expected values
        assertEquals("Test Strategy", strategy.getName(), "Name should match");
        assertEquals("Test Strategy Description", strategy.getDescription(), "Description should match");
        assertEquals("Test Parameters", strategy.getParametersAsString(), "Parameters string should match");
        assertEquals("KGHM, PKO", strategy.getInstruments(), "Instruments should match parameters");
    }
    
    @Test
    public void testGuiIntegration() {
        // Given
        TradingAppGUI gui = new TradingAppGUI();
        
        // When
        strategy.setGui(gui);
        
        // Then
        assertSame(gui, strategy.getGui(), "GUI should be stored");
    }
}
