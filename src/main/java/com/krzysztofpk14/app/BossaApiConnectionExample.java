package com.krzysztofpk14.app;


import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.request.SecurityListRequest;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.bossaapi.model.response.SecurityList;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.server.BossaApiServer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BossaApiConnectionExample {
    private static BossaApiServer server;
    private static BossaApiClient client;
    private static final int PORT = 24445; // Inny port niż domyślny, by uniknąć konfliktów
    private static final String HOST = "127.0.0.1";
    private static int requestIdCounter = 1000;

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
            
            // Zarejestruj obsługę raportów wykonania
            client.registerExecutionReportHandler(report -> {
                System.out.println("[TEST] Otrzymano raport wykonania: " + report.getExecutionType() +
                        " dla zlecenia " + report.getClientOrderId());
            });

            // Logowanie synchroniczne
            UserResponse response = client.loginSync("BOS", "BOS");
            // System.out.println(response);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Błąd podczas uruchamiania testu: " + e.getMessage());
        }
        finally {
            // Zatrzymaj serwer po zakończeniu testu
            if (server != null) {
                server.stop();
                System.out.println("Serwer zatrzymany.");
            }
            if (client != null) {
                client.disconnect();
                System.out.println("Klient rozłączony.");
            }
        }
    }
}
