// package com.krzysztofpk14.app.bossaapi;

// import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
// import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
// import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
// import com.krzysztofpk14.app.bossaapi.model.request.SecurityListRequest;
// import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
// import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
// import com.krzysztofpk14.app.bossaapi.model.response.SecurityList;
// import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
// import com.krzysztofpk14.app.bossaapi.server.BossaApiServer;

// import jakarta.xml.bind.JAXBException;

// import org.junit.jupiter.api.*;

// import java.io.IOException;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.atomic.AtomicBoolean;
// import java.util.concurrent.atomic.AtomicReference;

// import static org.junit.jupiter.api.Assertions.*;

// /**
//  * Test integracyjny sprawdzający interakcję klienta BossaAPI z serwerem.
//  * Test uruchamia serwer, loguje klienta i przeprowadza operacje rynkowe.
//  */
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// public class BossaApiIntegrationTest {

//     private static BossaApiServer server;
//     private static BossaApiClient client;
//     private static final int PORT = 24445; // Inny port niż domyślny, by uniknąć konfliktów
//     private static final String HOST = "127.0.0.1";
//     private static int requestIdCounter = 1000;

//     /**
//      * Konfiguracja przed wszystkimi testami - uruchomienie serwera i klienta
//      */
//     @BeforeAll
//     public static void setup() throws IOException {
//         // Uruchom serwer w osobnym wątku
//         server = new BossaApiServer(PORT);
//         Thread serverThread = new Thread(() -> {
//             try {
//                 server.start();
//             } catch (IOException e) {
//                 e.printStackTrace();
//                 fail("Nie udało się uruchomić serwera: " + e.getMessage());
//             }
//         });
//         serverThread.setDaemon(true); // Wątek zostanie zamknięty po zakończeniu JVM
//         serverThread.start();
        
//         // Poczekaj chwilę na uruchomienie serwera
//         try {
//             Thread.sleep(1000);
//         } catch (InterruptedException e) {
//             Thread.currentThread().interrupt();
//         }
        
//         // Utwórz klienta
//         client = new BossaApiClient();
        
//         // Połącz z serwerem
//         client.connect(HOST, PORT);
//         try {
//             // Poczekaj na połączenie
//             UserResponse response = client.loginSync("BOS", "BOS");
//         } catch (InterruptedException e) {
//             fail("Błąd podczas logowania: " + e.getMessage());
//         } catch(JAXBException e) {
//             fail("Błąd podczas przetwarzania XML: " + e.getMessage());
//         }
        
        
//         // Zarejestruj obsługę raportów wykonania
//         client.registerExecutionReportHandler(report -> {
//             System.out.println("[TEST] Otrzymano raport wykonania: " + report.getExecutionType() +
//                     " dla zlecenia " + report.getClientOrderId());
//         });
//     }
    
//     /**
//      * Sprzątanie po wszystkich testach - zamknięcie klienta i serwera
//      */
//     @AfterAll
//     public static void tearDown() {
//         // Rozłącz klienta jeśli jest połączony
//         if (client != null && client.isConnected()) {
//             client.disconnect();
//         }
        
//         // Zatrzymaj serwer
//         if (server != null) {
//             server.stop();
//         }
//     }
    
//     /**
//      * Test logowania do systemu
//      */
//     @Test
//     @Order(1)
//     public void testLogin() throws Exception {
//         // Logowanie synchroniczne
//         UserResponse response = client.loginSync("BOS", "BOS");
        
//         assertNotNull(response, "Odpowiedź logowania nie może być null");
//         assertTrue(response.isLoginSuccessful(), "Logowanie powinno się udać");
//         assertEquals(UserResponse.LOGGED_IN, response.getUserStatus(), "Status powinien być LOGGED_IN");
//     }
    
//     /**
//      * Test pobierania danych rynkowych
//      */
//     @Test
//     @Order(2)
//     public void testMarketDataSnapshot() throws Exception {
//         // Utwórz żądanie danych rynkowych
//         MarketDataRequest request = new MarketDataRequest();
//         request.setRequestId(generateRequestId());
//         request.setSubscriptionRequestType(MarketDataRequest.SNAPSHOT);
//         request.addInstrument("KGHM");
        
