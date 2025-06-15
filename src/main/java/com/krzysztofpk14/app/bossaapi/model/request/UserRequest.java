package com.krzysztofpk14.app.bossaapi.model.request;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;

/**
 * Klasa reprezentująca żądanie użytkownika (logowanie, wylogowanie).
 * Odpowiada tagowi UserReq w FIXML.
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class UserRequest extends BaseMessage {
    
    @XmlAttribute(name = "UserReqID")
    private String userReqID;
    
    @XmlAttribute(name = "UserReqTyp")
    private String userRequestType;
    
    @XmlAttribute(name = "Username")
    private String username;
    
    @XmlAttribute(name = "Password")
    private String password;
    
    // Stałe dla typów żądań
    public static final String LOGIN = "1";
    public static final String LOGOUT = "2";
    public static final String USER_STATUS = "4";

    // Konstruktory
    public UserRequest() {
    }
    
    public UserRequest(String userReqID, String username, String password) {
        this.userReqID = userReqID;
        this.userRequestType = LOGIN;
        this.username = username;
        this.password = password;
    }
    
    // Metoda pomocnicza do tworzenia żądania wylogowania
    public static UserRequest createLogoutRequest(String userReqID, String username) {
        UserRequest request = new UserRequest();
        request.setUserReqID(userReqID);
        request.setUserRequestType(LOGOUT);
        request.setUsername(username);
        return request;
    }
    
    // Gettery i settery
    public String getUserReqID() {
        return userReqID;
    }
    
    public void setUserReqID(String userReqID) {
        this.userReqID = userReqID;
    }
    
    public String getUserRequestType() {
        return userRequestType;
    }
    
    public void setUserRequestType(String userRequestType) {
        this.userRequestType = userRequestType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String getMessageType() {
        return "UserReq";
    }
}