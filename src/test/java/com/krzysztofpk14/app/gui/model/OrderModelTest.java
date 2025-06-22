package com.krzysztofpk14.app.gui.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport;

/**
 * Unit tests for the OrderModel class.
 */
public class OrderModelTest {
    
    private static final String TEST_ORDER_ID = "ORD12345";
    private static final String TEST_SYMBOL = "KGHM";
    private static final String TEST_SIDE = "1"; // BUY
    private static final String TEST_PRICE = "150.75";
    private static final String TEST_QUANTITY = "100";
    private static final String TEST_STATUS = "0"; // NEW_ORDER
    private static final String TEST_EXEC_TYPE = "0"; // NEW
    private static final String TEST_TIME = "20250621-14:30:45.123";
    
    private OrderModel model;
    
    @BeforeEach
    public void setUp() {
        model = new OrderModel(
            TEST_ORDER_ID, 
            TEST_SYMBOL, 
            TEST_SIDE, 
            TEST_PRICE, 
            TEST_QUANTITY, 
            TEST_STATUS, 
            TEST_EXEC_TYPE, 
            TEST_TIME
        );
    }
    
    @Test
    public void testConstructor() {
        assertEquals(TEST_ORDER_ID, model.getOrderId(), "Order ID should match");
        assertEquals(TEST_SYMBOL, model.getSymbol(), "Symbol should match");
        assertEquals("BUY", model.getSide(), "Side '1' should be converted to 'BUY'");
        assertEquals(TEST_PRICE, model.getPrice(), "Price should match");
        assertEquals(TEST_QUANTITY, model.getQuantity(), "Quantity should match");
        assertEquals("New Order", model.getStatus(), "Status '0' should be converted to 'New Order'");
        assertEquals("New", model.getExecType(), "Exec type '0' should be converted to 'New'");
        assertEquals(TEST_TIME, model.getTime(), "Time should match");
    }
    
    @ParameterizedTest
    @CsvSource({
        "1, BUY",
        "2, SELL", 
        "X, X"  // Unknown values should remain as-is
    })
    public void testSideConversion(String side, String expected) {
        // Create a new model with the test side
        OrderModel testModel = new OrderModel("", "", side, "", "", "", "", "");
        
        // Verify side conversion
        assertEquals(expected, testModel.getSide(), "Side should be converted correctly");
    }
    
    @Test
    public void testOrderStatusConversion() {
        // Test each order status code conversion
        
        // Setup with null status (should convert to "Unknown")
        OrderModel nullModel = new OrderModel("", "", "", "", "", null, "", "");
        assertEquals("Unknown", nullModel.getStatus(), "Null status should convert to 'Unknown'");
        
        // Test each status code
        assertEquals("New Order", convertStatus(ExecutionReport.NEW_ORDER));
        assertEquals("Active", convertStatus(ExecutionReport.ACTIVE));
        assertEquals("Done", convertStatus(ExecutionReport.DONE));
        assertEquals("Canceled", convertStatus(ExecutionReport.CANCELED));
        assertEquals("Order Rejected", convertStatus(ExecutionReport.REJECTED_ORDER));
        assertEquals("Archived", convertStatus(ExecutionReport.ARCHIVED));
        assertEquals("During modification", convertStatus(ExecutionReport.DURING_MODIFICATION));
        assertEquals("Canceling", convertStatus(ExecutionReport.ORDER_DURING_CANCELATION));
        assertEquals("UNKNOWN_STATUS", convertStatus("UNKNOWN_STATUS"));
    }
    
    @Test
    public void testExecutionTypeConversion() {
        // Test each execution type code conversion
        
        // Setup with null exec type (should convert to "Unknown")
        OrderModel nullModel = new OrderModel("", "", "", "", "", "", null, "");
        assertEquals("Unknown", nullModel.getExecType(), "Null exec type should convert to 'Unknown'");
        
        // Test each exec type code
        assertEquals("New", convertExecType(ExecutionReport.NEW));
        assertEquals("Trade", convertExecType(ExecutionReport.TRANSACTION));
        assertEquals("Canceled", convertExecType(ExecutionReport.CANCELING));
        assertEquals("Modified", convertExecType(ExecutionReport.MODIFICATION));
        assertEquals("Canceling", convertExecType(ExecutionReport.DURING_CANCELATION));
        assertEquals("Rejected", convertExecType(ExecutionReport.REJECTED));
        assertEquals("Order Status", convertExecType(ExecutionReport.ORDER_STATUS));
        assertEquals("UNKNOWN_EXEC", convertExecType("UNKNOWN_EXEC"));
    }
    
    @Test
    public void testPropertiesBinding() {
        // Test property binding functionality
        
        OrderModel boundModel = new OrderModel("", "", "", "", "", "", "", "");
        boundModel.orderIdProperty().bind(model.orderIdProperty());
        boundModel.symbolProperty().bind(model.symbolProperty());
        
        // Check initial binding
        assertEquals(TEST_ORDER_ID, boundModel.getOrderId());
        assertEquals(TEST_SYMBOL, boundModel.getSymbol());
        
        // Update original values
        model.setOrderId("NEW_ID");
        model.setSymbol("PKO");
        
        // Check binding propagation
        assertEquals("NEW_ID", boundModel.getOrderId());
        assertEquals("PKO", boundModel.getSymbol());
    }
    
    @Test
    public void testSetters() {
        // Test all setters
        model.setOrderId("ORD54321");
        model.setSymbol("PKO");
        model.setSide("SELL");
        model.setPrice("200.00");
        model.setQuantity("50");
        model.setStatus(ExecutionReport.DONE);
        model.setExecType(ExecutionReport.TRANSACTION);
        model.setTime("20250621-15:00:00.000");
        
        // Verify all values were set correctly
        assertEquals("ORD54321", model.getOrderId());
        assertEquals("PKO", model.getSymbol());
        assertEquals("SELL", model.getSide());
        assertEquals("200.00", model.getPrice());
        assertEquals("50", model.getQuantity());
        assertEquals("Done", model.getStatus());
        assertEquals("Trade", model.getExecType());
        assertEquals("20250621-15:00:00.000", model.getTime());
    }
    
    /**
     * Helper method to convert a status code through the model
     */
    private String convertStatus(String status) {
        OrderModel testModel = new OrderModel("", "", "", "", "", status, "", "");
        return testModel.getStatus();
    }
    
    /**
     * Helper method to convert an exec type code through the model
     */
    private String convertExecType(String execType) {
        OrderModel testModel = new OrderModel("", "", "", "", "", "", execType, "");
        return testModel.getExecType();
    }
}
