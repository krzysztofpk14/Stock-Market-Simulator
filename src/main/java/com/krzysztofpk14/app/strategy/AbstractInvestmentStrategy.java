package com.krzysztofpk14.app.strategy;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstrakcyjna klasa bazowa dla wszystkich strategii inwestycyjnych.
 * Implementuje wspólną funkcjonalność.
 */
public abstract class AbstractInvestmentStrategy implements InvestmentStrategy {
    
    // Klient API
    protected BossaApiClient apiClient;
    
    // Parametry strategii
    protected StrategyParameters parameters;
    
    // Status strategii
    protected volatile StrategyStatus status = StrategyStatus.INITIALIZED;
    
    // Statystyki
    protected StrategyStatistics statistics = new StrategyStatistics();
    
    // Lista identyfikatorów zleceń wysłanych przez strategię
    protected final List<String> orderIds = new ArrayList<>();
    
    // Mapa przechowująca ostatnie dane rynkowe dla każdego instrumentu
    protected final Map<String, MarketDataResponse> lastMarketData = new ConcurrentHashMap<>();
    
    // Mapa przechowująca pozycje otwarte przez strategię
    protected final Map<String, Position> positions = new ConcurrentHashMap<>();
    
    // Licznik wygenerowanych ID zleceń
    protected final AtomicLong orderIdCounter = new AtomicLong(1);
    
