package com.krzysztofpk14.app.strategy;

import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse;
import com.krzysztofpk14.app.gui.TradingAppGUI;

/**
 * Interfejs bazowy dla wszystkich strategii inwestycyjnych.
 */
public interface InvestmentStrategy {
    
    /**
     * Inicjalizuje strategię.
     * 
     * @param parameters Parametry strategii
     */
    void initialize(StrategyParameters parameters);
    
    /**
     * Uruchamia strategię.
     */
    void start();
    
    /**
     * Zatrzymuje strategię.
     */
    void stop();
    
    /**
     * Obsługuje nowe dane rynkowe.
     * 
     * @param marketData Dane rynkowe
     */
    void onMarketData(MarketDataResponse marketData);
    
    /**
     * Obsługuje raporty wykonania zleceń.
     * 
     * @param report Raport wykonania
     */
    void onExecutionReport(ExecutionReport report);
    
    /**
     * Zwraca nazwę strategii.
     * 
     * @return Nazwa strategii
     */
    String getName();
    
    /**
     * Zwraca opis strategii.
     * 
     * @return Opis strategii
     */
    String getDescription();
    
    /**
     * Zwraca status strategii.
     * 
     * @return Status strategii
     */
    StrategyStatus getStatus();

    /**
     * Zwraca status strategii jako łańcuch znaków.
     * 
     * @return Łańcuch znaków reprezentujący status strategii
     */
    String getStatusString();
    
    /**
     * Zwraca bieżące statystyki strategii.
     * 
     * @return Statystyki strategii
     */
    StrategyStatistics getStatistics();

    /**
     * Wyświetla statystyki strategii w konsoli.
     */
    void displayStatistics();


    /**
     * Zwraca parametry strategii jako łańcuch znaków.
     * 
     * @return Łańcuch znaków z parametrami strategii
     */
    String getParametersAsString();

    /**
     * Zwraca Instrumenty w strategii
     * 
     * @return String zawierający instrumenty strategii
     */
    String getInstruments();

    /**
     * Ustawia GUI aplikacji handlowej.
     * 
     * @param gui GUI aplikacji handlowej
     */
    void setGui(TradingAppGUI gui);


}