package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Position class.
 */
public class PositionTest {

    private Position position;
    private static final String TEST_SYMBOL = "KGHM";
    private static final double DELTA = 0.001; // Delta for floating-point comparisons
    
    @BeforeEach
    public void setUp() {
        position = new Position(TEST_SYMBOL);
    }
    
    @Test
    public void testConstructor() {
        assertEquals(TEST_SYMBOL, position.getSymbol(), "Symbol should match constructor value");
        assertEquals("0", position.getQuantity(), "Initial quantity should be 0");
        assertEquals(0.0, position.getAvgPrice(), DELTA, "Initial average price should be 0");
        assertEquals(0.0, position.getRealizedPnL(), DELTA, "Initial P&L should be 0");
        assertEquals(Position.Direction.FLAT, position.getDirection(), "Initial direction should be FLAT");
    }
    
    @Test
    public void testAddBuy_FromFlat() {
        // When: Adding a buy when position is flat
        position.addBuy(100, 150.0);
        
        // Then: Position should be long
        assertEquals("100", position.getQuantity(), "Quantity should match buy quantity");
        assertEquals(150.0, position.getAvgPrice(), DELTA, "Average price should match buy price");
        assertEquals(0.0, position.getRealizedPnL(), DELTA, "P&L should remain 0");
        assertEquals(Position.Direction.LONG, position.getDirection(), "Direction should be LONG");
    }
    
    @Test
    public void testAddBuy_AddToLongPosition() {
        // Given: A long position
        position.addBuy(100, 150.0);
        
        // When: Adding more to the long position
        position.addBuy(50, 160.0);
        
        // Then: Position should be updated with new average price
        assertEquals("150", position.getQuantity(), "Quantity should be summed");
        assertEquals(153.333, position.getAvgPrice(), 0.001, "Average price should be weighted average");
        assertEquals(Position.Direction.LONG, position.getDirection(), "Direction should remain LONG");
    }
    
    @Test
    public void testAddBuy_FromShort_PartialCover() {
        // Given: A short position
        position.addSell(100, 150.0);
        
        // When: Partially covering the short position
        position.addBuy(50, 140.0);
        
        // Then: Position should remain short but with reduced quantity
        assertEquals("50", position.getQuantity(), "Quantity should be reduced");
        assertEquals(150.0, position.getAvgPrice(), DELTA, "Average price should remain the same");
        assertEquals(500.0, position.getRealizedPnL(), DELTA, "P&L should reflect profit from short cover");
        assertEquals(Position.Direction.SHORT, position.getDirection(), "Direction should remain SHORT");
    }
    
    @Test
    public void testAddBuy_FromShort_FullCover() {
        // Given: A short position
        position.addSell(100, 150.0);
        
        // When: Fully covering the short position
        position.addBuy(100, 140.0);
        
        // Then: Position should become flat
        assertEquals("0", position.getQuantity(), "Quantity should be 0 after full cover");
        assertEquals(1000.0, position.getRealizedPnL(), DELTA, "P&L should reflect profit from short cover");
        assertEquals(Position.Direction.FLAT, position.getDirection(), "Direction should be FLAT");
    }
    
    @Test
    public void testAddBuy_FromShort_OverCover() {
        // Given: A short position
        position.addSell(100, 150.0);
        
        // When: Buying more than the short position
        position.addBuy(150, 140.0);
        
        // Then: Position should flip to long
        assertEquals("50", position.getQuantity(), "Quantity should be the excess buy amount");
        assertEquals(140.0, position.getAvgPrice(), DELTA, "Average price should be the buy price");
        assertEquals(1000.0, position.getRealizedPnL(), DELTA, "P&L should reflect profit from short cover");
        assertEquals(Position.Direction.LONG, position.getDirection(), "Direction should flip to LONG");
    }
    
    @Test
    public void testAddSell_FromFlat() {
        // When: Adding a sell when position is flat
        position.addSell(100, 150.0);
        
        // Then: Position should be short
        assertEquals("100", position.getQuantity(), "Quantity should match sell quantity");
        assertEquals(150.0, position.getAvgPrice(), DELTA, "Average price should match sell price");
        assertEquals(0.0, position.getRealizedPnL(), DELTA, "P&L should remain 0");
        assertEquals(Position.Direction.SHORT, position.getDirection(), "Direction should be SHORT");
    }
    
