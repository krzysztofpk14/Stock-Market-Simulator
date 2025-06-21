package com.krzysztofpk14.app.gui.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Utility for formatting XML strings.
 */
public class XMLFormatter {
    
    /**
     * Format XML string with proper indentation.
     * 
     * @param input The unformatted XML string
     * @return Formatted XML string
     * @throws Exception if XML parsing or transformation fails
     */
    public static String format(String input) throws Exception {
        // Skip if input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Enable namespace support
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(input)));
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            
            // Try to set indent-number if supported
            try {
                transformerFactory.setAttribute("indent-number", 2);
            } catch (IllegalArgumentException e) {
                // Attribute not supported by this transformer factory implementation
                // Just continue without it - indentation will still work but might be different
            }
            
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            
            // This is a widely supported property for controlling indentation
            try {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            } catch (IllegalArgumentException e) {
                // Property not supported, continue anyway
            }
            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            
            return writer.toString();
        } catch (Exception e) {
            System.err.println("XML formatting error: " + e.getMessage());
            // Return original if formatting fails
            return input;
        }
    }
    
    /**
     * Escapes XML special characters in a string for display.
     * Useful when showing raw XML content in UI.
     * 
     * @param input String to escape
     * @return Escaped string
     */
    public static String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}