//         // Wyślij żądanie
//         MarketDataResponse response = client.requestMarketData(request);
        
//         // Sprawdź odpowiedź
//         assertNotNull(response, "Odpowiedź danych rynkowych nie może być null");
//         assertEquals(request.getRequestId(), response.getRequestId(), "ID żądania powinno być zgodne");
//         assertNotNull(response.getInstrument(), "Instrument nie może być null");
//         assertEquals("KGHM", response.getInstrument().getSymbol(), "Symbol powinien być KGHM");
//         assertFalse(response.getMarketDataGroups().isEmpty(), "Powinny być dostępne dane rynkowe");
//     }
    
//     /**
//      * Test subskrypcji danych rynkowych
//      */
//     @Test
//     @Order(3)
//     public void testMarketDataSubscription() throws Exception {
//         // Utwórz licznik do synchronizacji
//         CountDownLatch latch = new CountDownLatch(3); // Oczekujemy przynajmniej 3 aktualizacji
//         AtomicBoolean gotUpdate = new AtomicBoolean(false);
//         AtomicReference<String> lastPrice = new AtomicReference<>("");
        
//         // Zarejestruj handler do obsługi aktualizacji danych rynkowych
//         // client.registerMarketDataHandler(marketData -> {
//         //     System.out.println("[TEST] Otrzymano aktualizację danych rynkowych: " + 
//         //            marketData.getInstrument().getSymbol() + " - " + 
//         //            marketData.getMarketDataGroups().get(0).getPrice());
//         //     gotUpdate.set(true);
//         //     lastPrice.set(marketData.getMarketDataGroups().get(0).getPrice());
//         //     latch.countDown();
//         // });
        
//         // Utwórz żądanie subskrypcji
//         MarketDataRequest request = new MarketDataRequest();
//         request.setRequestId(generateRequestId());
//         request.setSubscriptionRequestType(MarketDataRequest.SUBSCRIBE);
//         request.addInstrument("KGHM");
        
//         // Wyślij żądanie subskrypcji
//         // CompletableFuture<MarketDataResponse> future = client.requestMarketData(request);
//         // MarketDataResponse response = future.get(5, TimeUnit.SECONDS);

//         MarketDataResponse response = client.requestMarketData(request);
        
//         // Sprawdź potwierdzenie subskrypcji
//         assertNotNull(response, "Potwierdzenie subskrypcji nie może być null");
//         assertEquals(request.getRequestId(), response.getRequestId(), "ID żądania powinno być zgodne");
        
//         // Czekaj na aktualizacje (lub timeout)
//         boolean receivedUpdates = latch.await(10, TimeUnit.SECONDS);
        
//         // Anuluj subskrypcję
//         request.setSubscriptionRequestType(MarketDataRequest.UNSUBSCRIBE);
//         // client.unsubscribeMarketData(request);
        
//         // Sprawdź wyniki
//         assertTrue(receivedUpdates, "Powinniśmy otrzymać aktualizacje danych rynkowych");
//         assertTrue(gotUpdate.get(), "Powinniśmy otrzymać przynajmniej jedną aktualizację");
//         assertFalse(lastPrice.get().isEmpty(), "Cena nie powinna być pusta");
//     }
    
//     /**
//      * Test pobierania listy instrumentów
//      */
//     @Test
//     @Order(4)
//     public void testSecurityListRequest() throws Exception {
//         // Utwórz żądanie listy instrumentów
//         SecurityListRequest request = new SecurityListRequest();
//         request.setRequestId(generateRequestId());
//         request.setSecurityRequestType(SecurityListRequest.ALL_INSTRMNT);
        
//         // Wyślij żądanie
//         SecurityList response = client.requestSecurityList(request);
        
//         // Sprawdź odpowiedź
//         assertNotNull(response, "Lista instrumentów nie może być null");
//         assertEquals(request.getRequestId(), response.getRequestId(), "ID żądania powinno być zgodne");
//         assertFalse(response.getSecurities().isEmpty(), "Lista instrumentów nie powinna być pusta");
        
