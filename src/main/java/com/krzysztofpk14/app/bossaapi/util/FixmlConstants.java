package com.krzysztofpk14.app.bossaapi.util;

/**
 * Klasa zawierająca stałe wykorzystywane w komunikatach FIXML bossaAPI.
 */
public class FixmlConstants {
    
    // Wersje FIXML
    public static final String FIXML_VERSION = "5.0";
    public static final String FIXML_REVISION = "20080317";
    public static final String FIXML_SERVICE_PACK = "20080314";
    
    // Strony zleceń
    public static final String SIDE_BUY = "1";
    public static final String SIDE_SELL = "2";
    
    // Typy zleceń
    public static final String ORDER_TYPE_MARKET = "1";
    public static final String ORDER_TYPE_LIMIT = "2";
    
    // Ważność zleceń
    public static final String TIME_IN_FORCE_DAY = "0";
    public static final String TIME_IN_FORCE_GOOD_TILL_CANCEL = "4";
    
    // Typy wykonania zleceń
    public static final String EXEC_TYPE_NEW = "0";
    public static final String EXEC_TYPE_PARTIAL_FILL = "1";
    public static final String EXEC_TYPE_FILL = "2";
    public static final String EXEC_TYPE_CANCELED = "4";
    public static final String EXEC_TYPE_REJECTED = "8";
    
    // Statusy użytkownika
    public static final String USER_STATUS_LOGGED_IN = "1";
    public static final String USER_STATUS_LOGGED_OUT = "2";
    public static final String USER_STATUS_LOGIN_FAILED = "3";
    
    // Typy żądań danych rynkowych
    public static final String MARKET_DATA_REQUEST_SNAPSHOT = "0";
    public static final String MARKET_DATA_REQUEST_SUBSCRIBE = "1";
    public static final String MARKET_DATA_REQUEST_UNSUBSCRIBE = "2";
    
    // Typy danych rynkowych
    public static final String MARKET_DATA_ENTRY_BID = "0";
    public static final String MARKET_DATA_ENTRY_OFFER = "1";
    public static final String MARKET_DATA_ENTRY_TRADE = "2";
    public static final String MARKET_DATA_ENTRY_OPEN = "4";
    public static final String MARKET_DATA_ENTRY_CLOSE = "5";
    public static final String MARKET_DATA_ENTRY_HIGH = "7";
    public static final String MARKET_DATA_ENTRY_LOW = "8";
}