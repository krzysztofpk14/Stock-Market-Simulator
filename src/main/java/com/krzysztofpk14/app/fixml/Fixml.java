package com.krzysztofpk14.app.fixml;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "FIXML")
@XmlAccessorType(XmlAccessType.FIELD)
public class Fixml {
    @XmlAttribute(name = "v")
    private String version;
    
    @XmlAttribute(name = "r")
    private String revision;
    
    @XmlAttribute(name = "s")
    private String servicepack;
    
    @XmlElement(name = "UserReq")
    private UserReq userReq;
    
    // Add other possible child elements
    // @XmlElements({
    //    @XmlElement(name = "Order", type = Order.class),
    //    @XmlElement(name = "ExecutionReport", type = ExecutionReport.class)
    // })
    // private List<Object> messages = new ArrayList<>();
    
    // Getters and setters
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
    
    public UserReq getUserReq() { 
        return userReq; 
    }
    
    public void setUserReq(UserReq userReq) { 
        this.userReq = userReq; 
    }
}
