package com.krzysztofpk14.app.fixml;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class UserReq {
    @XmlAttribute(name = "UserReqID")
    private String userReqID;
    
    @XmlAttribute(name = "UserReqTyp")
    private String userReqType;
    
    @XmlAttribute(name = "Username")
    private String username;
    
    @XmlAttribute(name = "Password")
    private String password;
    
    // Getters and setters
    public String getUserReqID() {
        return userReqID;
    }
    
    public void setUserReqID(String userReqID) {
        this.userReqID = userReqID;
    }
    
    public String getUserReqType() {
        return userReqType;
    }
    
    public void setUserReqType(String userReqType) {
        this.userReqType = userReqType;
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
}