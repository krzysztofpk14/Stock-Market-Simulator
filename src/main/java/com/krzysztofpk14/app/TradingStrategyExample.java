package com.krzysztofpk14.app;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import com.krzysztofpk14.app.bossaapi.server.BossaApiServer;
import com.krzysztofpk14.app.strategy.MovingAverageCrossoverStrategy;
import com.krzysztofpk14.app.strategy.RSIStrategy;
import com.krzysztofpk14.app.strategy.StrategyManager;
import com.krzysztofpk14.app.strategy.StrategyParameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.LogManager;

/**
 * Przykładowa aplikacja używająca strategii inwestycyjnych.
 */
public class TradingStrategyExample {

    public static void main(String[] args) {
        // Konfiguracja logowania
        try {
            LogManager.getLogManager().readConfiguration(
                TradingStrategyExample.class.getResourceAsStream("/logging.properties"));
        } catch (Exception e) {
            System.err.println("Nie można skonfigurować logowania: " + e.getMessage());
        }
        
        // Parametry połączenia
        String host = "localhost";
        int port = 24444;
        String username = "BOS";
        String password = "BOS";

        // Otworzenie serwera BossaAPI
        System.out.println("Uruchamianie serwera BossaAPI na porcie " + port);
        BossaApiServer server = new BossaApiServer(port);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                System.err.println("Bład podczas uruchamiania serwera: " + e.getMessage());
            }
        });
        
        serverThread.setDaemon(true); // Wątek będzie zamknięty po zakończeniu głównego wątku
        serverThread.start();

        
        // Utwórz klienta API
        BossaApiClient client = new BossaApiClient();
        
        try {
            // Połącz z serwerem
            System.out.println("Laczenie z serwerem " + host + ":" + port + "...");
            client.connect(host, port);
            
            // Logowanie
            System.out.println("Logowanie jako " + username + "...");
            CompletableFuture<UserResponse> loginFuture = client.loginAsync(username, password);

             // Czekamy na wynik logowania z możliwością obsługi wyjątków
            UserResponse loginResponse = loginFuture.get();  // Blokuje wątek do czasu otrzymania odpowiedzi
            
            if (!loginResponse.isLoginSuccessful()) {
                System.err.println("Bład logowania: " + loginResponse.getUserStatusText());
                client.disconnect();
                server.stop();
                return;
            }
            
            System.out.println("Zalogowano pomyslnie.");
            
            // Utwórz manager strategii
            StrategyManager strategyManager = new StrategyManager(client);
            
            // Dodaj strategie z parametrami
            // Strategia MA Crossover
            StrategyParameters maParams = new StrategyParameters();
            maParams.setInstruments(Arrays.asList("KGHM"));
            maParams.setParam("shortSMAPeriod", 10);
            maParams.setParam("longSMAPeriod", 30);
            maParams.setParam("tradeSize", 10.0);
            strategyManager.addStrategy(new MovingAverageCrossoverStrategy(client), maParams);
            
            // Strategia RSI
            StrategyParameters rsiParams = new StrategyParameters();
            rsiParams.setInstruments(Arrays.asList("PKO"));
            rsiParams.setParam("rsiPeriod", 14);
            rsiParams.setParam("overboughtThreshold", 70.0);
            rsiParams.setParam("oversoldThreshold", 30.0);
            rsiParams.setParam("tradeSize", 20.0);
            strategyManager.addStrategy(new RSIStrategy(client), rsiParams);
            
            // Uruchom wszystkie strategie
            strategyManager.startAllStrategies();
            
            // Pętla główna
            System.out.println("\nStrategie uruchomione. Wpisz 'exit' aby zakończyć.");
            System.out.println("Wpisz 'help' aby zobaczyć dostępne komendy.");
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                String command = scanner.nextLine().trim().toLowerCase();
                
                if ("exit".equals(command)) {
                    break;
                } else if ("list".equals(command)) {
                    System.out.println("\nAktywne strategie:");
                    strategyManager.getStrategies().forEach(s -> 
                        System.out.println("- " + s.getName()));
                } else if ("statistics".equals(command)) {
                    strategyManager.displayStatistics();
                } else if ("help".equals(command)) {
                    System.out.println("\nDostępne komendy:");
                    System.out.println("list - wyświetl aktywne strategie");
                    System.out.println("statistics - wyświetl statystyki wszystkich strategii");
                    System.out.println("exit - zakończ program");
                    System.out.println("help - wyświetl pomoc");
                }
            }
            
            // Zatrzymaj strategie
            System.out.println("\nZatrzymywanie strategii...");
            strategyManager.stopAllStrategies();
            
            // Wyloguj i rozłącz
            System.out.println("Wylogowywanie...");
            client.logout().get();
            client.disconnect();
            scanner.close();
            
            System.out.println("Program zakończony.");
            
        } catch (Exception e) {
            System.err.println("Błąd: " + e.getMessage());
            e.printStackTrace();
            client.disconnect();
        } finally {
            // Zatrzymaj serwer
            if (server != null) {
                server.stop();
                System.out.println("Serwer BossaAPI zatrzymany.");
            }
        }
    }
}