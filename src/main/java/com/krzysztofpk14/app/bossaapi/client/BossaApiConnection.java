package com.krzysztofpk14.app.bossaapi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        input = socket.getInputStream();
        output = socket.getOutputStream();
        connected = true;
        executorService = Executors.newSingleThreadExecutor();
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
     * Wysyła komunikat XML do serwera.
     * 
     * @param xmlMessage Komunikat XML do wysłania
     * @throws IOException Jeśli wystąpi błąd podczas wysyłania
     */
    public void sendMessage(String xmlMessage) throws IOException {
        if (!connected) {
            throw new IOException("Nie nawiązano połączenia z serwerem");
        }
        
        // Dodajemy separator końca wiadomości
        String message = xmlMessage + "\n";
        output.write(message.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }
    
    /**
     * Rozpoczyna asynchroniczny odbiór komunikatów z serwera.
     * 
     * @param handler Funkcja przetwarzająca odebrane komunikaty
     */
    public void startReceiving(Consumer<String> handler) {
        if (!connected || receiveRunning) {
            return;
        }
        
        messageHandler = handler;
        receiveRunning = true;
        
        receiveThread = new Thread(() -> {
            StringBuilder buffer = new StringBuilder();
            byte[] readBuffer = new byte[4096];
            int bytesRead;
            
            try {
                while (receiveRunning) {
                    bytesRead = input.read(readBuffer);
                    
                    if (bytesRead == -1) {
                        // Koniec strumienia, serwer zamknął połączenie
                        break;
                    }
                    
                    String data = new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8);
                    buffer.append(data);
                    
                    // Szukamy zakończeń wiadomości
                    int endIndex;
                    while ((endIndex = buffer.indexOf("\n")) != -1) {
                        String message = buffer.substring(0, endIndex);
                        buffer.delete(0, endIndex + 1);
                        
                        // Przetwarzamy wiadomość w puli wątków
                        if (messageHandler != null) {
                            final String finalMessage = message;
                            executorService.submit(() -> messageHandler.accept(finalMessage));
                        }
                    }
                }
            } catch (IOException e) {
                if (receiveRunning) {
                    // Błąd tylko jeśli nie zamykamy celowo
                    System.err.println("Błąd podczas odbierania danych: " + e.getMessage());
                }
            } finally {
                receiveRunning = false;
            }
        });
        
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