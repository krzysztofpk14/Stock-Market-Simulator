package com.krzysztofpk14.app;

import com.krzysztofpk14.app.fixml.Fixml;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;

public class FixmlParser {
    
    public static void main(String[] args) {
        String xmlMessage = 
            "<FIXML v=\"5.0\" r=\"20080317\" s=\"20080314\">\n" +
            "  <UserReq UserReqID=\"0\" UserReqTyp=\"1\" Username=\"BOS\" Password=\"BOS\"/>\n" +
            "</FIXML>";
        
        try {
            // Create JAXB context for our classes
            JAXBContext context = JAXBContext.newInstance(Fixml.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            // Parse the XML string
            Fixml fixml = (Fixml) unmarshaller.unmarshal(new StringReader(xmlMessage));
            
            // Access the parsed data
            System.out.println("FIXML Version: " + fixml.getVersion());
            System.out.println("UserReq ID: " + fixml.getUserReq().getUserReqID());
            System.out.println("Username: " + fixml.getUserReq().getUsername());
            System.out.println("Password: " + fixml.getUserReq().getPassword());
            
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}