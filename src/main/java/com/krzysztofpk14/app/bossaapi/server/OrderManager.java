package com.krzysztofpk14.app.bossaapi.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;

/**
 * Manager obsługujący zlecenia.
 */
public class OrderManager {
    private final Map<String, OrderRequest> orders = new ConcurrentHashMap<>();
    private final AtomicInteger orderCounter = new AtomicInteger(1000);
    private final List<Consumer<ExecutionReport>> executionListeners = new ArrayList<>();
    
    /**
     * Przetwarza nowe zlecenie.
     * 
     * @param order Zlecenie do przetworzenia
     * @param username Nazwa użytkownika składającego zlecenie
     * @return Raport wykonania
     */
    public ExecutionReport processOrder(OrderRequest order, String username) {
        // Generuj unikalny ID zlecenia w systemie
        String orderId = generateOrderId();
        
        // Zapisz zlecenie
        orders.put(orderId, order);
        
        // Utwórz raport wykonania
        ExecutionReport report = createExecutionReport(order, orderId, username);
        
        // W rzeczywistym systemie tutaj byłaby logika realizacji zlecenia
        // Dla uproszczenia, przyjmujemy zlecenie i generujemy raport NEW
        
        // Notyfikuj obserwatorów
        notifyExecutionListeners(report);
        
        return report;
    }
    
    /**
     * Generuje unikalny identyfikator zlecenia.
     * 
     * @return Unikalny identyfikator
     */
    private String generateOrderId() {
        return "ORD" + orderCounter.incrementAndGet();
    }
    
    /**
     * Tworzy raport wykonania dla zlecenia.
     * 
     * @param order Zlecenie
     * @param orderId ID zlecenia w systemie
     * @param username Nazwa użytkownika
     * @return Raport wykonania
     */
    private ExecutionReport createExecutionReport(OrderRequest order, String orderId, String username) {
        ExecutionReport report = new ExecutionReport();
        
        // Wypełnij podstawowe pola
        report.setReportId(UUID.randomUUID().toString().substring(0, 8));
        report.setOrderId(orderId);
        report.setClientOrderId(order.getClientOrderId());
        report.setExecutionType(ExecutionReport.NEW);
        report.setOrderStatus(ExecutionReport.NEW);
        report.setSide(order.getSide());
        report.setOrderType(order.getOrderType());
        report.setPrice(order.getPrice());
        // report.setUsername(username);
        
        // Dodaj instrument
        ExecutionReport.Instrument instrument = new ExecutionReport.Instrument();
        instrument.setSymbol(order.getInstrument().getSymbol());
        instrument.setId(order.getInstrument().getId());
        instrument.setIdSource(order.getInstrument().getIdSource());
        report.setInstrument(instrument);
        
        // Dodaj ilość
        if (order.getOrderQuantity() != null) {
            ExecutionReport.OrderQuantity quantity = new ExecutionReport.OrderQuantity();
            quantity.setQuantity(order.getOrderQuantity().getQuantity());
            report.setOrderQuantity(quantity);
        }
        
        // Dodaj czas transakcji
        report.setTransactionTime(getCurrentTimeFormatted());
        
        return report;
    }
    
    /**
     * Zwraca aktualny czas w formacie FIXML.
     * 
     * @return Sformatowany czas
     */
    private String getCurrentTimeFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
    }
    
    /**
     * Rejestruje obserwatora raportów wykonania.
     * 
     * @param listener Funkcja odbierająca raporty wykonania
     */
    public synchronized void registerExecutionListener(Consumer<ExecutionReport> listener) {
        executionListeners.add(listener);
    }
    
    /**
     * Wyrejestrowuje obserwatora raportów wykonania.
     * 
     * @param listener Funkcja odbierająca raporty wykonania
     */
    public synchronized void unregisterExecutionListener(Consumer<ExecutionReport> listener) {
        executionListeners.remove(listener);
    }
    
    /**
     * Powiadamia wszystkich obserwatorów o nowym raporcie wykonania.
     * 
     * @param report Raport wykonania
     */
    private void notifyExecutionListeners(ExecutionReport report) {
        for (Consumer<ExecutionReport> listener : executionListeners) {
            try {
                listener.accept(report);
            } catch (Exception e) {
                System.err.println("Błąd podczas notyfikacji obserwatora: " + e.getMessage());
            }
        }
    }
    
    /**
     * Symuluje wykonanie zlecenia (fill).
     * 
     * @param clientOrderId ID zlecenia klienta
     * @param price Cena wykonania
     * @param quantity Ilość wykonana
     * @param username Nazwa użytkownika
     * @return Raport wykonania
     */
    public ExecutionReport simulateOrderExecution(String clientOrderId, String price, String quantity, String username) {
        // Znajdź zlecenie
        OrderRequest order = null;
        String orderId = null;
        
        for (Map.Entry<String, OrderRequest> entry : orders.entrySet()) {
            if (entry.getValue().getClientOrderId().equals(clientOrderId)) {
                order = entry.getValue();
                orderId = entry.getKey();
                break;
            }
        }
        
        if (order == null) {
            return null;
        }
        
        // Utwórz raport wykonania
        ExecutionReport report = createExecutionReport(order, orderId, username);
        report.setExecutionType(ExecutionReport.NEW);
        report.setOrderStatus(ExecutionReport.NEW_ORDER);
        report.setPrice(price);
        
        // Dodaj ilość wykonania
        ExecutionReport.OrderQuantity orderQty = new ExecutionReport.OrderQuantity();
        orderQty.setQuantity(quantity);
        report.setOrderQuantity(orderQty);
        
        // Notyfikuj obserwatorów
        notifyExecutionListeners(report);
        
        return report;
    }
}