package es.iesisidra.datastock.servicio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.repository.ProductoXmlRepository;
import es.iesisidra.datastock.util.ColoresConsola;

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

    // pasamos el inventario a inventarioXML, inventarioJSON e inventarioConsultas
    private final InventarioJSON inventarioJSON = new InventarioJSON(productos);
    private final InventarioXML inventarioXML = new InventarioXML(productos);
    private final InventarioConsultas inventarioConsultas = new InventarioConsultas(productos);
    private final InventarioDAT inventarioDAT = new InventarioDAT(productos);

    // instancio mi gestor de productos
    private final GestorProductos gestorProductos = new GestorProductos();

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
     * 
     * @Obtiene el gestor de productos que usa AccesoAleatorio
     */

    public GestorProductos getGestorProductos() {
    return gestorProductos;
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
        return gestorProductos.validarProducto(p, listaReferencia);
    }

    /**
     * Metodo para importar JSON con comprobaciones y validando el producto en
     * base a las reglas impuestas en nuestro metodo validarProducto
     *
     * @param ruta
     */
    public void importarJson(String ruta) {
        inventarioJSON.importarJson(ruta);
    }

    /**
     * Importa un catálogo de productos desde un fichero XML usando DOM. Lee el
     * fichero con el repositorio y valida los campos añade solo los registros
     * válidos al inventario.
     *
     * @param ruta la ruta de donde se importa el xml
     */
    public void importarXml(String ruta) {
        inventarioXML.importarXml(ruta);
    }

    private void ordenarPorCodigo(List<Producto> productos2) {
        gestorProductos.ordenarPorCodigo(productos2);
    }

    /**
     * Metodo para listar todos los productos ordenados por codigo
     */

    public void listarTodos() {
        inventarioConsultas.listarTodos();
    }

    /**
     * Metodo para buscar un producto en base al codigo
     *
     * @param codigo el codigo a buscar
     */

    public void buscarPorCodigo(String codigo) {
        inventarioConsultas.buscarPorCodigo(codigo);
    }

    /**
     * Metodo para encontrar coincidencias en base al nombre introducido
     *
     * @param nombre el nombre a buscar
     */

    public void buscarPorNombre(String nombre) {
        inventarioConsultas.buscarPorNombre(nombre);
    }

    /**
     * Metodo exportarJson que nos permite exportar el catalogo de productos a
     * un fichero en formato JSON usando la libreria GSON
     *
     * @param ruta Ruta del fichero donde se guaradara el JSON
     */
    public void exportarJson(String ruta) {
        inventarioJSON.exportarJson(ruta);
    }

    /**
     * Exporta el catálogo actual de productos a un fichero en formato XML
     * utilizando DOM
     *
     * @param ruta Ruta del fichero de destino donde se guarda el XML
     */
    public void exportarXml(String ruta) {
        inventarioXML.exportarXml(ruta);
    }


    /**
     * Metodo encargado de la serializacion de productos
     *
     * @param ruta donde se crea el archivo .dat
     */
    public void serializarProductos(String ruta) {
        inventarioDAT.serializarProductos(ruta);

    }

    /**
     * Metodo encargado de obtener los productos ya serializados
     *
     * @param ruta donde esta el archivo .dat
     */
    public void deserializarProductos(String ruta) {
        inventarioDAT.deserializarProductos(ruta);

    }

    

}