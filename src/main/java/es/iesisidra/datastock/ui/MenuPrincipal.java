package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.servicio.GestorBaseDatos;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>
 * Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación y
 * el procesamiento en métodos o clases independientes.</p>
 */
public class MenuPrincipal {

    private final Inventario inventario;
    private final GestorBaseDatos gestorBD = new GestorBaseDatos();

    /**
     * @param inventario colección común de productos de esta ejecución.
     */
    public MenuPrincipal(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Ejecuta el menú principal hasta que el usuario selecciona salir.
     */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("7. Gestionar base de datos MySQL");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 7);
            switch (opcion) {
                case 1 -> {
                    // pedimos la ruta
                    String ruta = LectorConsola.leerTexto("Introduce la ruta");
                    // llamamos al metodo
                    inventario.explorarRuta(ruta);
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero JSON: ");
                    inventario.importarJson(ruta);
                }
                case 3 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero XML: ");
                    inventario.importarXml(ruta);
                }
                case 4 ->
                    menuConsultas();
                case 5 ->
                    menuExportaciones();
                case 6 ->
                    menuSerializacion();
                case 7 ->
                    menuBD();
                case 0 ->
                    System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));

                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
    }

    private void mostrarCabeceraUT2() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         GESTIÓN MYSQL - UT2", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
    }

    private void menuBD () {
        int opcion;

        do {
            mostrarCabeceraUT2();
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            System.out.println("=========================================");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 7);

            switch (opcion) {
                case 1 -> {
                    gestorBD.comprobarConexion();
                }
            

                case 2 -> {
                    menuCategorias();
                }
                    
                
                case 3 -> {
                    menuProveedores();
                }

                case 4 -> {
                    menuProducto();
                }

                case 5 -> {
                    menuconsultaProducto();
                }

                case 6 -> {
                    menuRegistros();
                }

                case 7 -> {
                    menuHistorial();
                }

                case 8 -> {
                    gestorBD.copiarInventarioAMySQL(inventario.getProductos());
                }

                case 9 -> {
                    gestorBD.transacciones();
                    
                }


                default -> {
                    throw new IllegalStateException("Opción inesperada");
                }
                    
            }


            
        } while (opcion != 0);
        

    }

    private void menuConsultas() {
        int opcion;
        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 3);
            switch (opcion) {
                case 1 -> {
                    inventario.listarTodos();
                }
                case 2 -> {
                    String codigo = LectorConsola.leerTexto("Introduce el codigo");
                    inventario.buscarPorCodigo(codigo);
                }
                case 3 -> {
                    String nombre = LectorConsola.leerTexto("Introduce el nombre");
                    inventario.buscarPorNombre(nombre);
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuExportaciones() {
        int opcion;
        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta en donde exportar los productos");
                    inventario.exportarJson(ruta);
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta en donde exportar los productos");
                    inventario.exportarXml(ruta);
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuSerializacion() {
        int opcion;
        do {
            System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta");
                    inventario.serializarProductos(ruta);
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta");
                    inventario.deserializarProductos(ruta);
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }


    private void menuCategorias() {
        int opcion;
        do {
            System.out.println("1. Dar de alta una categoria");
            System.out.println("2. Listar categorias");
            System.out.println("3. Modificar el nombre de una categoria");
            System.out.println("4. Eliminar una categoria");
            System.out.println("0. Volver al menu MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opcion", 0, 4);
            
            switch (opcion) {
                case 1 -> {
                    String nombre = LectorConsola.leerTexto("Introduce el nombre");
                    int id = LectorConsola.leerEntero("Introduce el identificador");
                    gestorBD.altaCategoria(id, nombre);
                }
                case 2 -> {
                    gestorBD.listarCategorias();
                    
                }
                case 3 -> {
                    int id = LectorConsola.leerEntero("Introduce el identificador");
                    gestorBD.modificarNombreCategoria(id);

                }
                case 4 -> {
                    int id = LectorConsola.leerEntero("Introduce el identificador");
                    gestorBD.eliminarCategoria(id);
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
            
        
            
        } while (opcion != 0);
    }

    private void menuProveedores() {
        int opcion;
        do {
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar el nombre y/o email de un proveedor");
            System.out.println("4. Eliminar un proveedor");
            System.out.println("0. Volver al menu MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opcion", 0, 4);
            
            switch (opcion) {
                case 1 -> {
                    String nombre = LectorConsola.leerTexto("Introduce el nombre");
                    int id = LectorConsola.leerEntero("Introduce el identificador");
                    gestorBD.altaProveedor(id, nombre);
                }
                case 2 -> {
                    gestorBD.listarProveedor();
                    
                }
                case 3 -> {
                    int id = LectorConsola.leerEntero("Introduce el identificador");
                    gestorBD.modificarNombreProveedor(id);

                }
                case 4 -> {
                    int id = LectorConsola.leerEntero("Introduce el identificador");
                    gestorBD.eliminarProveedor(id);
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
            
        
            
        } while (opcion != 0);
    }

    private void menuProducto() {
        int opcion;
        do {
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por codigo");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menu MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opcion", 0, 4);
            
            switch (opcion) {
                case 1 -> {
                    gestorBD.altaProducto();
                }
                case 2 -> {
                    gestorBD.consultaProductoPorCodigo();
                    
                }
                case 3 -> {
                    gestorBD.modificarProducto();
                    

                }
                case 4 -> {
                    gestorBD.eliminarProducto();
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
            
        
            
        } while (opcion != 0);
    }

    private void menuconsultaProducto() {
        int opcion;
        do {
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por codigo");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock inferior a un umbral");
            System.out.println("0. Volver al menu MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opcion", 0, 4);
            
            switch (opcion) {
                case 1 -> {
                    gestorBD.listarProductos();
                }
                case 2 -> {
                    gestorBD.consultaProductoPorCodigo(); // reutilizamos el metodo
                }
                case 3 -> {
                    gestorBD.buscarProductoPorNombre();
                }
                case 4 -> {
                    gestorBD.consultarProductosPorStockInferior();
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
            
        
            
        } while (opcion != 0);
    }

    private void menuRegistros() {
        int opcion;
        do {
            System.out.println("1. Registrar una ENTRADA de stock");
            System.out.println("2. Registrar una SALIDA de stock");
            opcion = LectorConsola.leerOpcion("Selecciona una opcion", 0, 2);
            
            switch (opcion) {
                case 1 -> {
                    gestorBD.registrarEntrada();
                }
                case 2 -> {
                    gestorBD.registrarSalida();
                    
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
            
        
            
        } while (opcion != 0);
    }

    private void menuHistorial() {
        int opcion;
        do {
            System.out.println("1. Consultar todos los movimientos");
            System.out.println("2. Consultar movimientos de un producto");
            opcion = LectorConsola.leerOpcion("Selecciona una opcion", 0, 2);
            
            switch (opcion) {
                case 1 -> {
                    gestorBD.consultarTodosLosMovimientos();
                }
                case 2 -> {
                    gestorBD.consultarMovimientosPorProducto();
                    
                }
                case 0 -> {
                }
                default ->
                    throw new IllegalStateException("Opción inesperada");
            }
            
        
            
        } while (opcion != 0);
    }





    /**
     * Sustituir las llamadas por servicios propios a medida que se implementen
     * los RF.
     */
    /*private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar("[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    } */
}
