package com.krzysztofpk14.app.bossaapi.model.request;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Klasa reprezentująca zlecenie giełdowe.
 * Odpowiada tagowi Order w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderRequest extends BaseMessage {
    
    @XmlAttribute(name = "ID")
    private String orderId;
    
    @XmlAttribute(name = "ClOrdID")
    private String clientOrderId;
    
    @XmlAttribute(name = "Side")
    private String side;  // 1=kupno, 2=sprzedaż
    
    @XmlAttribute(name = "TmInForce")
    private String timeInForce;  // 0=dzienne, 4=do anulowania
    
    @XmlAttribute(name = "OrdTyp")
    private String orderType;  // 1=rynkowe, 2=limit
    
    @XmlAttribute(name = "Px")
    private String price;
    
    @XmlAttribute(name = "TransactTm")
    private String transactionTime;
    
    // Elementy podrzędne
    @XmlElement(name = "Instrmt")
    private Instrument instrument;
    
    @XmlElement(name = "OrdQty")
    private OrderQuantity orderQuantity;
    
    // Stałe dla stron zlecenia
    public static final String BUY = "1";
    public static final String SELL = "2";
    
    // Stałe dla typów zleceń
    public static final String MARKET = "1";
    public static final String LIMIT = "2";
    
    // Stałe dla ważności zleceń
    public static final String DAY = "0";
    public static final String GOOD_TILL_CANCEL = "4";
    
    // Konstruktory
    public OrderRequest() {
        this.transactionTime = generateTimestamp();
    }
    
    // Gettery i settery
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
    
    public String getSide() {
        return side;
    }
    
    public void setSide(String side) {
        this.side = side;
    }
    
    public String getTimeInForce() {
        return timeInForce;
    }
    
    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
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
    
    public String getTransactionTime() {
        return transactionTime;
    }
    
    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
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
    
    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }
    
    @Override
    public String getMessageType() {
        return "Order";
    }
    
    // Klasy wewnętrzne dla elementów podrzędnych
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "OrderInstrumentType", namespace = "com.krzysztofpk14.app.bossaapi.model.request")
    public static class Instrument {
        @XmlAttribute(name = "Sym")
        private String symbol;
        
        @XmlAttribute(name = "ID")
        private String id;
        
        @XmlAttribute(name = "IDSrc")
        private String idSource;
        
        @XmlAttribute(name = "CFI")
        private String cfi;
        
        // Gettery i settery
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
        
        public String getIdSource() {
            return idSource;
        }
        
        public void setIdSource(String idSource) {
            this.idSource = idSource;
        }
        
        public String getCfi() {
            return cfi;
        }
        
        public void setCfi(String cfi) {
            this.cfi = cfi;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ExecRptOrderQuantityType", namespace = "com.krzysztofpk14.app.bossaapi.model.request")
    public static class OrderQuantity {
        @XmlAttribute(name = "Qty")
        private String quantity;
        
        // Gettery i settery
        public String getQuantity() {
            return quantity;
        }
        
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }
    }
}