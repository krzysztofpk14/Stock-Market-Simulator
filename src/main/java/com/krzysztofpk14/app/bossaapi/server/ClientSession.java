package com.krzysztofpk14.app.bossaapi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.request.SecurityListRequest;
import com.krzysztofpk14.app.bossaapi.model.request.UserRequest;
import com.krzysztofpk14.app.bossaapi.model.response.BusinessMessageReject;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.bossaapi.model.response.SecurityList;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.util.FixmlGenerator;
import com.krzysztofpk14.app.bossaapi.util.FixmlParser;

import jakarta.xml.bind.JAXBException;

/**
 * Klasa reprezentująca sesję klienta bossaAPI.
 */
public class ClientSession {
    private final String sessionId;
    private final Socket socket;
    private InputStream input;
    private OutputStream output;
    private String username;
    private boolean authenticated = false;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Thread receiveThread;
    private Thread sendThread;
    private ConcurrentLinkedQueue<String> sendQueue = new ConcurrentLinkedQueue<>();
    
    private final SessionManager sessionManager;
    private final OrderManager orderManager;
    private final MarketDataManager marketDataManager;
    private final SecurityManager securityManager;
    
    /**
     * Tworzy nową sesję klienta.
     * 
     * @param socket Socket połączenia z klientem
     * @param sessionManager Manager sesji
     * @param orderManager Manager zleceń
     * @param marketDataManager Manager danych rynkowych
     * @param securityManager Manager instrumentów
     */
    public ClientSession(Socket socket, SessionManager sessionManager, OrderManager orderManager, 
                       MarketDataManager marketDataManager, SecurityManager securityManager) {
        this.sessionId = UUID.randomUUID().toString();
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.orderManager = orderManager;
        this.marketDataManager = marketDataManager;
        this.securityManager = securityManager;
        
        // Rejestruje tę sesję jako odbiorcę zdarzeń rynkowych
        this.marketDataManager.registerMarketDataListener(this::handleMarketDataEvent);
        
        // Rejestruje tę sesję jako odbiorcę raportów wykonania
        this.orderManager.registerExecutionListener(this::handleExecutionReport);
    }
    
