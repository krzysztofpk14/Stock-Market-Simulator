package com.krzysztofpk14.app.bossaapi.model.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class MarketDataResponseTest {

    @Test
    public void testDefaultConstructor() {
        // When
        MarketDataResponse response = new MarketDataResponse();
        
        // Then
        assertNull(response.getRequestId());
        assertNull(response.getInstrument());
        assertNull(response.getMarketDataGroups());
        assertNull(response.getResponseType());
        assertEquals("MktDataSnap", response.getMessageType());
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        MarketDataResponse response = new MarketDataResponse();
        String requestId = "MDR-123";
        String responseType = MarketDataResponse.FULL_REFRESH;
        
        MarketDataResponse.Instrument instrument = new MarketDataResponse.Instrument();
        instrument.setSymbol("AAPL");
        instrument.setId("Apple-ID");
        
        List<MarketDataResponse.MarketDataGroup> groups = new ArrayList<>();
        MarketDataResponse.MarketDataGroup group = new MarketDataResponse.MarketDataGroup();
        group.setMarketDataEntryType("0"); // Bid
        groups.add(group);
        
        // When
        response.setRequestId(requestId);
        response.setResponseType(responseType);
        response.setInstrument(instrument);
        response.setMarketDataGroups(groups);
        
        // Then
        assertEquals(requestId, response.getRequestId());
        assertEquals(responseType, response.getResponseType());
        assertNotNull(response.getInstrument());
        assertEquals("AAPL", response.getInstrument().getSymbol());
        assertEquals("Apple-ID", response.getInstrument().getId());
        assertNotNull(response.getMarketDataGroups());
        assertEquals(1, response.getMarketDataGroups().size());
        assertEquals("0", response.getMarketDataGroups().get(0).getMarketDataEntryType());
    }

    @Test
    public void testInstrumentSettersAndGetters() {
        // Given
        MarketDataResponse.Instrument instrument = new MarketDataResponse.Instrument();
        String symbol = "MSFT";
        String id = "Microsoft-ID";
        
        // When
        instrument.setSymbol(symbol);
        instrument.setId(id);
        
        // Then
        assertEquals(symbol, instrument.getSymbol());
        assertEquals(id, instrument.getId());
    }

    @Test
    public void testMarketDataEntryTypeConstants() {
        // Then
        assertEquals("0", MarketDataResponse.TRADE);
        assertEquals("1", MarketDataResponse.FULL_REFRESH);
        assertEquals("2", MarketDataResponse.UNSOLICITED_INDICATOR);
    }

    @Test
    public void testMessageId() {
        // Given
        String requestId = "MDR-456";
        MarketDataResponse response = new MarketDataResponse();
        response.setRequestId(requestId);
        
        // When/Then
        assertEquals(requestId, response.getMessageId());
    }
}
