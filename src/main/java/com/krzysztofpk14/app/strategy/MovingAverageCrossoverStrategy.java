package com.krzysztofpk14.app.strategy;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Strategia inwestycyjna bazująca na przecięciu średnich kroczących.
 * Kupuje gdy SMA krótkoterminowa przebija SMA długoterminową od dołu,
 * sprzedaje gdy SMA krótkoterminowa przebija SMA długoterminową od góry.
 */
public class MovingAverageCrossoverStrategy extends AbstractInvestmentStrategy {
    
    // Parametry strategii
    private int shortSMAPeriod;
    private int longSMAPeriod;
    private double tradeSize;
    
    // Bufory cenowe dla obliczania średnich kroczących
    private final Map<String, LinkedList<Double>> priceBuffers = new HashMap<>();
    
    // Ostatnie średnie kroczące
    private final Map<String, Double> lastShortSMA = new HashMap<>();
    private final Map<String, Double> lastLongSMA = new HashMap<>();
    
    // Ostatni sygnał
    private final Map<String, Signal> lastSignals = new HashMap<>();
    
    // Typ sygnału
    private enum Signal {
        NONE,
        BUY,
        SELL
    }
    
    /**
     * Konstruktor.
     * 
     * @param apiClient Klient API do komunikacji z serwerem
     */
    public MovingAverageCrossoverStrategy(BossaApiClient apiClient) {
        super(apiClient);
    }
    
    @Override
    protected void doInitialize(StrategyParameters parameters) {
        // Pobierz specyficzne parametry dla tej strategii
        this.shortSMAPeriod = parameters.getIntParam("shortSMAPeriod", 10);
        this.longSMAPeriod = parameters.getIntParam("longSMAPeriod", 30);
        this.tradeSize = parameters.getDoubleParam("tradeSize", 100.0);
        
        // Inicjalizacja buforów i sygnałów dla każdego instrumentu
        if (parameters.getInstruments() != null) {
            for (String symbol : parameters.getInstruments()) {
                priceBuffers.put(symbol, new LinkedList<>());
                lastShortSMA.put(symbol, 0.0);
                lastLongSMA.put(symbol, 0.0);
                lastSignals.put(symbol, Signal.NONE);
            }
        }
    }
    
    @Override
    protected void doStart() {
        System.out.println("\nStrategia Moving Average Crossover została uruchomiona.");
        System.out.println("Krótkoterminowe SMA: " + shortSMAPeriod);
        System.out.println("Długoterminowe SMA: " + longSMAPeriod);
    }
    
    @Override
    protected void doStop() {
        System.out.println("Strategia Moving Average Crossover została zatrzymana.");
    }
    
    @Override
    protected void processMarketData(MarketDataResponse marketData) {
        String symbol = marketData.getInstrument().getSymbol();
        
        // Pobierz cenę z danych rynkowych (zakładamy że jest dostępna)
        double price = getLastPrice(marketData);
        
        if (price <= 0) {
            return;  // Brak poprawnej ceny
        }
        
        // Dodaj cenę do bufora
        LinkedList<Double> buffer = priceBuffers.get(symbol);
        if (buffer == null) {
            buffer = new LinkedList<>();
            priceBuffers.put(symbol, buffer);
        }
        
        buffer.add(price);
        
        // Ogranicz rozmiar bufora do maksymalnego okresu
        while (buffer.size() > longSMAPeriod) {
            buffer.removeFirst();
        }
        
        // Jeśli mamy wystarczająco danych, oblicz średnie kroczące
        if (buffer.size() >= longSMAPeriod) {
            // Oblicz krótkoterminowe SMA
            double shortSMA = calculateSMA(buffer, shortSMAPeriod);
            lastShortSMA.put(symbol, shortSMA);
            
            // Oblicz długoterminowe SMA
            double longSMA = calculateSMA(buffer, longSMAPeriod);
            lastLongSMA.put(symbol, longSMA);
            
            // Generuj sygnał
            Signal currentSignal = Signal.NONE;
            
            if (shortSMA > longSMA) {
                currentSignal = Signal.BUY;
            } else if (shortSMA < longSMA) {
                currentSignal = Signal.SELL;
            }
            
            // Sprawdź czy doszło do zmiany sygnału
            Signal previousSignal = lastSignals.getOrDefault(symbol, Signal.NONE);
            if (currentSignal != previousSignal) {
                handleSignalChange(symbol, previousSignal, currentSignal, price);
                lastSignals.put(symbol, currentSignal);
            }
        }
    }
    
