package com.krzysztofpk14.app.gui.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for the MarketDataModel class.
 */
public class MarketDataModelTest {
    
    private static final String TEST_SYMBOL = "KGHM";
    private static final String TEST_PRICE = "150.75";
    private static final String TEST_TIMESTAMP = "20250621-14:30:45.123";
    
    private MarketDataModel model;
    
    @BeforeEach
    public void setUp() {
        model = new MarketDataModel(TEST_SYMBOL, TEST_PRICE, TEST_TIMESTAMP);
    }
    
    @Test
    public void testConstructor() {
        // Verify that constructor sets values properly
        assertEquals(TEST_SYMBOL, model.getSymbol(), "Symbol should match the constructor value");
        assertEquals(TEST_PRICE, model.getPrice(), "Price should match the constructor value");
        assertEquals(TEST_TIMESTAMP, model.getTimestamp(), "Timestamp should match the constructor value");
    }
    
    @Test
    public void testPropertyObjects() {
        // Verify property objects return the correct values
        assertEquals(TEST_SYMBOL, model.symbolProperty().get(), "Symbol property should match");
        assertEquals(TEST_PRICE, model.priceProperty().get(), "Price property should match");
        assertEquals(TEST_TIMESTAMP, model.timestampProperty().get(), "Timestamp property should match");
    }
    
    @Test
    public void testSetSymbol() {
        // Given
        String newSymbol = "PKO";
        
        // When
        model.setSymbol(newSymbol);
        
        // Then
        assertEquals(newSymbol, model.getSymbol(), "Symbol getter should return new value");
        assertEquals(newSymbol, model.symbolProperty().get(), "Symbol property should reflect new value");
    }
    
    @Test
    public void testSetPrice() {
        // Given
        String newPrice = "160.25";
        
        // When
        model.setPrice(newPrice);
        
        // Then
        assertEquals(newPrice, model.getPrice(), "Price getter should return new value");
        assertEquals(newPrice, model.priceProperty().get(), "Price property should reflect new value");
    }
    
    @Test
    public void testSetTimestamp() {
        // Given
        String newTimestamp = "20250621-15:45:30.456";
        
        // When
        model.setTimestamp(newTimestamp);
        
        // Then
        assertEquals(newTimestamp, model.getTimestamp(), "Timestamp getter should return new value");
        assertEquals(newTimestamp, model.timestampProperty().get(), "Timestamp property should reflect new value");
    }
    
    @Test
    public void testPropertyBindings() {
        // Create a new model to test property binding
        MarketDataModel anotherModel = new MarketDataModel("", "", "");
        
        // Bind properties
        anotherModel.symbolProperty().bind(model.symbolProperty());
        anotherModel.priceProperty().bind(model.priceProperty());
        anotherModel.timestampProperty().bind(model.timestampProperty());
        
        // Verify initial binding worked
        assertEquals(TEST_SYMBOL, anotherModel.getSymbol(), "Symbol should be bound");
        assertEquals(TEST_PRICE, anotherModel.getPrice(), "Price should be bound");
        assertEquals(TEST_TIMESTAMP, anotherModel.getTimestamp(), "Timestamp should be bound");
        
        // Change original properties
        model.setSymbol("PKN");
        model.setPrice("175.50");
        model.setTimestamp("20250621-16:00:00.000");
        
        // Verify changes propagated through bindings
        assertEquals("PKN", anotherModel.getSymbol(), "Symbol binding should update");
        assertEquals("175.50", anotherModel.getPrice(), "Price binding should update");
        assertEquals("20250621-16:00:00.000", anotherModel.getTimestamp(), "Timestamp binding should update");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "null", "INVALID"})
    public void testInvalidSymbols(String symbol) {
        // Test that the model handles potentially problematic symbol values
        if ("null".equals(symbol)) {
            model.setSymbol(null);
            assertNull(model.getSymbol(), "Null symbol should be allowed");
        } else {
            model.setSymbol(symbol);
            assertEquals(symbol, model.getSymbol(), "Any symbol string should be allowed");
        }
    }
}
