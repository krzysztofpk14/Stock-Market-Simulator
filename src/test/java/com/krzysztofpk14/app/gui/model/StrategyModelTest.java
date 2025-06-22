package com.krzysztofpk14.app.gui.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the StrategyModel class.
 */
public class StrategyModelTest {
    
    private static final String TEST_NAME = "RSI Strategy";
    private static final String TEST_STATUS = "RUNNING";
    private static final String TEST_TRADES = "42";
    private static final String TEST_WIN_RATE = "65.5%";
    private static final String TEST_PNL = "+4500.25";
    
    private StrategyModel model;
    
    @BeforeEach
    public void setUp() {
        model = new StrategyModel(TEST_NAME, TEST_STATUS, TEST_TRADES, TEST_WIN_RATE, TEST_PNL);
    }
    
    @Test
    public void testConstructor() {
        // Verify that constructor sets values properly
        assertEquals(TEST_NAME, model.getName(), "Name should match the constructor value");
        assertEquals(TEST_STATUS, model.getStatus(), "Status should match the constructor value");
        assertEquals(TEST_TRADES, model.getTrades(), "Trades should match the constructor value");
        assertEquals(TEST_WIN_RATE, model.getWinRate(), "WinRate should match the constructor value");
        assertEquals(TEST_PNL, model.getPnl(), "PNL should match the constructor value");
    }
    
    @Test
    public void testPropertyObjects() {
        // Verify property objects return the correct values
        assertEquals(TEST_NAME, model.nameProperty().get(), "Name property should match");
        assertEquals(TEST_STATUS, model.statusProperty().get(), "Status property should match");
        assertEquals(TEST_TRADES, model.tradesProperty().get(), "Trades property should match");
        assertEquals(TEST_WIN_RATE, model.winRateProperty().get(), "WinRate property should match");
        assertEquals(TEST_PNL, model.pnlProperty().get(), "PNL property should match");
    }
    
    @Test
    public void testSetName() {
        // Given
        String newName = "Moving Average Strategy";
        
        // When
        model.setName(newName);
        
        // Then
        assertEquals(newName, model.getName(), "Name getter should return new value");
        assertEquals(newName, model.nameProperty().get(), "Name property should reflect new value");
    }
    
    @Test
    public void testSetStatus() {
        // Given
        String newStatus = "STOPPED";
        
        // When
        model.setStatus(newStatus);
        
        // Then
        assertEquals(newStatus, model.getStatus(), "Status getter should return new value");
        assertEquals(newStatus, model.statusProperty().get(), "Status property should reflect new value");
    }
    
    @Test
    public void testSetTrades() {
        // Given
        String newTrades = "50";
        
        // When
        model.setTrades(newTrades);
        
        // Then
        assertEquals(newTrades, model.getTrades(), "Trades getter should return new value");
        assertEquals(newTrades, model.tradesProperty().get(), "Trades property should reflect new value");
    }
    
    @Test
    public void testSetWinRate() {
        // Given
        String newWinRate = "70.2%";
        
        // When
        model.setWinRate(newWinRate);
        
        // Then
        assertEquals(newWinRate, model.getWinRate(), "WinRate getter should return new value");
        assertEquals(newWinRate, model.winRateProperty().get(), "WinRate property should reflect new value");
    }
    
    @Test
    public void testSetPnl() {
        // Given
        String newPnl = "+6000.75";
        
        // When
        model.setPnl(newPnl);
        
        // Then
        assertEquals(newPnl, model.getPnl(), "PNL getter should return new value");
        assertEquals(newPnl, model.pnlProperty().get(), "PNL property should reflect new value");
    }
    
    @Test
    public void testPropertyBindings() {
        // Create a new model to test property binding
        StrategyModel anotherModel = new StrategyModel("", "", "", "", "");
        
        // Bind properties
        anotherModel.nameProperty().bind(model.nameProperty());
        anotherModel.statusProperty().bind(model.statusProperty());
        anotherModel.tradesProperty().bind(model.tradesProperty());
        anotherModel.winRateProperty().bind(model.winRateProperty());
        anotherModel.pnlProperty().bind(model.pnlProperty());
        
        // Verify initial binding worked
        assertEquals(TEST_NAME, anotherModel.getName(), "Name should be bound");
        assertEquals(TEST_STATUS, anotherModel.getStatus(), "Status should be bound");
        assertEquals(TEST_TRADES, anotherModel.getTrades(), "Trades should be bound");
        assertEquals(TEST_WIN_RATE, anotherModel.getWinRate(), "WinRate should be bound");
        assertEquals(TEST_PNL, anotherModel.getPnl(), "PNL should be bound");
        
        // Change original properties
        model.setName("MACD Strategy");
        model.setStatus("PAUSED");
        model.setTrades("55");
        model.setWinRate("68.3%");
        model.setPnl("+5250.50");
        
        // Verify changes propagated through bindings
        assertEquals("MACD Strategy", anotherModel.getName(), "Name binding should update");
        assertEquals("PAUSED", anotherModel.getStatus(), "Status binding should update");
        assertEquals("55", anotherModel.getTrades(), "Trades binding should update");
        assertEquals("68.3%", anotherModel.getWinRate(), "WinRate binding should update");
        assertEquals("+5250.50", anotherModel.getPnl(), "PNL binding should update");
    }
    
    @Test
    public void testNullValues() {
        // Create model with null values
        StrategyModel nullModel = new StrategyModel(null, null, null, null, null);
        
        // Test getters with null values
        assertNull(nullModel.getName(), "Name should be null");
        assertNull(nullModel.getStatus(), "Status should be null");
        assertNull(nullModel.getTrades(), "Trades should be null");
        assertNull(nullModel.getWinRate(), "WinRate should be null");
        assertNull(nullModel.getPnl(), "PNL should be null");
    }
}
