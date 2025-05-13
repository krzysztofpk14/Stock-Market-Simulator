package com.krzysztofpk14.app.bossaapi.util;

import com.krzysztofpk14.app.bossaapi.model.base.FixmlMessage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.InputStream;
import java.io.StringReader;

/**
 * Klasa narzędziowa do parsowania komunikatów FIXML.
 */
public class FixmlParser {
    private static JAXBContext jaxbContext;
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(FixmlMessage.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Nie można zainicjalizować kontekstu JAXB", e);
        }
    }
    
    /**
     * Parsuje komunikat FIXML z łańcucha znaków.
     * 
     * @param xmlString Tekst XML z komunikatem FIXML
     * @return Obiekt FixmlMessage reprezentujący komunikat
     * @throws JAXBException Jeśli wystąpi błąd parsowania
     */
    public static FixmlMessage parse(String xmlString) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (FixmlMessage) unmarshaller.unmarshal(new StringReader(xmlString));
    }
    
    /**
     * Parsuje komunikat FIXML ze strumienia.
     * 
     * @param inputStream Strumień zawierający XML z komunikatem FIXML
     * @return Obiekt FixmlMessage reprezentujący komunikat
     * @throws JAXBException Jeśli wystąpi błąd parsowania
     */
    public static FixmlMessage parse(InputStream inputStream) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (FixmlMessage) unmarshaller.unmarshal(inputStream);
    }
}