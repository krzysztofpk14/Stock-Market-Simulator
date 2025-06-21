package com.krzysztofpk14.app.strategy;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.TradingAppGUI;

import java.util.HashMap; 
import java.util.LinkedList;
import java.util.Map;

/**
 * Strategia inwestycyjna bazująca na wskaźniku RSI (Relative Strength Index).
 * Kupuje gdy RSI jest poniżej dolnego progu (warunek wyprzedania),
 * sprzedaje gdy RSI jest powyżej górnego progu (warunek wykupienia).
 */
public class RSIStrategy extends AbstractInvestmentStrategy {
    
    // Parametry strategii
    private int rsiPeriod;
    private double oversoldThreshold;
    private double overboughtThreshold;
    private double tradeSize; 
    
    // Bufory cenowe i danych RSI
    private final Map<String, LinkedList<Double>> priceBuffers = new HashMap<>();
    private final Map<String, Double> lastRSI = new HashMap<>();
    
    // Status pozycji
    private final Map<String, Boolean> inPosition = new HashMap<>();
    
    /**
     * Konstruktor.
     * 
     * @param apiClient Klient API do komunikacji z serwerem
     */
    public RSIStrategy(BossaApiClient apiClient) {
        super(apiClient);
    }
    
    @Override
    protected void doInitialize(StrategyParameters parameters) {
        // Pobierz specyficzne parametry dla tej strategii
        this.rsiPeriod = parameters.getIntParam("rsiPeriod", 14);
        this.oversoldThreshold = parameters.getDoubleParam("oversoldThreshold", 30.0);
        this.overboughtThreshold = parameters.getDoubleParam("overboughtThreshold", 70.0);
        this.tradeSize = parameters.getDoubleParam("tradeSize", 100.0);
        
        // Inicjalizacja buforów i statusów dla każdego instrumentu
        if (parameters.getInstruments() != null) {
            for (String symbol : parameters.getInstruments()) {
                priceBuffers.put(symbol, new LinkedList<>());
                lastRSI.put(symbol, 50.0); // Neutralna wartość początkowa
                inPosition.put(symbol, false);
            }
        }
    }
    
    @Override
    protected void doStart() {
        System.out.println("\nStrategia RSI została uruchomiona.");
        System.out.println("Okres RSI: " + rsiPeriod);
        System.out.println("Próg wyprzedania: " + oversoldThreshold);
        System.out.println("Próg wykupienia: " + overboughtThreshold);
    }
    
    @Override
    protected void doStop() {
        System.out.println("Strategia RSI została zatrzymana.");
    }
    
    @Override
    protected void processMarketData(MarketDataResponse marketData) {
        String symbol = marketData.getInstrument().getSymbol();
        
        // Pobierz cenę z danych rynkowych
        double price = getLastPrice(marketData);    
        
        // Dodaj cenę do bufora
        LinkedList<Double> buffer = priceBuffers.get(symbol);
        if (buffer == null) {
            buffer = new LinkedList<>();
            priceBuffers.put(symbol, buffer);
        }
        
        buffer.add(price);
        
        // Ogranicz rozmiar bufora do okresu RSI + 1 (potrzebujemy zmiany ceny)
        while (buffer.size() > rsiPeriod + 1) {
            buffer.removeFirst();
        }
        
        // Jeśli mamy wystarczająco danych, oblicz RSI
        if (buffer.size() > rsiPeriod) {
            double rsi = calculateRSI(buffer);
            lastRSI.put(symbol, rsi);
            
            
            // Generuj sygnały na podstawie RSI
            boolean hasPosition = inPosition.getOrDefault(symbol, false);
            
            if (rsi <= oversoldThreshold && !hasPosition) {
                // Sygnał kupna (wyprzedanie)
                openLongPosition(symbol, price);
                inPosition.put(symbol, true);
            } else if (rsi >= overboughtThreshold && hasPosition) {
                // Sygnał sprzedaży (wykupienie)
                closePosition(symbol);
                inPosition.put(symbol, false);
            }
        }
    }
    
    /**
     * Oblicza wskaźnik RSI.
     * 
     * @param prices Lista cen
     * @return Wartość RSI (0-100)
     */
    private double calculateRSI(LinkedList<Double> prices) {
        if (prices.size() <= 1) {
            return 50.0;
        }
        
        double sumGain = 0.0;
        double sumLoss = 0.0;
        
        // Oblicz początkowe sumy zysków i strat
        for (int i = 1; i < prices.size() && i <= rsiPeriod; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                sumGain += change;
            } else {
                sumLoss -= change;
            }
        }
        
        // Oblicz średnie zyski i straty
        double avgGain = sumGain / rsiPeriod;
        double avgLoss = sumLoss / rsiPeriod;
        
