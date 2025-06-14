package com.krzysztofpk14.app.bossaapi.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager sesji klientów.
 */
public class SessionManager {
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    
    /**
     * Dodaje nową sesję.
     * 
     * @param session Sesja do dodania
     */
    public void addSession(ClientSession session) {
        sessions.put(session.getSessionId(), session);
    }
    
    /**
     * Usuwa sesję.
     * 
     * @param session Sesja do usunięcia
     */
    public void removeSession(ClientSession session) {
        sessions.remove(session.getSessionId());
    }
    
    /**
     * Zwraca sesję o podanym identyfikatorze.
     * 
     * @param sessionId Identyfikator sesji
     * @return Sesja lub null jeśli nie istnieje
     */
    public ClientSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * Zamyka wszystkie sesje.
     */
    public void closeAllSessions() {
        for (ClientSession session : sessions.values()) {
            session.close();
        }
        sessions.clear();
    }
    
    /**
     * Zwraca liczbę aktywnych sesji.
     * 
     * @return Liczba aktywnych sesji
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream().filter(ClientSession::isAuthenticated).count();
    }
    
    /**
     * Zwraca wszystkie aktywne sesje.
     * 
     * @return Mapa sesji
     */
    public Map<String, ClientSession> getActiveSessions() {
        return sessions;
    }
}