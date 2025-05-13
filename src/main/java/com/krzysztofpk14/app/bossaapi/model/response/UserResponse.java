package com.krzysztofpk14.app.bossaapi.model.response;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;

/**
 * Klasa reprezentująca odpowiedź na żądanie użytkownika.
 * Odpowiada tagowi UserRsp w FIXML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserResponse extends BaseMessage {
    
    @XmlAttribute(name = "UserReqID")
    private String userReqID;
    
    @XmlAttribute(name = "UserStatus")
    private String userStatus;  // 1=zalogowany, 2=wylogowany, 3=nieudane logowanie
    
    @XmlAttribute(name = "UserStatusText")
    private String userStatusText;
    
    @XmlAttribute(name = "Username")
    private String username;
    
    // Stałe dla statusów
    public static final String LOGGED_IN = "1";
    public static final String LOGGED_OUT = "2";
    public static final String LOGIN_FAILED = "3";
    
    // Konstruktory
    public UserResponse() {
    }
    
    // Gettery i settery
    public String getUserReqID() {
        return userReqID;
    }
    
    public void setUserReqID(String userReqID) {
        this.userReqID = userReqID;
    }
    
    public String getUserStatus() {
        return userStatus;
    }
    
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
    
    public String getUserStatusText() {
        return userStatusText;
    }
    
    public void setUserStatusText(String userStatusText) {
        this.userStatusText = userStatusText;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public String getMessageType() {
        return "UserRsp";
    }
    
    /**
     * Sprawdza, czy logowanie zakończyło się sukcesem.
     * @return true jeśli zalogowano pomyślnie
     */
    public boolean isLoginSuccessful() {
        return LOGGED_IN.equals(userStatus);
    }
}