package com.krzysztofpk14.app.bossaapi.model.response;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    @Test
    void testDefaultConstructor() {
        // When
        UserResponse response = new UserResponse();
        
        // Then
        assertNull(response.getUserReqID());
        assertNull(response.getUserStatus());
        assertNull(response.getUserStatusText());
        assertNull(response.getUsername());
        assertNull(response.getMktDepth());
        assertEquals("UserRsp", response.getMessageType());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        UserResponse response = new UserResponse();
        String reqId = "1";
        String status = UserResponse.LOGGED_IN;
        String statusText = "Zalogowano pomyślnie";
        String username = "testUser";
        String mktDepth = UserResponse.FIVE_OFFERS;
        
        // When
        response.setUserReqID(reqId);
        response.setUserStatus(status);
        response.setUserStatusText(statusText);
        response.setUsername(username);
        response.setMktDepth(mktDepth);
        
        // Then
        assertEquals(reqId, response.getUserReqID());
        assertEquals(status, response.getUserStatus());
        assertEquals(statusText, response.getUserStatusText());
        assertEquals(username, response.getUsername());
        assertEquals(mktDepth, response.getMktDepth());
    }

    @Test
    void testIsLoginSuccessfulWithLoggedInStatus() {
        // Given
        UserResponse response = new UserResponse();
        response.setUserStatus(UserResponse.LOGGED_IN);
        
        // When
        boolean isSuccessful = response.isLoginSuccessful();
        
        // Then
        assertTrue(isSuccessful);
    }

    @Test
    void testIsLoginSuccessfulWithAlreadyLoggedInStatus() {
        // Given
        UserResponse response = new UserResponse();
        response.setUserStatus(UserResponse.OTHER);
        response.setUserStatusText("User is already logged");
        
        // When
        boolean isSuccessful = response.isLoginSuccessful();
        
        // Then
        assertTrue(isSuccessful);
    }

    @Test
    void testIsLoginSuccessfulWithFailedStatus() {
        // Given
        UserResponse response = new UserResponse();
        response.setUserStatus(UserResponse.WRONG_PASSWORD);
        
        // When
        boolean isSuccessful = response.isLoginSuccessful();
        
        // Then
        assertFalse(isSuccessful);
    }

    @Test
    void testIsLoginSuccessfulWithOtherStatusAndDifferentText() {
        // Given
        UserResponse response = new UserResponse();
        response.setUserStatus(UserResponse.OTHER);
        response.setUserStatusText("Unknown error");
        
        // When
        boolean isSuccessful = response.isLoginSuccessful();
        
        // Then
        assertFalse(isSuccessful);
    }

    @Test
    void testXmlMarshalling() throws JAXBException {
        // Given
        UserResponse response = new UserResponse();
        response.setUserReqID("2");
        response.setUserStatus(UserResponse.LOGGED_IN);
        response.setUserStatusText("Zalogowano pomyślnie");
        response.setUsername("testUser");
        response.setMktDepth(UserResponse.FIVE_OFFERS);
        FixmlMessage fixmlMessage = new FixmlMessage(response);
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(fixmlMessage, writer);
        String xml = writer.toString();
        
        // Then
        assertTrue(xml.contains("UserReqID=\"2\""));
        assertTrue(xml.contains("UserStat=\"1\""));
        assertTrue(xml.contains("UserStatText=\"Zalogowano pomyślnie\""));
        assertTrue(xml.contains("Username=\"testUser\""));
        assertTrue(xml.contains("MktDepth=\"5\""));
    }

    @Test
    void testXmlUnmarshalling() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">" +
                     "<UserRsp UserReqID=\"3\" Username=\"BOS\" MktDepth=\"5\" UserStat=\"1\" UserStatText=\"Login successful\"/>" + 
                     "</FIXML>";
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        FixmlMessage fixmlMessage= (FixmlMessage) unmarshaller.unmarshal(new StringReader(xml));
        UserResponse response = (UserResponse) fixmlMessage.getMessage();
        
        // Then
        assertEquals("3", response.getUserReqID());
        assertEquals("1", response.getUserStatus());
        assertEquals("Login successful", response.getUserStatusText());
        assertEquals("BOS", response.getUsername());
        assertEquals("5", response.getMktDepth());
    }

    @Test
    void testConstants() {
        // Verify status constants have correct values
        assertEquals("1", UserResponse.LOGGED_IN);
        assertEquals("2", UserResponse.LOGGED_OUT);
        assertEquals("3", UserResponse.USER_NOT_EXIST);
        assertEquals("4", UserResponse.WRONG_PASSWORD);
        assertEquals("5", UserResponse.INVESTOR_OFFLINE);
        assertEquals("6", UserResponse.OTHER);
        assertEquals("7", UserResponse.NOL_OFFLINE);
        
        // Verify MktDepth constants have correct values
        assertEquals("0", UserResponse.FULL_BOOK);
        assertEquals("1", UserResponse.TOP_OF_BOOK);
        assertEquals("5", UserResponse.FIVE_OFFERS);
    }
}