    /**
     * Konstruktor.
     * 
     * @param apiClient Klient API do komunikacji z serwerem
     */
    public AbstractInvestmentStrategy(BossaApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    @Override
    public void initialize(StrategyParameters parameters) {
        this.parameters = parameters;
        this.status = StrategyStatus.INITIALIZED;
        
        // Wyczyszczenie stanu strategii
        this.orderIds.clear();
        this.lastMarketData.clear();
        this.positions.clear();
        this.statistics = new StrategyStatistics();
        
        // Wywołanie inicjalizacji specyficznej dla konkretnej strategii
        doInitialize(parameters);
    }
    
    /**
     * Metoda inicjalizująca specyficzna dla konkretnej strategii.
     * 
     * @param parameters Parametry strategii
     */
    protected abstract void doInitialize(StrategyParameters parameters);
    
    @Override
    public void start() {
        if (status == StrategyStatus.INITIALIZED || status == StrategyStatus.STOPPED) {
            status = StrategyStatus.RUNNING;
            
            // Subskrypcja danych rynkowych
            subscribeToMarketData();
            
            // Wywołanie metody startującej specyficznej dla konkretnej strategii
            doStart();
            
            statistics.setStartTime(LocalDateTime.now());
        }
    }
    
    /**
     * Metoda startująca specyficzna dla konkretnej strategii.
     */
    protected abstract void doStart();
    
    @Override
    public void stop() {
        if (status == StrategyStatus.RUNNING) {
            status = StrategyStatus.STOPPING;
            
            // Anulowanie subskrypcji danych rynkowych
            unsubscribeFromMarketData();
            
            // Zamknięcie otwartych pozycji (opcjonalnie)
            if (parameters.isClosePositionsOnStop()) {
                closeAllPositions();
            }
            
            // Wywołanie metody zatrzymującej specyficznej dla konkretnej strategii
            doStop();
            
            status = StrategyStatus.STOPPED;
            statistics.setEndTime(LocalDateTime.now());
        }
    }
    
    /**
     * Metoda zatrzymująca specyficzna dla konkretnej strategii.
     */
    protected abstract void doStop();
    
    /**
     * Subskrybuje dane rynkowe dla instrumentów skonfigurowanych w parametrach.
     */
    protected void subscribeToMarketData() {
        if (parameters.getInstruments() != null && !parameters.getInstruments().isEmpty()) {
            for (String symbol : parameters.getInstruments()) {
                try {
                    // Tworzenie żądania subskrypcji
                    MarketDataRequest request = new MarketDataRequest();
                    request.setRequestId("MDR" + orderIdCounter.incrementAndGet());
                    request.setSubscriptionRequestType(MarketDataRequest.SUBSCRIBE);
                    request.addInstrument(symbol);
                    
                    // Wysłanie żądania
                    apiClient.subscribeMarketData(request);
                    
                } catch (Exception e) {
                    System.err.println("Błąd podczas subskrypcji danych rynkowych dla " + symbol + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Anuluje subskrypcję danych rynkowych.
     */
    protected void unsubscribeFromMarketData() {
        // Implementacja anulowania subskrypcji
        // ...
    }
    
    /**
     * Zamyka wszystkie otwarte pozycje.
     */
    protected void closeAllPositions() {
        for (Map.Entry<String, Position> entry : new HashMap<>(positions).entrySet()) {
            Position position = entry.getValue();
            
            try {
                // Tworzenie zlecenia zamykającego pozycję
                OrderRequest closeOrder = new OrderRequest();
                closeOrder.setClientOrderId("CLOSE" + orderIdCounter.incrementAndGet());
                
                // Ustawienie przeciwnego kierunku do pozycji
                if (position.getDirection() == Position.Direction.LONG) {
                    closeOrder.setSide(OrderRequest.SELL);
                } else {
                    closeOrder.setSide(OrderRequest.BUY);
                }
                
                // Ustawienie instrumentu
                OrderRequest.Instrument instrument = new OrderRequest.Instrument();
                instrument.setSymbol(position.getSymbol());
                closeOrder.setInstrument(instrument);
                
                // Ustawienie ilości
                OrderRequest.OrderQuantity quantity = new OrderRequest.OrderQuantity();
                quantity.setQuantity(position.getQuantity());
                closeOrder.setOrderQuantity(quantity);
                
                // Ustawienie typu zlecenia (rynkowe)
                closeOrder.setOrderType(OrderRequest.MARKET);
                closeOrder.setTimeInForce(OrderRequest.DAY);
                
                // Wysłanie zlecenia
                apiClient.sendOrder(closeOrder);
                
                // Zapisanie ID zlecenia
                orderIds.add(closeOrder.getClientOrderId());
                
            } catch (Exception e) {
                System.err.println("Błąd podczas zamykania pozycji " + position.getSymbol() + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public void onMarketData(MarketDataResponse marketData) {
        // Aktualizacja ostatnich danych rynkowych
        if (marketData.getInstrument() != null) {
            String symbol = marketData.getInstrument().getSymbol();
            lastMarketData.put(symbol, marketData);
            
            // Wywołanie metody przetwarzającej dane rynkowe specyficznej dla konkretnej strategii
            if (status == StrategyStatus.RUNNING) {
                processMarketData(marketData);
            }
        }
    }
    
    /**
     * Metoda przetwarzająca dane rynkowe specyficzna dla konkretnej strategii.
     * 
     * @param marketData Dane rynkowe
     */
    protected abstract void processMarketData(MarketDataResponse marketData);
    
    @Override
    public void onExecutionReport(ExecutionReport report) {
        // Sprawdzenie czy raport dotyczy zlecenia wysłanego przez tę strategię
        if (orderIds.contains(report.getClientOrderId())) {
            // Aktualizacja statystyk
            updateStatisticsFromReport(report);
            
            // Aktualizacja pozycji
            updatePositionFromReport(report);
            
            // Wywołanie metody przetwarzającej raport wykonania specyficznej dla konkretnej strategii
            if (status == StrategyStatus.RUNNING) {
                processExecutionReport(report);
            }
        }
    }
    
    /**
     * Metoda przetwarzająca raport wykonania specyficzna dla konkretnej strategii.
     * 
     * @param report Raport wykonania
     */
    protected abstract void processExecutionReport(ExecutionReport report);
    
    /**
     * Aktualizuje statystyki na podstawie raportu wykonania.
     * 
     * @param report Raport wykonania
     */
    protected void updateStatisticsFromReport(ExecutionReport report) {
        if (ExecutionReport.DONE.equals(report.getOrderStatus())) {
            
            // Pobranie ceny i ilości
            double price = Double.parseDouble(report.getPrice());
            double quantity = Double.parseDouble(report.getOrderQuantity().getQuantity());
            
            // Aktualizacja statystyk w zależności od kierunku
            if (OrderRequest.BUY.equals(report.getSide())) {
                statistics.incrementTotalBuys();
                statistics.addTotalBuyVolume(quantity);
                statistics.addTotalBuyValue(price * quantity);
            } else if (OrderRequest.SELL.equals(report.getSide())) {
                statistics.incrementTotalSells();
                statistics.addTotalSellVolume(quantity);
                statistics.addTotalSellValue(price * quantity);
            }
        }
    }
    
    /**
     * Aktualizuje pozycje na podstawie raportu wykonania.
     * 
     * @param report Raport wykonania
     */
    protected void updatePositionFromReport(ExecutionReport report) {
        if (ExecutionReport.DONE.equals(report.getOrderStatus())) {
            
            String symbol = report.getInstrument().getSymbol();
            double price = Double.parseDouble(report.getPrice());
            double quantity = Double.parseDouble(report.getOrderQuantity().getQuantity());
            
            Position position = positions.getOrDefault(symbol, new Position(symbol));
            
            if (OrderRequest.BUY.equals(report.getSide())) {
                position.addBuy(quantity, price);
            } else if (OrderRequest.SELL.equals(report.getSide())) {
                position.addSell(quantity, price);
            }
            
            if (position.getQuantity().equals("0")) {
                // Pozycja została zamknięta
                positions.remove(symbol);
                
                // Aktualizacja statystyk P&L
                double pnl = position.getRealizedPnL();
                if (pnl > 0) {
                    statistics.incrementWinningTrades();
                } else if (pnl < 0) {
                    statistics.incrementLosingTrades();
                }
                statistics.addTotalPnL(pnl);
                
            } else {
                // Aktualizacja lub dodanie pozycji
                positions.put(symbol, position);
            }
        }
    }
    
    /**
     * Wysyła zlecenie.
     * 
     * @param order Zlecenie do wysłania
     * @return ID zlecenia lub null w przypadku błędu
     */
    protected String sendOrder(OrderRequest order) {
        try {
            // Generowanie ID zlecenia jeśli nie zostało ustawione
            if (order.getClientOrderId() == null || order.getClientOrderId().isEmpty()) {
                order.setClientOrderId("ORD" + orderIdCounter.incrementAndGet());
            }
            
            // Wysłanie zlecenia
            apiClient.sendOrder(order);
            System.out.println("Zlecenie wysłane: " + order.getClientOrderId() + " - " + order.getInstrument().getSymbol() + " - " + order.getSide() + " " + order.getOrderQuantity().getQuantity() + " @ " + order.getPrice());
            
            // Zapisanie ID zlecenia
            orderIds.add(order.getClientOrderId());
            
            return order.getClientOrderId();
        } catch (Exception e) {
            System.err.println("Błąd podczas wysyłania zlecenia: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public StrategyStatus getStatus() {
        return status;
    }
    
    @Override
    public StrategyStatistics getStatistics() {
        return statistics;
    }

    @Override
    public void displayStatistics() {
        System.out.println(statistics.displayStatistics());
    }

}