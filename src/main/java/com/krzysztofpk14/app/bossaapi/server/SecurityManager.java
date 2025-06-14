package com.krzysztofpk14.app.bossaapi.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krzysztofpk14.app.bossaapi.model.request.SecurityListRequest;
import com.krzysztofpk14.app.bossaapi.model.response.SecurityList;

/**
 * Manager obsługujący instrumenty finansowe.
 */
public class SecurityManager {
    private final Map<String, SecurityDefinition> securities = new HashMap<>();
    
    /**
     * Klasa pomocnicza do przechowywania definicji instrumentu.
     */
    private static class SecurityDefinition {
        private String symbol;
        private String isin;
        private String name;
        private String securityType;
        private String market;
        
        public SecurityDefinition(String symbol, String isin, String name, String securityType, String market) {
            this.symbol = symbol;
            this.isin = isin;
            this.name = name;
            this.securityType = securityType;
            this.market = market;
        }
    }
    
    /**
     * Tworzy nowy manager instrumentów.
     */
    public SecurityManager() {
        // Inicjalizuj przykładowe instrumenty
        initSampleSecurities();
    }
    
    /**
     * Inicjalizuje przykładowe instrumenty.
     */
    private void initSampleSecurities() {
        addSecurity("KGHM", "PLKGHM000017", "KGHM Polska Miedź S.A.", "CS", "GPW");
        addSecurity("PKO", "PLPKO0000016", "PKO Bank Polski S.A.", "CS", "GPW");
        addSecurity("PKN", "PLPKN0000018", "PKN Orlen S.A.", "CS", "GPW");
        addSecurity("PZU", "PLPZU0000011", "PZU S.A.", "CS", "GPW");
        addSecurity("CDR", "PLOPTTC00011", "CD Projekt S.A.", "CS", "GPW");
        addSecurity("LPP", "PLLPP0000011", "LPP S.A.", "CS", "GPW");
        addSecurity("PGE", "PLPGER000010", "PGE S.A.", "CS", "GPW");
        addSecurity("SPL", "PLBZ00000044", "Santander Bank Polska S.A.", "CS", "GPW");
        addSecurity("DNP", "PLDINPL00011", "Dino Polska S.A.", "CS", "GPW");
        addSecurity("CPS", "PLCFRPT00013", "Cyfrowy Polsat S.A.", "CS", "GPW");
    }
    
    /**
     * Dodaje nowy instrument.
     */
    private void addSecurity(String symbol, String isin, String name, String securityType, String market) {
        securities.put(symbol, new SecurityDefinition(symbol, isin, name, securityType, market));
    }
    
    /**
     * Zwraca listę instrumentów na podstawie żądania.
     * 
     * @param request Żądanie listy instrumentów
     * @return Odpowiedź z listą instrumentów
     */
    public SecurityList getSecurities(SecurityListRequest request) {
        SecurityList response = new SecurityList();
        response.setRequestId(request.getRequestId());
        response.setSecurityRequestType(request.getSecurityRequestType());
        response.setSecurityResponseType(SecurityList.FULL_RESPONSE);
        
        SecurityList.SecurityListGroup secListGroup = new SecurityList.SecurityListGroup();
        List<SecurityList.SecurityDefinition> secDefs = new ArrayList<>();
        
        // Wybierz instrumenty zgodnie z kryteriami w żądaniu
        String requestType = request.getSecurityRequestType();
        
        if (SecurityListRequest.ALL_INSTRMNT.equals(requestType)) {
            // Wszystkie instrumenty
            for (Map.Entry<String, SecurityDefinition> entry : securities.entrySet()) {
                secDefs.add(createSecurityDefinition(entry.getValue()));
            }
        } else {
            // Filtrowanie po symbolu, typie, itp. - uproszczona implementacja
            String symbol = null;
            if (request.getInstrument() != null) {
                symbol = request.getInstrument().getSymbol();
            }
            
            if (symbol != null && securities.containsKey(symbol)) {
                secDefs.add(createSecurityDefinition(securities.get(symbol)));
            } else {
                // Domyślnie zwróć wszystkie
                for (Map.Entry<String, SecurityDefinition> entry : securities.entrySet()) {
                    secDefs.add(createSecurityDefinition(entry.getValue()));
                }
            }
        }
        
        secListGroup.setSecurityDefinitions(secDefs);
        response.setSecurityListGroup(secListGroup);
        response.setTotalNumberOfSecurities(String.valueOf(secDefs.size()));
        response.setLastFragment("Y"); // Ostatni fragment odpowiedzi
        
        return response;
    }
    
    /**
     * Tworzy definicję instrumentu dla odpowiedzi.
     * 
     * @param definition Definicja instrumentu
     * @return Definicja instrumentu w formacie odpowiedzi
     */
    private SecurityList.SecurityDefinition createSecurityDefinition(SecurityDefinition definition) {
        SecurityList.SecurityDefinition secDef = new SecurityList.SecurityDefinition();
        
        // Utwórz instrument
        SecurityList.Instrument instrument = new SecurityList.Instrument();
        instrument.setSymbol(definition.symbol);
        instrument.setId(definition.isin);
        instrument.setIdSource("4"); // 4 = ISIN
        instrument.setSecurityType(definition.securityType);
        instrument.setDescription(definition.name);
        
        secDef.setInstrument(instrument);
        
        // Utwórz segment rynku
        SecurityList.MarketSegmentGroup marketSegmentGroup = new SecurityList.MarketSegmentGroup();
        List<SecurityList.MarketSegmentId> marketSegments = new ArrayList<>();
        SecurityList.MarketSegmentId marketSegment = new SecurityList.MarketSegmentId();
        marketSegment.setMarketId(definition.market);
        marketSegment.setMarketSegmentId("MAIN");
        marketSegments.add(marketSegment);
        marketSegmentGroup.setMarketSegmentIds(marketSegments);
        
        secDef.setMarketSegmentGroup(marketSegmentGroup);
        
        // Możesz dodać więcej informacji, jak godziny handlu, zasady cenowe, itp.
        
        return secDef;
    }
}