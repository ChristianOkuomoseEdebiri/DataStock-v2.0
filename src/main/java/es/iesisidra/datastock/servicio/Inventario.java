package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    // pasamos el inventario a inventarioXML, inventarioJSON e inventarioConsultas
    private final InventarioJSON inventarioJSON = new InventarioJSON(productos);
    private final InventarioXML inventarioXML = new InventarioXML(productos);
    private final InventarioConsultas inventarioConsultas = new InventarioConsultas(productos);

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