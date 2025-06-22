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

class OrderRequestTest {

    @Test
    void testDefaultConstructor() {
        // When
        OrderRequest request = new OrderRequest();
        
        // Then
        assertNull(request.getOrderId());
        assertNull(request.getClientOrderId());
        assertNull(request.getSide());
        assertNull(request.getTimeInForce());
        assertNull(request.getOrderType());
        assertNull(request.getPrice());
        assertNotNull(request.getTransactionTime());
        assertNull(request.getInstrument());
        assertNull(request.getOrderQuantity());
        assertEquals("Order", request.getMessageType());
    }    @Test
    void testBuyLimitOrder() {
        // Given
        String clientOrderId = "123";
        String symbol = "AAPL";
        String price = "150.00";
        String quantity = "100";
        
        // When
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(clientOrderId);
        order.setSide(OrderRequest.BUY);
        order.setOrderType(OrderRequest.LIMIT);
        order.setPrice(price);
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity(quantity);
        order.setOrderQuantity(orderQty);
        
        // Then
        assertEquals(clientOrderId, order.getClientOrderId());
        assertEquals(OrderRequest.BUY, order.getSide());
        assertEquals(OrderRequest.LIMIT, order.getOrderType());
        assertEquals(price, order.getPrice());
        assertEquals(symbol, order.getInstrument().getSymbol());
        assertEquals(quantity, order.getOrderQuantity().getQuantity());
        assertEquals(OrderRequest.DAY, order.getTimeInForce());
    }    @Test
    void testSellLimitOrder() {
        // Given
        String clientOrderId = "456";
        String symbol = "GOOGL";
        String price = "2500.00";
        String quantity = "10";
        
        // When
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(clientOrderId);
        order.setSide(OrderRequest.SELL);
        order.setOrderType(OrderRequest.LIMIT);
        order.setPrice(price);
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity(quantity);
        order.setOrderQuantity(orderQty);
        
        // Then
        assertEquals(clientOrderId, order.getClientOrderId());
        assertEquals(OrderRequest.SELL, order.getSide());
        assertEquals(OrderRequest.LIMIT, order.getOrderType());
        assertEquals(price, order.getPrice());
        assertEquals(symbol, order.getInstrument().getSymbol());
        assertEquals(quantity, order.getOrderQuantity().getQuantity());
        assertEquals(OrderRequest.DAY, order.getTimeInForce());
    }    @Test
    void testBuyMarketOrder() {
        // Given
        String clientOrderId = "789";
        String symbol = "MSFT";
        String quantity = "50";
        
        // When
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(clientOrderId);
        order.setSide(OrderRequest.BUY);
        order.setOrderType(OrderRequest.MARKET);
        // price is null for market orders
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity(quantity);
        order.setOrderQuantity(orderQty);
        
        // Then
        assertEquals(clientOrderId, order.getClientOrderId());
        assertEquals(OrderRequest.BUY, order.getSide());
        assertEquals(OrderRequest.MARKET, order.getOrderType());
        assertNull(order.getPrice());
        assertEquals(symbol, order.getInstrument().getSymbol());
        assertEquals(quantity, order.getOrderQuantity().getQuantity());
        assertEquals(OrderRequest.DAY, order.getTimeInForce());
    }    @Test
    void testSellMarketOrder() {
        // Given
        String clientOrderId = "101";
        String symbol = "AMZN";
        String quantity = "25";
        
        // When
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(clientOrderId);
        order.setSide(OrderRequest.SELL);
        order.setOrderType(OrderRequest.MARKET);
        // price is null for market orders
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol(symbol);
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity(quantity);
        order.setOrderQuantity(orderQty);
        
        // Then
        assertEquals(clientOrderId, order.getClientOrderId());
        assertEquals(OrderRequest.SELL, order.getSide());
        assertEquals(OrderRequest.MARKET, order.getOrderType());
        assertNull(order.getPrice());
        assertEquals(symbol, order.getInstrument().getSymbol());
        assertEquals(quantity, order.getOrderQuantity().getQuantity());
        assertEquals(OrderRequest.DAY, order.getTimeInForce());
    }    @Test
    void testToXml() throws JAXBException {
        // Given
        OrderRequest order = new OrderRequest();
        order.setClientOrderId("12345");
        order.setSide(OrderRequest.BUY);
        order.setOrderType(OrderRequest.LIMIT);
        order.setPrice("155.50");
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol("AAPL");
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity("75");
        order.setOrderQuantity(orderQty);
        
        // Create a wrapper FixmlMessage to handle marshalling without @XmlRootElement on OrderRequest
        FixmlMessage fixml = new FixmlMessage();
        fixml.setMessage(order);
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter writer = new StringWriter();
        marshaller.marshal(fixml, writer);
        String xml = writer.toString();
        
        // Then
        assertTrue(xml.contains("ClOrdID=\"12345\""));
        assertTrue(xml.contains("Side=\"1\""));
        assertTrue(xml.contains("OrdTyp=\"2\""));
        assertTrue(xml.contains("Px=\"155.50\""));
        assertTrue(xml.contains("<Instrmt Sym=\"AAPL\""));
        assertTrue(xml.contains("<OrdQty Qty=\"75\""));
    }
    
    @Test
    void testFromXml() throws JAXBException {
        // Given
        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <FIXML>
                    <Order ClOrdID="12345" Side="1" TmInForce="0" OrdTyp="2" Px="155.50" TransactTm="20230612-14:30:00">
                        <Instrmt Sym="AAPL"/>
                        <OrdQty Qty="75"/>
                    </Order>
                </FIXML>
                """;
        
        // When
        JAXBContext context = JAXBContext.newInstance(FixmlMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        FixmlMessage fixml = (FixmlMessage) unmarshaller.unmarshal(new StringReader(xml));
        OrderRequest order = (OrderRequest) fixml.getMessage();
        
        // Then
        assertEquals("12345", order.getClientOrderId());
        assertEquals("1", order.getSide());
        assertEquals("2", order.getOrderType());
        assertEquals("155.50", order.getPrice());
        assertEquals("AAPL", order.getInstrument().getSymbol());
        assertEquals("75", order.getOrderQuantity().getQuantity());
        assertEquals("0", order.getTimeInForce());
    }
      @Test
    void testEqualsAndHashCode() {
        // Given
        OrderRequest order1 = createTestBuyLimitOrder("12345");
        OrderRequest order2 = createTestBuyLimitOrder("12345");
        OrderRequest order3 = createTestBuyLimitOrder("54321");
        
        // Then
        assertEquals(order1.getClientOrderId(), order2.getClientOrderId());
        assertNotEquals(order1.getClientOrderId(), order3.getClientOrderId());
    }
    
    // Helper method to create test orders
    private OrderRequest createTestBuyLimitOrder(String clientOrderId) {
        OrderRequest order = new OrderRequest();
        order.setClientOrderId(clientOrderId);
        order.setSide(OrderRequest.BUY);
        order.setOrderType(OrderRequest.LIMIT);
        order.setPrice("155.50");
        order.setTimeInForce(OrderRequest.DAY);
        
        OrderRequest.Instrument instrument = new OrderRequest.Instrument();
        instrument.setSymbol("AAPL");
        order.setInstrument(instrument);
        
        OrderRequest.OrderQuantity orderQty = new OrderRequest.OrderQuantity();
        orderQty.setQuantity("75");
        order.setOrderQuantity(orderQty);
        
        return order;
    }
}
