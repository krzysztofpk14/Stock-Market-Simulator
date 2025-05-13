package com.krzysztofpk14.app.bossaapi.model.response;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;

/**
 * Klasa reprezentująca raport wykonania zlecenia.
 * Odpowiada tagowi ExecRpt w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExecutionReport extends BaseMessage {
    
    @XmlAttribute(name = "ID")
    private String reportId;
    
    @XmlAttribute(name = "OrdID")
    private String orderId;
    
    @XmlAttribute(name = "ClOrdID")
    private String clientOrderId;
    
    @XmlAttribute(name = "ExecTyp")
    private String executionType;
    
    @XmlAttribute(name = "OrdStatus")
    private String orderStatus;
    
    @XmlAttribute(name = "Side")
    private String side;
    
    @XmlAttribute(name = "OrdTyp")
    private String orderType;
    
    @XmlAttribute(name = "Px")
    private String price;
    
    @XmlAttribute(name = "LastPx")
    private String lastPrice;
    
    @XmlAttribute(name = "LastQty")
    private String lastQuantity;
    
    @XmlAttribute(name = "CumQty")
    private String cumulativeQuantity;
    
    @XmlAttribute(name = "AvgPx")
    private String averagePrice;
    
    @XmlAttribute(name = "LeavesQty")
    private String leavesQuantity;
    
    @XmlAttribute(name = "TxnTm")
    private String transactionTime;
    
    @XmlAttribute(name = "Text")
    private String text;
    
    @XmlElement(name = "Instrmt")
    private Instrument instrument;
    
    @XmlElement(name = "OrdQty")
    private OrderQuantity orderQuantity;
    
    // Stałe dla typów wykonania
    public static final String NEW = "0";
    public static final String PARTIALLY_FILLED = "1";
    public static final String FILLED = "2";
    public static final String CANCELED = "4";
    public static final String REJECTED = "8";
    
    // Konstruktory
    public ExecutionReport() {
    }
    
    // Gettery i settery
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getClientOrderId() {
        return clientOrderId;
    }
    
    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }
    
    public String getExecutionType() {
        return executionType;
    }
    
    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }
    
    public String getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public String getSide() {
        return side;
    }
    
    public void setSide(String side) {
        this.side = side;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public String getLastPrice() {
        return lastPrice;
    }
    
    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }
    
    public String getLastQuantity() {
        return lastQuantity;
    }
    
    public void setLastQuantity(String lastQuantity) {
        this.lastQuantity = lastQuantity;
    }
    
    public String getCumulativeQuantity() {
        return cumulativeQuantity;
    }
    
    public void setCumulativeQuantity(String cumulativeQuantity) {
        this.cumulativeQuantity = cumulativeQuantity;
    }
    
    public String getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(String averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public String getLeavesQuantity() {
        return leavesQuantity;
    }
    
    public void setLeavesQuantity(String leavesQuantity) {
        this.leavesQuantity = leavesQuantity;
    }
    
    public String getTransactionTime() {
        return transactionTime;
    }
    
    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public Instrument getInstrument() {
        return instrument;
    }
    
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    
    public OrderQuantity getOrderQuantity() {
        return orderQuantity;
    }
    
    public void setOrderQuantity(OrderQuantity orderQuantity) {
        this.orderQuantity = orderQuantity;
    }
    
    @Override
    public String getMessageType() {
        return "ExecRpt";
    }
    
    // Klasy wewnętrzne dla elementów podrzędnych
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ExecRptInstrumentType", namespace = "com.krzysztofpk14.app.bossaapi.model.response")
    public static class Instrument {
        @XmlAttribute(name = "Sym")
        private String symbol;
        
        @XmlAttribute(name = "ID")
        private String id;
        
        public String getSymbol() {
            return symbol;
        }
        
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ExecRptOrderQuantityType", namespace = "com.krzysztofpk14.app.bossaapi.model.response")
    public static class OrderQuantity {
        @XmlAttribute(name = "Qty")
        private String quantity;
        
        public String getQuantity() {
            return quantity;
        }
        
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }
    }
}