//         // Wypisz listę instrumentów
//         System.out.println("[TEST] Otrzymano listę instrumentów:");
//         response.getSecurities().forEach(security -> 
//             System.out.println("  - " + security.getInstrument().getSymbol() + 
//                               " (" + security.getInstrument().getDescription() + ")"));
//     }
    
//     /**
//      * Test składania zlecenia
//      */
//     @Test
//     @Order(5)
//     public void testOrderExecution() throws Exception {
//         // Utwórz obiekt zlecenia
//         OrderRequest order = createSampleOrder();
        
//         // Licznik do synchronizacji
//         CountDownLatch orderLatch = new CountDownLatch(1);
//         AtomicReference<ExecutionReport> reportRef = new AtomicReference<>();
        
//         // Zarejestruj handler do obsługi raportów wykonania dla konkretnego zlecenia
//         client.registerExecutionReportHandler(report -> {
//             if (order.getClientOrderId().equals(report.getClientOrderId())) {
//                 System.out.println("[TEST] Otrzymano raport dla zlecenia: " + report.getClientOrderId() + 
//                                   ", status: " + report.getOrderStatus() + 
//                                   ", typ: " + report.getExecutionType());
//                 reportRef.set(report);
//                 orderLatch.countDown();
//             }
//         });
        
//         // Wyślij zlecenie
//         CompletableFuture<ExecutionReport> future = client.sendOrder(order);
//         ExecutionReport initialReport = future.get(5, TimeUnit.SECONDS);
        
//         // Sprawdź pierwszy raport
//         assertNotNull(initialReport, "Raport wykonania nie może być null");
//         assertEquals(order.getClientOrderId(), initialReport.getClientOrderId(), "ID zlecenia powinno być zgodne");
//         assertEquals(ExecutionReport.NEW, initialReport.getExecutionType(), "Typ wykonania powinien być NEW");
        
//         // Czekaj na kolejny raport (lub timeout)
//         boolean receivedReport = orderLatch.await(10, TimeUnit.SECONDS);
        
//         // Testuj wyniki
//         assertTrue(receivedReport || reportRef.get() != null, 
//                   "Powinniśmy otrzymać dodatkowy raport wykonania lub initial report jest wystarczający");
//     }
    
//     /**
//      * Test wylogowania
//      */
//     @Test
//     @Order(6)
//     public void testLogout() throws Exception {
//         // Wylogowanie asynchroniczne
//         CompletableFuture<UserResponse> future = client.logout();
//         UserResponse response = future.get(5, TimeUnit.SECONDS);
        
//         // Sprawdź odpowiedź
//         assertNotNull(response, "Odpowiedź wylogowania nie może być null");
//         assertEquals(UserResponse.LOGGED_OUT, response.getUserStatus(), "Status powinien być LOGGED_OUT");
//     }
    
//     /**
//      * Tworzy przykładowe zlecenie kupna akcji
//      */
//     private OrderRequest createSampleOrder() {
//         OrderRequest order = new OrderRequest();
        
//         // ID zlecenia
//         order.setClientOrderId(generateRequestId());
        
//         // Parametry zlecenia
//         order.setSide(OrderRequest.BUY);  // Kupno
//         order.setOrderType(OrderRequest.LIMIT);  // Limit
//         order.setTimeInForce(OrderRequest.DAY);  // Dzienne
//         order.setPrice("150.00");  // Cena 150 PLN
        
//         // Instrument
//         OrderRequest.Instrument instrument = new OrderRequest.Instrument();
//         instrument.setSymbol("KGHM");
//         instrument.setId("PLKGHM000017");
//         instrument.setIdSource("4");  // 4 = ISIN number
//         order.setInstrument(instrument);
        
//         // Ilość
//         OrderRequest.OrderQuantity quantity = new OrderRequest.OrderQuantity();
//         quantity.setQuantity("10");  // 10 sztuk
//         order.setOrderQuantity(quantity);
        
//         return order;
//     }
    
//     /**
//      * Generuje unikalny ID żądania
//      */
//     private static synchronized String generateRequestId() {
//         return "TEST" + requestIdCounter++;
//     }
// }