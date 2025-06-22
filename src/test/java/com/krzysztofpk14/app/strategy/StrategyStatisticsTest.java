package com.krzysztofpk14.app.strategy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Unit tests for the StrategyStatistics class.
 */
public class StrategyStatisticsTest {

    private StrategyStatistics stats;
    private static final double DELTA = 0.001; // Delta for floating-point comparisons
    
    @BeforeEach
    public void setUp() {
        stats = new StrategyStatistics();
    }
    
    @Test
    public void testInitialValues() {
        // Initial values should be zero
        assertEquals(0, stats.getTotalBuys(), "Initial total buys should be 0");
        assertEquals(0, stats.getTotalSells(), "Initial total sells should be 0");
        assertEquals(0.0, stats.getTotalBuyVolume(), DELTA, "Initial buy volume should be 0");
        assertEquals(0.0, stats.getTotalSellVolume(), DELTA, "Initial sell volume should be 0");
        assertEquals(0.0, stats.getTotalBuyValue(), DELTA, "Initial buy value should be 0");
        assertEquals(0.0, stats.getTotalSellValue(), DELTA, "Initial sell value should be 0");
        assertEquals(0, stats.getTradeCount(), "Initial trade count should be 0");
        assertEquals(0, stats.getWinningTrades(), "Initial winning trades should be 0");
        assertEquals(0, stats.getLosingTrades(), "Initial losing trades should be 0");
        assertEquals(0.0, stats.getTotalPnL(), DELTA, "Initial P&L should be 0");
        assertEquals(0.0, stats.getWinRatio(), DELTA, "Initial win ratio should be 0");
        assertNull(stats.getStartTime(), "Initial start time should be null");
        assertNull(stats.getEndTime(), "Initial end time should be null");
    }
    
    @Test
    public void testIncrementBuysAndSells() {
        // When
        stats.incrementTotalBuys();
        stats.incrementTotalBuys();
        stats.incrementTotalSells();
        
        // Then
        assertEquals(2, stats.getTotalBuys(), "Total buys should be 2");
        assertEquals(1, stats.getTotalSells(), "Total sells should be 1");
        assertEquals(3, stats.getTradeCount(), "Trade count should be 3");
    }
    
    @Test
    public void testAddVolumes() {
        // When
        stats.addTotalBuyVolume(100.0);
        stats.addTotalBuyVolume(50.0);
        stats.addTotalSellVolume(75.0);
        
        // Then
        assertEquals(150.0, stats.getTotalBuyVolume(), DELTA, "Total buy volume should be summed correctly");
        assertEquals(75.0, stats.getTotalSellVolume(), DELTA, "Total sell volume should be summed correctly");
    }
    
    @Test
    public void testAddValues() {
        // When
        stats.addTotalBuyValue(10000.0);
        stats.addTotalBuyValue(5000.0);
        stats.addTotalSellValue(8000.0);
        
        // Then
        assertEquals(15000.0, stats.getTotalBuyValue(), DELTA, "Total buy value should be summed correctly");
        assertEquals(8000.0, stats.getTotalSellValue(), DELTA, "Total sell value should be summed correctly");
    }
    
    @Test
    public void testWinningAndLosingTrades() {
        // When
        stats.incrementWinningTrades();
        stats.incrementWinningTrades();
        stats.incrementWinningTrades();
        stats.incrementLosingTrades();
        stats.incrementLosingTrades();
        
        // Then
        assertEquals(3, stats.getWinningTrades(), "Winning trades should be 3");
        assertEquals(2, stats.getLosingTrades(), "Losing trades should be 2");
        assertEquals(0.6, stats.getWinRatio(), DELTA, "Win ratio should be 0.6 (60%)");
    }
    
    @Test
    public void testWinRatioWithZeroTrades() {
        // When no trades
        assertEquals(0.0, stats.getWinRatio(), DELTA, "Win ratio should be 0 when no trades");
        
        // When only losing trades
        stats.incrementLosingTrades();
        assertEquals(0.0, stats.getWinRatio(), DELTA, "Win ratio should be 0 when only losing trades");
        
        // When only winning trades
        stats = new StrategyStatistics(); // Reset
        stats.incrementWinningTrades();
        assertEquals(1.0, stats.getWinRatio(), DELTA, "Win ratio should be 1 when only winning trades");
    }
    
