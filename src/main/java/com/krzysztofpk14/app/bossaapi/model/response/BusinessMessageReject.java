package com.krzysztofpk14.app.bossaapi.model.response;

import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Klasa reprezentująca wiadomość BusinessMessageReject.
 */
@XmlRootElement(name = "BusinessMessageReject")
public class BusinessMessageReject extends BaseMessage {

    @XmlElement(name = "RefMsgType")
    private String refMsgType;

    @XmlElement(name = "BusinessRejectReason")
    private String businessRejectReason;

    @XmlElement(name = "Text")
    private String text;

    public String getRefMsgType() {
        return refMsgType;
    }

    public void setRefMsgType(String refMsgType) {
        this.refMsgType = refMsgType;
    }

    public String getBusinessRejectReason() {
        return businessRejectReason;
    }

    public void setBusinessRejectReason(String businessRejectReason) {
        this.businessRejectReason = businessRejectReason;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getMessageType() {
        return "BusinessMessageReject";
    }

    @Override
    public String toString() {
        return "BusinessMessageReject{" +
                "refMsgType='" + refMsgType + '\'' +
                ", businessRejectReason='" + businessRejectReason + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}