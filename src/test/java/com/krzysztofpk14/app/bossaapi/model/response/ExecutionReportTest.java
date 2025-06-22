package com.krzysztofpk14.app.bossaapi.model.response;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ExecutionReportTest {

    @Test
    void testDefaultConstructor() {
        // When
        ExecutionReport report = new ExecutionReport();
        
        // Then
        assertNull(report.getReportId());
        assertNull(report.getOrderId());
        assertNull(report.getClientOrderId());
        assertNull(report.getExecutionType());
        assertNull(report.getOrderStatus());
        assertNull(report.getSide());
        assertNull(report.getOrderType());
        assertNull(report.getPrice());
        assertNull(report.getLastPrice());
        assertNull(report.getLastQuantity());
        assertNull(report.getCumulativeQuantity());
        assertNull(report.getAveragePrice());
        assertNull(report.getLeavesQuantity());
        assertNull(report.getTransactionTime());
        assertNull(report.getText());
        assertNull(report.getInstrument());
        assertNull(report.getOrderQuantity());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ExecutionReport report = new ExecutionReport();
        String reportId = "ER-12345";
        String orderId = "O-12345";
        String clientOrderId = "CO-12345";
        String executionType = ExecutionReport.NEW;
        String orderStatus = ExecutionReport.NEW_ORDER;
        
        // When
        report.setReportId(reportId);
        report.setOrderId(orderId);
        report.setClientOrderId(clientOrderId);
        report.setExecutionType(executionType);
        report.setOrderStatus(orderStatus);
        
        // Then
        assertEquals(reportId, report.getReportId());
        assertEquals(orderId, report.getOrderId());
        assertEquals(clientOrderId, report.getClientOrderId());
        assertEquals(executionType, report.getExecutionType());
        assertEquals(orderStatus, report.getOrderStatus());
    }

    @Test
    void testExecTypeConstants() {
        // Then
        assertEquals("0", ExecutionReport.NEW);
        assertEquals("F", ExecutionReport.TRANSACTION);
        assertEquals("4", ExecutionReport.CANCELING);
        assertEquals("E", ExecutionReport.MODIFICATION);
        assertEquals("6", ExecutionReport.DURING_CANCELATION);
        assertEquals("8", ExecutionReport.REJECTED);
        assertEquals("I", ExecutionReport.ORDER_STATUS);
    }

    @Test
    void testOrderStatusConstants() {
        // Then
        assertEquals("0", ExecutionReport.NEW_ORDER);
        assertEquals("C", ExecutionReport.ARCHIVED);
        assertEquals("E", ExecutionReport.DURING_MODIFICATION);
        assertEquals("1", ExecutionReport.ACTIVE);
        assertEquals("2", ExecutionReport.DONE);
        assertEquals("4", ExecutionReport.CANCELED);
        assertEquals("6", ExecutionReport.ORDER_DURING_CANCELATION);
        assertEquals("8", ExecutionReport.REJECTED_ORDER);
    }

    @Test
    void testMessageType() {
        // Given
        ExecutionReport report = new ExecutionReport();
        
        // When/Then
        assertEquals("ExecRpt", report.getMessageType());
    }
    
    @Test
    void testMessageId() {
        // Given
        ExecutionReport report = new ExecutionReport();
        String orderId = "O-12345";

        
        // When
        report.setClientOrderId(orderId);
        
        // Then
        assertEquals(orderId, report.getMessageId());
    }
}
