package es.iesisidra.datastock.complementarias;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Punto de entrada reservado para RF-FIC-07: consultas XPath.
 *
 */
public final class XPathActividad {
    private XPathActividad() { }
    private ProductoXmlRepository xmlRepository = new ProductoXmlRepository();
    
    public static void main(String[] args) {
        XPathActividad actividad = new XPathActividad();
        actividad.consultaXPath("datos/ejemplos/catalogo_valido.xml");
    }

    /**
     * Metodo para realizar consultas XML mediante XPath
     * @param ruta donde esta el documento XML
     */

    public void consultaXPath(String ruta) {
        // valido si la ruta recibida es nula o está vacía
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "ERROR: La ruta está vacía." + ColoresConsola.RESET);
            return;
        }

        // compruebo existencia del fichero
        Path path = Paths.get(ruta.trim());
        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR: El fichero no existe o es un directorio." + ColoresConsola.RESET);
            return;
        }

        try {
            // leo el documento
            Document doc = xmlRepository.leer(path);

            // creo el evaluador: sabe construir motores XPath en Java y me devuelve el
            // objeto listo para ejecutar expresiones sobre el doc
            XPath xpath = XPathFactory.newInstance().newXPath();

            // muestro por pantalla las opciones y doy a elegir
            int opcion;
            do {
                System.out.println("---- CONSULTAS XPATH ----");
                System.out.println("Fichero: " + ruta.trim());
                System.out.println("1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Volver");

                opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);

                // 6. Usamos switch-case para gestionar cada consulta XPath
                switch (opcion) {
                    case 1 -> {
                        // busqueda por codigo seleccionando nodos para evitar inyección XPath
                        String codBuscado = LectorConsola.leerTexto("Introduce el codigo: ");
                        // .NODESET indica que espera una coleccion de nodos
                        NodeList nodos = (NodeList) xpath.evaluate("/catalogo/producto", doc, XPathConstants.NODESET);
                        int contador = 0;

                        for (int i = 0; i < nodos.getLength(); i++) {
                            Element elem = (Element) nodos.item(i);
                            // 0 ya que el codigo es el primero
                            String cod = elem.getElementsByTagName("codigo").item(0).getTextContent();

                            if (cod.equalsIgnoreCase(codBuscado.trim())) { // si es el mismo codigo imprimimos sus datos
                                // y sumamos al contador
                                imprimirLineaProductoXml(elem);
                                contador = contador + 1;
                            }
                        }
                        System.out.println("Productos encontrados: " + contador);
                    }
                    case 2 -> {
                        // Filtrar por categoría usando expresión XPath con predicado [categoriaId=X]
                        int catId = LectorConsola.leerEntero("ID Categoria: ");
                        String expresion = "/catalogo/producto[categoriaId=" + catId + "]";
                        NodeList nodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

                        for (int i = 0; i < nodos.getLength(); i++) {
                            imprimirLineaProductoXml((Element) nodos.item(i));
                        }
                        System.out.println("Productos encontrados: " + nodos.getLength());
                    }
                    case 3 -> {
                        // Filtrar por precio superior a un valor numérico [precio > X]
                        double precioMin = LectorConsola.leerDouble("Precio mínimo: ");
                        String expresion = "/catalogo/producto[precio > " + precioMin + "]";
                        NodeList nodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

                        for (int i = 0; i < nodos.getLength(); i++) {
                            imprimirLineaProductoXml((Element) nodos.item(i));
                        }
                        System.out.println("Productos encontrados: " + nodos.getLength());
                    }
                    case 4 -> {
                        // Contar nodos <producto> mediante la función de agregación count()
                        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
                        System.out.println("Productos encontrados: " + total.intValue());
                    }
                    case 0 ->
                        System.out
                                .println(ColoresConsola.VERDE + "Saliendo de la consulta XPath..."
                                        + ColoresConsola.RESET);
                    default ->
                        throw new IllegalStateException("Opción inesperada: " + opcion);
                }

            } while (opcion != 0);

        } catch (Exception e) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR en la consulta XPath: " + e.getMessage() + ColoresConsola.RESET);
        }

    }

    /**
     * metodo auxiliar para mostrar los datos de un nodo <producto> en el
     * formato de ejemplo de ejecucion
     *
     */
    private void imprimirLineaProductoXml(Element elem) {
        String cod = elem.getElementsByTagName("codigo").item(0).getTextContent();
        String nom = elem.getElementsByTagName("nombre").item(0).getTextContent();
        String precio = elem.getElementsByTagName("precio").item(0).getTextContent();

        System.out.println(cod + "   " + nom + "   " + precio + "   euros"); // el simbolo de euro por consola sale como
                                                                             // interrogacion
    }
}
