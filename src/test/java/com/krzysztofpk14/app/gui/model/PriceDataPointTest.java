package com.krzysztofpk14.app.gui.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 * Unit tests for the PriceDataPoint class.
 */
public class PriceDataPointTest {
    
    private static final long TEST_TIMESTAMP = 1672531200000L; // 2023-01-01 00:00:00 UTC
    private static final double TEST_PRICE = 150.75;
    private static final int TEST_INDEX = 42;
    
    private PriceDataPoint dataPoint;
    
    @BeforeEach
    public void setUp() {
        dataPoint = new PriceDataPoint(TEST_TIMESTAMP, TEST_PRICE, TEST_INDEX);
    }
    
    @Test
    public void testConstructor() {
        // Verify that constructor sets values properly
        assertEquals(TEST_TIMESTAMP, dataPoint.getTimestamp(), "Timestamp should match the constructor value");
        assertEquals(TEST_PRICE, dataPoint.getPrice(), "Price should match the constructor value");
        assertEquals(TEST_INDEX, dataPoint.getIndex(), "Index should match the constructor value");
    }
    
    @Test
    public void testGetTimestamp() {
        assertEquals(TEST_TIMESTAMP, dataPoint.getTimestamp(), "Timestamp getter should return correct value");
    }
    
    @Test
    public void testGetPrice() {
        assertEquals(TEST_PRICE, dataPoint.getPrice(), "Price getter should return correct value");
    }
    
    @Test
    public void testGetIndex() {
        assertEquals(TEST_INDEX, dataPoint.getIndex(), "Index getter should return correct value");
    }
    
    @Test
    public void testImmutability() {
        // Create a new data point with the same values
        PriceDataPoint newDataPoint = new PriceDataPoint(TEST_TIMESTAMP, TEST_PRICE, TEST_INDEX);
        
        // Verify objects are different instances but have equal values
        assertNotSame(dataPoint, newDataPoint, "Different instances should not be the same object");
        assertEquals(dataPoint.getTimestamp(), newDataPoint.getTimestamp(), "Timestamp values should be equal");
        assertEquals(dataPoint.getPrice(), newDataPoint.getPrice(), "Price values should be equal");
        assertEquals(dataPoint.getIndex(), newDataPoint.getIndex(), "Index values should be equal");
    }
    
    @Test
    public void testWithDifferentValues() {
        // Create data points with different values
        PriceDataPoint dataPoint1 = new PriceDataPoint(TEST_TIMESTAMP + 1000, TEST_PRICE + 1.0, TEST_INDEX + 1);
        PriceDataPoint dataPoint2 = new PriceDataPoint(TEST_TIMESTAMP - 1000, TEST_PRICE - 1.0, TEST_INDEX - 1);
        
        // Verify values are as expected
        assertEquals(TEST_TIMESTAMP + 1000, dataPoint1.getTimestamp());
        assertEquals(TEST_PRICE + 1.0, dataPoint1.getPrice());
        assertEquals(TEST_INDEX + 1, dataPoint1.getIndex());
        
        assertEquals(TEST_TIMESTAMP - 1000, dataPoint2.getTimestamp());
        assertEquals(TEST_PRICE - 1.0, dataPoint2.getPrice());
        assertEquals(TEST_INDEX - 1, dataPoint2.getIndex());
    }
}
