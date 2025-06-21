package com.krzysztofpk14.app.bossaapi.model.response;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca odpowiedź z listą bezpieczeństw.
 * Odpowiada tagowi SecList w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityList extends BaseMessage {
    
    @XmlAttribute(name = "ReqID")
    private String requestId;
    
    @XmlAttribute(name = "SecurityReqTyp")
    private String securityRequestType;
    
    @XmlAttribute(name = "SecurityResponseTyp")
    private String securityResponseType;
    
    @XmlAttribute(name = "TotNoRelSyms")
    private String totalNumberOfSecurities;
    
    @XmlAttribute(name = "LastFragment")
    private String lastFragment;
    
    @XmlElement(name = "SecListGrp")
    private SecurityListGroup securityListGroup;
    
    // Stałe dla typów odpowiedzi
    public static final String FULL_RESPONSE = "1";     // Pełna odpowiedź
    public static final String PARTIAL_RESPONSE = "2";  // Częściowa odpowiedź, będą następne fragmenty
    public static final String NO_INSTRUMENTS = "3";    // Brak instrumentów spełniających kryteria
    public static final String REJECTED = "5";          // Żądanie odrzucone
    
    /**
     * Konstruktor domyślny.
     */
    public SecurityList() {
        securityListGroup = new SecurityListGroup();
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
    
    public String getSecurityResponseType() {
        return securityResponseType;
    }
    
    public void setSecurityResponseType(String securityResponseType) {
        this.securityResponseType = securityResponseType;
    }
    
    public String getTotalNumberOfSecurities() {
        return totalNumberOfSecurities;
    }
    
    public void setTotalNumberOfSecurities(String totalNumberOfSecurities) {
        this.totalNumberOfSecurities = totalNumberOfSecurities;
    }
    
    public String getLastFragment() {
        return lastFragment;
    }
    
    public void setLastFragment(String lastFragment) {
        this.lastFragment = lastFragment;
    }
    
    public SecurityListGroup getSecurityListGroup() {
        return securityListGroup;
    }
    
    public void setSecurityListGroup(SecurityListGroup securityListGroup) {
        this.securityListGroup = securityListGroup;
    }
    
    public List<SecurityDefinition> getSecurities() {
        return securityListGroup.getSecurityDefinitions();
    }
    
    public void addSecurity(SecurityDefinition security) {
        securityListGroup.addSecurityDefinition(security);
    }
    
    @Override
    public String getMessageType() {
        return "SecList";
    }

    @Override
    public String getMessageId() {
        return requestId;
    }
    
    /**
     * Sprawdza czy odpowiedź zawiera błąd
     *
     * @return true jeśli odpowiedź jest typu REJECTED
     */
    public boolean isRejected() {
        return REJECTED.equals(securityResponseType);
    }
    
    /**
     * Sprawdza czy odpowiedź jest kompletna
     * 
     * @return true jeśli jest to ostatni fragment odpowiedzi lub odpowiedź jest pełna
     */
    public boolean isComplete() {
        return "Y".equals(lastFragment) || FULL_RESPONSE.equals(securityResponseType);
    }
    
    /**
     * Klasa wewnętrzna grupująca definicje bezpieczeństw.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SecurityListGroup", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class SecurityListGroup {
        @XmlElement(name = "SecDef")
        private List<SecurityDefinition> securityDefinitions;
        
        public SecurityListGroup() {
            securityDefinitions = new ArrayList<>();
        }
        
        public List<SecurityDefinition> getSecurityDefinitions() {
            return securityDefinitions;
        }
        
        public void setSecurityDefinitions(List<SecurityDefinition> securityDefinitions) {
            this.securityDefinitions = securityDefinitions;
        }
        
        public void addSecurityDefinition(SecurityDefinition securityDefinition) {
            if (securityDefinitions == null) {
                securityDefinitions = new ArrayList<>();
            }
            securityDefinitions.add(securityDefinition);
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca definicję bezpieczeństwa.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SecurityDefinition", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class SecurityDefinition {
        @XmlElement(name = "Instrmt")
        private Instrument instrument;
        
        @XmlElement(name = "MktSegGrp")
        private MarketSegmentGroup marketSegmentGroup;
        
        @XmlElement(name = "TradingSessionRulesGrp")
        private TradingSessionRulesGroup tradingSessionRulesGroup;
        
        @XmlElement(name = "StrkRulesGrp")
        private StrikeRulesGroup strikeRulesGroup;
        
        public Instrument getInstrument() {
            return instrument;
        }
        
        public void setInstrument(Instrument instrument) {
            this.instrument = instrument;
        }
        
        public MarketSegmentGroup getMarketSegmentGroup() {
            return marketSegmentGroup;
        }
        
        public void setMarketSegmentGroup(MarketSegmentGroup marketSegmentGroup) {
            this.marketSegmentGroup = marketSegmentGroup;
        }
        
        public TradingSessionRulesGroup getTradingSessionRulesGroup() {
            return tradingSessionRulesGroup;
        }
        
        public void setTradingSessionRulesGroup(TradingSessionRulesGroup tradingSessionRulesGroup) {
            this.tradingSessionRulesGroup = tradingSessionRulesGroup;
        }
        
        public StrikeRulesGroup getStrikeRulesGroup() {
            return strikeRulesGroup;
        }
        
        public void setStrikeRulesGroup(StrikeRulesGroup strikeRulesGroup) {
            this.strikeRulesGroup = strikeRulesGroup;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca instrument.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SecurityListInstrument", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class Instrument {
        @XmlAttribute(name = "Sym")
        private String symbol;
        
        @XmlAttribute(name = "ID")
        private String id;
        
        @XmlAttribute(name = "IDSrc")
        private String idSource;
        
        @XmlAttribute(name = "SecTyp")
        private String securityType;
        
        @XmlAttribute(name = "CFI")
        private String cfi;
        
        @XmlAttribute(name = "MatDt")
        private String maturityDate;
        
        @XmlAttribute(name = "MMY")
        private String maturityMonthYear;
        
        @XmlAttribute(name = "Desc")
        private String description;
        
        @XmlAttribute(name = "MinPxIncr")
        private String minimumPriceIncrement;
        
        @XmlAttribute(name = "MinPxIncrAmt")
        private String minimumPriceIncrementAmount;
        
        @XmlAttribute(name = "RndLot")
        private String roundLot;
        
        @XmlAttribute(name = "PxPrcsn")
        private String pricePrecision;
        
        @XmlAttribute(name = "Fctr")
        private String factor;
        
        @XmlAttribute(name = "ContractMult")
        private String contractMultiplier;
        
        @XmlAttribute(name = "ValMeth")
        private String valuationMethod;
        
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
        
        public String getSecurityType() {
            return securityType;
        }
        
        public void setSecurityType(String securityType) {
            this.securityType = securityType;
        }
        
        public String getCfi() {
            return cfi;
        }
        
        public void setCfi(String cfi) {
            this.cfi = cfi;
        }
        
        public String getMaturityDate() {
            return maturityDate;
        }
        
        public void setMaturityDate(String maturityDate) {
            this.maturityDate = maturityDate;
        }
        
        public String getMaturityMonthYear() {
            return maturityMonthYear;
        }
        
        public void setMaturityMonthYear(String maturityMonthYear) {
            this.maturityMonthYear = maturityMonthYear;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getMinimumPriceIncrement() {
            return minimumPriceIncrement;
        }
        
        public void setMinimumPriceIncrement(String minimumPriceIncrement) {
            this.minimumPriceIncrement = minimumPriceIncrement;
        }
        
        public String getMinimumPriceIncrementAmount() {
            return minimumPriceIncrementAmount;
        }
        
        public void setMinimumPriceIncrementAmount(String minimumPriceIncrementAmount) {
            this.minimumPriceIncrementAmount = minimumPriceIncrementAmount;
        }
        
        public String getRoundLot() {
            return roundLot;
        }
        
        public void setRoundLot(String roundLot) {
            this.roundLot = roundLot;
        }
        
        public String getPricePrecision() {
            return pricePrecision;
        }
        
        public void setPricePrecision(String pricePrecision) {
            this.pricePrecision = pricePrecision;
        }
        
        public String getFactor() {
            return factor;
        }
        
        public void setFactor(String factor) {
            this.factor = factor;
        }
        
        public String getContractMultiplier() {
            return contractMultiplier;
        }
        
        public void setContractMultiplier(String contractMultiplier) {
            this.contractMultiplier = contractMultiplier;
        }
        
        public String getValuationMethod() {
            return valuationMethod;
        }
        
        public void setValuationMethod(String valuationMethod) {
            this.valuationMethod = valuationMethod;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca segment rynkowy.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MarketSegmentGroup", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class MarketSegmentGroup {
        @XmlElement(name = "MktSegID")
        private List<MarketSegmentId> marketSegmentIds;
        
        public List<MarketSegmentId> getMarketSegmentIds() {
            return marketSegmentIds;
        }
        
        public void setMarketSegmentIds(List<MarketSegmentId> marketSegmentIds) {
            this.marketSegmentIds = marketSegmentIds;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca identyfikator segmentu rynku.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MarketSegmentId", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class MarketSegmentId {
        @XmlAttribute(name = "MktID")
        private String marketId;
        
        @XmlAttribute(name = "MktSegID")
        private String marketSegmentId;
        
        public String getMarketId() {
            return marketId;
        }
        
        public void setMarketId(String marketId) {
            this.marketId = marketId;
        }
        
        public String getMarketSegmentId() {
            return marketSegmentId;
        }
        
        public void setMarketSegmentId(String marketSegmentId) {
            this.marketSegmentId = marketSegmentId;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca reguły sesji handlowej.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "TradingSessionRulesGroup", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class TradingSessionRulesGroup {
        @XmlElement(name = "TradSesRules")
        private List<TradingSessionRules> tradingSessionRules;
        
        public List<TradingSessionRules> getTradingSessionRules() {
            return tradingSessionRules;
        }
        
        public void setTradingSessionRules(List<TradingSessionRules> tradingSessionRules) {
            this.tradingSessionRules = tradingSessionRules;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca reguły sesji handlowej.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "TradingSessionRules", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class TradingSessionRules {
        @XmlAttribute(name = "SesID")
        private String sessionId;
        
        @XmlAttribute(name = "TradSesOpnTm")
        private String tradingSessionOpenTime;
        
        @XmlAttribute(name = "TradSesCls")
        private String tradingSessionCloseTime;
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public String getTradingSessionOpenTime() {
            return tradingSessionOpenTime;
        }
        
        public void setTradingSessionOpenTime(String tradingSessionOpenTime) {
            this.tradingSessionOpenTime = tradingSessionOpenTime;
        }
        
        public String getTradingSessionCloseTime() {
            return tradingSessionCloseTime;
        }
        
        public void setTradingSessionCloseTime(String tradingSessionCloseTime) {
            this.tradingSessionCloseTime = tradingSessionCloseTime;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca reguły dla cen wykonania opcji.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "StrikeRulesGroup", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class StrikeRulesGroup {
        @XmlElement(name = "StrkRule")
        private List<StrikeRule> strikeRules;
        
        public List<StrikeRule> getStrikeRules() {
            return strikeRules;
        }
        
        public void setStrikeRules(List<StrikeRule> strikeRules) {
            this.strikeRules = strikeRules;
        }
    }
    
    /**
     * Klasa wewnętrzna reprezentująca regułę dla ceny wykonania opcji.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "StrikeRule", namespace = "com.krzysztofpk14.bossaapi.response")
    public static class StrikeRule {
        @XmlAttribute(name = "StrkRule")
        private String strikeRule;
        
        @XmlAttribute(name = "StartStrkPxRng")
        private String startStrikePriceRange;
        
        @XmlAttribute(name = "EndStrkPxRng")
        private String endStrikePriceRange;
        
        @XmlAttribute(name = "StrkIncr")
        private String strikeIncrement;
        
        public String getStrikeRule() {
            return strikeRule;
        }
        
        public void setStrikeRule(String strikeRule) {
            this.strikeRule = strikeRule;
        }
        
        public String getStartStrikePriceRange() {
            return startStrikePriceRange;
        }
        
        public void setStartStrikePriceRange(String startStrikePriceRange) {
            this.startStrikePriceRange = startStrikePriceRange;
        }
        
        public String getEndStrikePriceRange() {
            return endStrikePriceRange;
        }
        
        public void setEndStrikePriceRange(String endStrikePriceRange) {
            this.endStrikePriceRange = endStrikePriceRange;
        }
        
        public String getStrikeIncrement() {
            return strikeIncrement;
        }
        
        public void setStrikeIncrement(String strikeIncrement) {
            this.strikeIncrement = strikeIncrement;
        }
    }
}
