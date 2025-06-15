package com.krzysztofpk14.app.bossaapi.model.base;

import com.krzysztofpk14.app.bossaapi.model.request.UserRequest;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseMessageTest {

    @Test
    void testUserRequestImplementation() {
        // Given
        UserRequest request = new UserRequest();
        
        // When
        BaseMessage message = request;
        
        // Then
        assertEquals("UserReq", message.getMessageType());
    }

    @Test
    void testUserResponseImplementation() {
        // Given
        UserResponse response = new UserResponse();
        
        // When
        BaseMessage message = response;
        
        // Then
        assertEquals("UserRsp", message.getMessageType());
    }
    
    @Test
    void testInheritance() {
        // Given
        UserRequest request = new UserRequest();
        
        // Then
        assertTrue(request instanceof BaseMessage);
    }

    @Test
    void testPolymorphism() {
        // Given
        BaseMessage[] messages = new BaseMessage[2];
        messages[0] = new UserRequest();
        messages[1] = new UserResponse();
        
        // When / Then
        assertEquals("UserReq", messages[0].getMessageType());
        assertEquals("UserRsp", messages[1].getMessageType());
    }
}