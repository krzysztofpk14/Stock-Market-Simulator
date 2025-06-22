package com.krzysztofpk14.app.bossaapi.model.response;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;

public class BusinessMessageRejectTest {

    @Test
    void testDefaultConstructor() {
        // When
        BusinessMessageReject reject = new BusinessMessageReject();
        
        // Then
        assertNull(reject.getRefMsgType());
        assertNull(reject.getBusinessRejectReason());
        assertNull(reject.getText());
        assertEquals("BizMsgRej", reject.getMessageType());
        assertEquals("None", reject.getMessageId());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        BusinessMessageReject reject = new BusinessMessageReject();
        String refMsgType = BusinessMessageReject.LOG_IN_OUT;
        String businessRejectReason = BusinessMessageReject.NOT_AUTHORIZED;
        String text = "User not authorized";
        
        // When
        reject.setRefMsgType(refMsgType);
        reject.setBusinessRejectReason(businessRejectReason);
        reject.setText(text);
        
        // Then
        assertEquals(refMsgType, reject.getRefMsgType());
        assertEquals(businessRejectReason, reject.getBusinessRejectReason());
        assertEquals(text, reject.getText());
    }

    @Test
    void testBaseMessageImplementation() {
        // Given
        BusinessMessageReject reject = new BusinessMessageReject();
        
        // When
        BaseMessage message = reject;
        
        // Then
        assertEquals("BizMsgRej", message.getMessageType());
        assertEquals("None", message.getMessageId());
    }

    @Test
    void testRefMsgTypeConstants() {
        // Then
        assertEquals("BE", BusinessMessageReject.LOG_IN_OUT);
        assertEquals("D", BusinessMessageReject.NEW_ORDER);
        assertEquals("F", BusinessMessageReject.CANCEL_ORDER);
        assertEquals("G", BusinessMessageReject.MODIFY_ORDER);
        assertEquals("H", BusinessMessageReject.ORDER_STATUS);
        assertEquals("V", BusinessMessageReject.MARKET_DATA_REQUEST);
        assertEquals("g", BusinessMessageReject.STATUS);
    }

    @Test
    void testBusinessRejectReasonConstants() {
        // Then
        assertEquals("0", BusinessMessageReject.OTHER);
        assertEquals("1", BusinessMessageReject.UNKNOWN_ID);
        assertEquals("2", BusinessMessageReject.UNKNOWN_SECURITY);
        assertEquals("3", BusinessMessageReject.UNSUPPORTED_MESSAGE_TYPE);
        assertEquals("4", BusinessMessageReject.APPLICATION_NOT_AVAILABLE);
        assertEquals("5", BusinessMessageReject.CONDITIONALLY_REQUIRED_FIELD_MISSING);
        assertEquals("6", BusinessMessageReject.NOT_AUTHORIZED);
        assertEquals("7", BusinessMessageReject.LACK_OF_COMMUNICATION);
    }
}
