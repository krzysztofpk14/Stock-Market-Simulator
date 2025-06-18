package com.krzysztofpk14.app.strategy;

import com.krzysztofpk14.app.bossaapi.client.BossaApiClient;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa zarządzająca strategiami inwestycyjnymi.
 */
public class StrategyManager {
    private static final Logger logger = Logger.getLogger(StrategyManager.class.getName());
    
    private final BossaApiClient client;
    private final List<InvestmentStrategy> strategies = new CopyOnWriteArrayList<>();
    
    /**
     * Konstruktor.
     * 
     * @param client Klient API Bossa
     */
    public StrategyManager(BossaApiClient client) {
        this.client = client;
        
        // Rejestruj obserwatorów zdarzeń
        client.registerMarketDataHandler(this::distributeMarketData);
        client.registerExecutionReportHandler(this::distributeExecutionReport);
    }
    
    /**
     * Dodaje strategię do managera.
     * 
     * @param strategy Strategia do dodania
     * @return true jeśli strategia została dodana
     */
    public boolean addStrategy(InvestmentStrategy strategy, StrategyParameters parameters) {
        if (strategy == null || parameters == null) {
            return false;
        }
        
        // Inicjalizuj strategię
        strategy.initialize(parameters);

        // Dodaj strategię do listy
        strategies.add(strategy);
        logger.info("Dodano strategię: " + strategy.getName());
        return true;
    }
    
    /**
     * Uruchamia wszystkie strategie.
     */
    public void startAllStrategies() {
        logger.info("Uruchamianie wszystkich strategii...");
        for (InvestmentStrategy strategy : strategies) {
            try {
                strategy.start();
                logger.info("Uruchomiono strategię: " + strategy.getName());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Błąd podczas uruchamiania strategii: " + strategy.getName(), e);
            }
        }
    }
    
    /**
     * Zatrzymuje wszystkie strategie.
     */
    public void stopAllStrategies() {
        logger.info("Zatrzymywanie wszystkich strategii...");
        for (InvestmentStrategy strategy : strategies) {
            try {
                strategy.stop();
                logger.info("Zatrzymano strategię: " + strategy.getName());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Błąd podczas zatrzymywania strategii: " + strategy.getName(), e);
            }
        }
    }
    
    /**
     * Zwraca listę wszystkich strategii.
     * 
     * @return Lista strategii
     */
    public List<InvestmentStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }
    
    /**
     * Usuwa strategię.
     * 
     * @param strategyName Nazwa strategii do usunięcia
     * @return true jeśli strategia została usunięta
     */
    public boolean removeStrategy(String strategyName) {
        Iterator<InvestmentStrategy> iterator = strategies.iterator();
        while (iterator.hasNext()) {
            InvestmentStrategy strategy = iterator.next();
            if (strategy.getName().equals(strategyName)) {
                strategy.stop();
                strategies.remove(strategy);
                logger.info("Usunięto strategię: " + strategyName);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Rozpowszechnia dane rynkowe do wszystkich strategii.
     * 
     * @param data Dane rynkowe
     */
    private void distributeMarketData(MarketDataResponse data) {
        for (InvestmentStrategy strategy : strategies) {
            try {
                strategy.onMarketData(data);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Błąd podczas przetwarzania danych rynkowych w strategii: " + strategy.getName(), e);
            }
        }
    }
    
    /**
     * Rozpowszechnia raporty wykonania do wszystkich strategii.
     * 
     * @param report Raport wykonania
     */
    private void distributeExecutionReport(ExecutionReport report) {
        for (InvestmentStrategy strategy : strategies) {
            try {
                strategy.onExecutionReport(report);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Błąd podczas przetwarzania raportu wykonania w strategii: " + strategy.getName(), e);
            }
        }
    }

    public void displayStatistics(){
        System.out.println("\nStatystyki strategii:");
        for (InvestmentStrategy strategy : strategies) {
            System.out.println("Nazwa: " + strategy.getName() + ", Opis: " + strategy.getDescription() + ", Status: " + strategy.getStatus());
            System.out.println("Statystyki:");
            strategy.displayStatistics();
            System.out.println("--------------------------------------------------");
        }
    }
}