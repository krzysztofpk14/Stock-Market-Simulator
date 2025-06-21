package com.krzysztofpk14.app.bossaapi.model.response;

import jakarta.xml.bind.annotation.*;
import com.krzysztofpk14.app.bossaapi.model.base.BaseMessage;

/**
 * Klasa reprezentująca wiadomość BusinessMessageReject.
 */
// @XmlRootElement(name = "BusinessMessageReject")
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessMessageReject extends BaseMessage {

    @XmlElement(name = "RefMsgType")
    private String refMsgType;

    @XmlElement(name = "BizRejRsn")
    private String businessRejectReason;

    @XmlElement(name = "Text")
    private String text;

    // Stałe RefMsgType
    public static final String LOG_IN_OUT = "BE";
    public static final String NEW_ORDER = "D";
    public static final String CANCEL_ORDER = "F";
    public static final String MODIFY_ORDER = "G";
    public static final String ORDER_STATUS = "H";
    public static final String MARKET_DATA_REQUEST = "V";
    public static final String STATUS = "g";

    // Stałe BusinessRejectReason
    public static final String OTHER = "0";
    public static final String UNKNOWN_ID = "1";
    public static final String UNKNOWN_SECURITY = "2";
    public static final String UNSUPPORTED_MESSAGE_TYPE = "3";
    public static final String APPLICATION_NOT_AVAILABLE = "4";
    public static final String CONDITIONALLY_REQUIRED_FIELD_MISSING = "5";
    public static final String NOT_AUTHORIZED = "6";
    public static final String LACK_OF_COMMUNICATION = "7";



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
        return "BizMsgRej";
    }

    @Override
    public String getMessageId() {
        return "None";
    }
}