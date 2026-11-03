package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.config.XmlConfig;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Estado común del inventario durante la ejecución de UT1.
 * <p>
 * Esta clase solo proporciona la colección compartida. No implementa
 * importación, validación, búsqueda, exportación ni persistencia: esos son
 * ejercicios que deberá desarrollar el alumnado.
 * </p>
 */
public class Inventario {

    private final List<Producto> productos = new ArrayList<>();
    // Instancia reutilizable del repositorio
    private ProductoXmlRepository xmlRepository = new ProductoXmlRepository();

    /**
     * Obtiene la colección que comparten las opciones del menú.
     *
     */
    public List<Producto> getProductos() {
        return productos;
    }

    /**
     * metodo explorarRuta que nos permite ver toda la informacion subyacente de
     * una ruta dada por el usuario
     *
     * @param ruta la ruta que se va a explorar
     */
    public void explorarRuta(String ruta) {

        try {
            if (ruta == null || ruta.trim().isEmpty()) { // la ruta que me dan no debe estar vacia
                System.out.println(ColoresConsola.ROJO
                        + "ERROR: Se debe proporcionar una ruta válida (el texto está vacio)" + ColoresConsola.RESET);
                return; // no seguimos
            }
            // en caso de que la entrada no este vacia

            File archivo = new File(ruta.trim());

            // Primer caso: la ruta no existe
            if (!archivo.exists()) {
                System.out.println(ColoresConsola.ROJO + "ERROR: Se debe proporcionar una ruta válida (no existe)"
                        + ColoresConsola.RESET);
                return; // no seguimos
            }

            // comprobamos si es un fichero y si es el caso mostramos su informacion
            if (archivo.isFile()) {
                System.out.println("---- EXPLORAR FICHERO ----");
                System.out.println("[NOMBRE] " + archivo.getName());
                System.out.println("[RUTA ABSOLUTA] " + archivo.getAbsolutePath());
                System.out.println("[TAMAÑO EN BYTES] " + archivo.length());
                return;
            }

            if (archivo.isDirectory()) {
                System.out.println("---- EXPLORAR DIRECTORIO ----");
                System.out.println("[RUTA] " + ruta);
                File[] contenido = archivo.listFiles(); // obtenemos el contenido del directorio y nos preparamos para
                // recorrerlo en caso de que no este vacio

                if (contenido == null) { // es nulo o no contiene nada
                    System.out.println(ColoresConsola.AMARILLO + "AVISO: NO HAY CONTENIDO EN LA RUTA PROPORCIONADA"
                            + ColoresConsola.RESET);
                    return; // no seguimos
                }

                // inicializo y sumare mientras recorro
                int totalFicheros = 0;
                int totalDirectorios = 0;

                for (File file : contenido) {
                    if (file.isFile()) {
                        System.out.println("[FICHERO] " + file.getName() + " " + file.length() + " bytes");
                        totalFicheros++; // sumo
                    } else if (file.isDirectory()) {
                        System.out.println("[DIR] " + file.getName());
                        totalDirectorios++; // sumo
                    }
                }

                System.out.println("Total ficheros: " + totalFicheros + " | Directorios: " + totalDirectorios);

            }

        } catch (SecurityException se) { // el sistema operativo bloquea por permisos
            System.out.println(ColoresConsola.ROJO + "Error de acceso: Permisos denegados por el sistema "
                    + se.getMessage() + ColoresConsola.RESET);

        } catch (Exception e) { // cualquier otra excepcion inesperada
            System.out.println(ColoresConsola.ROJO + "Error inesperado al explorar la ruta " + e.getMessage()
                    + ColoresConsola.RESET);
        }

    }

