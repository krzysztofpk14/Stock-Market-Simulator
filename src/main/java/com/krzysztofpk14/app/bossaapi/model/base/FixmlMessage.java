package com.krzysztofpk14.app.bossaapi.model.base;

import jakarta.xml.bind.annotation.*;

/**
 * Główny kontener dla wiadomości FIXML zgodnych z bossaAPI.
 */
@XmlRootElement(name = "FIXML")
@XmlAccessorType(XmlAccessType.FIELD)
public class FixmlMessage {
    @XmlAttribute(name = "v")
    private String version;
    
    @XmlAttribute(name = "r")
    private String revision;
    
    @XmlAttribute(name = "s")
    private String servicepack;
    
    @XmlElements({
        @XmlElement(name = "UserReq", type = com.krzysztofpk14.app.bossaapi.model.request.UserRequest.class),
        @XmlElement(name = "UserRsp", type = com.krzysztofpk14.app.bossaapi.model.response.UserResponse.class),
        @XmlElement(name = "Order", type = com.krzysztofpk14.app.bossaapi.model.request.OrderRequest.class),
        @XmlElement(name = "ExecRpt", type = com.krzysztofpk14.app.bossaapi.model.response.ExecutionReport.class),
        @XmlElement(name = "MktDataReq", type = com.krzysztofpk14.app.bossaapi.model.request.MarketDataRequest.class),
        @XmlElement(name = "MktDataSnap", type = com.krzysztofpk14.app.bossaapi.model.response.MarketDataResponse.class)
    })
    private BaseMessage message;
    
    // Konstruktory
    public FixmlMessage() {
        this.version = "5.0";  // Domyślne wartości według dokumentacji bossaAPI
        this.revision = "20080317";
        this.servicepack = "20080314";
    }
    
    public FixmlMessage(BaseMessage message) {
        this();
        this.message = message;
    }
    
    // Gettery i settery
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getRevision() {
        return revision;
    }
    
    public void setRevision(String revision) {
        this.revision = revision;
    }
    
    public String getServicepack() {
        return servicepack;
    }
    
    public void setServicepack(String servicepack) {
        this.servicepack = servicepack;
    }
    
    public BaseMessage getMessage() {
        return message;
    }
    
    public void setMessage(BaseMessage message) {
        this.message = message;
    }
}