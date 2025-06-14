package com.krzysztofpk14.app.bossaapi.client;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.request.UserRequest;
import com.krzysztofpk14.app.bossaapi.model.response.BusinessMessageReject;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.util.FixmlGenerator;
import com.krzysztofpk14.app.bossaapi.util.FixmlParser;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
// import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Główna klasa klienta do komunikacji z bossaAPI.
 */
public class BossaApiClient {
    private final BossaApiConnection connection;
    private final Map<String, CompletableFuture<UserResponse>> loginResponses = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ExecutionReport>> orderResponses = new ConcurrentHashMap<>();
    
    private final Map<String, Consumer<MarketDataResponse>> marketDataHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<ExecutionReport>> executionReportHandlers = new HashMap<>();
    
    private boolean loggedIn = false;
    private String username;
    
    /**
     * Tworzy nowy obiekt klienta bossaAPI.
     */
    public BossaApiClient() {
        this.connection = new BossaApiConnection();
    }
    
    /**
     * Nawiązuje połączenie z serwerem bossaAPI.
     * 
     * @param host Adres hosta serwera
     * @param port Port serwera
     * @throws IOException Jeśli wystąpi błąd połączenia
     */
    public void connect(String host, int port) throws IOException {
        connection.connect(host, port);
        // connection.startReceiving(this::handleMessage);
    }
    
    /**
     * Zamyka połączenie z serwerem.
     */
    public void disconnect() {
        if (isLoggedIn()) {
            try {
                logout();
            } catch (Exception e) {
                // Ignorujemy błędy przy wylogowaniu
            }
        }
        
        connection.disconnect();
    }
    
    /**
     * Loguje użytkownika do systemu bossaAPI.
     * 
     * @param username Nazwa użytkownika
     * @param password Hasło
     * @return Future z odpowiedzią na żądanie logowania
     * @throws IOException Jeśli wystąpi błąd połączenia
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    public CompletableFuture<UserResponse> loginAsync(String username, String password) throws IOException, JAXBException {
        String requestId = generateRequestId();
        
        UserRequest request = new UserRequest(requestId, username, password);
        CompletableFuture<UserResponse> future = new CompletableFuture<>();
        loginResponses.put(requestId, future);
        
        this.username = username;
        
        sendMessage(request);
        return future;
    }


    public UserResponse loginSync(String username, String password) throws IOException, JAXBException, InterruptedException {
        String requestId = generateRequestId();
        UserRequest request = new UserRequest(requestId, username, password);
        String requestXml = FixmlGenerator.generateXml(request);
        String response = connection.sendAndReceive(requestXml, 5000); // Timeout 5 sekund
        System.out.println("Odpowiedź: " + response);

        if (response != null) {
            FixmlMessage fixmlMessage = FixmlParser.parse(response);
            if (fixmlMessage != null && fixmlMessage.getMessage() instanceof UserResponse) {
                UserResponse userResp = (UserResponse) fixmlMessage.getMessage();
                if (userResp.getUserStatus().equals(UserResponse.LOGGED_IN)) {
                    loggedIn = true;
                } else if (userResp.getUserStatus().equals(UserResponse.LOGGED_OUT)) {
                    loggedIn = false;
                }
                return userResp;
            }
        }

        return null;
    }
    
    /**
     * Wylogowuje użytkownika z systemu bossaAPI.
     * 
     * @return Future z odpowiedzią na żądanie wylogowania
     * @throws IOException Jeśli wystąpi błąd połączenia
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    public CompletableFuture<UserResponse> logout() throws IOException, JAXBException {
        if (!isLoggedIn()) {
            CompletableFuture<UserResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Użytkownik nie jest zalogowany"));
            return future;
        }
        
        String requestId = generateRequestId();
        
        UserRequest request = UserRequest.createLogoutRequest(requestId, username);
        CompletableFuture<UserResponse> future = new CompletableFuture<>();
        loginResponses.put(requestId, future);
        
        sendMessage(request);
        return future;
    }
    
    /**
     * Wysyła zlecenie do systemu bossaAPI.
     * 
     * @param order Obiekt zlecenia do wysłania
     * @return Future z raportem wykonania zlecenia
     * @throws IOException Jeśli wystąpi błąd połączenia
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    public CompletableFuture<ExecutionReport> sendOrder(OrderRequest order) throws IOException, JAXBException {
        if (!isLoggedIn()) {
            CompletableFuture<ExecutionReport> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Użytkownik nie jest zalogowany"));
            return future;
        }
        
        String clientOrderId = order.getClientOrderId();
        if (clientOrderId == null || clientOrderId.isEmpty()) {
            clientOrderId = generateRequestId();
            order.setClientOrderId(clientOrderId);
        }
        
        CompletableFuture<ExecutionReport> future = new CompletableFuture<>();
        orderResponses.put(clientOrderId, future);
        
        sendMessage(order);
        return future;
    }
    
    /**
     * Wysyła żądanie danych rynkowych.
     * 
     * @param request Obiekt żądania danych rynkowych
     * @param handler Obsługa otrzymywanych danych rynkowych
     * @throws IOException Jeśli wystąpi błąd połączenia
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    public MarketDataResponse requestMarketData(MarketDataRequest request) throws IOException, JAXBException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Uzytkownik nie jest zalogowany");
        }
         
        String requestId = request.getRequestId();
        if (requestId == null || requestId.isEmpty()) {
            requestId = generateRequestId();
            request.setRequestId(requestId);
        }

        // marketDataHandlers.put(requestId, handler);
        System.out.println("Wysyłanie żądania danych rynkowych: " + requestId);

        String requestXml = FixmlGenerator.generateXml(request);
        String response = connection.sendAndReceive(requestXml, 5000); // Timeout 5 sekund
        System.out.println("Odpowiedź: " + response);

        if (response != null) {
            FixmlMessage fixmlMessage = FixmlParser.parse(response);
            if (fixmlMessage != null && fixmlMessage.getMessage() instanceof MarketDataResponse) {
                MarketDataResponse marketDataResponse = (MarketDataResponse) fixmlMessage.getMessage();
                return marketDataResponse;
            } else {
                System.err.println("Otrzymano nieprawidłową odpowiedź: " + response);
            }
        } else {
            System.err.println("Brak odpowiedzi z serwera");
        }
        return null;
    }
    
    /**
     * Rejestruje obsługę raportów wykonania zleceń.
     * 
     * @param handler Funkcja obsługująca raporty wykonania
     */
    public void registerExecutionReportHandler(Consumer<ExecutionReport> handler) {
        executionReportHandlers.put("default", handler);
    }
    
