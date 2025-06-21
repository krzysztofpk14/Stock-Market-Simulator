package com.krzysztofpk14.app.gui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Logger for API communication between client and server.
 * This class captures requests and responses for debugging purposes.
 */
public class ApiCommunicationLogger {
    private static final int MAX_LOG_ENTRIES = 1000;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final List<ApiLogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());
    private Consumer<ApiLogEntry> logEntryAddedListener;
    
    /**
     * Logs a client request.
     * 
     * @param requestId The request ID
     * @param content The request content
     */
    public void logRequest(String requestId, String content) {
        addLogEntry(new ApiLogEntry(
                LocalDateTime.now(),
                ApiLogEntry.Direction.REQUEST,
                requestId,
                content
        ));
    }
    
    /**
     * Logs a server response.
     * 
     * @param requestId The request ID this response is for
     * @param content The response content
     */
    public void logResponse(String requestId, String content) {
        addLogEntry(new ApiLogEntry(
                LocalDateTime.now(),
                ApiLogEntry.Direction.RESPONSE,
                requestId,
                content
        ));
    }
    
    private void addLogEntry(ApiLogEntry entry) {
        synchronized (logEntries) {
            // Keep the log size manageable
            if (logEntries.size() >= MAX_LOG_ENTRIES) {
                logEntries.remove(0);
            }
            logEntries.add(entry);
        }
        
        // Notify listeners
        if (logEntryAddedListener != null) {
            logEntryAddedListener.accept(entry);
        }
    }
    
    /**
     * Gets all log entries.
     * 
     * @return Unmodifiable list of all log entries
     */
    public List<ApiLogEntry> getLogEntries() {
        synchronized (logEntries) {
            return Collections.unmodifiableList(new ArrayList<>(logEntries));
        }
    }
    
    /**
     * Clears all log entries.
     */
    public void clearLogs() {
        synchronized (logEntries) {
            logEntries.clear();
        }
    }
    
    /**
     * Sets a listener to be notified when new log entries are added.
     * 
     * @param listener The listener
     */
    public void setLogEntryAddedListener(Consumer<ApiLogEntry> listener) {
        this.logEntryAddedListener = listener;
    }
    
    /**
     * Represents a single API communication log entry.
     */
    public static class ApiLogEntry {
        private final LocalDateTime timestamp;
        private final Direction direction;
        private final String requestId;
        private final String content;
        
        public enum Direction {
            REQUEST("→"), RESPONSE("←");
            
            private final String symbol;
            
            Direction(String symbol) {
                this.symbol = symbol;
            }
            
            public String getSymbol() {
                return symbol;
            }
        }
        
        public ApiLogEntry(LocalDateTime timestamp, Direction direction, String requestId, String content) {
            this.timestamp = timestamp;
            this.direction = direction;
            this.requestId = requestId;
            this.content = content;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public Direction getDirection() {
            return direction;
        }
        
        public String getRequestId() {
            return requestId;
        }
        
        public String getContent() {
            return content;
        }
        
        public String getFormattedTimestamp() {
            return timestamp.format(TIME_FORMATTER);
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s %s: %s",
                    getFormattedTimestamp(),
                    direction.getSymbol(),
                    requestId,
                    content);
        }
    }
}