    /**
     * Valida un producto según las reglas dadas Devuelve una lista de errores,
     * si esta vacia: el producto es valido
     *
     * @param p el producto que pasa por las validaciones
     * @return una lista con los errores
     */
    private List<String> validarProducto(Producto p, List<Producto> listaReferencia) {
        List<String> errores = new ArrayList<>();

        // 1. Comprobaciones del codigo
        if (p.getCodigo() == null || p.getCodigo().trim().isEmpty()) { // no puede estar vacio
            errores.add("El codigo es obligatorio");
        } else {
            String codigoLimpio = p.getCodigo().trim();
            if (codigoLimpio.length() > 12) { // no puede superar los 12 caracteres
                errores.add("No puede superar los 12 caracteres");
            }
            if (!codigoLimpio.matches("[A-Z0-9-]+")) {
                errores.add("No cumple con el patron valido");
            }

            // hay que comprobar si existe en memoria
            boolean duplicado = false;
            int i = 0; // para recorrer todos los productos
            while (i < listaReferencia.size() && !duplicado) { // mientras no tenga mas posiciones de procutos que
                                                               // recorrer y
                // no haya encontradado un duplicado
                Producto prod = listaReferencia.get(i);
                if (prod.getCodigo() != null && prod.getCodigo().equalsIgnoreCase(codigoLimpio)) {
                    duplicado = true;

                }
                i++;

            }

            if (duplicado == true) {
                errores.add("Codigo duplicado");
            }
        }

        // 2. Comprobaciones del nombre
        if (p.getNombre() == null || p.getNombre().trim().length() < 3 || p.getNombre().trim().length() > 100) {
            errores.add("El nombre debe tener entre 3 y 100 caracteres además de no poder estar vacio");
        }

        // 3. Comprobaciones del precio
        if (p.getPrecio() == null || p.getPrecio().doubleValue() <= 0) {
            errores.add("precio debe ser mayor que 0");
        }

        // 4. Stock
        if (p.getStock() < 0) {
            errores.add("El stock no puede ser negativo");
        }

        // 5. CategoriaId y ProveedorId
        if (p.getCategoriaId() <= 0) {
            errores.add("El ID de categoría debe ser mayor que 0");
        }
        if (p.getProveedorId() <= 0) {
            errores.add("El ID de proveedor debe ser mayor que 0");
        }

        return errores;
    }

