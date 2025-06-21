package com.krzysztofpk14.app.bossaapi.model.response;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import java.util.List;

/**
 * Klasa reprezentująca odpowiedź zawierającą dane rynkowe.
 * Odpowiada tagowi MktDataSnap w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MarketDataResponse extends BaseMessage {
    
    @XmlAttribute(name = "ReqID")
    private String requestId;
    
    @XmlElement(name = "Instrmt")
    private Instrument instrument;
    
    @XmlElement(name = "MktDataGrp")
    private List<MarketDataGroup> marketDataGroups;

    @XmlElement(name = "RespType")
    private String responseType;  // Typ odpowiedzi, np. "0" dla pełnego odświeżenia


    // Stałe dla typów danych rynkowych
    public static final String TRADE = "0";
    public static final String FULL_REFRESH = "1";
    public static final String UNSOLICITED_INDICATOR = "2";
    
    // Konstruktory
    public MarketDataResponse() {
    }
    
    // Gettery i settery
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Instrument getInstrument() {
        return instrument;
    }
    
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    
    public List<MarketDataGroup> getMarketDataGroups() {
        return marketDataGroups;
    }
    
    public void setMarketDataGroups(List<MarketDataGroup> marketDataGroups) {
        this.marketDataGroups = marketDataGroups;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
    
    @Override
    public String getMessageType() {
        return "MktDataSnap";
    }

    @Override
    public String getMessageId() {
        return requestId;
    }
    
    // Klasy wewnętrzne dla elementów podrzędnych
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MktDataRespInstrument", namespace ="com.krzysztofpk14.app.bossaapi.model.response")
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
    public static class MarketDataGroup {
        @XmlAttribute(name = "MDEntryTyp")
        private String marketDataEntryType;  // 0=bid, 1=offer, 2=trade, etc.
        
        @XmlAttribute(name = "MDEntryPx")
        private String price;
        
        @XmlAttribute(name = "MDEntrySize")
        private String size;
        
        @XmlAttribute(name = "MDEntryTime")
        private String time;
        
        @XmlAttribute(name = "MDEntryID")
        private String entryId;
        
        // Stałe dla typów danych rynkowych
        public static final String BID = "0";
        public static final String OFFER = "1";
        public static final String TRADE = "2";
        public static final String OPEN_PRICE = "4";
        public static final String CLOSE_PRICE = "5";
        public static final String HIGH_PRICE = "7";
        public static final String LOW_PRICE = "8";
        
        public String getMarketDataEntryType() {
            return marketDataEntryType;
        }
        
        public void setMarketDataEntryType(String marketDataEntryType) {
            this.marketDataEntryType = marketDataEntryType;
        }
        
        public String getPrice() {
            return price;
        }
        
        public void setPrice(String price) {
            this.price = price;
        }
        
        public String getSize() {
            return size;
        }
        
        public void setSize(String size) {
            this.size = size;
        }
        
        public String getTime() {
            return time;
        }
        
        public void setTime(String time) {
            this.time = time;
        }
        
        public String getEntryId() {
            return entryId;
        }
        
        public void setEntryId(String entryId) {
            this.entryId = entryId;
        }
    }
}