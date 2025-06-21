package com.krzysztofpk14.app.strategy;

/**
 * Klasa reprezentująca pozycję inwestycyjną.
 */
public class Position {
    /**
     * Kierunek pozycji (długa lub krótka).
     */
    public enum Direction {
        LONG, SHORT, FLAT
    }
    
    private final String symbol;
    private double quantity;
    private double avgPrice;
    private double realizedPnL;
    
    /**
     * Tworzy nową pozycję dla danego instrumentu.
     * 
     * @param symbol Symbol instrumentu
     */
    public Position(String symbol) {
        this.symbol = symbol;
        this.quantity = 0;
        this.avgPrice = 0;
        this.realizedPnL = 0;
    }
    
    /**
     * Dodaje transakcję kupna do pozycji.
     * 
     * @param quantity Ilość
     * @param price Cena
     */
    public void addBuy(double quantity, double price) {
        if (this.quantity < 0) {
            // Jeśli mamy pozycję krótką, zamykamy część lub całość
            double closeQuantity = Math.min(Math.abs(this.quantity), quantity);
            double remainingQuantity = quantity - closeQuantity;
            
            // Oblicz zysk/stratę na zamkniętej części
            double pnl = closeQuantity * (this.avgPrice - price);
            this.realizedPnL += pnl;
            
            // Aktualizuj pozostałą ilość
            this.quantity += closeQuantity;
            
            // Jeśli pozostała ilość kupna, dodaj jako nową pozycję długą
            if (remainingQuantity > 0) {
                this.quantity += remainingQuantity;
                this.avgPrice = price;
            }
        } else {
            // Mamy pozycję długą lub brak pozycji, dodajemy do długiej
            if (this.quantity == 0) {
                this.avgPrice = price;
                this.quantity = quantity;
            } else {
                // Uśrednij cenę
                double totalValue = this.quantity * this.avgPrice + quantity * price;
                this.quantity += quantity;
                this.avgPrice = totalValue / this.quantity;
            }
        }
    }
    
    /**
     * Dodaje transakcję sprzedaży do pozycji.
     * 
     * @param quantity Ilość
     * @param price Cena
     */
    public void addSell(double quantity, double price) {
        if (this.quantity > 0) {
            // Jeśli mamy pozycję długą, zamykamy część lub całość
            double closeQuantity = Math.min(this.quantity, quantity);
            double remainingQuantity = quantity - closeQuantity;
            
            // Oblicz zysk/stratę na zamkniętej części
            double pnl = closeQuantity * (price - this.avgPrice);
            this.realizedPnL += pnl;
            
            // Aktualizuj pozostałą ilość
            this.quantity -= closeQuantity;
            
            // Jeśli pozostała ilość sprzedaży, dodaj jako nową pozycję krótką
            if (remainingQuantity > 0) {
                this.quantity = -remainingQuantity;
                this.avgPrice = price;
            }
        } else {
            // Mamy pozycję krótką lub brak pozycji, dodajemy do krótkiej
            if (this.quantity == 0) {
                this.avgPrice = price;
                this.quantity = -quantity;
            } else {
                // Uśrednij cenę
                double totalValue = Math.abs(this.quantity) * this.avgPrice + quantity * price;
                this.quantity -= quantity;
                this.avgPrice = totalValue / Math.abs(this.quantity);
            }
        }
    }
    
    /**
     * Zwraca symbol instrumentu.
     * 
     * @return Symbol instrumentu
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * Zwraca ilość instrumentu jako String.
     * 
     * @return Ilość instrumentu
     */
    public String getQuantity() {
        return String.valueOf((int)Math.abs(this.quantity));
    }
    
    /**
     * Zwraca cenę średnią pozycji.
     * 
     * @return Cena średnia
     */
    public double getAvgPrice() {
        return avgPrice;
    }
    
    /**
     * Zwraca zrealizowany P&L.
     * 
     * @return Zrealizowany P&L
     */
    public double getRealizedPnL() {
        return realizedPnL;
    }

    public Direction getDirection() {
        if (this.quantity > 0) {
            return Direction.LONG;
        } else if (this.quantity < 0) {
            return Direction.SHORT;
        } else {
            return Direction.FLAT;
        }
    }
}