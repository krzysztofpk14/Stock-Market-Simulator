package com.krzysztofpk14.app.bossaapi.model.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class MarketDataRequestTest {

    @Test
    public void testDefaultConstructor() {
        // When
        MarketDataRequest request = new MarketDataRequest();
        
        // Then
        assertNull(request.getRequestId());
        assertNull(request.getSubscriptionRequestType());
        assertNull(request.getMarketDepth());
        assertNotNull(request.getInstruments());
        assertTrue(request.getInstruments().isEmpty());
        assertEquals("MktDataReq", request.getMessageType());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Given
        MarketDataRequest request = new MarketDataRequest();
        String requestId = "REQ-123";
        String subType = MarketDataRequest.SUBSCRIBE;
        String marketDepth = "0";  // full book
        
        // When
        request.setRequestId(requestId);
        request.setSubscriptionRequestType(subType);
        request.setMarketDepth(marketDepth);
        
        // Then
        assertEquals(requestId, request.getRequestId());
        assertEquals(subType, request.getSubscriptionRequestType());
        assertEquals(marketDepth, request.getMarketDepth());
        assertEquals(requestId, request.getMessageId());
    }
    
    @Test
    public void testAddInstrument() {
        // Given
        MarketDataRequest request = new MarketDataRequest();
        String symbol1 = "AAPL";
        String symbol2 = "MSFT";
        
        // When
        request.addInstrument(symbol1);
        request.addInstrument(symbol2);
        
        // Then
        List<MarketDataRequest.InstrumentMarketDataRequest> instruments = request.getInstruments();
        assertNotNull(instruments);
        assertEquals(2, instruments.size());
        assertEquals(symbol1, instruments.get(0).getInstrument().getSymbol());
        assertEquals(symbol2, instruments.get(1).getInstrument().getSymbol());
    }
    
    @Test
    public void testConstants() {
        // When/Then
        assertEquals("0", MarketDataRequest.SNAPSHOT);
        assertEquals("1", MarketDataRequest.SUBSCRIBE);
        assertEquals("2", MarketDataRequest.UNSUBSCRIBE);
    }
}
