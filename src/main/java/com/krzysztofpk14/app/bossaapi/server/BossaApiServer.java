package com.krzysztofpk14.app.bossaapi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serwer bossaAPI obsługujący połączenia z klientami.
 */
public class BossaApiServer {
    private int port;
    private ServerSocket serverSocket;
    private boolean running = false;
    private ExecutorService executorService;
    private SessionManager sessionManager;
    private OrderManager orderManager;
    private MarketDataManager marketDataManager;
    private SecurityManager securityManager;

    /**
     * Tworzy nowy serwer bossaAPI.
     * 
     * @param port Port, na którym serwer będzie nasłuchiwał
     */
    public BossaApiServer(int port) {
        this.port = port;
        this.sessionManager = new SessionManager();
        this.orderManager = new OrderManager();
        this.marketDataManager = new MarketDataManager();
        this.securityManager = new SecurityManager();
        this.orderManager.setMarketDataManager(this.marketDataManager);
    }

    /**
     * Uruchamia serwer.
     * 
     * @throws IOException Jeśli wystąpi błąd podczas uruchamiania serwera
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        executorService = Executors.newCachedThreadPool();
        
        System.out.println("Serwer bossaAPI uruchomiony na porcie " + port);
        
        // Wątek akceptujący nowe połączenia
        Thread acceptThread = new Thread(this::acceptConnections);
        acceptThread.setName("BossaAPI-AcceptThread");
        acceptThread.start();
        
        // Uruchom symulację rynku (generowanie notowań)
        marketDataManager.startMarketSimulation();
    }
    
    /**
     * Akceptuje połączenia od klientów.
     */
    private void acceptConnections() {
        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowe polaczenie od: " + clientSocket.getInetAddress());
                
                // Utwórz nową sesję klienta
                ClientSession session = new ClientSession(clientSocket, sessionManager, orderManager, 
                                                        marketDataManager, securityManager);
                
                // Dodaj sesję do managera
                sessionManager.addSession(session);
                
                // Uruchom sesję w puli wątków
                executorService.submit(session::start);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Błąd podczas akceptowania połączeń: " + e.getMessage());
            }
        }
    }

    /**
     * Zatrzymuje serwer.
     */
    public void stop() {
        running = false;
        
        // Zatrzymaj symulację rynku
        marketDataManager.stopMarketSimulation();
        
        // Zamknij wszystkie sesje
        sessionManager.closeAllSessions();
        
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas zamykania serwera: " + e.getMessage());
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        System.out.println("Serwer bossaAPI zatrzymany");
    }
    
    /**
     * Główna metoda uruchamiająca serwer.
     */
    public static void main(String[] args) {
        int port = 24444; // Domyślny port
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Nieprawidłowy port: " + args[0] + ". Używam domyślnego portu: " + port);
            }
        }
        
        BossaApiServer server = new BossaApiServer(port);
        
        // Dodaj shutdown hook, aby poprawnie zatrzymać serwer
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        try {
            server.start();
            System.out.println("Naciśnij Ctrl+C, aby zatrzymać serwer");
        } catch (IOException e) {
            System.err.println("Błąd podczas uruchamiania serwera: " + e.getMessage());
            System.exit(1);
        }
    }
}
