package com.krzysztofpk14.app.bossaapi.model.request;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import jakarta.xml.bind.annotation.*;

/**
 * Klasa reprezentująca żądanie listy bezpieczeństw.
 * Odpowiada tagowi SecListReq w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityListRequest extends BaseMessage {
    
    @XmlAttribute(name = "ReqID")
    private String requestId;
    
    @XmlAttribute(name = "SecurityReqTyp")
    private String securityRequestType;
    
    @XmlAttribute(name = "SubscriptionReqTyp")
    private String subscriptionRequestType;
    
    @XmlElement(name = "Instrmt")
    private Instrument instrument;
    
    @XmlElement(name = "InstrmtLegGrp")
    private InstrumentLegGroup instrumentLegGroup;

    // Stałe dla SecurityListRequestType
    public static final String ONE_INSTRMNT = "0"; // Żądanie pojedynczego instrumentu
    public static final String ONE_INSTRMNT_TYPE = "1"; //  lista jednego typu instrumentu
    public static final String ALL_INSTRMNT = "4"; // cała lista
    public static final String MARKET_TYPE = "5"; // lista dla jednego kodu rynku

    // Stałe MarketID
    public static final String CASH_MARKET  = "NM"; // Rynek kasowy
    public static final String DERIVATIVES_MARKET = "DN"; // Rynek instrumentów pochodnych
    
    /**
     * Konstruktor domyślny.
     */
    public SecurityListRequest() {
    }
    
    /**
     * Konstruktor z parametrami.
     * 
     * @param requestId ID żądania
     * @param securityRequestType Typ żądania (0-6)
     */
    public SecurityListRequest(String requestId, String securityRequestType) {
        this.requestId = requestId;
        this.securityRequestType = securityRequestType;
    }
    
    /**
     * Konstruktor tworzączy żądanie po symbolu.
     * 
     * @param requestId ID żądania
     * @param symbol Symbol instrumentu
     */
    public SecurityListRequest(String requestId, String symbol, String subscriptionType) {
        this.requestId = requestId;
        this.securityRequestType = ONE_INSTRMNT;
        this.subscriptionRequestType = subscriptionType;
        
        this.instrument = new Instrument();
        this.instrument.setSymbol(symbol);
    }
    
    // Gettery i settery
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getSecurityRequestType() {
        return securityRequestType;
    }
    
    public void setSecurityRequestType(String securityRequestType) {
        this.securityRequestType = securityRequestType;
    }
    
    public String getSubscriptionRequestType() {
        return subscriptionRequestType;
    }
    
    public void setSubscriptionRequestType(String subscriptionRequestType) {
        this.subscriptionRequestType = subscriptionRequestType;
    }
    
    public Instrument getInstrument() {
        return instrument;
    }
    
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    
    public InstrumentLegGroup getInstrumentLegGroup() {
        return instrumentLegGroup;
    }
    
    public void setInstrumentLegGroup(InstrumentLegGroup instrumentLegGroup) {
        this.instrumentLegGroup = instrumentLegGroup;
    }
    
    @Override
    public String getMessageType() {
        return "SecListReq";
    }

    @Override
    public String getMessageId() {
        return requestId;
    }
    
    /**
     * Klasa wewnętrzna reprezentująca instrument.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SecListReqInstrument", namespace = "com.krzysztofpk14.bossaapi.request")
    public static class Instrument {
        @XmlAttribute(name = "Sym")
        private String symbol;
        
        @XmlAttribute(name = "ID")
        private String id;
        
        @XmlAttribute(name = "IDSrc")
        private String idSource;
        
        @XmlAttribute(name = "Desc")
        private String description;
        
        @XmlAttribute(name = "CFI")
        private String cfi;
        
        @XmlAttribute(name = "SecTyp")
        private String securityType;
        
        @XmlAttribute(name = "MMY")
        private String maturityMonthYear;
        
        @XmlAttribute(name = "ProdCmplx")
        private String productComplex;
        
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
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getCfi() {
            return cfi;
        }
        
        public void setCfi(String cfi) {
            this.cfi = cfi;
        }
        
        public String getSecurityType() {
            return securityType;
        }
        
        public void setSecurityType(String securityType) {
            this.securityType = securityType;
        }
        
        public String getMaturityMonthYear() {
            return maturityMonthYear;
        }
        
        public void setMaturityMonthYear(String maturityMonthYear) {
            this.maturityMonthYear = maturityMonthYear;
        }
        
        public String getProductComplex() {
            return productComplex;
        }
        
        public void setProductComplex(String productComplex) {
            this.productComplex = productComplex;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca grupę instrumentów.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SecListReqInstrumentLegGroup", namespace = "com.krzysztofpk14.bossaapi.request")
    public static class InstrumentLegGroup {
        @XmlElement(name = "InstrmtLeg")
        private InstrumentLeg instrumentLeg;
        
        public InstrumentLeg getInstrumentLeg() {
            return instrumentLeg;
        }
        
        public void setInstrumentLeg(InstrumentLeg instrumentLeg) {
            this.instrumentLeg = instrumentLeg;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca nogę instrumentu (dla instrumentów złożonych).
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SecListReqInstrumentLeg", namespace = "com.krzysztofpk14.bossaapi.request")
    public static class InstrumentLeg {
        @XmlAttribute(name = "Sym")
        private String symbol;
        
        @XmlAttribute(name = "ID")
        private String id;
        
        @XmlAttribute(name = "IDSrc")
        private String idSource;
        
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
    }
}