    @Test
    public void testTotalPnL() {
        // When
        stats.addTotalPnL(500.0);
        stats.addTotalPnL(-200.0);
        stats.addTotalPnL(300.0);
        
        // Then
        assertEquals(600.0, stats.getTotalPnL(), DELTA, "Total P&L should be summed correctly");
    }
    
    @Test
    public void testStartAndEndTime() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        // When
        stats.setStartTime(startTime);
        stats.setEndTime(endTime);
        
        // Then
        assertEquals(startTime, stats.getStartTime(), "Start time should be set correctly");
        assertEquals(endTime, stats.getEndTime(), "End time should be set correctly");
    }
    
    @Test
    public void testRunningTime() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);
        
        // When
        stats.setStartTime(startTime);
        stats.setEndTime(endTime);
        
        // Then
        assertEquals(60, stats.getRunningTime().toMinutes(), "Running time should be 1 hour (60 minutes)");
    }
    
    @Test
    public void testRunningTimeOngoing() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        
        // When
        stats.setStartTime(startTime);
        // No end time set means strategy is still running
        
        // Then - this test might be slightly flaky due to timing, so we check the range
        long runningMinutes = stats.getRunningTime().toMinutes();
        assertTrue(runningMinutes >= 29 && runningMinutes <= 31, 
                "Running time should be approximately 30 minutes, was: " + runningMinutes);
    }
    
    @Test
    public void testRunningTimeNoStart() {
        // When no start time is set
        assertEquals(0, stats.getRunningTime().toSeconds(), "Running time should be 0 when no start time");
    }
    
    @Test
    public void testToStringMethod() {
        // When adding some data
        stats.incrementTotalBuys();
        stats.incrementWinningTrades();
        stats.addTotalPnL(100.0);
        
        // Then toString should include key information
        String toStringResult = stats.toString();
        assertTrue(toStringResult.contains("totalBuys=1"), "toString should include buys count");
        assertTrue(toStringResult.contains("winningTrades=1"), "toString should include winning trades");
        assertTrue(toStringResult.contains("totalPnL=100.0"), "toString should include PnL");
    }
    
    @Test
    public void testDisplayStatistics() {
        // When adding some data
        stats.incrementTotalBuys();
        stats.incrementWinningTrades();
        stats.addTotalPnL(100.0);
        
        // Then display should include key information
        String display = stats.displayStatistics();
        assertTrue(display.contains("Total Buys: 1"), "Display should include buys count");
        assertTrue(display.contains("Winning Trades: 1"), "Display should include winning trades");
        assertTrue(display.contains("Total P&L: 100.0"), "Display should include PnL");
        assertTrue(display.contains("Win Ratio: 100.0%"), "Display should include win ratio");
    }
    
    @Test
    public void testThreadSafety() {
        // This is a basic test of thread safety by simulating concurrent operations
        // For a thorough test, more sophisticated concurrency testing would be needed
        
        // Create several threads all updating the same statistics
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                stats.incrementTotalBuys();
                stats.addTotalBuyVolume(10.0);
                stats.addTotalPnL(5.0);
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                stats.incrementTotalSells();
                stats.addTotalSellVolume(10.0);
                stats.addTotalPnL(-3.0);
            }
        });
        
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                stats.incrementWinningTrades();
                stats.addTotalBuyValue(100.0);
            }
            for (int i = 0; i < 50; i++) {
                stats.incrementLosingTrades();
                stats.addTotalSellValue(100.0);
            }
        });
        
        // Start all threads
        t1.start();
        t2.start();
        t3.start();
        
        // Wait for completion
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            fail("Thread execution was interrupted");
        }
        
        // Verify results - the exact values may vary if operations aren't atomic
        assertEquals(100, stats.getTotalBuys(), "Total buys should be 100");
        assertEquals(100, stats.getTotalSells(), "Total sells should be 100");
        assertEquals(1000.0, stats.getTotalBuyVolume(), DELTA, "Total buy volume should be 1000");
        assertEquals(1000.0, stats.getTotalSellVolume(), DELTA, "Total sell volume should be 1000");
        assertEquals(5000.0, stats.getTotalBuyValue(), DELTA, "Total buy value should be 5000");
        assertEquals(5000.0, stats.getTotalSellValue(), DELTA, "Total sell value should be 5000");
        assertEquals(50, stats.getWinningTrades(), "Winning trades should be 50");
        assertEquals(50, stats.getLosingTrades(), "Losing trades should be 50");
        assertEquals(200.0, stats.getTotalPnL(), DELTA, "Total P&L should be 200");
    }
}
