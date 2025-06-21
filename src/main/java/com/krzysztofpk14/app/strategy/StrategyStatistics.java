package com.krzysztofpk14.app.strategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Klasa przechowująca statystyki strategii inwestycyjnej.
 */
public class StrategyStatistics {
    private final AtomicInteger totalBuys = new AtomicInteger(0);
    private final AtomicInteger totalSells = new AtomicInteger(0);
    private final AtomicReference<Double> totalBuyVolume = new AtomicReference<>(0.0);
    private final AtomicReference<Double> totalSellVolume = new AtomicReference<>(0.0);
    private final AtomicReference<Double> totalBuyValue = new AtomicReference<>(0.0);
    private final AtomicReference<Double> totalSellValue = new AtomicReference<>(0.0);
    private final AtomicInteger winningTrades = new AtomicInteger(0);
    private final AtomicInteger losingTrades = new AtomicInteger(0);
    private final AtomicReference<Double> totalPnL = new AtomicReference<>(0.0);
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /**
     * Zwiększa liczbę transakcji kupna.
     */
    public void incrementTotalBuys() {
        totalBuys.incrementAndGet();
    }
    
    /**
     * Zwiększa liczbę transakcji sprzedaży.
     */
    public void incrementTotalSells() {
        totalSells.incrementAndGet();
    }
    
    /**
     * Dodaje ilość do całkowitego wolumenu kupna.
     * 
     * @param volume Ilość
     */
    public void addTotalBuyVolume(double volume) {
        totalBuyVolume.updateAndGet(v -> v + volume);
    }
    
    /**
     * Dodaje ilość do całkowitego wolumenu sprzedaży.
     * 
     * @param volume Ilość
     */
    public void addTotalSellVolume(double volume) {
        totalSellVolume.updateAndGet(v -> v + volume);
    }
    
    /**
     * Dodaje wartość do całkowitej wartości kupna.
     * 
     * @param value Wartość
     */
    public void addTotalBuyValue(double value) {
        totalBuyValue.updateAndGet(v -> v + value);
    }
    
    /**
     * Dodaje wartość do całkowitej wartości sprzedaży.
     * 
     * @param value Wartość
     */
    public void addTotalSellValue(double value) {
        totalSellValue.updateAndGet(v -> v + value);
    }
    
    /**
     * Zwiększa liczbę zyskownych transakcji.
     */
    public void incrementWinningTrades() {
        winningTrades.incrementAndGet();
    }
    
    /**
     * Zwiększa liczbę stratnych transakcji.
     */
    public void incrementLosingTrades() {
        losingTrades.incrementAndGet();
    }
    
    /**
     * Dodaje wartość do całkowitego P&L.
     * 
     * @param pnl Wartość P&L
     */
    public void addTotalPnL(double pnl) {
        totalPnL.updateAndGet(v -> v + pnl);
    }
    
    /**
     * Ustawia czas rozpoczęcia działania strategii.
     * 
     * @param startTime Czas rozpoczęcia
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Ustawia czas zakończenia działania strategii.
     * 
     * @param endTime Czas zakończenia
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Zwraca liczbę transakcji kupna.
     * 
     * @return Liczba transakcji kupna
     */
    public int getTotalBuys() {
        return totalBuys.get();
    }
    
    /**
     * Zwraca liczbę transakcji sprzedaży.
     * 
     * @return Liczba transakcji sprzedaży
     */
    public int getTotalSells() {
        return totalSells.get();
    }
    
    /**
     * Zwraca całkowitą ilość kupna.
     * 
     * @return Całkowita ilość kupna
     */
    public double getTotalBuyVolume() {
        return totalBuyVolume.get();
    }
    
    /**
     * Zwraca całkowitą ilość sprzedaży.
     * 
     * @return Całkowita ilość sprzedaży
     */
    public double getTotalSellVolume() {
        return totalSellVolume.get();
    }

    /**
     * Zwraca liczbę wszystkich transakcji (kupna + sprzedaży).
     * 
     * @return Liczba wszystkich transakcji
     */
    public int getTradeCount() {
        return totalBuys.get() + totalSells.get();
    }
    
    /**
     * Zwraca całkowitą wartość kupna.
     * 
     * @return Całkowita wartość kupna
     */
    public double getTotalBuyValue() {
        return totalBuyValue.get();
    }
    
    /**
     * Zwraca całkowitą wartość sprzedaży.
     * 
     * @return Całkowita wartość sprzedaży
     */
    public double getTotalSellValue() {
        return totalSellValue.get();
    }
    
    /**
     * Zwraca liczbę zyskownych transakcji.
     * 
     * @return Liczba zyskownych transakcji
     */
    public int getWinningTrades() {
        return winningTrades.get();
    }
    
    /**
     * Zwraca liczbę stratnych transakcji.
     * 
     * @return Liczba stratnych transakcji
     */
    public int getLosingTrades() {
        return losingTrades.get();
    }
    
    /**
     * Zwraca całkowity P&L (Profit and Loss).
     * 
     * @return Całkowity P&L
     */
    public double getTotalPnL() {
        return totalPnL.get();
    }
    
    /**
     * Zwraca czas rozpoczęcia działania strategii.
     * 
     * @return Czas rozpoczęcia
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Zwraca czas zakończenia działania strategii.
     * 
     * @return Czas zakończenia
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * Zwraca czas działania strategii.
     * 
     * @return Czas działania
     */
    public Duration getRunningTime() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return startTime != null ? Duration.between(startTime, end) : Duration.ZERO;
    }
    
    /**
     * Zwraca win ratio (stosunek zyskownych transakcji do wszystkich).
     * 
     * @return Win ratio (0.0-1.0)
     */
    public double getWinRatio() {
        int total = winningTrades.get() + losingTrades.get();
        return total > 0 ? (double) winningTrades.get() / total : 0.0;
    }

    public String toString() {
        return "StrategyStatistics{" +
                "totalBuys=" + totalBuys +
                ", totalSells=" + totalSells +
                ", totalBuyVolume=" + totalBuyVolume +
                ", totalSellVolume=" + totalSellVolume +
                ", totalBuyValue=" + totalBuyValue +
                ", totalSellValue=" + totalSellValue +
                ", winningTrades=" + winningTrades +
                ", losingTrades=" + losingTrades +
                ", totalPnL=" + totalPnL +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public String displayStatistics() {
        return "Strategy Statistics:\n" +
                "Total Buys: " + totalBuys.get() + "\n" +
                "Total Sells: " + totalSells.get() + "\n" +
                "Total Buy Volume: " + totalBuyVolume.get() + "\n" +
                "Total Sell Volume: " + totalSellVolume.get() + "\n" +
                "Total Buy Value: " + totalBuyValue.get() + "\n" +
                "Total Sell Value: " + totalSellValue.get() + "\n" +
                "Winning Trades: " + winningTrades.get() + "\n" +
                "Losing Trades: " + losingTrades.get() + "\n" +
                "Total P&L: " + totalPnL.get() + "\n" +
                "Win Ratio: " + getWinRatio() * 100 + "%\n" +
                "Running Time: " + getRunningTime().toMinutes() + " minutes";
    }
}