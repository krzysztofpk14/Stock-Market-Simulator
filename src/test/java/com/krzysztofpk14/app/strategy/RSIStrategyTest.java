package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

import java.util.Arrays;

/**
 * Unit tests for the RSIStrategy class.
 * This test demonstrates testing a concrete strategy implementation without using Mockito.
 */
public class RSIStrategyTest {

    private TestBossaApiClient testClient;
    private RSIStrategy strategy;
    private StrategyParameters parameters;
    
    /**
     * Test double for BossaApiClient
     */
    private static class TestBossaApiClient extends BossaApiClient {
        public TestBossaApiClient() {
            super();
        }
    }
    
    /**
     * Test implementation of MarketDataResponse
     */
    private static class TestMarketDataResponse extends MarketDataResponse {
        public TestMarketDataResponse(String symbol, double price) {
            // Set up the necessary fields
            Instrument instrument = new Instrument();
            instrument.setSymbol(symbol);
            setInstrument(instrument);
            
            MarketDataGroup group = new MarketDataGroup();
            group.setPrice(Double.toString(price));
            setMarketDataGroups(Arrays.asList(group));
        }
    }
    
    @BeforeEach
    public void setUp() {
        testClient = new TestBossaApiClient();
        strategy = new RSIStrategy(testClient);
        
        // Create and initialize parameters
        parameters = new StrategyParameters();
        parameters.setInstruments(Arrays.asList("KGHM", "PKO"))
                .setParam("rsiPeriod", 5)  // Use small period for testing
                .setParam("oversoldThreshold", 30.0)
                .setParam("overboughtThreshold", 70.0)
                .setParam("tradeSize", 100.0);
        
        strategy.initialize(parameters);
    }
    
    @Test
    public void testInitialize() {
        // Given
        // Parameters already set in setUp()
        
        // When - initialize is called in setUp()
        
        // Then
        assertEquals(StrategyStatus.INITIALIZED, strategy.getStatus(), 
                "Status should be INITIALIZED after initialization");
    }
    
    @Test
    public void testStartAndStop() {
        // When
        strategy.start();
        
        // Then
        assertEquals(StrategyStatus.RUNNING, strategy.getStatus(), 
                "Status should be RUNNING after start");
        
        // When
        strategy.stop();
        
        // Then
        assertEquals(StrategyStatus.STOPPED, strategy.getStatus(), 
                "Status should be STOPPED after stop");
    }
    
    @Test
    public void testGetters() {
        // Test basic getters
        assertEquals("RSI Strategy", strategy.getName(), 
                "Name should match expected value");
        assertTrue(strategy.getDescription().contains("RSI"), 
                "Description should contain 'RSI'");
        assertNotNull(strategy.getStatistics(), 
                "Statistics object should not be null");
    }
    
    @Test
    public void testStrategyProcessesMarketData() {
        // Given
        strategy.start();
        TestMarketDataResponse data = new TestMarketDataResponse("KGHM", 100.0);
        
        // When
        strategy.onMarketData(data);
        
        // Then - since we're not using Mockito, we verify by checking the internal state
        // This is a basic smoke test - more comprehensive testing would need to verify
        // trading logic which would require access to private fields
    }
    
    @Test
    public void testStrategyBuildsPriceHistory() {
        // Given
        strategy.start();
        String symbol = "KGHM";
        
        // When - feed several price points
        for (int i = 0; i < 15; i++) {
            TestMarketDataResponse data = new TestMarketDataResponse(symbol, 100.0 + i);
            strategy.onMarketData(data);
        }
        
        // Then - we'd need to verify internal state, but this is mostly a smoke test
        // to ensure processing multiple data points doesn't throw exceptions
    }
    
    @Test
    public void testGetParametersAsString() {
        // When
        String paramsString = strategy.getParametersAsString();
        
        // Then
        assertNotNull(paramsString, "Parameters string should not be null");
        assertTrue(paramsString.length() > 0, "Parameters string should not be empty");
    }
    
    @Test
    public void testGetInstruments() {
        // When
        String instruments = strategy.getInstruments();
        
        // Then
        assertTrue(instruments.contains("KGHM"), "Instruments should contain KGHM");
        assertTrue(instruments.contains("PKO"), "Instruments should contain PKO");
    }
}