    @Test
    public void testAddSell_AddToShortPosition() {
        // Given: A short position
        position.addSell(100, 150.0);
        
        // When: Adding more to the short position
        position.addSell(50, 160.0);
        
        // Then: Position should be updated with new average price
        assertEquals("150", position.getQuantity(), "Quantity should be summed");
        assertEquals(153.333, position.getAvgPrice(), 0.001, "Average price should be weighted average");
        assertEquals(Position.Direction.SHORT, position.getDirection(), "Direction should remain SHORT");
    }
    
    @Test
    public void testAddSell_FromLong_PartialClose() {
        // Given: A long position
        position.addBuy(100, 150.0);
        
        // When: Partially closing the long position
        position.addSell(50, 160.0);
        
        // Then: Position should remain long but with reduced quantity
        assertEquals("50", position.getQuantity(), "Quantity should be reduced");
        assertEquals(150.0, position.getAvgPrice(), DELTA, "Average price should remain the same");
        assertEquals(500.0, position.getRealizedPnL(), DELTA, "P&L should reflect profit from partial close");
        assertEquals(Position.Direction.LONG, position.getDirection(), "Direction should remain LONG");
    }
    
    @Test
    public void testAddSell_FromLong_FullClose() {
        // Given: A long position
        position.addBuy(100, 150.0);
        
        // When: Fully closing the long position
        position.addSell(100, 160.0);
        
        // Then: Position should become flat
        assertEquals("0", position.getQuantity(), "Quantity should be 0 after full close");
        assertEquals(1000.0, position.getRealizedPnL(), DELTA, "P&L should reflect profit from close");
        assertEquals(Position.Direction.FLAT, position.getDirection(), "Direction should be FLAT");
    }
    
    @Test
    public void testAddSell_FromLong_OverClose() {
        // Given: A long position
        position.addBuy(100, 150.0);
        
        // When: Selling more than the long position
        position.addSell(150, 160.0);
        
        // Then: Position should flip to short
        assertEquals("50", position.getQuantity(), "Quantity should be the excess sell amount");
        assertEquals(160.0, position.getAvgPrice(), DELTA, "Average price should be the sell price");
        assertEquals(1000.0, position.getRealizedPnL(), DELTA, "P&L should reflect profit from long close");
        assertEquals(Position.Direction.SHORT, position.getDirection(), "Direction should flip to SHORT");
    }
    
    @Test
    public void testComplexScenario() {
        // A series of buys and sells representing a realistic trading scenario
        position.addBuy(100, 100.0);   // Long 100@100
        position.addSell(50, 110.0);   // Long 50@100, P&L: 500
        position.addSell(100, 90.0);   // Short 50@90, P&L: 500 - 500 = 0
        position.addBuy(70, 80.0);     // Short 50 - 70 = Long 20@80, P&L: 0 + 50*10 = 500
        position.addSell(40, 90.0);    // Long 20 - 40 = Short 20@90, P&L: 500 + 20*10 = 700
        position.addBuy(20, 85.0);     // Flat, P&L: 700 + 20*5 = 800
        
        // Final assertions
        assertEquals("0", position.getQuantity(), "Final quantity should be 0");
        assertEquals(800.0, position.getRealizedPnL(), DELTA, "Final P&L should be 800");
        assertEquals(Position.Direction.FLAT, position.getDirection(), "Final direction should be FLAT");
    }
    
    @Test
    public void testDirectionEnum() {
        // Test direction enum logic
        position.addBuy(100, 100.0);
        assertEquals(Position.Direction.LONG, position.getDirection(), "Direction should be LONG");
        
        position.addSell(100, 100.0);
        assertEquals(Position.Direction.FLAT, position.getDirection(), "Direction should be FLAT");
        
        position.addSell(50, 100.0);
        assertEquals(Position.Direction.SHORT, position.getDirection(), "Direction should be SHORT");
    }
}
