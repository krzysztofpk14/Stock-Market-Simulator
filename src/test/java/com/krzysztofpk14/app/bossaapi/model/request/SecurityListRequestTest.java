package com.krzysztofpk14.app.bossaapi.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SecurityListRequestTest {

    @Test
    public void testDefaultConstructor() {
        // When
        SecurityListRequest request = new SecurityListRequest();
        
        // Then
        assertNull(request.getRequestId());
        assertNull(request.getSecurityRequestType());
        assertNull(request.getSubscriptionRequestType());
        assertNull(request.getInstrument());
        assertNull(request.getInstrumentLegGroup());
    }

    @Test
    public void testConstructorWithParameters() {
        // Given
        String requestId = "SL-123";
        String securityRequestType = SecurityListRequest.ALL_INSTRMNT;
        
        // When
        SecurityListRequest request = new SecurityListRequest(requestId, securityRequestType);
        
        // Then
        assertEquals(requestId, request.getRequestId());
        assertEquals(securityRequestType, request.getSecurityRequestType());
    }

    @Test
    public void testConstructorWithSymbol() {
        // Given
        String requestId = "SL-456";
        String symbol = "AAPL";
        String subscriptionType = "1";
        
        // When
        SecurityListRequest request = new SecurityListRequest(requestId, symbol, subscriptionType);
        
        // Then
        assertEquals(requestId, request.getRequestId());
        assertEquals(SecurityListRequest.ONE_INSTRMNT, request.getSecurityRequestType());
        assertEquals(subscriptionType, request.getSubscriptionRequestType());
        assertNotNull(request.getInstrument());
        assertEquals(symbol, request.getInstrument().getSymbol());
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        SecurityListRequest request = new SecurityListRequest();
        String requestId = "SL-789";
        String securityRequestType = SecurityListRequest.MARKET_TYPE;
        String subscriptionRequestType = "0";
        
        // When
        request.setRequestId(requestId);
        request.setSecurityRequestType(securityRequestType);
        request.setSubscriptionRequestType(subscriptionRequestType);
        
        // Then
        assertEquals(requestId, request.getRequestId());
        assertEquals(securityRequestType, request.getSecurityRequestType());
        assertEquals(subscriptionRequestType, request.getSubscriptionRequestType());
    }

    @Test
    public void testConstants() {
        // Security List Request Types
        assertEquals("0", SecurityListRequest.ONE_INSTRMNT);
        assertEquals("1", SecurityListRequest.ONE_INSTRMNT_TYPE);
        assertEquals("4", SecurityListRequest.ALL_INSTRMNT);
        assertEquals("5", SecurityListRequest.MARKET_TYPE);
        
        // Market IDs
        assertEquals("NM", SecurityListRequest.CASH_MARKET);
        assertEquals("DN", SecurityListRequest.DERIVATIVES_MARKET);
    }

    @Test
    public void testGetMessageType() {
        // Given
        SecurityListRequest request = new SecurityListRequest();
        
        // When/Then
        assertEquals("SecListReq", request.getMessageType());
    }

    @Test
    public void testGetMessageId() {
        // Given
        String requestId = "SL-101";
        SecurityListRequest request = new SecurityListRequest();
        request.setRequestId(requestId);
        
        // When/Then
        assertEquals(requestId, request.getMessageId());
    }
}
