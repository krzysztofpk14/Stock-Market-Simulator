package com.krzysztofpk14.app.bossaapi.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class BossaApiServerTest {
    
    private static final int TEST_PORT = 24444;
    private BossaApiServer server;
    
    @BeforeEach
    public void setUp() {
        server = new BossaApiServer(TEST_PORT);
    }
    
    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }
    
    @Test
    public void testServerCreation() {
        // When/Then - Just verifying server creation doesn't throw exceptions
        assertNotNull(server);
    }
    
    @Test
    public void testStartAndStopServer() throws IOException {
        // Given
        assertNotNull(server);
        
        // When - start the server
        server.start();
        
        // Then - sleep a bit to let it initialize
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Finally - stop the server (cleanup happens in tearDown)
        server.stop();
    }
}