    /**
     * Metodo para importar JSON con comprobaciones y validando el producto en
     * base a las reglas impuestas en nuestro metodo validarProducto
     *
     * @param ruta
     */
    public void importarJson(String ruta) {
        // primero valido la ruta
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "No puedes introducir una ruta vacia" + ColoresConsola.RESET);
            return; // salgo
        }

        // si no esta vacia le quito espacios y verifico que exista
        File archivo = new File(ruta.trim());
        try {
            if (!archivo.exists() || !archivo.isFile()) { // debe de existir y no ser un directorio
                System.out.println(
                        ColoresConsola.ROJO + "La ruta introducida no existe/es un directorio" + ColoresConsola.RESET);
                return;
            }

        } catch (SecurityException se) {
            System.out.println(ColoresConsola.ROJO + "Error de permisos: " + se.getMessage() + ColoresConsola.RESET);
            return;
        } catch (Exception e) {
            System.out.println(ColoresConsola.ROJO + "Error imprevisto: " + e.getMessage() + ColoresConsola.RESET);
            return;
        }

        // uso el try-with-resources para el cierre automatico del FileReader
        try (FileReader reader = new FileReader(archivo)) {
            // convierto el contenido del archivo en una estructura JSON
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            // verifico que la raiz tenga "productos" y que efectivamente sea un array
            if (!root.has("productos") || !root.get("productos").isJsonArray()) {
                System.out.println(
                        ColoresConsola.ROJO + "La raiz debe contener el array de productos" + ColoresConsola.RESET);
                return;
            }

            // obtengo el array de productos y preparo el Gson para la conversion
            JsonArray arrayproductos = root.getAsJsonArray("productos");

            Gson gson = new Gson();

            // inicializo contadores y preparo la lista donde guardo los mensajes de error
            int encontrados = arrayproductos.size();
            int rechazados = 0;
            int importados = 0;

            List<String> incidencias = new ArrayList<>();

            for (int i = 0; i < encontrados; i++) {

                try {
                    // Verificamos que el elemento actual del array sea efectivamente un objeto JSON
                    if (!arrayproductos.get(i).isJsonObject()) {
                        throw new IllegalArgumentException("El elemento no es un objeto JSON válido.");
                    }

                    JsonObject elem = arrayproductos.get(i).getAsJsonObject();

                    // Validamos que los campos obligatorios existan y no sean tipos incompatibles
                    if (!elem.has("codigo") || elem.get("codigo").isJsonNull()) {
                        throw new IllegalArgumentException("El código es obligatorio (campo ausente/vacío)");
                    }
                    if (!elem.has("precio") || elem.get("precio").isJsonNull()) {
                        throw new IllegalArgumentException("El precio es obligatorio (campo ausente/vacío)");
                    }
                    if (!elem.has("stock") || elem.get("stock").isJsonNull()) {
                        throw new IllegalArgumentException("El stock es obligatorio (campo ausente/vacío)");
                    }

                    // deserializo el objeto en una instancia de la clase Producto
                    Producto p = gson.fromJson(elem, Producto.class);

                    // limpio espacios del codigo del producto
                    if (p.getCodigo() != null) { // si es nulo e intento hacer .trim tendria problemas
                        p.setCodigo(p.getCodigo().trim());
                    }

                    // validamos las reglas predefinidas antes
                    List<String> errores = validarProducto(p, productos);

                    // si esta vacio no hay errores
                    if (errores.isEmpty()) {
                        productos.add(p);
                        importados++;
                    } else {
                        rechazados++; // tiene minimo un error

                        // construyo la cadena con los motivos de rechazo
                        StringBuilder mensajeError = new StringBuilder();

                        for (int j = 0; j < errores.size(); j++) {
                            mensajeError.append(errores.get(j));

                            if (j < errores.size() - 1) {
                                mensajeError.append(", "); // separador entre errores
                            }
                        }
                        // guardo la incidencia pero comprobando que el codigo no sea nulo
                        String cod;
                        if (p.getCodigo() != null && !p.getCodigo().isEmpty()) {
                            cod = p.getCodigo();
                        } else {
                            cod = "Producto " + (i + 1);
                        }

                        incidencias.add(cod + ": " + mensajeError.toString());
                    }

                } catch (IllegalArgumentException iae) {
                    rechazados++;
                    incidencias.add("Producto " + (i + 1) + ": " + iae.getMessage());
                } catch (Exception e) {
                    rechazados++;
                    incidencias.add("Producto " + (i + 1) + ": Error en el formato de datos.");
                }

            }

            // resultado
            System.out.println("--- IMPORTAR JSON ---");
            System.out.println("Ruta: " + ruta);
            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);

            // si se registraron incidencias durante el proceso, las muestro
            if (!incidencias.isEmpty()) {
                System.out.println("INCIDENCIAS:");
                for (String inc : incidencias) {
                    System.out.println("  - " + inc);
                }
                System.out.println("Importación finalizada con incidencias");
            } else {
                System.out.println("Importación finalizada con éxito");
            }
        } catch (JsonSyntaxException jse) { // suelen ser errores de sintaxis del JSON
            System.out
                    .println(ColoresConsola.ROJO + "Error: El fichero JSON está malformado. Se cancela la importación. "
                            + jse.getMessage() + ColoresConsola.RESET);
        } catch (Exception e) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR al leer el fichero: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

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
                        List<String> errores = validarProducto(p, this.productos);

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

    private void ordenarPorCodigo(List<Producto> productos2) {
        // ordeno por el metodo burbuja
        int n = productos2.size();
        for (int i = 0; i < n - 1; i++) { // este for se encarga de repetir el proceso hasta que se culmine la
            // ordenacion
            for (int j = 0; j < n - i - 1; j++) { // este for nos ayuda a comparar a "vecinos" es decir de dos en dos
                String c1 = productos2.get(j).getCodigo();
                // siempre seleccionamos el que este un puesto a la derecha del
                String c2 = productos2.get(j + 1).getCodigo();

                // tengo que tener cuidado ya que no puedo hacer un compare.ToIgnoraCase si uno
                // de los dos es null
                if (c1 == null) {
                    c1 = "";
                }
                if (c2 == null) {
                    c2 = "";
                }

                // si el código actual es alfabeticamente mayor que el siguiente se intercambian
                if (c1.compareToIgnoreCase(c2) > 0) {
                    Producto temp = productos2.get(j);
                    productos2.set(j, productos2.get(j + 1));
                    productos2.set(j + 1, temp);
                }

            }

        }
    }

    /**
     * Metodo para listar todos los productos ordenados por codigo
     */

    public void listarTodos() {
        if (productos.isEmpty()) { // si esta vacio no debo mostrar nada mas que el aviso
            System.out.println(ColoresConsola.AMARILLO + "El inventario esta vacio" + ColoresConsola.RESET);
            return;
        }
        // ordeno por codigo
        // copiamos los productos antes de ordenarlos para lo alterar el orden.
        List<Producto> copiaOrdenada = new ArrayList<>(productos);
        ordenarPorCodigo(copiaOrdenada);

        System.out.println("---- LISTADO DE PRODUCTOS ----");
        for (int k = 0; k < copiaOrdenada.size(); k++) { // enseño todos los productos
            System.out.println(copiaOrdenada.get(k).toString());

        }
    }

    /**
     * Metodo para buscar un producto en base al codigo
     *
     * @param codigo el codigo a buscar
     */

    public void buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "El codigo no puede estar vacio" + ColoresConsola.RESET);
            return;
        }

        boolean encontrado = false;

        // en caso de que el codigo tenga algo entramos al for a comparar
        for (Producto producto : productos) {
            if (producto.getCodigo() != null) { // no puedo comparar si es null
                if (producto.getCodigo().equalsIgnoreCase(codigo.trim())) { // comparamos codigos y en caso de que
                                                                            // coincidan
                    // enseño su info
                    System.out.println(producto.toString());
                    encontrado = true;
                }

            }

        }

        if (!encontrado) {
            System.out.println(ColoresConsola.AMARILLO + "No se ha encontrado nada" + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para encontrar coincidencias en base al nombre introducido
     *
     * @param nombre el nombre a buscar
     */

    public void buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "El nombre no puede estar vacio" + ColoresConsola.RESET);
            return;
        }
        boolean encontrado = false;

        // en caso de que el nombre tenga coincidencia entramos al for a comparar
        for (Producto producto : productos) {
            if (producto.getNombre() != null) { // no puedo comparar si es null
                if (producto.getNombre().toLowerCase().contains(nombre.toLowerCase().trim())) {
                    System.out.println(producto.toString());
                    encontrado = true;
                }

            }

        }

        if (!encontrado) {
            System.out.println(ColoresConsola.AMARILLO + "No se ha encontrado nada" + ColoresConsola.RESET);
        }

    }

    /**
     * Metodo exportarJson que nos permite exportar el catalogo de productos a
     * un fichero en formato JSON usando la libreria GSON
     *
     * @param ruta Ruta del fichero donde se guaradara el JSON
     */
    public void exportarJson(String ruta) {
        // si no hay nada no exportamos
        if (productos.isEmpty()) {
            System.out.println(
                    ColoresConsola.ROJO + "El inventario está vacío. No hay nada que exportar." + ColoresConsola.RESET);
            return;
        }

        // la ruta debe ser valida
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

        // ordeno los productos sin modificar el inventario
        List<Producto> copiaOrdenada = new ArrayList<>(productos);
        ordenarPorCodigo(copiaOrdenada);

        // Construyo el objeto raiz que espera importarJSON
        JsonObject raiz = new JsonObject();
        raiz.add("productos", new Gson().toJsonTree(copiaOrdenada));

        // creo una instancia de Gson configurada para aplicar formato legible (pretty
        // printing)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // try-with-resources para abrir y cerrar el writer de forma segura
        try (FileWriter writer = new FileWriter(ruta.trim())) {

            // convierto la lista de productos completa a texto JSON
            gson.toJson(raiz, writer);

            System.out.println(ColoresConsola.VERDE + "Catalogo exportado correctamente a JSON en: " + ruta
                    + ColoresConsola.RESET);

        } catch (Exception e) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR al exportar a JSON: " + e.getMessage() + ColoresConsola.RESET);
        }

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
            ordenarPorCodigo(copiaOrdenada);

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

    /**
     * Metodo encargado de la serializacion de productos
     *
     * @param ruta donde se crea el archivo .dat
     */
    public void serializarProductos(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "La ruta no puede estar vacia" + ColoresConsola.RESET);
            return; // no sigo
        }

        if (productos.isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "No hay productos" + ColoresConsola.RESET);
            return;
        }

        // si es false no sigo
        if (!LectorConsola.confirmar("Todo preparado para la serializacion confirmacion: S/N")) {
            System.out.println(ColoresConsola.VERDE + "NEGACION RECIBIDA SALIENDO..." + ColoresConsola.RESET);
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta.trim()))) {
            oos.writeObject(productos);
            System.out.println(ColoresConsola.VERDE + productos.size() + " productos serializados correctamente"
                    + ColoresConsola.RESET);
        } catch (Exception e) {
            System.out.println(ColoresConsola.ROJO + "ERROR a la hora de serializar los productos: " + e.getMessage()
                    + ColoresConsola.RESET);

        }

    }

    /**
     * Metodo encargado de obtener los productos ya serializados
     *
     * @param ruta donde esta el archivo .dat
     */
    public void deserializarProductos(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "La ruta no puede estar vacia" + ColoresConsola.RESET);
            return; // no sigo
        }

        // compruebo si existe
        Path path = Paths.get(ruta.trim());
        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR: El fichero no existe o es un directorio." + ColoresConsola.RESET);
            return;
        }

        // si es false no sigo
        if (!LectorConsola.confirmar(
                "Todo preparado para la deserializacion confirmacion para sustituir los productos en memoria: S/N")) {
            System.out.println(ColoresConsola.VERDE + "NEGACION RECIBIDA SALIENDO..." + ColoresConsola.RESET);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            // obtengo los productos
            Object obtencion = ois.readObject();
            List<?> productosObtenidos = new ArrayList<>();

            // compruebo que sea una lista

            if (obtencion instanceof List<?>) {
                productosObtenidos = (List<?>) obtencion;
            } else {
                throw new IllegalArgumentException("La ruta devuelve un objeto que no es una lista");
            }
            // creo una lista auxiliar a la que añadire todos los productos en caso de que
            // no haya errores
            List<Producto> listaAuxiliar = new ArrayList<>();

            // si el producto cumple todo entonces va a mi lista auxiliar, pero si uno no
            // cumple los requisitos paramos todo el proceso (Todo o nada)
            for (Object obj : productosObtenidos) {
                if (obj instanceof Producto p) {
                    if (validarProducto(p, listaAuxiliar).isEmpty()) {
                        listaAuxiliar.add(p);
                    } else {
                        throw new IllegalArgumentException("Error a la hora de validar un producto");
                        // salgo
                    }
                } else {
                    throw new IllegalArgumentException("Se encontro un objeto que no corresponde a un producto");
                    // salgo
                }
            }

            // en caso de que salga todo todo bien vacio mi lista de productos y añado todos
            // los que puse en mi auxiliar

            productos.clear();
            productos.addAll(listaAuxiliar);
            System.out.println(ColoresConsola.VERDE + productos.size() + " productos obtenidos correctamente"
                    + ColoresConsola.RESET);

        } catch (Exception e) {
            System.out.println(ColoresConsola.ROJO + "ERROR: " + e.getMessage()
                    + ColoresConsola.RESET);
        }

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

    /**
     * Metodo para la generacion del fichero stock.pin a partir de la copia en
     * memoria (ordenada por codigo)
     *
     * @param ruta
     */
    public void generarStockBinario(String ruta) {
        // compruebo si el inventario de la lista esta vacio
        if (productos.isEmpty()) {
            System.out.println(
                    ColoresConsola.ROJO + "El inventario esta vacio no se generara el fichero" + ColoresConsola.RESET);
            return;
        }

        if (ruta == null || ruta.isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "La ruta esta vacia" + ColoresConsola.RESET);
            return;
        }

        // pido confirmacion al usuario antes de sobrescribir el archivo
        if (!LectorConsola
                .confirmar("Se va a generar/sobrescribir el fichero '" + ruta.trim() + "'. ¿Confirmar? (S/N): ")) {
            System.out.println(ColoresConsola.VERDE + "Operación cancelada por el usuario." + ColoresConsola.RESET);
            return;
        }

        // creo la copia independiente de los productos en memoria y la ordeno por
        // codigo
        List<Producto> copiaOrdenada = new ArrayList<>(productos);
        ordenarPorCodigo(copiaOrdenada);

        try {
            Path path = Paths.get(ruta.trim());
            // aseguro que la carpeta contenedora exista (en este caso es datos)
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent()); // en caso de que no exista /datos
            }

            // abro el fichero en modo lectura/escritura y trunco el contenido previo a 0
            // bytes
            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
                raf.setLength(0);

                // recorro la copia para escribir los registros con longitud fija de 36 bytes
                for (Producto p : copiaOrdenada) {
                    // obtengo y limpio el codigo
                    String codigoFijo = "";
                    if (p.getCodigo() != null) {
                        codigoFijo = p.getCodigo().trim();
                    }

                    // ajusto el codigo a exactamente 12 caracteres rellenando con espacios
                    if (codigoFijo.length() > 12) {
                        codigoFijo = codigoFijo.substring(0, 12);
                    } else {
                        while (codigoFijo.length() < 12) {
                            codigoFijo = codigoFijo + " ";
                        }
                    }
                    raf.writeChars(codigoFijo); // 12 chars = 24 bytes

                    // extraigo el precio convirtiendolo a double
                    double precioDouble = 0.0;
                    if (p.getPrecio() != null) {
                        precioDouble = p.getPrecio().doubleValue();
                    }
                    raf.writeDouble(precioDouble); // 8 bytes

                    // escribo el stock como entero
                    raf.writeInt(p.getStock()); // 4 bytes
                }

                System.out.println(ColoresConsola.VERDE + "Fichero " + ruta.trim() + " generado con éxito ("
                        + copiaOrdenada.size() + " registros, " + raf.length() + " bytes)." + ColoresConsola.RESET);
            }

        } catch (Exception e) {
            System.out.println(
                    ColoresConsola.ROJO + "ERROR al generar stock.bin: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para gestionar un registro en el fichero binario con seek
     *
     * @param ruta el fichero que se abre en modo lectura y escritura
     */
    public void gestionarAccesoAleatorioStock(String ruta) {
        // valido que la ruta recibida no sea nula ni este vacia
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "La ruta no puede estar vacía." + ColoresConsola.RESET);
            return;
        }

        // el fichero ha de existir
        Path path = Paths.get(ruta.trim());
        if (!Files.exists(path) || Files.isDirectory(path)) {
            System.out.println(ColoresConsola.ROJO + "ERROR: El fichero binario no existe." + ColoresConsola.RESET);
            return;
        }

        // abro el fichero en modo lectura y escritura
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            long tamanoTotal = raf.length();

            // valido que no este vacio y que sea múltiplo del tamaño de registro (36 bytes)
            if (tamanoTotal == 0 || tamanoTotal % 36 != 0) {
                System.out.println(ColoresConsola.ROJO + "ERROR: El fichero esta vacio o su tamaño (" + tamanoTotal
                        + " bytes) no es multiplo de 36." + ColoresConsola.RESET);
                return;
            }

            // calculo la cantidad total de registros en disco
            long totalRegistros = tamanoTotal / 36;

            System.out.println("---- ACCESO ALEATORIO ----");
            System.out.println("Fichero: " + ruta.trim());
            System.out.println("Registros: " + totalRegistros + " | Tamaño: " + tamanoTotal + " bytes");

            // solicito la posicion validando directamente el rango valido
            int pos = LectorConsola.leerOpcion("Consultar posición (0-" + (totalRegistros - 1) + "): ", 0,
                    (int) (totalRegistros - 1));

            // calculo el desplazamiento directo al inicio del registro
            long desplazamientoRegistro = (long) pos * 36;

            // me posiciono con el puntero al inicio del registro seleccionado
            raf.seek(desplazamientoRegistro);

            // leo los 12 caracteres correspondientes al codigo
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                sb.append(raf.readChar());
            }
            String codigo = sb.toString().trim();

            // obtengo el precio y el stock antiguo guardado en el fichero
            double precio = raf.readDouble();
            int stockAntiguo = raf.readInt();

            // muestro la informacion del registro leido
            System.out.println(codigo + " | " + precio + " euros | stock " + stockAntiguo);

            // solicito el nuevo valor del stock
            int nuevoStock = LectorConsola.leerEntero("Nuevo stock: ");

            // valido que no se introduzca un stock negativo
            if (nuevoStock < 0) {
                System.out.println(ColoresConsola.ROJO + "ERROR: El stock no puede ser un número negativo."
                        + ColoresConsola.RESET);
                return;
            }

            // calculo el desplazamiento directo hacia el campo stock (posicion * 36 + 32)
            long desplazamientoStock = desplazamientoRegistro + 32;

            // desplazo el puntero directamente al campo del entero sin reescribir la linea
            // completa
            raf.seek(desplazamientoStock);

            // sobrescribo los 4 bytes del entero stock en el fichero
            raf.writeInt(nuevoStock);

            // muestro la confirmacion del cambio realizado
            System.out.println("Actualización correcta: stock " + stockAntiguo + " -> " + nuevoStock);

        } catch (Exception e) {
            System.out.println(ColoresConsola.ROJO + "ERROR: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

}