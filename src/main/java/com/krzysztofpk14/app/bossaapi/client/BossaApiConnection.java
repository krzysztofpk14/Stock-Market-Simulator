package com.krzysztofpk14.app.bossaapi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Klasa do obsługi połączenia z serwerem bossaAPI.
 */
public class BossaApiConnection {
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private boolean connected = false;
    private ExecutorService executorService;
    private Thread receiveThread;
    private boolean receiveRunning = false;
    private Consumer<String> messageHandler;


    
    /**
     * Nawiązuje połączenie z serwerem bossaAPI.
     * 
     * @param host Adres hosta serwera
     * @param port Port serwera
     * @throws IOException Jeśli wystąpi błąd połączenia
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setKeepAlive(true);
        input = socket.getInputStream();
        output = socket.getOutputStream();
        connected = true;
        executorService = Executors.newSingleThreadExecutor();
        socket.setSoTimeout(30000); // Timeout na 30 sekund
    }
    
    /**
     * Zamyka połączenie z serwerem.
     */
    public void disconnect() {
        stopReceiving();
        
        connected = false;
        
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignorujemy błędy przy zamykaniu
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Wysyła komunikat do serwera i czeka na odpowiedź synchronicznie.
     * 
     * @param xmlMessage Komunikat XML do wysłania
     * @param timeout Maksymalny czas oczekiwania na odpowiedź (w milisekundach)
     * @return Odebrana odpowiedź lub null jeśli timeout
     * @throws IOException Jeśli wystąpi błąd podczas komunikacji
     */
    public String sendAndReceive(String xmlMessage) throws IOException, SocketException {
        sendMessage(xmlMessage);
        String response = receiveMessage();
        // System.out.println("Wiadomosc przez socket synchroniczny");
    
        return response;
    }

    /**
     * Wysyła komunikat XML do serwera.
     * 
     * @param xmlMessage Komunikat XML do wysłania
     * @throws IOException Jeśli wystąpi błąd podczas wysyłania
     */
    public void sendMessage(String xmlMessage) throws IOException {
        if (!connected) {
            throw new IOException("Nie nawiązano połączenia z serwerem");
        }
            
        // Wyślij wiadomość
        byte[] messageBytes = xmlMessage.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;

        // Wyślij długość jako 4 bajty (big-endian)
        output.write((messageLength >> 24) & 0xFF);
        output.write((messageLength >> 16) & 0xFF);
        output.write((messageLength >> 8) & 0xFF);
        output.write(messageLength & 0xFF);

        // System.out.println("Wysłano wiadomość o długości: " + messageLength);
        output.write(messageBytes);
        output.flush();


    }

    /**
     * Przetwarza odpowiedź serwera.
     * 
     * @param xmlMessage Komunikat XML do wysłania
     * @return Odebrana odpowiedź lub null jeśli wystąpił błąd
     */
    public String receiveMessage() {
        try{
            // Najpierw odczytaj 4 bajty długości
            byte[] lengthBuffer = new byte[4];
            int bytesRead = 0;
            
            // Czekaj na odczyt całego nagłówka długości (4 bajty)
            while (bytesRead < 4) {
                int read = input.read(lengthBuffer, bytesRead, 4 - bytesRead);
                if (read == -1) {
                    throw new IOException("Połączenie zamknięte podczas odczytu długości wiadomości");
                }
                bytesRead += read;
            }
            
            // Oblicz długość wiadomości z 4 bajtów
            int responseLength = ((lengthBuffer[0] & 0xFF) << 24) |
                                ((lengthBuffer[1] & 0xFF) << 16) |
                                ((lengthBuffer[2] & 0xFF) << 8) |
                                (lengthBuffer[3] & 0xFF);
            
            // System.out.println("Odczytano długość odpowiedzi: " + responseLength + " bajtów");
            
            // Sprawdź czy długość ma sens
            if (responseLength <= 0 || responseLength > 10_000_000) { // 10MB jako rozsądny limit
                throw new IOException("Nieprawidłowa długość odpowiedzi: " + responseLength);
            }
            byte[] responseBuffer = new byte[responseLength];
            bytesRead = 0;
            
            // Czekaj na odczyt całej wiadomości
            while (bytesRead < responseLength) {
                int read = input.read(responseBuffer, bytesRead, responseLength - bytesRead);
                if (read == -1) {
                    throw new IOException("Połączenie zamknięte podczas odczytu treści wiadomości");
                }
                bytesRead += read;
            }
            
            // Konwertuj odpowiedź na string
            String response = new String(responseBuffer, StandardCharsets.UTF_8);
            // System.out.println("Odebrano odpowiedź o długości: " + bytesRead + " bajtów");
            
            return response;
        } catch (IOException e) {
            System.err.println("Błąd podczas odbierania wiadomości: " + e.getMessage());
            return null; // lub można rzucić 
        }    
    }
    
    /**
     * Rozpoczyna asynchroniczny odbiór komunikatów z serwera.
     * 
     * @param handler Funkcja przetwarzająca odebrane komunikaty
     */
    public void startReceivingAsync(Consumer<String> handler) {
        System.out.println("Rozpoczeto odbieranie wiadomosci...");
        if (!connected || receiveRunning) {
            return;
        }
        
        messageHandler = handler;
        receiveRunning = true;
        
        receiveThread = new Thread(() -> {
            try {
                while (receiveRunning) {
                    // Użyj istniejącej metody receiveMessage() do odczytania wiadomości
                    String message = receiveMessage();
                    // System.out.println("Wiadomość przez socket asynchroniczny");
                    
                    // Jeśli odczytano kompletną wiadomość, przekaż ją do handlera
                    if (message != null && messageHandler != null) {
                        // Użyj puli wątków do przetworzenia wiadomości
                        final String finalMessage = message;
                        executorService.submit(() -> messageHandler.accept(finalMessage));
                    } else if (message == null) {
                        // Jeśli receiveMessage zwraca null, to wystąpił błąd odczytu
                        // Wstrzymaj chwilę pętlę przed ponowną próbą
                        Thread.sleep(1000);
                        
                        // Sprawdź czy połączenie nadal istnieje
                        if (!isConnected()) {
                            System.out.println("Połączenie zostało zamknięte - przerywam odbiór");
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                // Wątek został przerwany, zakończ działanie
                Thread.currentThread().interrupt();
                System.out.println("Wątek odbierania został przerwany");
            } catch (Exception e) {
                // Obsługa innych wyjątków, które mogą się pojawić
                System.err.println("Błąd w wątku odbierania: " + e.getMessage());
            } finally {
                // Jeśli pętla została przerwana z powodu błędu, a nie celowego zatrzymania:
                if (receiveRunning) {
                    receiveRunning = false;
                    System.out.println("Odbiór wiadomości został przerwany nieoczekiwanie");
                    // Tutaj można dodać kod do powiadamiania klienta o utracie połączenia
                }
                System.out.println("Zakończono wątek odbierania");
            }
        });

        receiveThread.setName("BossaAPI-Receiver");
        receiveThread.start();
    }
    
    /**
     * Zatrzymuje odbiór komunikatów z serwera.
     */
    public void stopReceiving() {
        receiveRunning = false;
        
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join(1000); // Czekamy maks. 1 sekundę
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    
    
    /**
     * Sprawdza, czy połączenie jest aktywne.
     * 
     * @return true jeśli połączenie jest aktywne
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

}