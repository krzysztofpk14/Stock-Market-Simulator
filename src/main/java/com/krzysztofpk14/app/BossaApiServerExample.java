package com.krzysztofpk14.app;

import java.io.IOException;

import com.krzysztofpk14.app.bossaapi.server.BossaApiServer;

/**
 * Przykład uruchomienia serwera bossaAPI.
 */
public class BossaApiServerExample {
    
    public static void main(String[] args) {
        int port = 24444; // Domyślny port
        
        System.out.println("Uruchamianie serwera bossaAPI na porcie " + port);
        
        BossaApiServer server = new BossaApiServer(port);
        
        try {
            server.start();
            
            System.out.println("Serwer bossaAPI uruchomiony");
            System.out.println("Naciśnij Enter, aby zatrzymać serwer");
            
            // Czekaj na Enter
            System.in.read();
            
        } catch (IOException e) {
            System.err.println("Błąd podczas uruchamiania serwera: " + e.getMessage());
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}