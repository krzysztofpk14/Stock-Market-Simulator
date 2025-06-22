package com.krzysztofpk14.app.bossaapi.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.krzysztofpk14.app.bossaapi.model.request.SecurityListRequest;
import com.krzysztofpk14.app.bossaapi.model.response.SecurityList;

public class SecurityManagerTest {
    
    private SecurityManager securityManager;
    
    @BeforeEach
    public void setUp() {
        securityManager = new SecurityManager();
    }
    
    @Test
    public void testGetAllSecurities() {
        // Given
        SecurityListRequest request = new SecurityListRequest("REQ1", SecurityListRequest.ALL_INSTRMNT);
        
        // When
        SecurityList response = securityManager.getSecurities(request);
        
        // Then
        assertNotNull(response);
        assertEquals("REQ1", response.getRequestId());
        assertEquals(SecurityListRequest.ALL_INSTRMNT, response.getSecurityRequestType());
        assertFalse(response.isRejected());
        assertTrue(response.isComplete());
        assertNotNull(response.getSecurities());
        assertTrue(response.getSecurities().size() > 0);
    }
    
    @Test
    public void testGetSpecificSecurity() {
        // Given
        String symbol = "KGHM";
        SecurityListRequest request = new SecurityListRequest("REQ2", symbol, "1");
        
        // When
        SecurityList response = securityManager.getSecurities(request);
        
        // Then
        assertNotNull(response);
        assertEquals("REQ2", response.getRequestId());
        assertFalse(response.isRejected());
        assertTrue(response.isComplete());
        assertNotNull(response.getSecurities());
        assertEquals(1, response.getSecurities().size());
        assertEquals(symbol, response.getSecurities().get(0).getInstrument().getSymbol());
    }
}
