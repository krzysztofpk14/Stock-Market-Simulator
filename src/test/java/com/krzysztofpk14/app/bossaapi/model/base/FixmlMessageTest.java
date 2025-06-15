package com.krzysztofpk14.app.bossaapi.model.base;

import com.krzysztofpk14.app.bossaapi.model.request.UserRequest;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class FixmlMessageTest {

    @Test
    void testConstructorInitializesDefaultValues() {
        // Given
        FixmlMessage message = new FixmlMessage();
        
        // Then
        assertEquals("5.0", message.getVersion());
        assertEquals("20080317", message.getRevision());
        assertEquals("20080314", message.getServicepack());
        assertNull(message.getMessage());
    }

    @Test
    void testParameterizedConstructorSetsMessage() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testUser");
        
        // When
        FixmlMessage message = new FixmlMessage(userRequest);
        
        // Then
        assertEquals("5.0", message.getVersion());
        assertEquals("20080317", message.getRevision());
        assertEquals("20080314", message.getServicepack());
        assertNotNull(message.getMessage());
        assertEquals(userRequest, message.getMessage());
        assertEquals("UserReq", message.getMessage().getMessageType());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        FixmlMessage message = new FixmlMessage();
        UserRequest userRequest = new UserRequest();
        
        // When
        message.setVersion("4.0");
        message.setRevision("20070101");
        message.setServicepack("20070101");
        message.setMessage(userRequest);
        
        // Then
        assertEquals("4.0", message.getVersion());
        assertEquals("20070101", message.getRevision());
        assertEquals("20070101", message.getServicepack());
        assertEquals(userRequest, message.getMessage());
    }

    @Test
    void testXmlMarshalling() throws JAXBException {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testUser");
        userRequest.setPassword("testPass");
        userRequest.setUserRequestType("1"); // login
        userRequest.setUserReqID("123");
        
        FixmlMessage fixmlMessage = new FixmlMessage(userRequest);
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(fixmlMessage, writer);
        String xml = writer.toString();
        
        // Then
        assertTrue(xml.contains("<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">"));
        assertTrue(xml.contains("<UserReq"));
        assertTrue(xml.contains("ReqID=\"123\""));
        assertTrue(xml.contains("Username=\"testUser\""));
        assertTrue(xml.contains("Password=\"testPass\""));
        assertTrue(xml.contains("UserReqTyp=\"1\""));
    }

    @Test
    void testXmlUnmarshalling() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">" +
                     "<UserRsp UserReqID=\"123\" UserStat=\"1\" UserStatText=\"Zalogowano pomyślnie\"/>" +
                     "</FIXML>";
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        FixmlMessage fixmlMessage = (FixmlMessage) unmarshaller.unmarshal(new StringReader(xml));
        
        // Then
        assertNotNull(fixmlMessage);
        assertEquals("5.0", fixmlMessage.getVersion());
        assertEquals("20080317", fixmlMessage.getRevision());
        assertEquals("20080314", fixmlMessage.getServicepack());
        assertNotNull(fixmlMessage.getMessage());
        assertTrue(fixmlMessage.getMessage() instanceof UserResponse);
        
        UserResponse response = (UserResponse) fixmlMessage.getMessage();
        assertEquals("123", response.getUserReqID());
        assertEquals("1", response.getUserStatus());
        assertEquals("Zalogowano pomyślnie", response.getUserStatusText());
    }
    
    @Test
    void testDifferentMessageTypesHandling() throws JAXBException {
        // Given - UserRequest XML
        String userReqXml = "<FIXML v=\"5.0\">" +
                          "<UserReq ReqID=\"123\" Username=\"test\" Password=\"pass\" UserReqTyp=\"1\"/>" +
                          "</FIXML>";
                          
        // Given - UserResponse XML
        String userRspXml = "<FIXML v=\"5.0\">" +
                          "<UserRsp UserReqID=\"123\" UserStat=\"1\" UserStatText=\"OK\"/>" +
                          "</FIXML>";
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        FixmlMessage userReqMessage = (FixmlMessage) unmarshaller.unmarshal(new StringReader(userReqXml));
        FixmlMessage userRspMessage = (FixmlMessage) unmarshaller.unmarshal(new StringReader(userRspXml));
        
        // Then
        assertTrue(userReqMessage.getMessage() instanceof UserRequest);
        assertTrue(userRspMessage.getMessage() instanceof UserResponse);
        
        assertEquals("UserReq", userReqMessage.getMessage().getMessageType());
        assertEquals("UserRsp", userRspMessage.getMessage().getMessageType());
    }
}