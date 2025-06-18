package com.krzysztofpk14.app.strategy;

/**
 * Enumeracja reprezentująca status strategii inwestycyjnej.
 */
public enum StrategyStatus {
    /**
     * Strategia została zainicjalizowana, ale jeszcze nie uruchomiona.
     */
    INITIALIZED,
    
    /**
     * Strategia jest uruchomiona.
     */
    RUNNING,
    
    /**
     * Strategia jest w trakcie zatrzymywania.
     */
    STOPPING,
    
    /**
     * Strategia została zatrzymana.
     */
    STOPPED,
    
    /**
     * Strategia jest w stanie błędu.
     */
    ERROR
}