    /**
     * Oblicza średnią kroczącą dla podanego okresu.
     * 
     * @param buffer Bufor cenowy
     * @param period Okres średniej
     * @return Wartość średniej kroczącej
     */
    private double calculateSMA(LinkedList<Double> buffer, int period) {
        int startIndex = Math.max(0, buffer.size() - period);
        double sum = 0;
        
        for (int i = startIndex; i < buffer.size(); i++) {
            sum += buffer.get(i);
        }
        
        return sum / (buffer.size() - startIndex);
    }
    
    /**
     * Obsługuje zmianę sygnału.
     * 
     * @param symbol Symbol instrumentu
     * @param previousSignal Poprzedni sygnał
     * @param currentSignal Aktualny sygnał
     * @param price Aktualna cena
     */
    private void handleSignalChange(String symbol, Signal previousSignal, Signal currentSignal, double price) {
        // Sprawdź czy mamy zmianę sygnału na BUY lub SELL
        if (currentSignal == Signal.BUY && previousSignal != Signal.BUY) {
            // Jeśli mamy otwartą pozycję short, zamknij ją
            Position position = positions.get(symbol);
            if (position != null && position.getDirection() == Position.Direction.SHORT) {
                closePosition(symbol);
            }
            
            // Otwórz długą pozycję
            openLongPosition(symbol, price);
            
        } else if (currentSignal == Signal.SELL && previousSignal != Signal.SELL) {
            // Jeśli mamy otwartą pozycję long, zamknij ją
            Position position = positions.get(symbol);
            if (position != null && position.getDirection() == Position.Direction.LONG) {
                closePosition(symbol);
            }
            
            // Otwórz krótką pozycję
            openShortPosition(symbol, price);
        }
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
        
        System.out.println("Generowanie sygnału BUY dla " + symbol + " po cenie " + price + ", ilość: " + quantity);
        
        // Tworzenie zlecenia kupna
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("ORD" + orderIdCounter.incrementAndGet());
        order.setSide(OrderRequest.BUY);
        
        // Ustawienie instrumentu
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        // Ustawienie ilości
        OrderRequest.OrderQuantity orderQuantity = new OrderRequest.OrderQuantity();
        orderQuantity.setQuantity(String.valueOf(quantity));
        order.setOrderQuantity(orderQuantity);
        
        // Ustawienie typu zlecenia (rynkowe)
        order.setOrderType(OrderRequest.MARKET);
        order.setTimeInForce(OrderRequest.DAY);
        
        // Wysłanie zlecenia
        sendOrder(order);
    }
    
    /**
     * Otwiera krótką pozycję.
     * 
     * @param symbol Symbol instrumentu
     * @param price Cena
     */
    private void openShortPosition(String symbol, double price) {
        // Sprawdź czy nie mamy już pozycji dla tego instrumentu
        if (positions.containsKey(symbol)) {
            return;
        }
        
        int quantity = calculateQuantity(price);
        
        System.out.println("Generowanie sygnału SELL dla " + symbol + " po cenie " + price + ", ilość: " + quantity);
        
        // Tworzenie zlecenia sprzedaży
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("ORD" + orderIdCounter.incrementAndGet());
        order.setSide(OrderRequest.SELL);
        
        // Ustawienie instrumentu
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        // Ustawienie ilości
        OrderRequest.OrderQuantity orderQuantity = new OrderRequest.OrderQuantity();
        orderQuantity.setQuantity(String.valueOf(quantity));
        order.setOrderQuantity(orderQuantity);
        
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
        order.setClientOrderId("CLOSE" + orderIdCounter.incrementAndGet());
        
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
        order.setOrderType(OrderRequest.MARKET);
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
        // Logowanie informacji o wykonaniu zlecenia
        System.out.println("Otrzymano raport wykonania: " + report.getExecutionType() + 
                          " dla zlecenia " + report.getClientOrderId() + 
                          ", status: " + report.getOrderStatus());
    }
    
    @Override
    public String getName() {
        return "Moving Average Crossover Strategy";
    }
    
    @Override
    public String getDescription() {
        return "Strategia bazująca na przecięciu krótkoterminowej i długoterminowej średniej kroczącej (SMA). " +
               "Kupuje gdy krótka MA przebija długą MA od dołu, sprzedaje w przeciwnym przypadku.";
    }
}