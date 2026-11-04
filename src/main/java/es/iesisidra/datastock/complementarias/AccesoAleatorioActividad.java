package es.iesisidra.datastock.complementarias;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.GestorProductos;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Punto de entrada reservado para RF-FIC-08: RandomAccessFile.
 *
 */
public final class AccesoAleatorioActividad {

    private final List<Producto> productos;
    private final GestorProductos gestorProductos;

    public AccesoAleatorioActividad(
            List<Producto> productos,
            GestorProductos gestorProductos) {

        this.productos = productos;
        this.gestorProductos = gestorProductos;
    }

    public static void main(String[] args) {
        Inventario inventario = new Inventario();

        // Cargamos los productos desde el XML
        inventario.importarXml("datos/ejemplos/catalogo_valido.xml");

        // Creamos la actividad con las dependencias del inventario
        AccesoAleatorioActividad actividad
                = new AccesoAleatorioActividad(
                        inventario.getProductos(),
                        inventario.getGestorProductos()
                );

        // Generamos el fichero binario
        actividad.generarStockBinario("datos/salidas/stock.bin");

        // Gestionamos el acceso aleatorio
        actividad.gestionarAccesoAleatorioStock("datos/salidas/stock.bin");
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
        gestorProductos.ordenarPorCodigo(copiaOrdenada);

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
