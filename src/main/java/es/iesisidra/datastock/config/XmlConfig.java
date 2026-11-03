package es.iesisidra.datastock.config;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/** Clase XmlConfig importada de java-xml-ejemplos
 * Configuración segura compartida para parsers XML (DOM y SAX).
 * Deshabilita el procesamiento de DTDs y entidades externas para prevenir vulnerabilidades de tipo XXE.
 */

public class XmlConfig {
    private XmlConfig() { } // Constructor privado para evitar que la clase sea instanciada

    public static DocumentBuilderFactory domFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        // Deshabilita la declaración de DOCTYPEs y entidades externas por seguridad
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        
        // Habilita el procesamiento seguro estándar
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        
        return factory;
    
    
}

    public static SAXParserFactory saxFactory() throws ParserConfigurationException, org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setXIncludeAware(false);
        
        return factory;
    }

}