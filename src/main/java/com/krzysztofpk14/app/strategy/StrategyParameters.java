package com.krzysztofpk14.app.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasa przechowująca parametry strategii inwestycyjnej.
 */
public class StrategyParameters {
    private List<String> instruments;
    private boolean closePositionsOnStop = true;
    private final Map<String, Object> parameters = new HashMap<>();
    
    /**
     * Ustawia listę instrumentów dla strategii.
     * 
     * @param instruments Lista symboli instrumentów
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setInstruments(List<String> instruments) {
        this.instruments = instruments;
        return this;
    }
    
    /**
     * Zwraca listę instrumentów.
     * 
     * @return Lista symboli instrumentów
     */
    public List<String> getInstruments() {
        return instruments;
    }
    
    /**
     * Ustawia flagę zamykania pozycji przy zatrzymaniu strategii.
     * 
     * @param closePositionsOnStop true jeśli pozycje mają być zamykane
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setClosePositionsOnStop(boolean closePositionsOnStop) {
        this.closePositionsOnStop = closePositionsOnStop;
        return this;
    }
    
    /**
     * Sprawdza czy pozycje mają być zamykane przy zatrzymaniu strategii.
     * 
     * @return true jeśli pozycje mają być zamykane
     */
    public boolean isClosePositionsOnStop() {
        return closePositionsOnStop;
    }
    
    /**
     * Ustawia parametr typu String.
     * 
     * @param key Klucz parametru
     * @param value Wartość parametru
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setParam(String key, String value) {
        parameters.put(key, value);
        return this;
    }
    
    /**
     * Ustawia parametr typu int.
     * 
     * @param key Klucz parametru
     * @param value Wartość parametru
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setParam(String key, int value) {
        parameters.put(key, value);
        return this;
    }
    
    /**
     * Ustawia parametr typu double.
     * 
     * @param key Klucz parametru
     * @param value Wartość parametru
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setParam(String key, double value) {
        parameters.put(key, value);
        return this;
    }
    
    /**
     * Ustawia parametr typu boolean.
     * 
     * @param key Klucz parametru
     * @param value Wartość parametru
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setParam(String key, boolean value) {
        parameters.put(key, value);
        return this;
    }
    
    /**
     * Ustawia parametr dowolnego typu.
     * 
     * @param key Klucz parametru
     * @param value Wartość parametru
     * @return Ten obiekt dla fluent API
     */
    public StrategyParameters setParam(String key, Object value) {
        parameters.put(key, value);
        return this;
    }
    
    /**
     * Zwraca parametr jako String.
     * 
     * @param key Klucz parametru
     * @param defaultValue Wartość domyślna
     * @return Wartość parametru lub wartość domyślna
     */
    public String getStringParam(String key, String defaultValue) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Zwraca parametr jako int.
     * 
     * @param key Klucz parametru
     * @param defaultValue Wartość domyślna
     * @return Wartość parametru lub wartość domyślna
     */
    public int getIntParam(String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Zwraca parametr jako double.
     * 
     * @param key Klucz parametru
     * @param defaultValue Wartość domyślna
     * @return Wartość parametru lub wartość domyślna
     */
    public double getDoubleParam(String key, double defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Zwraca parametr jako boolean.
     * 
     * @param key Klucz parametru
     * @param defaultValue Wartość domyślna
     * @return Wartość parametru lub wartość domyślna
     */
    public boolean getBooleanParam(String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    /**
     * Zwraca parametr jako obiekt.
     * 
     * @param key Klucz parametru
     * @return Wartość parametru lub null
     */
    public Object getParam(String key) {
        return parameters.get(key);
    }

    /**
     * Zwraca wszystkie parametry strategii.
     * 
     * @return Mapowanie kluczy na wartości parametrów
     */
    public Map<String, Object> getAllParameters() {
        return parameters;
    }
}