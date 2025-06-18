package com.krzysztofpk14.app.bossaapi.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

/**
 * Manager obsługujący dane rynkowe.
 */
public class MarketDataManager {
    // Mapa subskrypcji: ID żądania -> lista subskrybujących sesji
    private final Map<String, List<ClientSession>> subscriptions = new ConcurrentHashMap<>();
    
    // Mapa symulowanych cen: symbol -> cena
    private final Map<String, Double> instrumentPrices = new HashMap<>();
    
    // Słuchacze zdarzeń rynkowych
    private final List<Consumer<MarketDataResponse>> marketDataListeners = new ArrayList<>();
    
    // Timer do symulacji rynku
    private Timer marketSimulationTimer;
    
    /**
     * Tworzy nowy manager danych rynkowych.
     */
    public MarketDataManager() {
        // Inicjalizacja cen startowych dla popularnych spółek
        instrumentPrices.put("KGHM", 150.0);
        instrumentPrices.put("PKO", 47.2);
        instrumentPrices.put("PKN", 78.5);
        instrumentPrices.put("PZU", 33.8);
        instrumentPrices.put("CDR", 331.5);
        instrumentPrices.put("LPP", 9120.0);
        instrumentPrices.put("PGE", 8.35);
        instrumentPrices.put("SPL", 216.8);
        instrumentPrices.put("DNP", 420.0);
        instrumentPrices.put("CPS", 28.7);
    }
    
