package com.krzysztofpk14.app;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest.Instrument;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest.OrderQuantity;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
// import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutionException;

public class BossaApiExample {
    
    public static void main(String[] args) {
        BossaApiClient client = new BossaApiClient();
        
        try {
            // Połączenie z serwerem
            System.out.println("Łączenie z serwerem bossaAPI...");
            client.connect("127.0.0.1", 24444); // Przykładowy adres i port
            
            // Logowanie
            System.out.println("Logowanie do systemu...");
            CompletableFuture<UserResponse> loginFuture = client.login("BOS", "BOS");
            UserResponse loginResponse = loginFuture.get(); // Czekamy na odpowiedź
            
            if (loginResponse.isLoginSuccessful()) {
                System.out.println("Zalogowano pomyślnie!");
                
                // Rejestracja obsługi raportów wykonania
                client.registerExecutionReportHandler(report -> {
                    System.out.println("Otrzymano raport wykonania: " + report.getExecutionType() +
                            " dla zlecenia " + report.getClientOrderId());
                });
                
                // Żądanie danych rynkowych
                System.out.println("Żądanie danych rynkowych dla KGHM...");
                MarketDataRequest marketDataRequest = new MarketDataRequest();
                marketDataRequest.setRequestId(generateId());
                marketDataRequest.setSubscriptionRequestType(MarketDataRequest.SNAPSHOT);
                marketDataRequest.addInstrument("KGHM");
                
                client.requestMarketData(marketDataRequest, response -> {
                    System.out.println("Otrzymano dane rynkowe dla: " + response.getInstrument().getSymbol());
                    response.getMarketDataGroups().forEach(group -> {
                        System.out.println("  Typ: " + group.getMarketDataEntryType() +
                                ", Cena: " + group.getPrice() +
                                ", Ilość: " + group.getSize());
                    });
                });
                
                // Wysłanie zlecenia kupna
                System.out.println("Wysyłanie zlecenia kupna...");
                OrderRequest orderRequest = createSampleOrder();
                CompletableFuture<ExecutionReport> orderFuture = client.sendOrder(orderRequest);
                ExecutionReport report = orderFuture.get(); // Czekamy na odpowiedź
                
                System.out.println("Otrzymano potwierdzenie: " + report.getExecutionType());
                
                // Czekamy na dane i raporty
                Thread.sleep(5000);
                
                // Wylogowanie
                System.out.println("Wylogowywanie...");
                CompletableFuture<UserResponse> logoutFuture = client.logout();
                UserResponse logoutResponse = logoutFuture.get();
                System.out.println("Wylogowano: " + logoutResponse.getUserStatus());
            } else {
                System.out.println("Nie udało się zalogować: " + loginResponse.getUserStatusText());
            }
              } catch (Exception e) {
            System.err.println("ERROR: " + e);
        } finally {
            // Zawsze zamykamy połączenie na końcu
            if (client.isConnected()) {
                client.disconnect();
            }
        }
    }
    
    /**
     * Tworzy przykładowe zlecenie kupna akcji KGHM
     * @return Obiekt zlecenia gotowy do wysłania
     */
    private static OrderRequest createSampleOrder() {
        OrderRequest order = new OrderRequest();
        
        // Ustawienie podstawowych parametrów zlecenia
        order.setClientOrderId(generateId());
        order.setSide(OrderRequest.BUY);  // Kupno
        order.setOrderType(OrderRequest.LIMIT);  // Limit
        order.setTimeInForce(OrderRequest.DAY);  // Dzienne
        order.setPrice("150.00");  // Cena 150 PLN
        
        // Ustawienie instrumentu
        Instrument instrument = new Instrument();
        instrument.setSymbol("KGHM");  // Symbol ISIN instrumentu
        instrument.setId("PLKGHM000017");  // ISIN
        instrument.setIdSource("4");  // 4 = ISIN number
        order.setInstrument(instrument);
        
        // Ustawienie ilości
        OrderQuantity quantity = new OrderQuantity();
        quantity.setQuantity("10");  // 10 sztuk
        order.setOrderQuantity(quantity);
        
        return order;
    }
    
    /**
     * Generuje unikalny identyfikator dla żądań
     * @return Unikalny identyfikator
     */
    private static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

