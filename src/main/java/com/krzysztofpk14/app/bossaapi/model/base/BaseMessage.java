package com.krzysztofpk14.app.bossaapi.model.base;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Klasa bazowa dla wszystkich rodzajów wiadomości FIXML.
 * Służy jako interfejs dla różnych typów wiadomości.
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseMessage {
    
    /**
     * Zwraca typ wiadomości FIXML.
     * 
     * @return Łańcuch znaków określający typ wiadomości
     */
    public abstract String getMessageType();
}