    /**
     * Rozpoczyna sesję klienta.
     */
    public void start() {
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            running.set(true);
            
            // Wątek odbierający wiadomości od klienta
            receiveThread = new Thread(this::receiveLoop);
            receiveThread.setName("BossaAPI-Session-" + sessionId + "-Receiver");
            receiveThread.start();
            
            // Wątek wysyłający wiadomości do klienta
            sendThread = new Thread(this::sendLoop);
            sendThread.setName("BossaAPI-Session-" + sessionId + "-Sender");
            sendThread.start();
            
            System.out.println("Rozpoczeto sesje: " + sessionId);
        } catch (IOException e) {
            System.err.println("Błąd podczas inicjalizacji sesji: " + e.getMessage());
            close();
        }
    }
    
    /**
     * Pętla odbierająca wiadomości od klienta.
     */
    private void receiveLoop() {
        byte[] lengthBuffer = new byte[4];
        
        try {
            while (running.get()) {
                // Odczytaj długość wiadomości (4 bajty)
                if (readFully(lengthBuffer) != 4) {
                    break;
                }
                
                int length = ((lengthBuffer[0] & 0xFF) << 24) | 
                             ((lengthBuffer[1] & 0xFF) << 16) | 
                             ((lengthBuffer[2] & 0xFF) << 8)  | 
                             (lengthBuffer[3] & 0xFF);
                
                // Sprawdź czy długość jest sensowna
                if (length <= 0 || length > 1_000_000) {
                    System.err.println("Otrzymano nieprawidłową długość wiadomości: " + length);
                    continue;
                }
                // Odczytaj treść wiadomości
                byte[] messageBuffer = new byte[length];
                if (readFully(messageBuffer) != length) {
                    break;
                }
                
                String message = new String(messageBuffer, StandardCharsets.UTF_8);
                // System.out.println("Sesja " + sessionId + " otrzymała: " + message);
                
                // Przetwórz wiadomość
                processMessage(message);
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Błąd podczas odbierania danych: " + e.getMessage());
            }
        }
        
        close();
    }
    
    /**
     * Czyta dokładnie określoną ilość bajtów z wejścia.
     * 
     * @param buffer Bufor do zapisu danych
     * @return Liczba odczytanych bajtów
     * @throws IOException Jeśli wystąpi błąd podczas odczytu
     */
    private int readFully(byte[] buffer) throws IOException {
        int totalRead = 0;
        int bytesRead;
        
        while (totalRead < buffer.length) {
            bytesRead = input.read(buffer, totalRead, buffer.length - totalRead);
            
            if (bytesRead == -1) {
                return totalRead;
            }
            
            totalRead += bytesRead;
        }
        
        return totalRead;
    }
    
    /**
     * Pętla wysyłająca wiadomości do klienta.
     */
    private void sendLoop() {
        try {
            while (running.get()) {
                String message = sendQueue.poll();
                
                if (message != null) {
                    sendMessageInternal(message);
                } else {
                    // Czekaj na nowe wiadomości
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Błąd podczas wysyłania danych: " + e.getMessage());
            }
        }
    }
    
    /**
     * Wysyła wiadomość do klienta.
     * 
     * @param message Wiadomość XML do wysłania
     */
    public void sendMessage(String message) {
        if (running.get()) {
            sendQueue.add(message);
        }
    }
    
    /**
     * Wysyła obiekt wiadomości do klienta.
     * 
     * @param message Obiekt wiadomości do wysłania
     * @throws JAXBException Jeśli wystąpi błąd podczas generowania XML
     */
    public void sendMessage(BaseMessage message) {
        try {
            String xml = FixmlGenerator.generateXml(message);
            sendMessage(xml);
        } catch (JAXBException e) {
            System.err.println("Błąd podczas generowania XML: " + e.getMessage());
        }
    }
    
    /**
     * Faktycznie wysyła wiadomość poprzez socket.
     * 
     * @param message Wiadomość XML do wysłania
     * @throws IOException Jeśli wystąpi błąd podczas wysyłania
     */
    private synchronized void sendMessageInternal(String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;
        
        // Wysyła długość wiadomości jako 4 bajty
        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = (byte) ((messageLength >> 24) & 0xFF);
        lengthBytes[1] = (byte) ((messageLength >> 16) & 0xFF);
        lengthBytes[2] = (byte) ((messageLength >> 8) & 0xFF);
        lengthBytes[3] = (byte) (messageLength & 0xFF);
        
        output.write(lengthBytes);
        output.write(messageBytes);
        output.flush();
        
        // System.out.println("Sesja " + sessionId + " wyslala: " + message);
    }
    
    /**
     * Przetwarza otrzymaną wiadomość.
     * 
     * @param messageXml Treść wiadomości XML
     */
    private void processMessage(String messageXml) {
        try {
            FixmlMessage fixmlMessage = FixmlParser.parse(messageXml);
            
            if (fixmlMessage == null || fixmlMessage.getMessage() == null) {
                System.err.println("Nieprawidłowa wiadomość FIXML");
                sendReject("Nieprawidłowa wiadomość FIXML");
                return;
            }
            
            BaseMessage baseMessage = fixmlMessage.getMessage();
            String messageType = baseMessage.getMessageType();
            
            // Sprawdź czy wiadomość wymaga autoryzacji
            if (!"UserReq".equals(messageType) && !authenticated) {
                sendReject("Nieautoryzowany dostęp", messageType);
                return;
            }
            
            // Przetwórz wiadomość w zależności od typu
            switch (messageType) {
                case "UserReq":
                    handleUserRequest((UserRequest) baseMessage);
                    break;
                case "Order":
                    handleOrderRequest((OrderRequest) baseMessage);
                    break;
                case "MktDataReq":
                    handleMarketDataRequest((MarketDataRequest) baseMessage);
                    break;
                case "SecListReq":
                    handleSecurityListRequest((SecurityListRequest) baseMessage);
                    break;
                default:
                    sendReject("Nieobsługiwany typ wiadomości: " + messageType, messageType);
                    break;
            }
        } catch (JAXBException e) {
            System.err.println("Błąd podczas parsowania wiadomości: " + e.getMessage());
            sendReject("Błąd parsowania wiadomości");
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas przetwarzania wiadomości: " + e.getMessage());
            e.printStackTrace();
            sendReject("Nieoczekiwany błąd");
        }
    }
    
    /**
     * Obsługuje żądanie użytkownika (login/logout).
     * 
     * @param request Żądanie użytkownika
     */
    private void handleUserRequest(UserRequest request) {
        String requestType = request.getUserRequestType();
        
        if ("1".equals(requestType)) { // Login
            boolean loginSuccessful = validateLogin(request.getUsername(), request.getPassword());
            
            UserResponse response = new UserResponse();
            response.setUserReqID(request.getUserReqID());
            
            if (loginSuccessful) {
                response.setUserStatus(UserResponse.LOGGED_IN);
                response.setUserStatusText("Zalogowano pomyślnie");
                response.setUsername(request.getUsername());
                response.setMktDepth(UserResponse.FIVE_OFFERS);
                this.username = request.getUsername();
                this.authenticated = true;
            } else {
                response.setUserStatus(UserResponse.WRONG_PASSWORD);
                response.setUserStatusText("Nieprawidłowe dane logowania");
            }
            
            sendMessage(response);
        } else if ("2".equals(requestType)) { // Logout
            UserResponse response = new UserResponse();
            response.setUserReqID(request.getUserReqID());
            response.setUserStatus(UserResponse.LOGGED_OUT);
            response.setUsername(this.username);
            response.setUserStatusText("Wylogowano pomyślnie");
            
            // Wyczyść stan sesji
            this.authenticated = false;
            this.username = null;
            
            sendMessage(response);
        } else if ("4".equals(requestType)) { // Check status
            UserResponse response = new UserResponse();
            response.setUserReqID(request.getUserReqID());
            response.setUsername(this.username);
            response.setUserStatus(authenticated ? UserResponse.LOGGED_IN : UserResponse.INVESTOR_OFFLINE);
            response.setUserStatusText(authenticated ? "Zalogowany" : "Niezalogowany");
            response.setMktDepth(UserResponse.FIVE_OFFERS);

            sendMessage(response);
        } else {
            sendReject("Nieobsługiwany typ żądania użytkownika: " + requestType, "UserReq");
        }
    }
    
    /**
     * Waliduje dane logowania.
     * 
     * @param username Nazwa użytkownika
     * @param password Hasło
     * @return true jeśli dane są poprawne
     */
    private boolean validateLogin(String username, String password) {
        // W dokumentacji aplikacji jest stały login i hasło
        return "BOS".equals(username) && "BOS".equals(password);
    }
    
    /**
     * Obsługuje żądanie złożenia zlecenia.
     * 
     * @param order Obiekt zlecenia
     */
    private void handleOrderRequest(OrderRequest order) {
        // Przekaż zlecenie do manager'a zleceń
        ExecutionReport report = orderManager.processOrder(order, username);
        
        // Wyślij raport wykonania
        sendMessage(report);
    }
    
    /**
     * Obsługuje żądanie danych rynkowych.
     * 
     * @param request Obiekt żądania danych rynkowych
     */
    private void handleMarketDataRequest(MarketDataRequest request) {
        String requestType = request.getSubscriptionRequestType();
        
        if (MarketDataRequest.SNAPSHOT.equals(requestType)) {
            // Jednorazowy snapshot
            MarketDataResponse response = marketDataManager.getMarketDataSnapshot(request);
            sendMessage(response);
        } else if (MarketDataRequest.SUBSCRIBE.equals(requestType)) {
            // Subskrypcja - rejestruje żądanie i odsyła potwierdzenie
            marketDataManager.subscribeMarketData(request, this);
            
            // Wyślij potwierdzenie subskrypcji
            MarketDataResponse confirmation = marketDataManager.createSubscriptionConfirmation(request);
            sendMessage(confirmation);
        } else if (MarketDataRequest.UNSUBSCRIBE.equals(requestType)) {
            // Anulowanie subskrypcji
            marketDataManager.unsubscribeMarketData(request.getRequestId());
            
            // Wyślij potwierdzenie anulowania
            MarketDataResponse confirmation = marketDataManager.createUnsubscribeConfirmation(request);
            sendMessage(confirmation);
        } else {
            sendReject("Nieobsługiwany typ żądania danych rynkowych: " + requestType, "MktDataReq");
        }
    }
    
    /**
     * Obsługuje żądanie listy bezpieczeństw.
     * 
     * @param request Obiekt żądania listy bezpieczeństw
     */
    private void handleSecurityListRequest(SecurityListRequest request) {
        // Pobierz listę instrumentów z managera
        SecurityList securityList = securityManager.getSecurities(request);
        sendMessage(securityList);
    }
    
    /**
     * Obsługuje zdarzenie rynkowe (zmiana notowań).
     * 
     * @param marketData Obiekt z danymi rynkowymi
     */
    private void handleMarketDataEvent(MarketDataResponse marketData) {
        // Jeśli ten klient subskrybuje te dane, wyślij mu aktualizację
        if (authenticated && marketDataManager.hasSubscription(marketData.getRequestId(), this)) {
            sendMessage(marketData);
        }
    }
    
    /**
     * Obsługuje raport wykonania dotyczący zlecenia klienta.
     * 
     * @param report Raport wykonania
     */
    private void handleExecutionReport(ExecutionReport report) {
        // Wyślij raport tylko jeśli dotyczy tego klienta
        if (authenticated) {
            sendMessage(report);
        }
    }
    
    /**
     * Wysyła odrzucenie wiadomości do klienta.
     * 
     * @param reason Powód odrzucenia
     */
    private void sendReject(String reason) {
        sendReject(reason, null);
    }
    
    /**
     * Wysyła odrzucenie wiadomości do klienta.
     * 
     * @param reason Powód odrzucenia
     * @param refMsgType Typ odrzuconej wiadomości
     */
    private void sendReject(String reason, String refMsgType) {
        BusinessMessageReject reject = new BusinessMessageReject();
        reject.setText(reason);
        reject.setRefMsgType(refMsgType);
        reject.setBusinessRejectReason(BusinessMessageReject.OTHER);
        
        sendMessage(reject);
    }
    
    /**
     * Zamyka sesję klienta.
     */
    public void close() {
        if (running.compareAndSet(true, false)) {
            System.out.println("Zamykanie sesji: " + sessionId);
            
            // Wyrejestruj odbiorców zdarzeń
            marketDataManager.unregisterMarketDataListener(this::handleMarketDataEvent);
            marketDataManager.unsubscribeAllMarketData(this);
            orderManager.unregisterExecutionListener(this::handleExecutionReport);
            
            // Usuń sesję z managera
            sessionManager.removeSession(this);
            
            // Zamknij socket
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Błąd podczas zamykania socketu: " + e.getMessage());
            }
            
            // Przerwij wątki
            if (receiveThread != null) {
                receiveThread.interrupt();
            }
            if (sendThread != null) {
                sendThread.interrupt();
            }
        }
    }
    
    /**
     * Zwraca identyfikator sesji.
     * 
     * @return Identyfikator sesji
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Zwraca nazwę użytkownika.
     * 
     * @return Nazwa użytkownika
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sprawdza, czy sesja jest uwierzytelniona.
     * 
     * @return true jeśli sesja jest uwierzytelniona
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
}