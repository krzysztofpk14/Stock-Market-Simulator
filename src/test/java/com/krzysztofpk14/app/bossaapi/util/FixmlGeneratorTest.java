package com.krzysztofpk14.app.bossaapi.util;

import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;
import com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest;
import com.krzysztofpk14.app.bossaapi.model.request.OrderRequest;
import com.krzysztofpk14.app.bossaapi.model.request.UserRequest;
import com.krzysztofpk14.app.bossaapi.model.response.BusinessMessageReject;
import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;
import com.krzysztofpk14.app.bossaapi.model.response.UserResponse;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FixmlGeneratorTest {

    @Test
    void testGenerateXmlFromFixmlMessage() throws JAXBException {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUserReqID("REQ123");
        userRequest.setUserRequestType(UserRequest.LOGIN);
        userRequest.setUsername("testUser");
        userRequest.setPassword("testPass");
        
        FixmlMessage fixmlMessage = new FixmlMessage(userRequest);

        // When
        String xml = FixmlGenerator.generateXml(fixmlMessage);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<FIXML"));
        assertTrue(xml.contains("v=\"5.0\""));
        assertTrue(xml.contains("<UserReq"));
        assertTrue(xml.contains("UserReqID=\"REQ123\""));
        assertTrue(xml.contains("UserReqTyp=\"1\""));
        assertTrue(xml.contains("Username=\"testUser\""));
        assertTrue(xml.contains("Password=\"testPass\""));
    }

    @Test
    void testGenerateXmlFromBaseMessage() throws JAXBException {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUserReqID("REQ123");
        userRequest.setUserRequestType(UserRequest.LOGIN);
        userRequest.setUsername("testUser");
        userRequest.setPassword("testPass");

        // When
        String xml = FixmlGenerator.generateXml(userRequest);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<FIXML"));
        assertTrue(xml.contains("<UserReq"));
        assertTrue(xml.contains("UserReqID=\"REQ123\""));
    }

    @Test
    void testCreateFixmlMessage() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUserReqID("REQ123");

        // When
        FixmlMessage fixmlMessage = FixmlGenerator.createFixmlMessage(userRequest);

        // Then
        assertNotNull(fixmlMessage);
        assertEquals("5.0", fixmlMessage.getVersion());
        assertEquals("20080317", fixmlMessage.getRevision());
        assertEquals("20080314", fixmlMessage.getServicepack());
        assertEquals(userRequest, fixmlMessage.getMessage());
    }

    @Test
    void testGenerateXmlWithUserResponse() throws JAXBException {
        // Given
        UserResponse response = new UserResponse();
        response.setUserReqID("REQ123");
        response.setUserStatus(UserResponse.LOGGED_IN);
        response.setUserStatusText("Zalogowano pomyślnie");

        // When
        String xml = FixmlGenerator.generateXml(response);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<UserRsp"));
        assertTrue(xml.contains("UserReqID=\"REQ123\""));
        assertTrue(xml.contains("UserStat=\"1\""));
        assertTrue(xml.contains("UserStatText=\"Zalogowano pomyślnie\""));
    }

    @Test
    void testGenerateXmlWithOrderRequest() throws JAXBException {
        // Given
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("ORD123");
        order.setSide(OrderRequest.BUY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol("KGHM");
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity quantity = new OrderRequest.OrderQuantity();
        quantity.setQuantity("100");
        order.setOrderQuantity(quantity);

        // When
        String xml = FixmlGenerator.generateXml(order);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<Order"));
        assertTrue(xml.contains("ClOrdID=\"ORD123\""));
        assertTrue(xml.contains("Side=\"1\""));
        assertTrue(xml.contains("<Instrmt"));
        assertTrue(xml.contains("Sym=\"KGHM\""));
        assertTrue(xml.contains("<OrdQty"));
        assertTrue(xml.contains("Qty=\"100\""));
    }

    @Test
    void testGenerateXmlWithExecutionReport() throws JAXBException {
        // Given
        ExecutionReport report = new ExecutionReport();
        report.setOrderId("ORD123");
        report.setExecutionType(ExecutionReport.NEW);
        report.setOrderStatus(ExecutionReport.NEW);
        
        ExecutionReport.Instrument instrument = new ExecutionReport.Instrument();
        instrument.setSymbol("KGHM");
        report.setInstrument(instrument);

        // When
        String xml = FixmlGenerator.generateXml(report);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<ExecRpt"));
        assertTrue(xml.contains("OrdID=\"ORD123\""));
        assertTrue(xml.contains("ExecTyp=\"0\""));
        assertTrue(xml.contains("OrdStatus=\"0\""));
        assertTrue(xml.contains("<Instrmt"));
        assertTrue(xml.contains("Sym=\"KGHM\""));
    }

    @Test
    void testGenerateXmlWithMarketDataRequest() throws JAXBException {
        // Given
        MarketDataRequest request = new MarketDataRequest();
        request.setRequestId("MDR123");
        request.setSubscriptionRequestType(MarketDataRequest.SNAPSHOT);
        
        MarketDataRequest.InstrumentMarketDataRequest instrumentRequest = new MarketDataRequest.InstrumentMarketDataRequest();
        MarketDataRequest.Instrument instrument = new MarketDataRequest.Instrument();
        instrument.setSymbol("KGHM");
        instrumentRequest.setInstrument(instrument);

        // Inicjalizacja z istniejącymi obiektami
        List<MarketDataRequest.InstrumentMarketDataRequest> instruments = new ArrayList<>(Arrays.asList(
            instrumentRequest
        ));
        request.setInstruments(instruments);

        // When
        String xml = FixmlGenerator.generateXml(request);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<MktDataReq"));
        assertTrue(xml.contains("ReqID=\"MDR123\""));
        assertTrue(xml.contains("SubReqTyp=\"0\""));
        assertTrue(xml.contains("<InstrmtMDReq"));
        assertTrue(xml.contains("<Instrmt"));
        assertTrue(xml.contains("Sym=\"KGHM\""));
    }

    @Test
    void testGenerateXmlWithBusinessMessageReject() throws JAXBException {
        // Given
        BusinessMessageReject reject = new BusinessMessageReject();
        reject.setRefMsgType("Order");
        reject.setBusinessRejectReason(BusinessMessageReject.OTHER);
        reject.setText("Nieznany błąd");

        // When
        String xml = FixmlGenerator.generateXml(reject);

        // ThenRefMsgType
        assertNotNull(xml);
        System.out.println(xml); // For debugging purposes
        assertTrue(xml.contains("<BizMsgRej"));
        assertTrue(xml.contains("BizRejRsn"));
        assertTrue(xml.contains("RefMsgType"));
        assertTrue(xml.contains("Text"));
    }

    @Test
    void testRoundTripConversion() throws JAXBException {
        // Given
        UserRequest originalRequest = new UserRequest();
        originalRequest.setUserReqID("REQ123");
        originalRequest.setUserRequestType(UserRequest.LOGIN);
        originalRequest.setUsername("testUser");
        originalRequest.setPassword("testPass");

        // When
        String xml = FixmlGenerator.generateXml(originalRequest);
        FixmlMessage parsedMessage = FixmlParser.parse(xml);
        UserRequest parsedRequest = (UserRequest) parsedMessage.getMessage();

        // Then
        assertNotNull(parsedRequest);
        assertEquals(originalRequest.getUserReqID(), parsedRequest.getUserReqID());
        assertEquals(originalRequest.getUserRequestType(), parsedRequest.getUserRequestType());
        assertEquals(originalRequest.getUsername(), parsedRequest.getUsername());
        assertEquals(originalRequest.getPassword(), parsedRequest.getPassword());
    }
}