package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the StrategyParameters class.
 */
public class StrategyParametersTest {

    private StrategyParameters params;
    private static final List<String> TEST_INSTRUMENTS = Arrays.asList("KGHM", "PKO", "PKN");
    
    @BeforeEach
    public void setUp() {
        params = new StrategyParameters();
    }
    
    @Test
    public void testInstruments() {
        // When
        params.setInstruments(TEST_INSTRUMENTS);
        
        // Then
        assertEquals(TEST_INSTRUMENTS, params.getInstruments(), "Instruments should match what was set");
        
        // Ensure method is chainable
        StrategyParameters result = params.setInstruments(Arrays.asList("WIG20"));
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testClosePositionsOnStop() {
        // Default value should be true
        assertTrue(params.isClosePositionsOnStop(), "Default close positions on stop should be true");
        
        // When
        params.setClosePositionsOnStop(false);
        
        // Then
        assertFalse(params.isClosePositionsOnStop(), "Close positions on stop should be false after setting");
        
        // Ensure method is chainable
        StrategyParameters result = params.setClosePositionsOnStop(true);
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testSetStringParam() {
        // When
        params.setParam("name", "MovingAverageStrategy");
        
        // Then
        assertEquals("MovingAverageStrategy", params.getStringParam("name", "default"), 
                "String param should match what was set");
        
        // Test default value
        assertEquals("default", params.getStringParam("nonexistent", "default"), 
                "Should return default value for nonexistent param");
        
        // Ensure method is chainable
        StrategyParameters result = params.setParam("another", "value");
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testSetIntParam() {
        // When
        params.setParam("period", 10);
        
        // Then
        assertEquals(10, params.getIntParam("period", 0), 
                "Int param should match what was set");
        
        // Test default value
        assertEquals(42, params.getIntParam("nonexistent", 42), 
                "Should return default value for nonexistent param");
        
        // Test String to int conversion
        params.setParam("stringInt", "20");
        assertEquals(20, params.getIntParam("stringInt", 0), 
                "Should convert String to int if necessary");
        
        // Ensure method is chainable
        StrategyParameters result = params.setParam("another", 30);
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testSetDoubleParam() {
        // When
        params.setParam("threshold", 0.5);
        
        // Then
        assertEquals(0.5, params.getDoubleParam("threshold", 0.0), 0.001, 
                "Double param should match what was set");
        
        // Test default value
        assertEquals(3.14, params.getDoubleParam("nonexistent", 3.14), 0.001, 
                "Should return default value for nonexistent param");
        
        // Test String to double conversion
        params.setParam("stringDouble", "1.5");
        assertEquals(1.5, params.getDoubleParam("stringDouble", 0.0), 0.001, 
                "Should convert String to double if necessary");
        
        // Test int to double conversion
        params.setParam("intDouble", 42);
        assertEquals(42.0, params.getDoubleParam("intDouble", 0.0), 0.001, 
                "Should convert int to double if necessary");
        
        // Ensure method is chainable
        StrategyParameters result = params.setParam("another", 2.5);
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testSetBooleanParam() {
        // When
        params.setParam("active", true);
        
        // Then
        assertTrue(params.getBooleanParam("active", false), 
                "Boolean param should match what was set");
        
        // Test default value
        assertTrue(params.getBooleanParam("nonexistent", true), 
                "Should return default value for nonexistent param");
        
        // Test String to boolean conversion
        params.setParam("stringBoolean", "true");
        assertTrue(params.getBooleanParam("stringBoolean", false), 
                "Should convert String 'true' to boolean true");
        
        // Ensure method is chainable
        StrategyParameters result = params.setParam("another", false);
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testSetObjectParam() {
        // Given
        Object customObject = new Object();
        
        // When
        params.setParam("object", customObject);
        
        // Then
        assertSame(customObject, params.getParam("object"), 
                "Object param should be the same instance that was set");
        
        // Test non-existent
        assertNull(params.getParam("nonexistent"), 
                "Should return null for nonexistent param");
        
        // Ensure method is chainable
        StrategyParameters result = params.setParam("another", new Object());
        assertSame(params, result, "Method should return this for chaining");
    }
    
    @Test
    public void testGetAllParameters() {
        // Given
        params.setParam("string", "value");
        params.setParam("int", 10);
        params.setParam("double", 1.5);
        params.setParam("boolean", true);
        
        // When
        Map<String, Object> allParams = params.getAllParameters();
        
        // Then
        assertEquals(4, allParams.size(), "All parameters map should have correct size");
        assertEquals("value", allParams.get("string"), "String parameter should be in map");
        assertEquals(10, allParams.get("int"), "Integer parameter should be in map");
        assertEquals(1.5, allParams.get("double"), "Double parameter should be in map");
        assertEquals(true, allParams.get("boolean"), "Boolean parameter should be in map");
    }
    
    @Test
    public void testInvalidParameterConversions() {
        // Set non-numeric value for int
        params.setParam("badInt", "not-a-number");
        assertEquals(42, params.getIntParam("badInt", 42), 
                "Should return default value when string can't be parsed as int");
        
        // Set non-numeric value for double
        params.setParam("badDouble", "not-a-number");
        assertEquals(3.14, params.getDoubleParam("badDouble", 3.14), 0.001, 
                "Should return default value when string can't be parsed as double");
    }
    
    @Test
    public void testComprehensiveChaining() {
        // Test comprehensive method chaining with multiple parameter types
        params.setInstruments(TEST_INSTRUMENTS)
              .setClosePositionsOnStop(false)
              .setParam("name", "Strategy1")
              .setParam("period", 14)
              .setParam("threshold", 0.75)
              .setParam("active", true);
        
        // Verify all parameters were set correctly
        assertEquals(TEST_INSTRUMENTS, params.getInstruments());
        assertFalse(params.isClosePositionsOnStop());
        assertEquals("Strategy1", params.getStringParam("name", ""));
        assertEquals(14, params.getIntParam("period", 0));
        assertEquals(0.75, params.getDoubleParam("threshold", 0.0), 0.001);
        assertTrue(params.getBooleanParam("active", false));
    }
}
