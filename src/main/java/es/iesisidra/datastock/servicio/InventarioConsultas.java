package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;

public class InventarioConsultas {

    private final List<Producto> productos;
    private final GestorProductos gestorProductos = new GestorProductos();

    public InventarioConsultas(List<Producto> productos) {
        this.productos = productos;
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
        gestorProductos.ordenarPorCodigo(copiaOrdenada);

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

}
