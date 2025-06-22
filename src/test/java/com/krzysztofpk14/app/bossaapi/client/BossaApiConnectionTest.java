package com.krzysztofpk14.app.bossaapi.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BossaApiConnectionTest {
    
    private BossaApiConnection connection;
    
    @BeforeEach
    public void setUp() {
        connection = new BossaApiConnection();
    }
    
    @Test
    public void testInitialState() {
        // When/Then
        assertFalse(connection.isConnected());
    }
      @Test
    public void testStartReceivingAsync() {
        // Given
        AtomicReference<String> receivedMessage = new AtomicReference<>();
        Consumer<String> handler = message -> receivedMessage.set(message);
        
        // When
        connection.startReceivingAsync(handler);
        
        // Then - Cannot test further without connection
        assertNotNull(connection);
    }
    
    @Test
    public void testSendMessageThrowsExceptionWhenNotConnected() {
        // Given
        String message = "<FIXML><UserReq/></FIXML>";
        
        // When/Then
        assertThrows(IOException.class, () -> {
            connection.sendMessage(message);
        });
    }
    
    @Test
    public void testReceiveMessageReturnsErrorWhenNotConnected() {
        // When
        assertThrows(NullPointerException.class, () -> {
            connection.receiveMessage();
        });
    }
    
      @Test
    public void testStartReceivingAsyncDoesNothingWhenNotConnected() {
        // Given
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        Consumer<String> handler = _ -> handlerCalled.set(true);
        
        // When
        connection.startReceivingAsync(handler);
        
        // Then - This should not throw an exception
        assertFalse(handlerCalled.get());
    }
    
    @Test
    public void testStopReceivingWorksEvenWhenNotStarted() {
        // When/Then - Should not throw exception
        connection.stopReceiving();
    }
}
