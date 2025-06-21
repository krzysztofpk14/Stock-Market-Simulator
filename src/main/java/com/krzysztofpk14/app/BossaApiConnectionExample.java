package com.krzysztofpk14.app;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.server.BossaApiServer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class BossaApiConnectionExample {
    private static BossaApiServer server;
    private static BossaApiClient client;
    private static final int PORT = 24445; // Inny port niż domyślny, by uniknąć konfliktów
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args){
        try{
            server = new BossaApiServer(PORT);
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Nie udało się uruchomić serwera: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true); // Wątek zostanie zamknięty po zakończeniu JVM
            serverThread.start();
            
            // Poczekaj chwilę na uruchomienie serwera
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Utwórz klienta
            client = new BossaApiClient();
            
            // Połącz z serwerem
            client.connect(HOST, PORT);

            // Logowanie do systemu
            CompletableFuture<UserResponse> response = client.loginAsync("BOS", "BOS");
            UserResponse loginResponse = response.get(); // Czekamy na odpowiedź
            if (loginResponse.isLoginSuccessful()) {
                System.out.println("Zalogowano pomyslnie!");
                
                // Wylogowanie
                client.logout();
            } else {
                System.out.println("Logowanie nie powiodło się.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Błąd podczas uruchamiania testu: " + e.getMessage());
        }
        finally {
            if (client != null) {
                client.disconnect();
                System.out.println("Klient rozlaczony.");
            }
            // Zatrzymaj serwer po zakończeniu testu
            if (server != null) {
                server.stop();
                System.out.println("Serwer zatrzymany.");
            }
        }
    }
}
