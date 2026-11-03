package es.iesisidra.datastock.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.config.XmlConfig;

/**
 * Repositorio encargado exclusivamente de la entrada y salida de datos en formato XML.
 */

public class ProductoXmlRepository {

    /**
     * Lee un archivo XML desde la ruta especificada y devuelve su árbol DOM.
     */
    public Document leer(Path ruta) throws IOException, SAXException, ParserConfigurationException {
        // try-with-resources para garantizar el cierre al terminar la lectura
        try (InputStream entrada = Files.newInputStream(ruta)) {
            // Genera el parser DOM usando la fábrica segura configurada en XmlConfig
            Document documento = XmlConfig.domFactory().newDocumentBuilder().parse(entrada);
            
            // Elimina nodos de texto vacios y combina nodos adyacentes para simplificar el recorrido
            documento.getDocumentElement().normalize();
            return documento;
        }
    }

    /**
     * Escribe un objeto Document en un archivo XML aplicando indentación.
     */
    public void guardar(Document documento, Path ruta) throws IOException, TransformerException {
        // Crea los directorios padre de la ruta si aún no existen
        Path padre = ruta.toAbsolutePath().getParent();
        if (padre != null) Files.createDirectories(padre);

        // Configuración del transformador XML a texto plano
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        
        Transformer transformador = factory.newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        try (OutputStream salida = Files.newOutputStream(ruta)) {
            // Transforma el árbol de nodos DOM a la salida en fichero
            transformador.transform(new DOMSource(documento), new StreamResult(salida));
        }
    }
}