    /**
     * Wysyła wiadomość do serwera.
     * 
     * @param message Obiekt wiadomości do wysłania
     * @throws IOException Jeśli wystąpi błąd połączenia
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    private void sendMessage(BaseMessage message) throws IOException, JAXBException {
        String xml = FixmlGenerator.generateXml(message);
        System.out.println("Wysyłanie wiadomości: " + xml);
        connection.sendMessage(xml);
    }
    
    /**
     * Obsługuje otrzymaną wiadomość od serwera.
     * 
     * @param xml Treść wiadomości XML
     */
    private void handleMessage(String xml) {
        System.out.println("Odebrano wiadomość:" + xml);

        try {
            FixmlMessage message = FixmlParser.parse(xml);
            BaseMessage baseMessage = message.getMessage();
            System.out.println("Typ wiadomości: " + baseMessage.getMessageType());
            
            if (baseMessage instanceof UserResponse) {
                handleUserResponse((UserResponse) baseMessage);
            } else if (baseMessage instanceof ExecutionReport) {
                handleExecutionReport((ExecutionReport) baseMessage);
            } else if (baseMessage instanceof MarketDataResponse) {
                handleMarketDataResponse((MarketDataResponse) baseMessage);
            } else if (baseMessage instanceof BusinessMessageReject) {
            handleBusinessMessageReject((BusinessMessageReject) baseMessage);
        }
        } catch (JAXBException e) {
            System.err.println("Błąd podczas parsowania wiadomości FIXML: " + e);
            e.printStackTrace();
        }
    }
    
    /**
     * Obsługuje odpowiedź na żądanie użytkownika.
     * 
     * @param response Odpowiedź na żądanie użytkownika
     */
    private void handleUserResponse(UserResponse response) {
        String requestId = response.getUserReqID();
        CompletableFuture<UserResponse> future = loginResponses.remove(requestId);
        
        if (future != null) {
            // Aktualizacja stanu zalogowania
            if (response.getUserStatus().equals(UserResponse.LOGGED_IN)) {
                loggedIn = true;
            } else if (response.getUserStatus().equals(UserResponse.LOGGED_OUT)) {
                loggedIn = false;
                username = null;
            }
            
            future.complete(response);
        }
    }
    
    /**
     * Obsługuje raport wykonania zlecenia.
     * 
     * @param report Raport wykonania zlecenia
     */
    private void handleExecutionReport(ExecutionReport report) {
        String clientOrderId = report.getClientOrderId();
        
        // Powiadamiamy o raporcie wykonania dla konkretnego zlecenia
        CompletableFuture<ExecutionReport> future = orderResponses.get(clientOrderId);
        if (future != null) {
            future.complete(report);
            
            // Usuwamy tylko dla zakończonych zleceń
            String execType = report.getExecutionType();
            if (execType.equals(ExecutionReport.FILLED) || 
                execType.equals(ExecutionReport.CANCELED) || 
                execType.equals(ExecutionReport.REJECTED)) {
                orderResponses.remove(clientOrderId);
            }
        }
        
        // Powiadamiamy ogólnych obserwatorów raportów wykonania
        executionReportHandlers.values().forEach(handler -> handler.accept(report));
    }
    
    /**
     * Obsługuje odpowiedź z danymi rynkowymi.
     * 
     * @param response Odpowiedź z danymi rynkowymi
     */
    private void handleMarketDataResponse(MarketDataResponse response) {
        String requestId = response.getRequestId();
        Consumer<MarketDataResponse> handler = marketDataHandlers.get(requestId);
        
        if (handler != null) {
            handler.accept(response);
        }
    }

    /**
     * Obsługuje odrzucone wiadomości biznesowe.
     * 
     * @param reject Odrzucona wiadomość biznesowa
     */
    private void handleBusinessMessageReject(BusinessMessageReject reject) {
        System.err.println("Otrzymano BusinessMessageReject:");
        System.err.println("RefMsgType: " + reject.getRefMsgType());
        System.err.println("BusinessRejectReason: " + reject.getBusinessRejectReason());
        System.err.println("Text: " + reject.getText());
    }
    
    /**
     * Sprawdza, czy użytkownik jest zalogowany.
     * 
     * @return true jeśli użytkownik jest zalogowany
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    /**
     * Sprawdza, czy połączenie jest aktywne.
     * 
     * @return true jeśli połączenie jest aktywne
     */
    public boolean isConnected() {
        return connection.isConnected();
    }
    
    /**
     * Generuje unikalny identyfikator żądania.
     * 
     * @return Unikalny identyfikator
     */
    private String generateRequestId() {
        return "5";
    }
}