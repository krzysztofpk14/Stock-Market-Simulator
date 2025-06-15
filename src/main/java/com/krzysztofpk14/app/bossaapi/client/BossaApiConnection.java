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
    public String sendAndReceive(String xmlMessage, int timeout) throws IOException, SocketException {
        if (!connected) {
            throw new IOException("Nie nawiązano połączenia z serwerem");
        }

        
        // Ustaw timeout na sockecie
        int originalTimeout = socket.getSoTimeout();
        try {
            socket.setSoTimeout(timeout);
            
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
  
        } finally {
            // Przywróć oryginalny timeout
            socket.setSoTimeout(originalTimeout);
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
            StringBuilder buffer = new StringBuilder();
            byte[] readBuffer = new byte[4096];
            int bytesRead;

            try {
                while (receiveRunning) {
                    bytesRead = input.read(readBuffer);

                    if (bytesRead == -1) {
                        // Koniec strumienia, serwer zamknął połączenie
                        System.out.println("Serwer zamknął połączenie.");
                        break;
                    }

                    String data = new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8);
                    buffer.append(data);

                    // Sprawdź czy mamy kompletną wiadomość
                    // W tym przykładzie zakładamy, że wiadomość ma format:
                    // - pierwsze 4 bajty to długość wiadomości w XML
                    // - następnie sama wiadomość XML
                    if (buffer.length() >= 2) { // Minimalna długość sensownej wiadomości
                        String message = buffer.toString();

                        // Przetwórz wiadomość i przekaż do handlera
                        System.out.println("Przekazywanie wiadomości do handlera, długość: " + message.length());

                        if (messageHandler != null) {
                            final String finalMessage = message;
                            // Użyj puli wątków do przetworzenia wiadomości
                            executorService.submit(() -> messageHandler.accept(finalMessage));
                        }

                        // Wyczyść bufor, aby przygotować go na następną wiadomość
                        buffer.setLength(0);
                    }
                }
            } catch (IOException e) {
                if (receiveRunning) {
                    System.err.println("Błąd podczas odbierania danych: " + e.getMessage());

                    // Próba ponownego nawiązania połączenia mogłaby być tutaj
                    // Jednak wymaga to informowania klienta, więc pozostawiamy przerwanie

                    // Czekaj chwilę przed kolejną próbą
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    // Jeśli połączenie jest zamknięte, przerywamy pętlę
                    if (!isConnected()) {
                        System.out.println("Połączenie zostało zamknięte - przerywam odbiór");
                    }
                }
            } finally {
                // NIE ustawiamy tutaj receiveRunning=false, aby umożliwić ponowne uruchomienie
                System.out.println("Zakończono wątek odbierania");

                // Zamiast tego, jeśli pętla została przerwana z powodu błędu, a nie celowego zatrzymania:
                if (receiveRunning) {
                    receiveRunning = false;
                    System.out.println("Odbiór wiadomości został przerwany nieoczekiwanie");

                    // Tutaj można dodać kod do powiadamiania klienta o utracie połączenia
                    // np. wywołanie callbacku onConnectionLost
                }
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