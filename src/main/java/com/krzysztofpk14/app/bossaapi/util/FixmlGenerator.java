package com.krzysztofpk14.app.bossaapi.util;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;
import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;

/**
 * Klasa narzędziowa do generowania komunikatów FIXML z obiektów Java.
 */
public class FixmlGenerator {
    private static JAXBContext jaxbContext;
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(
            FixmlMessage.class,             
            com.krzysztofpk14.app.bossaapi.model.request.UserRequest.class,
            com.krzysztofpk14.app.bossaapi.model.response.UserResponse.class,
            com.krzysztofpk14.app.bossaapi.model.request.OrderRequest.class,
            com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport.class,
            com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest.class,
            com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse.class,
            com.krzysztofpk14.app.bossaapi.model.response.BusinessMessageReject.class);
        } catch (JAXBException e) {
            System.err.println("Blad inicjalizacji JAXB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nie mozna zainicjalizowac kontekstu JAXB", e);
        }
    }
    
    /**
     * Generuje XML FIXML z obiektu wiadomości.
     * 
     * @param message Obiekt reprezentujący komunikat FIXML
     * @return Łańcuch znaków zawierający XML
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    public static String generateXml(FixmlMessage message) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(message, writer);
        return writer.toString();
    }
    
    /**
     * Tworzy pełną wiadomość FIXML z obiektu wiadomości bazowej.
     * 
     * @param message Obiekt wiadomości do opakowania
     * @return Kompletny obiekt FixmlMessage
     */
    public static FixmlMessage createFixmlMessage(BaseMessage message) {
        return new FixmlMessage(message);
    }
    
    /**
     * Generuje XML FIXML bezpośrednio z obiektu wiadomości bazowej.
     * 
     * @param message Obiekt wiadomości do przekształcenia w XML
     * @return Łańcuch znaków zawierający XML
     * @throws JAXBException Jeśli wystąpi błąd generowania XML
     */
    public static String generateXml(BaseMessage message) throws JAXBException {
        FixmlMessage fixmlMessage = createFixmlMessage(message);
        return generateXml(fixmlMessage);
    }
}