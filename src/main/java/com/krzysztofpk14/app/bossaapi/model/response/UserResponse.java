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
    
    @XmlAttribute(name = "UserStat")
    private String userStatus; 
    
    @XmlAttribute(name = "UserStatText")
    private String userStatusText;
    
    @XmlAttribute(name = "Username")
    private String username;

    @XmlAttribute(name = "MktDepth")
    private String mktDepth;
    
    // Stałe dla statusów
    public static final String LOGGED_IN = "1";
    public static final String LOGGED_OUT = "2";
    public static final String USER_NOT_EXIST = "3";
    public static final String WRONG_PASSWORD = "4";
    public static final String INVESTOR_OFFLINE = "5";
    public static final String OTHER = "6";
    public static final String NOL_OFFLINE = "7";

    // Stałe dla MktDepth
    public static final String FULL_BOOK = "0"; // 5 ofert (rezerwa na cały arkusz)
    public static final String TOP_OF_BOOK = "1"; // 1 oferta
    public static final String FIVE_OFFERS = "5"; // 5 ofert

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

    public String getMktDepth() {
        return mktDepth;
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

    public void setMktDepth(String mktDepth) {
        this.mktDepth = mktDepth;
    }
    
    @Override
    public String getMessageType() {
        return "UserRsp";
    }

    @Override
    public String getMessageId() {
        return userReqID;
    }
    
    /**
     * Sprawdza, czy logowanie zakończyło się sukcesem.
     * @return true jeśli zalogowano pomyślnie
     */
    public boolean isLoginSuccessful() {
        return LOGGED_IN.equals(userStatus)||  (OTHER.equals(userStatus) && (userStatusText == "User is already logged"));
    }
}