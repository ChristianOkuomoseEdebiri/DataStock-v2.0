package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

public class GestorProductos {
    /**
     * Valida un producto según las reglas dadas Devuelve una lista de errores,
     * si esta vacia: el producto es valido
     *
     * @param p el producto que pasa por las validaciones
     * @return una lista con los errores
     */
    public List<String> validarProducto(Producto p, List<Producto> listaReferencia) {
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

    public void ordenarPorCodigo(List<Producto> productos2) {
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
    
}