    /**
     * Rozpoczyna symulację ruchu cen na rynku.
     */
    public void startMarketSimulation() {
        marketSimulationTimer = new Timer("MarketSimulation");
        marketSimulationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                generateMarketDataUpdates();
            }
        }, 1000, 1000); // Aktualizacja co 1 sekundę
    }
    
    /**
     * Zatrzymuje symulację rynku.
     */
    public void stopMarketSimulation() {
        if (marketSimulationTimer != null) {
            marketSimulationTimer.cancel();
            marketSimulationTimer = null;
        }
    }
    
    /**
     * Generuje losowe zmiany cen i powiadamia subskrybentów.
     */
    private void generateMarketDataUpdates() {
        Random random = new Random();
        
        for (Map.Entry<String, Double> entry : instrumentPrices.entrySet()) {
            // Losowa zmiana procentowa -1% do +1%
            double changePercent = (random.nextDouble() - 0.5) * 0.02;
            double newPrice = entry.getValue() * (1 + changePercent);
            
            // Zaokrąglenie do 2 miejsc po przecinku
            newPrice = Math.round(newPrice * 100.0) / 100.0;
            instrumentPrices.put(entry.getKey(), newPrice);
            
            // Sprawdź czy ktoś subskrybuje ten instrument
            for (Map.Entry<String, List<ClientSession>> sub : subscriptions.entrySet()) {
                if (!sub.getValue().isEmpty()) {
                    // Dla uproszczenia zakładamy, że każda subskrypcja dotyczy wszystkich instrumentów
                    // W rzeczywistej implementacji należałoby sprawdzić, których instrumentów dotyczy subskrypcja
                    
                    // Utwórz odpowiedź z danymi rynkowymi
                    MarketDataResponse response = createMarketDataResponse(entry.getKey(), 
                                                                        String.valueOf(newPrice),
                                                                        sub.getKey());
                    
                    // Powiadom obserwatorów
                    notifyMarketDataListeners(response);
                }
            }
        }
    }
    
    /**
     * Rejestruje odbiorcy zdarzeń rynkowych.
     * 
     * @param listener Funkcja przetwarzająca zdarzenia rynkowe
     */
    public synchronized void registerMarketDataListener(Consumer<MarketDataResponse> listener) {
        marketDataListeners.add(listener);
    }
    
    /**
     * Wyrejestrowuje odbiorcę zdarzeń rynkowych.
     * 
     * @param listener Funkcja przetwarzająca zdarzenia rynkowe
     */
    public synchronized void unregisterMarketDataListener(Consumer<MarketDataResponse> listener) {
        marketDataListeners.remove(listener);
    }
    
    /**
     * Powiadamia wszystkich obserwatorów o zdarzeniu rynkowym.
     * 
     * @param marketData Dane rynkowe
     */
    private void notifyMarketDataListeners(MarketDataResponse marketData) {
        for (Consumer<MarketDataResponse> listener : new ArrayList<>(marketDataListeners)) {
            try {
                listener.accept(marketData);
            } catch (Exception e) {
                System.err.println("Błąd podczas notyfikacji obserwatora: " + e.getMessage());
            }
        }
    }
    
    /**
     * Sprawdza, czy klient ma subskrypcję na dane zdarzenie rynkowe.
     * 
     * @param requestId ID żądania
     * @param session Sesja klienta
     * @return true jeśli klient ma subskrypcję
     */
    public boolean hasSubscription(String requestId, ClientSession session) {
        List<ClientSession> subscribers = subscriptions.get(requestId);
        return subscribers != null && subscribers.contains(session);
    }
    
    /**
     * Zwraca aktualne dane rynkowe dla instrumentu.
     * 
     * @param request Żądanie danych rynkowych
     * @return Odpowiedź z danymi rynkowymi
     */
    public MarketDataResponse getMarketDataSnapshot(MarketDataRequest request) {
        String symbol = null;
        
        // Pobierz symbol instrumentu z żądania
        if (request.getInstruments() != null && !request.getInstruments().isEmpty()) {
            for (MarketDataRequest.InstrumentMarketDataRequest instr : request.getInstruments()) {
                if (instr.getInstrument() != null) {
                    symbol = instr.getInstrument().getSymbol();
                    break;
                }
            }
        }
        
        if (symbol == null) {
            // Domyślnie KGHM
            symbol = "KGHM";
        }
        
        // Pobierz cenę instrumentu
        String price = "0.00";
        if (instrumentPrices.containsKey(symbol)) {
            price = String.valueOf(instrumentPrices.get(symbol));
        }
        
        return createMarketDataResponse(symbol, price, request.getRequestId());
    }
    
    /**
     * Tworzy odpowiedź z danymi rynkowymi.
     * 
     * @param symbol Symbol instrumentu
     * @param price Cena
     * @param requestId ID żądania
     * @return Odpowiedź z danymi rynkowymi
     */
    private MarketDataResponse createMarketDataResponse(String symbol, String price, String requestId) {
        MarketDataResponse response = new MarketDataResponse();
        response.setRequestId(requestId);
        
        // Utwórz instrument
        MarketDataResponse.Instrument instrument = new MarketDataResponse.Instrument();
        instrument.setSymbol(symbol);
        response.setInstrument(instrument);
        
        // Utwórz grupę danych rynkowych
        MarketDataResponse.MarketDataGroup group = new MarketDataResponse.MarketDataGroup();
        group.setMarketDataEntryType(MarketDataResponse.TRADE); // Transakcja
        group.setPrice(price);
        group.setSize("100"); // Przykładowy wolumen
        group.setTime(getCurrentTimeFormatted());
        
        List<MarketDataResponse.MarketDataGroup> groups = new ArrayList<>();
        groups.add(group);
        response.setMarketDataGroups(groups);
        
        return response;
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
     * Subskrybuje dane rynkowe.
     * 
     * @param request Żądanie subskrypcji
     * @param session Sesja klienta
     */
    public void subscribeMarketData(MarketDataRequest request, ClientSession session) {
        String requestId = request.getRequestId();
        
        subscriptions.computeIfAbsent(requestId, k -> new ArrayList<>()).add(session);
        
        // System.out.println("Dodano subskrypcję danych rynkowych dla requestId: " + requestId);
    }
    
    /**
     * Anuluje subskrypcję danych rynkowych.
     * 
     * @param requestId ID żądania
     */
    public void unsubscribeMarketData(String requestId) {
        subscriptions.remove(requestId);
        // System.out.println("Usunięto subskrypcję danych rynkowych dla requestId: " + requestId);
    }
    
    /**
     * Anuluje wszystkie subskrypcje dla sesji.
     * 
     * @param session Sesja klienta
     */
    public void unsubscribeAllMarketData(ClientSession session) {
        for (List<ClientSession> sessions : subscriptions.values()) {
            sessions.remove(session);
        }
        
        // Usuń puste listy
        subscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Tworzy potwierdzenie subskrypcji.
     * 
     * @param request Żądanie subskrypcji
     * @return Potwierdzenie subskrypcji
     */
    public MarketDataResponse createSubscriptionConfirmation(MarketDataRequest request) {
        MarketDataResponse response = new MarketDataResponse();
        response.setRequestId(request.getRequestId());
        response.setResponseType(MarketDataResponse.FULL_REFRESH);
        return response;
    }
    
    /**
     * Tworzy potwierdzenie anulowania subskrypcji.
     * 
     * @param request Żądanie anulowania subskrypcji
     * @return Potwierdzenie anulowania
     */
    public MarketDataResponse createUnsubscribeConfirmation(MarketDataRequest request) {
        MarketDataResponse response = new MarketDataResponse();
        response.setRequestId(request.getRequestId());
        response.setResponseType(MarketDataResponse.UNSOLICITED_INDICATOR);
        return response;
    }
}