        // Oblicz RS (Relative Strength)
        double rs = avgLoss > 0 ? avgGain / avgLoss : 100.0;
        
        // Oblicz RSI
        return 100.0 - (100.0 / (1.0 + rs));
    }
    
    /**
     * Otwiera długą pozycję.
     * 
     * @param symbol Symbol instrumentu
     * @param price Cena
     */
    private void openLongPosition(String symbol, double price) {
        // Sprawdź czy nie mamy już pozycji dla tego instrumentu
        if (positions.containsKey(symbol)) {
            return;
        }
        
        int quantity = calculateQuantity(price);
        
        System.out.println("Generowanie sygnalu BUY dla " + symbol + " po cenie " + price + ", ilość: " + quantity);
        
        // Tworzenie zlecenia kupna
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("RSI-ORD" + orderIdCounter.incrementAndGet());
        order.setSide(OrderRequest.BUY);
        
        // Ustawienie instrumentu
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        // Ustawienie ilości
        OrderRequest.OrderQuantity orderQuantity = new OrderRequest.OrderQuantity();
        orderQuantity.setQuantity(String.valueOf(quantity));
        order.setOrderQuantity(orderQuantity);
        order.setPrice(String.valueOf(price));
        
        // Ustawienie typu zlecenia (rynkowe)
        order.setOrderType(OrderRequest.MARKET);
        order.setTimeInForce(OrderRequest.DAY);
        
        // Wysłanie zlecenia
        sendOrder(order);
    }
    
    /**
     * Zamyka istniejącą pozycję dla danego instrumentu.
     * 
     * @param symbol Symbol instrumentu
     */
    private void closePosition(String symbol) {
        Position position = positions.get(symbol);
        
        if (position == null) {
            return;
        }
        
        System.out.println("Zamykanie pozycji dla " + symbol + ", kierunek: " + position.getDirection() + 
                          ", ilość: " + position.getQuantity());
        
        // Tworzenie zlecenia zamykającego pozycję
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("RSI-ORD-CLOSE" + orderIdCounter.incrementAndGet());
        
        // Ustawienie przeciwnego kierunku do pozycji
        if (position.getDirection() == Position.Direction.LONG) {
            order.setSide(OrderRequest.SELL);
        } else {
            order.setSide(OrderRequest.BUY);
        }
        
        // Ustawienie instrumentu
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        // Ustawienie ilości
        OrderRequest.OrderQuantity quantity = new OrderRequest.OrderQuantity();
        quantity.setQuantity(position.getQuantity());
        order.setOrderQuantity(quantity);
        
        // Ustawienie typu zlecenia (rynkowe)
        order.setOrderType(OrderRequest.MARKET); // Po każdej cenie
        order.setTimeInForce(OrderRequest.DAY);
        
        // Wysłanie zlecenia
        sendOrder(order);
    }
    
    /**
     * Oblicza ilość instrumentów do kupna/sprzedaży na podstawie ceny.
     * 
     * @param price Cena instrumentu
     * @return Ilość instrumentów
     */
    private int calculateQuantity(double price) {
        if (price <= 0) {
            return 0;
        }
        
        // Oblicz ilość instrumentów na podstawie wartości transakcji i ceny
        int quantity = (int) Math.floor(tradeSize / price);
        
        // Minimalna ilość to 1
        return Math.max(1, quantity);
    }
    
    /**
     * Pobiera ostatnią cenę z danych rynkowych.
     * 
     * @param marketData Dane rynkowe
     * @return Ostatnia cena lub 0 jeśli niedostępna
     */
    private double getLastPrice(MarketDataResponse marketData) {
        if (marketData.getMarketDataGroups() != null && !marketData.getMarketDataGroups().isEmpty()) {
            for (MarketDataResponse.MarketDataGroup group : marketData.getMarketDataGroups()) {
                if (MarketDataResponse.TRADE.equals(group.getMarketDataEntryType())) {
                    try {
                        return Double.parseDouble(group.getPrice());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }
    
    @Override
    protected void processExecutionReport(ExecutionReport report) {
        System.out.println("Otrzymano raport wykonania: " + report.getExecutionType() + 
                          " dla zlecenia " + report.getClientOrderId() + 
                          ", status: " + report.getOrderStatus());
    }
    
    @Override
    public String getName() {
        return "RSI Strategy";
    }
    
    @Override
    public String getDescription() {
        return "Strategia bazująca na wskaźniku RSI (Relative Strength Index). " +
               "Kupuje gdy RSI jest poniżej progu wyprzedania, sprzedaje gdy RSI jest powyżej progu wykupienia.";
    }

    @Override
    public void setGui(TradingAppGUI gui) {
        super.setGui(gui);
    }
}