package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class InventarioJSON {
    // cabe aclarar que aqui nos preparamos para pasar la lista de productos pero realmente se pasa en Inventario
    private final List<Producto> productos;

    // instancio el gestor para poder hacer uso de metodos de gestion de productos
    private final GestorProductos gestorProductos;

    public InventarioJSON(List<Producto> productos) {
        this.productos = productos;
        this.gestorProductos = new GestorProductos();
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
                    List<String> errores = gestorProductos.validarProducto(p, productos);

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
        gestorProductos.ordenarPorCodigo(copiaOrdenada);

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



}
