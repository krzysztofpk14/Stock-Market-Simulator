package com.krzysztofpk14.app.bossaapi.model.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SecurityListTest {

    @Test
    public void testDefaultConstructor() {
        // When
        SecurityList securityList = new SecurityList();
        
        // Then
        assertNull(securityList.getRequestId());
        assertNull(securityList.getSecurityRequestType());
        assertNull(securityList.getSecurityResponseType());
        assertNull(securityList.getTotalNumberOfSecurities());
        assertNull(securityList.getLastFragment());
        assertNotNull(securityList.getSecurityListGroup());
        assertEquals("SecList", securityList.getMessageType());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Given
        SecurityList securityList = new SecurityList();
        String requestId = "SL-123";
        String securityRequestType = "0";
        String securityResponseType = SecurityList.FULL_RESPONSE;
        String totalNumber = "10";
        String lastFragment = "Y";
        
        // When
        securityList.setRequestId(requestId);
        securityList.setSecurityRequestType(securityRequestType);
        securityList.setSecurityResponseType(securityResponseType);
        securityList.setTotalNumberOfSecurities(totalNumber);
        securityList.setLastFragment(lastFragment);
        
        // Then
        assertEquals(requestId, securityList.getRequestId());
        assertEquals(securityRequestType, securityList.getSecurityRequestType());
        assertEquals(securityResponseType, securityList.getSecurityResponseType());
        assertEquals(totalNumber, securityList.getTotalNumberOfSecurities());
        assertEquals(lastFragment, securityList.getLastFragment());
    }
    
    @Test
    public void testAddSecurity() {
        // Given
        SecurityList securityList = new SecurityList();
        SecurityList.SecurityDefinition security1 = createSecurityDefinition("AAPL");
        SecurityList.SecurityDefinition security2 = createSecurityDefinition("MSFT");
        
        // When
        securityList.addSecurity(security1);
        securityList.addSecurity(security2);
        
        // Then
        assertNotNull(securityList.getSecurities());
        assertEquals(2, securityList.getSecurities().size());
        assertEquals("AAPL", securityList.getSecurities().get(0).getInstrument().getSymbol());
        assertEquals("MSFT", securityList.getSecurities().get(1).getInstrument().getSymbol());
    }
    
    @Test
    public void testIsRejected() {
        // Given
        SecurityList securityList = new SecurityList();
        
        // When/Then - Not rejected by default
        assertFalse(securityList.isRejected());
        
        // When set to rejected
        securityList.setSecurityResponseType(SecurityList.REJECTED);
        
        // Then
        assertTrue(securityList.isRejected());
    }
    
    @Test
    public void testIsComplete() {
        // Given
        SecurityList securityList = new SecurityList();
        
        // When/Then with full response
        securityList.setSecurityResponseType(SecurityList.FULL_RESPONSE);
        assertTrue(securityList.isComplete());
        
        // When/Then with partial response but last fragment
        securityList.setSecurityResponseType(SecurityList.PARTIAL_RESPONSE);
        securityList.setLastFragment("Y");
        assertTrue(securityList.isComplete());
        
        // When/Then with partial response and not last fragment
        securityList.setLastFragment("N");
        assertFalse(securityList.isComplete());
    }
    
    @Test
    public void testResponseTypeConstants() {
        // Then
        assertEquals("1", SecurityList.FULL_RESPONSE);
        assertEquals("2", SecurityList.PARTIAL_RESPONSE);
        assertEquals("3", SecurityList.NO_INSTRUMENTS);
        assertEquals("5", SecurityList.REJECTED);
    }
    
    // Helper methods
    private SecurityList.SecurityDefinition createSecurityDefinition(String symbol) {
        SecurityList.SecurityDefinition securityDef = new SecurityList.SecurityDefinition();
        SecurityList.Instrument instrument = new SecurityList.Instrument();
        instrument.setSymbol(symbol);
        securityDef.setInstrument(instrument);
        return securityDef;
    }
}
