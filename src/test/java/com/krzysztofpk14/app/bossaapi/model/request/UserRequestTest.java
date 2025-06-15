package com.krzysztofpk14.app.bossaapi.model.request;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {

    @Test
    void testDefaultConstructor() {
        // When
        UserRequest request = new UserRequest();
        
        // Then
        assertNull(request.getUserReqID());
        assertNull(request.getUserRequestType());
        assertNull(request.getUsername());
        assertNull(request.getPassword());
        assertEquals("UserReq", request.getMessageType());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String reqId = "1";
        String username = "testUser";
        String password = "testPass";
        
        // When
        UserRequest request = new UserRequest(reqId, username, password);
        
        // Then
        assertEquals(reqId, request.getUserReqID());
        assertEquals(UserRequest.LOGIN, request.getUserRequestType());
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testCreateLogoutRequest() {
        // Given
        String reqId = "2";
        String username = "testUser";
        
        // When
        UserRequest request = UserRequest.createLogoutRequest(reqId, username);
        
        // Then
        assertEquals(reqId, request.getUserReqID());
        assertEquals(UserRequest.LOGOUT, request.getUserRequestType());
        assertEquals(username, request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        UserRequest request = new UserRequest();
        String reqId = "3";
        String username = "user123";
        String password = "pass456";
        String requestType = UserRequest.USER_STATUS;
        
        // When
        request.setUserReqID(reqId);
        request.setUsername(username);
        request.setPassword(password);
        request.setUserRequestType(requestType);
        
        // Then
        assertEquals(reqId, request.getUserReqID());
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
        assertEquals(requestType, request.getUserRequestType());
    }

    @Test
    void testXmlMarshallingLogin() throws JAXBException {
        // Given
        UserRequest request = new UserRequest("4", "testUser", "testPass");
        FixmlMessage fixmlMessage = new FixmlMessage(request);
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(fixmlMessage, writer);
        String xml = writer.toString();
        
        // Then
        assertTrue(xml.contains("UserReqID=\"4\""));
        assertTrue(xml.contains("UserReqTyp=\"1\""));
        assertTrue(xml.contains("Username=\"testUser\""));
        assertTrue(xml.contains("Password=\"testPass\""));
    }

    @Test
    void testXmlMarshallingLogout() throws JAXBException {
        // Given
        UserRequest request = UserRequest.createLogoutRequest("5", "testUser");
        FixmlMessage fixmlMessage = new FixmlMessage(request);
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(fixmlMessage, writer);
        String xml = writer.toString();
        
        // Then
        assertTrue(xml.contains("UserReqID=\"5\""));
        assertTrue(xml.contains("UserReqTyp=\"2\""));
        assertTrue(xml.contains("Username=\"testUser\""));
        assertFalse(xml.contains("Password="));
    }

    @Test
    void testXmlUnmarshalling() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">" +
                     "<UserReq UserReqID=\"6\" UserReqTyp=\"1\" Username=\"BOS\" Password=\"BOS\"/>" + 
                     "</FIXML>";
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        FixmlMessage fixmlMessage = (FixmlMessage) unmarshaller.unmarshal(new StringReader(xml));
        UserRequest request = (UserRequest) fixmlMessage.getMessage();
        
        // Then
        assertEquals("6", request.getUserReqID());
        assertEquals("1", request.getUserRequestType());
        assertEquals("BOS", request.getUsername());
        assertEquals("BOS", request.getPassword());
    }

    @Test
    void testConstants() {
        // Verify constants have correct values
        assertEquals("1", UserRequest.LOGIN);
        assertEquals("2", UserRequest.LOGOUT);
        assertEquals("4", UserRequest.USER_STATUS);
    }
}