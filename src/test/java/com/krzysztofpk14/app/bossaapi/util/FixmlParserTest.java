package com.krzysztofpk14.app.bossaapi.util;

import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;
import com.krzysztofpk14.app.bossaapi.model.request.UserRequest;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FixmlParserTest {

    @Test
    void testParseValidUserRequest() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">" +
                "<UserReq UserReqID=\"REQ123\" UserReqTyp=\"1\" Username=\"testUser\" Password=\"testPass\"/>" +
                "</FIXML>";

        // When
        FixmlMessage message = FixmlParser.parse(xml);

        // Then
        assertNotNull(message);
        assertEquals("5.0", message.getVersion());
        assertEquals("20080317", message.getRevision());
        assertEquals("20080314", message.getServicepack());
        
        assertTrue(message.getMessage() instanceof UserRequest);
        UserRequest userRequest = (UserRequest) message.getMessage();
        assertEquals("REQ123", userRequest.getUserReqID());
        assertEquals("1", userRequest.getUserRequestType());
        assertEquals("testUser", userRequest.getUsername());
        assertEquals("testPass", userRequest.getPassword());
    }

    @Test
    void testParseValidUserResponse() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">" +
                "<UserRsp UserReqID=\"REQ123\" UserStat=\"1\" UserStatText=\"Zalogowano pomyślnie\" Username=\"testUser\"/>" +
                "</FIXML>";

        // When
        FixmlMessage message = FixmlParser.parse(xml);

        // Then
        assertNotNull(message);
        assertTrue(message.getMessage() instanceof UserResponse);
        UserResponse response = (UserResponse) message.getMessage();
        assertEquals("REQ123", response.getUserReqID());
        assertEquals("1", response.getUserStatus());
        assertEquals("Zalogowano pomyślnie", response.getUserStatusText());
        assertEquals("testUser", response.getUsername());
    }
    
    @Test
    void testParseWithLeadingWhitespace() throws JAXBException {
        // Given
        String xml = "  \n\t<FIXML v=\"5.0\">" +
                "<UserReq UserReqID=\"REQ123\" UserReqTyp=\"1\" Username=\"testUser\" Password=\"testPass\"/>" +
                "</FIXML>";

        // When
        FixmlMessage message = FixmlParser.parse(xml);

        // Then
        assertNotNull(message);
        assertTrue(message.getMessage() instanceof UserRequest);
    }

    @Test
    void testParseWithNullCharacters() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\">\u0000" +
                "<UserReq UserReqID=\"REQ123\" UserReqTyp=\"1\" Username=\"testUser\" Password=\"testPass\"/>" +
                "</FIXML>";

        // When
        FixmlMessage message = FixmlParser.parse(xml);

        // Then
        assertNotNull(message);
        assertTrue(message.getMessage() instanceof UserRequest);
    }

    @Test
    void testParseFromInputStream() throws JAXBException {
        // Given
        String xml = "<FIXML v=\"5.0\">" +
                "<UserReq UserReqID=\"REQ123\" UserReqTyp=\"1\" Username=\"testUser\" Password=\"testPass\"/>" +
                "</FIXML>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        // When
        FixmlMessage message = FixmlParser.parse(inputStream);

        // Then
        assertNotNull(message);
        assertTrue(message.getMessage() instanceof UserRequest);
    }
}