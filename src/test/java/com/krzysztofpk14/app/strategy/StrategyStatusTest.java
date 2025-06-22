package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the StrategyStatus enum.
 */
public class StrategyStatusTest {
    
    @Test
    public void testEnumValues() {
        // Verify all expected enum values exist
        StrategyStatus[] statuses = StrategyStatus.values();
        assertEquals(5, statuses.length, "Should have 5 status values");
        
        // Verify each enum value individually
        assertEquals("INITIALIZED", StrategyStatus.INITIALIZED.name(), "Enum name should be INITIALIZED");
        assertEquals("RUNNING", StrategyStatus.RUNNING.name(), "Enum name should be RUNNING");
        assertEquals("STOPPING", StrategyStatus.STOPPING.name(), "Enum name should be STOPPING");
        assertEquals("STOPPED", StrategyStatus.STOPPED.name(), "Enum name should be STOPPED");
        assertEquals("ERROR", StrategyStatus.ERROR.name(), "Enum name should be ERROR");
    }
    
    @Test
    public void testEnumOrderAndOrdinals() {
        // Verify ordinals match expected order
        assertEquals(0, StrategyStatus.INITIALIZED.ordinal(), "INITIALIZED should have ordinal 0");
        assertEquals(1, StrategyStatus.RUNNING.ordinal(), "RUNNING should have ordinal 1");
        assertEquals(2, StrategyStatus.STOPPING.ordinal(), "STOPPING should have ordinal 2");
        assertEquals(3, StrategyStatus.STOPPED.ordinal(), "STOPPED should have ordinal 3");
        assertEquals(4, StrategyStatus.ERROR.ordinal(), "ERROR should have ordinal 4");
    }
    
    @Test
    public void testValueOf() {
        // Verify valueOf method works correctly
        assertEquals(StrategyStatus.INITIALIZED, StrategyStatus.valueOf("INITIALIZED"), 
                "valueOf should return correct enum for INITIALIZED");
        assertEquals(StrategyStatus.RUNNING, StrategyStatus.valueOf("RUNNING"), 
                "valueOf should return correct enum for RUNNING");
        assertEquals(StrategyStatus.STOPPING, StrategyStatus.valueOf("STOPPING"), 
                "valueOf should return correct enum for STOPPING");
        assertEquals(StrategyStatus.STOPPED, StrategyStatus.valueOf("STOPPED"), 
                "valueOf should return correct enum for STOPPED");
        assertEquals(StrategyStatus.ERROR, StrategyStatus.valueOf("ERROR"), 
                "valueOf should return correct enum for ERROR");
    }
    
    @Test
    public void testValueOfInvalidName() {
        // Verify that valueOf throws exception for invalid names
        assertThrows(IllegalArgumentException.class, () -> StrategyStatus.valueOf("NONEXISTENT"),
                "valueOf should throw IllegalArgumentException for invalid name");
    }
}
