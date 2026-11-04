package es.iesisidra.datastock.servicio;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.iesisidra.datastock.config.XmlConfig;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class InventarioXML {
    // cabe aclarar que aqui nos preparamos para pasar la lista de productos pero realmente se pasa en Inventario
    private final List<Producto> productos;

    // instancio el gestor para poder hacer uso de metodos de gestion de productos

    private final GestorProductos gestorProductos;

    public InventarioXML(List<Producto> productos) {
        this.productos = productos;
        this.gestorProductos = new GestorProductos();
    }

    // Instancia reutilizable del repositorio
    private ProductoXmlRepository xmlRepository = new ProductoXmlRepository();


    /**
     * Importa un catálogo de productos desde un fichero XML usando DOM. Lee el
     * fichero con el repositorio y valida los campos añade solo los registros
     * válidos al inventario.
     *
     * @param ruta la ruta de donde se importa el xml
     */
    public void importarXml(String ruta) {
        // 1. Validar si la ruta recibida es nula o está vacía
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "ERROR: La ruta está vacía." + ColoresConsola.RESET);
            return;
        }

        // Comprobar existencia del fichero
        Path path = Paths.get(ruta.trim());
        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR: El fichero no existe o es un directorio." + ColoresConsola.RESET);
            return;
        }

        try {
            // 3. Cargar el árbol de nodos DOM a través del repositorio seguro
            Document doc = xmlRepository.leer(path);

            // no puede estar vacio y la razi debe de ser catalogo

            if (doc.getDocumentElement() == null
                    || !"catalogo".equals(
                            doc.getDocumentElement().getTagName())) {

                System.out.println(
                        ColoresConsola.ROJO
                                + "ERROR: La raíz del XML debe ser <catalogo>."
                                + ColoresConsola.RESET);
                return;
            }

            // Obtenemos todos los elementos con la etiqueta <producto>
            NodeList listaNodos = doc.getElementsByTagName("producto");
            int encontrados = listaNodos.getLength();
            int importados = 0;
            int rechazados = 0;
            List<String> incidencias = new ArrayList<>();

            for (int i = 0; i < encontrados; i++) {
                Node nodo = listaNodos.item(i);

                // Filtrar solo los nodos tipo elemento para evitar nodos de texto/saltos de
                // línea
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element elemento = (Element) nodo;
                    try {
                        // Extraer textos de las etiquetas
                        String codigo = obtenerTextoEtiqueta(elemento, "codigo");
                        String nombre = obtenerTextoEtiqueta(elemento, "nombre");

                        // Parseo del precio
                        String strPrecio = obtenerTextoEtiqueta(elemento, "precio");
                        if (strPrecio == null || strPrecio.trim().isEmpty()) {
                            throw new IllegalArgumentException("El precio es obligatorio (campo ausente/vacío)");
                        }
                        BigDecimal precio;
                        try {
                            precio = new BigDecimal(strPrecio.trim().replace(',', '.'));
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException(
                                    "El precio no tiene un formato decimal válido ('" + strPrecio + "')");
                        }

                        // Parseo del stock
                        String strStock = obtenerTextoEtiqueta(elemento, "stock");
                        if (strStock == null || strStock.trim().isEmpty()) {
                            throw new IllegalArgumentException("El stock es obligatorio (campo ausente/vacío)");
                        }
                        int stock;
                        try {
                            stock = Integer.parseInt(strStock.trim());
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("El stock debe ser un entero válido (" + strStock + ")");
                        }

                        // Parseo de los IDs
                        String strCatId = obtenerTextoEtiqueta(elemento, "categoriaId");
                        int categoriaId = 0;
                        if (strCatId != null && !strCatId.trim().isEmpty()) {
                            try {
                                categoriaId = Integer.parseInt(strCatId.trim());
                            } catch (NumberFormatException nfe) {
                                throw new IllegalArgumentException(
                                        "El ID de categoría debe ser numérico (" + strCatId + ")");
                            }
                        }

                        String strProvId = obtenerTextoEtiqueta(elemento, "proveedorId");
                        int proveedorId = 0;
                        if (strProvId != null && !strProvId.trim().isEmpty()) {
                            try {
                                proveedorId = Integer.parseInt(strProvId.trim());
                            } catch (NumberFormatException nfe) {
                                throw new IllegalArgumentException(
                                        "El ID de proveedor debe ser numérico ('" + strProvId + "')");
                            }
                        }

                        // Construcción del objeto Producto
                        Producto p = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);

                        if (p.getCodigo() != null) {
                            p.setCodigo(p.getCodigo().trim());
                        }

                        // Reutilizar el método de validación común
                        List<String> errores = gestorProductos.validarProducto(p, this.productos);

                        if (errores.isEmpty()) {
                            productos.add(p);
                            importados++;
                        } else {
                            rechazados++;
                            StringBuilder msjError = new StringBuilder();
                            for (int j = 0; j < errores.size(); j++) {
                                msjError.append(errores.get(j));
                                if (j < errores.size() - 1) {
                                    msjError.append(", ");
                                }
                            }

                            // Determinar el identificador
                            String cod;
                            if (p.getCodigo() != null && !p.getCodigo().isEmpty()) {
                                cod = p.getCodigo();
                            } else {
                                cod = "Producto " + (i + 1);
                            }

                            incidencias.add(cod + ": " + msjError.toString());
                        }

                    } catch (IllegalArgumentException iae) {
                        rechazados++;
                        String cod = obtenerTextoEtiqueta(elemento, "codigo");
                        if (cod == null || cod.trim().isEmpty()) {
                            cod = "Producto " + (i + 1);
                        }
                        incidencias.add(cod + ": " + iae.getMessage());
                    } catch (Exception e) {
                        rechazados++;
                        incidencias.add("Producto " + (i + 1) + ": Error al parsear los datos.");
                    }
                }
            }

            System.out.println("---- IMPORTAR XML ----");
            System.out.println("Ruta: " + ruta);
            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);

            if (!incidencias.isEmpty()) {
                System.out.println("INCIDENCIAS:");
                for (String inc : incidencias) {
                    System.out.println("  - " + inc);
                }
            }

        } catch (org.xml.sax.SAXException e) {
            System.out.println(ColoresConsola.ROJO + "ERROR: El fichero XML está malformado. Importación cancelada."
                    + ColoresConsola.RESET);
        } catch (Exception e) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR al leer el fichero: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Método auxiliar privado para obtener el valor texto de una etiqueta hija.
     */
    private String obtenerTextoEtiqueta(Element elementoPadre, String nombreEtiqueta) {

        NodeList lista = elementoPadre.getElementsByTagName(nombreEtiqueta);
        // si la lista existe y tiene al menos un elemento
        if (lista != null && lista.getLength() > 0) {
            Node nodo = lista.item(0);
            // si el nodo existe y tiene texto dentro
            if (nodo != null && nodo.getTextContent() != null) {
                return nodo.getTextContent().trim();
            }
        }
        // si la etiqueta no existia o estaba vacia
        return null;
    }


    /**
     * Exporta el catálogo actual de productos a un fichero en formato XML
     * utilizando DOM
     *
     * @param ruta Ruta del fichero de destino donde se guarda el XML
     */
    public void exportarXml(String ruta) {
        if (productos.isEmpty()) {
            System.out.println(
                    ColoresConsola.ROJO + "El inventario está vacío. No hay nada que exportar." + ColoresConsola.RESET);
            return;
        }

        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "ERROR: La ruta de destino no es válida." + ColoresConsola.RESET);
            return;
        }

        File archivo = new File(ruta.trim());

        // no debe ser un directorio
        if (archivo.isDirectory()) {
            System.out.println("ERROR: La ruta corresponde a un directorio.");
            return;
        }

        // pido confirmacion antes que cualquier cosa (en el caso de que exista)

        if (archivo.exists() && !LectorConsola.confirmar("Seguro que quieres sobrescribir: " + ruta.trim())) {
            System.out.println(
                    ColoresConsola.VERDE + "Cancelacion recibida saliendo correctamente" + ColoresConsola.RESET);
            return;
        }

        try {
            // Inicializo un nuevo documento DOM en blanco
            DocumentBuilder builder = XmlConfig.domFactory().newDocumentBuilder();
            Document doc = builder.newDocument();

            // creo el nodo raiz <catalogo> y lo añadimos al documento
            Element root = doc.createElement("catalogo");
            doc.appendChild(root);

            int contador = 0;

            // ordeno los productos sin modificar el inventario
            List<Producto> copiaOrdenada = new ArrayList<>(productos);
            gestorProductos.ordenarPorCodigo(copiaOrdenada);

            // Recorro para incluir todos los productos
            for (int i = 0; i < copiaOrdenada.size(); i++) {
                Producto p = copiaOrdenada.get(i);

                // creo la etiqueta del nodo producto
                Element elemProducto = doc.createElement("producto");

                // añado las etiquetas hijas con sus valores correspondientes
                crearEtiquetaHija(doc, elemProducto, "codigo", p.getCodigo());
                crearEtiquetaHija(doc, elemProducto, "nombre", p.getNombre());

                String strPrecio = "";
                if (p.getPrecio() != null) {
                    strPrecio = p.getPrecio().toString();
                }

                // creo las etiquetas de los atributos de cada producto
                crearEtiquetaHija(doc, elemProducto, "precio", strPrecio);
                crearEtiquetaHija(doc, elemProducto, "stock", String.valueOf(p.getStock()));
                crearEtiquetaHija(doc, elemProducto, "categoriaId", String.valueOf(p.getCategoriaId()));
                crearEtiquetaHija(doc, elemProducto, "proveedorId", String.valueOf(p.getProveedorId()));

                // pongo cada producto en el catalogo
                root.appendChild(elemProducto);
                contador++; //si llega hasta aqui se suma

            }

            // uso el metodo del repositorio para guardar toda la estructura del documento creado
            Path pathDestino = Paths.get(ruta.trim());
            xmlRepository.guardar(doc, pathDestino);

            System.out.println(
                    ColoresConsola.VERDE + "Catálogo exportado correctamente a XML en: " + ruta +  " numero de productos exportados: " + contador +  ColoresConsola.RESET);

        } catch (Exception e) {
            System.out
                    .println(ColoresConsola.ROJO + "ERROR al exportar a XML: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo auxiliar privado para simplificar la creación y adición de
     * etiquetas hijas al DOM
     */
    private void crearEtiquetaHija(Document doc, Element padre, String nombreEtiqueta, String valorTexto) {
        Element hija = doc.createElement(nombreEtiqueta);
        if (valorTexto != null) {
            hija.setTextContent(valorTexto);
        } else {
            hija.setTextContent("");
        }
        padre.appendChild(hija);
    }



}
