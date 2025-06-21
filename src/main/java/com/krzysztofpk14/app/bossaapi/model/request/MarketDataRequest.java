package com.krzysztofpk14.app.bossaapi.model.request;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca żądanie danych rynkowych.
 * Odpowiada tagowi MktDataReq w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MarketDataRequest extends BaseMessage {
    
    @XmlAttribute(name = "ReqID")
    private String requestId;
    
    @XmlAttribute(name = "SubReqTyp")
    private String subscriptionRequestType;  // 0=snapshot, 1=subscribe, 2=unsubscribe
    
    @XmlAttribute(name = "MktDepth")
    private String marketDepth;  // 0=full book, 1=top of book
    
    // Elementy podrzędne
    @XmlElement(name = "InstrmtMDReq")
    private List<InstrumentMarketDataRequest> instruments;
    
    // Stałe dla typów żądań
    public static final String SNAPSHOT = "0";
    public static final String SUBSCRIBE = "1";
    public static final String UNSUBSCRIBE = "2";
    
    // Konstruktory
    public MarketDataRequest() {
        instruments = new ArrayList<>();
    }
    
    // Metody
    public void addInstrument(String symbol) {
        InstrumentMarketDataRequest instr = new InstrumentMarketDataRequest();
        Instrument instrument = new Instrument();
        instrument.setSymbol(symbol);
        instr.setInstrument(instrument);
        instruments.add(instr);
    }
    
    // Gettery i settery
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getSubscriptionRequestType() {
        return subscriptionRequestType;
    }
    
    public void setSubscriptionRequestType(String subscriptionRequestType) {
        this.subscriptionRequestType = subscriptionRequestType;
    }
    
    public String getMarketDepth() {
        return marketDepth;
    }
    
    public void setMarketDepth(String marketDepth) {
        this.marketDepth = marketDepth;
    }
    
    public List<InstrumentMarketDataRequest> getInstruments() {
        return instruments;
    }
    
    public void setInstruments(List<InstrumentMarketDataRequest> instruments) {
        this.instruments = instruments;
    }
    
    @Override
    public String getMessageType() {
        return "MktDataReq";
    }

    @Override
    public String getMessageId() {
        return requestId;
    }
    
    // Klasy wewnętrzne
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class InstrumentMarketDataRequest {
        @XmlElement(name = "Instrmt")
        private Instrument instrument;
        
        public Instrument getInstrument() {
            return instrument;
        }
        
        public void setInstrument(Instrument instrument) {
            this.instrument = instrument;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MktDataReqInstrument", namespace = "com.krzysztofpk14.app.bossaapi.model.request")
    public static class Instrument {
        @XmlAttribute(name = "Sym")
        private String symbol;
        
        public String getSymbol() {
            return symbol;
        }
        
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
    }
}