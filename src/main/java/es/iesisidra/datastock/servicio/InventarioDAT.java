package es.iesisidra.datastock.servicio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class InventarioDAT {
    private final List<Producto> productos;
    private final GestorProductos gestorProductos = new GestorProductos();

    public InventarioDAT(List<Producto> productos) {
        this.productos = productos;
    }

    /**
     * Metodo encargado de la serializacion de productos
     *
     * @param ruta donde se crea el archivo .dat
     */
    public void serializarProductos(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] La ruta no puede estar vacia" + ColoresConsola.RESET);
            return; // no sigo
        }

        if (productos.isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] No hay productos" + ColoresConsola.RESET);
            return;
        }

        // si es false no sigo
        if (!LectorConsola.confirmar("[AVISO] Todo preparado para la serializacion confirmacion: S/N")) {
            System.out.println(ColoresConsola.VERDE + "NEGACION RECIBIDA SALIENDO..." + ColoresConsola.RESET);
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta.trim()))) {
            oos.writeObject(productos);
            System.out.println(ColoresConsola.VERDE + productos.size() + " productos serializados correctamente"
                    + ColoresConsola.RESET);
        } catch (Exception e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] a la hora de serializar los productos: " + e.getMessage()
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
                    if (gestorProductos.validarProducto(p, listaAuxiliar).isEmpty()) {
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
            System.out.println(ColoresConsola.VERDE + productos.size() + " [OK] productos obtenidos correctamente"
                    + ColoresConsola.RESET);

        } catch (Exception e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR]: " + e.getMessage()
                    + ColoresConsola.RESET);
        }

    }
    
}
