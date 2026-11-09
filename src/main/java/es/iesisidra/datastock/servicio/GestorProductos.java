package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

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

    /**
     * Valida un producto según las reglas dadas Devuelve una lista de errores,
     * si esta vacia: el producto es valido
     *
     * @param p el producto que pasa por las validaciones
     * @return una lista con los errores
     */
    public List<String> validarProducto(Producto p) {
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
        }

        // 2. Comprobaciones del nombre
        if (p.getNombre() == null || p.getNombre().trim().length() < 3 || p.getNombre().trim().length() > 100) {
            errores.add("El nombre debe tener entre 3 y 100 caracteres además de no poder estar vacio");
        }

        // 3. Comprobaciones del precio
        if (p.getPrecio() == null || p.getPrecio().doubleValue() <= 0) {
            errores.add("precio debe ser mayor que 0");

            if (p.getPrecio().scale() > 2) {
                errores.add("precio maximo 2 decimales");
            }

            if ((p.getPrecio().precision() - p.getPrecio().scale()) > 8) {
                errores.add("precio no cumple con el rango (DECIMAL(10,2))");
            }
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

    // los siguientes metodos corresponden a la UT2
    public void altaProducto() {
        String codigo = LectorConsola.leerTexto("Introduce el codigo"); // tengo que convertirlo en mayusculas
        String nombre = LectorConsola.leerTexto("Introduce el nombre");
        double precio = LectorConsola.leerDouble("Introduce el precio");
        BigDecimal precioValido = BigDecimal.valueOf(precio);
        int stock = LectorConsola.leerEntero("Introduce el stock");
        int idCategoria = LectorConsola.leerEntero("Introduce el id de categoria");
        int idProveedor = LectorConsola.leerEntero("Introduce el id de proveedor");

        Producto p = new Producto(codigo.trim().toUpperCase(), nombre, precioValido, stock, idCategoria, idProveedor);

        // comprobamos si cumple las reglas de negocio
        List<String> errores = validarProducto(p);

        if (!errores.isEmpty()) { // caso malo
            System.out.println("== SE HAN ENCONTRADO ERRORES ==");
            for (String error : errores) {
                System.out.println(ColoresConsola.ROJO + "- " + error + "  -" + ColoresConsola.RESET);
            }
            return;
        }

        // ahora compruebo con la base de datos si el codigo existe y si ambas referencias existen en MySQL
        String buscarCodigo = "SELECT * FROM producto WHERE codigo = ?";
        String referenciaCategoria = "SELECT * FROM categoria WHERE id = ?";
        String referenciaProveedor = "SELECT * FROM proveedor WHERE id = ?";
        String insertarProducto = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psCodigo = con.prepareStatement(buscarCodigo); PreparedStatement psCategoria = con.prepareStatement(referenciaCategoria); PreparedStatement psProveedor = con.prepareStatement(referenciaProveedor); PreparedStatement psInsertar = con.prepareStatement(insertarProducto)) {

            psCodigo.setString(1, p.getCodigo());
            try (ResultSet rs = psCodigo.executeQuery()) {
                if (rs.next()) {  // si ya hay un codigo con ese id
                    System.out.println(ColoresConsola.ROJO + "[ERROR] el codigo esta duplicado" + ColoresConsola.RESET);
                    return;
                }
            }

            psCategoria.setInt(1, p.getCategoriaId());
            try (ResultSet rs = psCategoria.executeQuery()) {
                if (!rs.next()) { // si no hay nada
                    System.out.println(ColoresConsola.ROJO + "[ERROR] el codigo de la categoria no existe" + ColoresConsola.RESET);
                    return;
                }
            }

            psProveedor.setInt(1, p.getProveedorId());
            try (ResultSet rs = psProveedor.executeQuery()) {
                if (!rs.next()) { // si no hay nada
                    System.out.println(ColoresConsola.ROJO + "[ERROR] el codigo del proveedor no existe" + ColoresConsola.RESET);
                    return;
                }
            }

            // si llego hasta aqui hemos pasado todas las validaciones, debemos introducir el producto en la base de datos
            psInsertar.setString(1, p.getCodigo().trim());
            psInsertar.setString(2, p.getNombre().trim());
            psInsertar.setBigDecimal(3, p.getPrecio());
            psInsertar.setInt(4, p.getStock());
            psInsertar.setInt(5, p.getCategoriaId());
            psInsertar.setInt(6, p.getProveedorId());

            int filasIntroducidas = psInsertar.executeUpdate();

            if (filasIntroducidas >= 1) {
                System.out.println(ColoresConsola.VERDE + "[OK] PRODUCTO DADO DE ALTA CORRECTAMENTE, FILAS INTRODUCIDAS: " + filasIntroducidas + ColoresConsola.RESET);
            } else {
                System.out.println(ColoresConsola.ROJO + "[ERROR] NO SE PUEDO INTRODUCIR EL PRODUCTO" + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        }

    }

    /**
     * Metodo para consultar un producto por su codigo mostrando todos sus datos
     * y los nombres de su categoria y proveedor asociados
     */
    public void consultarProductoPorCodigo() {
        String codigoIntroducido = LectorConsola.leerTexto("Introduce el codigo del producto");

        if (codigoIntroducido == null || codigoIntroducido.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El codigo no puede estar vacio" + ColoresConsola.RESET);
            return;
        }

        // normalizo el codigo quitando espacios y pasandolo a mayusculas
        String codigoLimpio = codigoIntroducido.trim().toUpperCase();

        // uso INNER JOIN con alias para traer de golpe los nombres de categoria y proveedor
        String sqlConsulta = "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
                + "c.nombre AS nombre_categoria, pr.nombre AS nombre_proveedor "
                + "FROM producto p "
                + "INNER JOIN categoria c ON p.categoria_id = c.id "
                + "INNER JOIN proveedor pr ON p.proveedor_id = pr.id "
                + "WHERE p.codigo = ?";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sqlConsulta)) {

            // establezco el codigo en la posicion de la interrogacion
            ps.setString(1, codigoLimpio);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { // si no entra es que no existe ningun producto con ese codigo
                    System.out.println(ColoresConsola.AZUL + "[AVISO] No existe ningun producto con el codigo: " + codigoLimpio + ColoresConsola.RESET);
                    return;
                }

                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                BigDecimal precio = rs.getBigDecimal("precio");
                int stock = rs.getInt("stock");
                int idCategoria = rs.getInt("categoria_id");
                String nombreCategoria = rs.getString("nombre_categoria");
                int idProveedor = rs.getInt("proveedor_id");
                String nombreProveedor = rs.getString("nombre_proveedor");

                System.out.println("[CODIGO] " + codigo
                        + " [NOMBRE] " + nombre
                        + " [PRECIO] " + precio
                        + " [STOCK] " + stock
                        + " [CATEGORIA] " + idCategoria + " (" + nombreCategoria + ")"
                        + " [PROVEEDOR] " + idProveedor + " (" + nombreProveedor + ")");
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para modificar el nombre, precio, categoria y proveedor de un
     * producto conservando el valor actual si se pulsa unicamente Intro
     */
    public void modificarProducto() {
        String codigoIntroducido = LectorConsola.leerTexto("Introduce el codigo del producto a modificar");

        if (codigoIntroducido == null || codigoIntroducido.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El codigo no puede estar vacio" + ColoresConsola.RESET);
            return;
        }

        String codigoLimpio = codigoIntroducido.trim().toUpperCase();

        String consultaCodigo = "SELECT codigo, nombre, precio, stock, categoria_id, proveedor_id FROM producto WHERE codigo = ?";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psCodigo = con.prepareStatement(consultaCodigo)) {

            psCodigo.setString(1, codigoLimpio);

            try (ResultSet rs = psCodigo.executeQuery()) {
                if (!rs.next()) { // si no hay algo en la consulta es que NO ha devuelto un producto
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No existe un producto con ese codigo" + ColoresConsola.RESET);
                    return;
                }

                // guardo los valores actuales por si el usuario pulsa Intro para conservarlos
                String nombreActual = rs.getString("nombre");
                BigDecimal precioActual = rs.getBigDecimal("precio");
                int stockActual = rs.getInt("stock");
                int categoriaActual = rs.getInt("categoria_id");
                int proveedorActual = rs.getInt("proveedor_id");

                System.out.println("DATOS ACTUALES -> NOMBRE: " + nombreActual
                        + " | PRECIO: " + precioActual
                        + " | STOCK (no modificable aqui): " + stockActual
                        + " | ID CATEGORIA: " + categoriaActual
                        + " | ID PROVEEDOR: " + proveedorActual);
                System.out.println("SI SE INTRODUCE UNICAMENTE INTRO SE CONSERVA EL DATO ACTUAL");

                // 1. Lectura del nuevo nombre
                String nombreIntroducido = LectorConsola.leerTexto("Introduce el nuevo nombre");
                boolean cambioNombre = (nombreIntroducido != null && !nombreIntroducido.trim().isEmpty());

                // 2. Lectura del nuevo precio (leemos como texto para detectar el Intro)
                String precioTexto = LectorConsola.leerTexto("Introduce el nuevo precio");
                boolean cambioPrecio = (precioTexto != null && !precioTexto.trim().isEmpty());

                // 3. Lectura de la nueva categoria
                String categoriaTexto = LectorConsola.leerTexto("Introduce el nuevo ID de categoria");
                boolean cambioCategoria = (categoriaTexto != null && !categoriaTexto.trim().isEmpty());

                // 4. Lectura del nuevo proveedor
                String proveedorTexto = LectorConsola.leerTexto("Introduce el nuevo ID de proveedor");
                boolean cambioProveedor = (proveedorTexto != null && !proveedorTexto.trim().isEmpty());

                if (!cambioNombre && !cambioPrecio && !cambioCategoria && !cambioProveedor) {
                    System.out.println(ColoresConsola.VERDE + "No se realizan cambios" + ColoresConsola.RESET);
                    return;
                }

                // empezamos asumiendo que los valores finales son los actuales
                String nombreFinal = nombreActual;
                BigDecimal precioFinal = precioActual;
                int categoriaFinal = categoriaActual;
                int proveedorFinal = proveedorActual;

                if (cambioNombre) {
                    nombreFinal = nombreIntroducido.trim();
                }

                if (cambioPrecio) {
                    try {
                        // reemplazo la coma por punto por si el usuario escribe decimales con coma
                        precioFinal = new BigDecimal(precioTexto.trim().replace(",", "."));
                    } catch (NumberFormatException e) {
                        System.out.println(ColoresConsola.ROJO + "[ERROR] El precio introducido no es un numero valido" + ColoresConsola.RESET);
                        return;
                    }
                }

                if (cambioCategoria) {
                    try {
                        categoriaFinal = Integer.parseInt(categoriaTexto.trim());
                    } catch (NumberFormatException e) {
                        System.out.println(ColoresConsola.ROJO + "[ERROR] El ID de categoria debe ser un numero entero" + ColoresConsola.RESET);
                        return;
                    }
                }

                if (cambioProveedor) {
                    try {
                        proveedorFinal = Integer.parseInt(proveedorTexto.trim());
                    } catch (NumberFormatException e) {
                        System.out.println(ColoresConsola.ROJO + "[ERROR] El ID de proveedor debe ser un numero entero" + ColoresConsola.RESET);
                        return;
                    }
                }

                // reutilizo mi metodo validarProducto creando un objeto temporal con los datos finales
                Producto prodModificado = new Producto(codigoLimpio, nombreFinal, precioFinal, stockActual, categoriaFinal, proveedorFinal);
                List<String> errores = validarProducto(prodModificado);

                if (!errores.isEmpty()) { // caso malo
                    System.out.println("== SE HAN ENCONTRADO ERRORES ==");
                    for (String error : errores) {
                        System.out.println(ColoresConsola.ROJO + "- " + error + " -" + ColoresConsola.RESET);
                    }
                    return;
                }

                String referenciaCategoria = "SELECT id FROM categoria WHERE id = ?";
                String referenciaProveedor = "SELECT id FROM proveedor WHERE id = ?";
                String actualizar = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";

                try (PreparedStatement psCategoria = con.prepareStatement(referenciaCategoria); PreparedStatement psProveedor = con.prepareStatement(referenciaProveedor); PreparedStatement psActualizar = con.prepareStatement(actualizar)) {

                    // si ha cambiado la categoria, compruebo que exista en MySQL
                    if (cambioCategoria) {
                        psCategoria.setInt(1, categoriaFinal);
                        try (ResultSet rsCat = psCategoria.executeQuery()) {
                            if (!rsCat.next()) {
                                System.out.println(ColoresConsola.ROJO + "[ERROR] El ID de la categoria no existe en la base de datos" + ColoresConsola.RESET);
                                return;
                            }
                        }
                    }

                    // si ha cambiado el proveedor, compruebo que exista en MySQL
                    if (cambioProveedor) {
                        psProveedor.setInt(1, proveedorFinal);
                        try (ResultSet rsProv = psProveedor.executeQuery()) {
                            if (!rsProv.next()) {
                                System.out.println(ColoresConsola.ROJO + "[ERROR] El ID del proveedor no existe en la base de datos" + ColoresConsola.RESET);
                                return;
                            }
                        }
                    }

                    // pedimos confirmacion antes de hacer el UPDATE
                    if (!LectorConsola.confirmar("[AVISO] CONFIRMA EL CAMBIO DE DATOS S/N")) {
                        System.out.println(ColoresConsola.VERDE + "[RECIBIDO, SALIENDO CORRECTAMENTE...]" + ColoresConsola.RESET);
                        return;
                    }

                    psActualizar.setString(1, nombreFinal);
                    psActualizar.setBigDecimal(2, precioFinal);
                    psActualizar.setInt(3, categoriaFinal);
                    psActualizar.setInt(4, proveedorFinal);
                    psActualizar.setString(5, codigoLimpio);

                    int filasAfectadas = psActualizar.executeUpdate();
                    System.out.println(ColoresConsola.VERDE + "[OK ACTUALIZADO CORRECTAMENTE] filas afectadas: " + filasAfectadas + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo auxiliar para comprobar si un producto tiene movimientos
     * historicos asociados
     *
     * @param codigo el codigo del producto a comprobar
     * @return true si tiene movimientos o hay error, false si se puede borrar
     */
    private boolean tieneMovimientosAsociados(String codigo) {
        String sql = "SELECT COUNT(*) FROM movimiento_stock WHERE producto_codigo = ?";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No se puede eliminar el producto porque tiene " + rs.getInt(1) + " movimientos historicos asociados. Se conserva el historial." + ColoresConsola.RESET);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
            return true; // por seguridad si falla la consulta impedimos el borrado
        }

        return false;
    }

    /**
     * Metodo para eliminar un producto por su codigo comprobando antes su
     * integridad referencial
     */
    public void eliminarProducto() {
        String codigoIntroducido = LectorConsola.leerTexto("Introduce el codigo del producto a eliminar");

        if (codigoIntroducido == null || codigoIntroducido.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El codigo no puede estar vacio" + ColoresConsola.RESET);
            return;
        }

        String codigoLimpio = codigoIntroducido.trim().toUpperCase();

        String sqlProducto = "SELECT nombre, precio, stock FROM producto WHERE codigo = ?";
        String eliminar = "DELETE FROM producto WHERE codigo = ?";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sqlProducto); PreparedStatement psEliminar = con.prepareStatement(eliminar)) {

            ps.setString(1, codigoLimpio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    BigDecimal precio = rs.getBigDecimal("precio");
                    int stock = rs.getInt("stock");

                    System.out.println(ColoresConsola.AZUL + "PRODUCTO OBTENIDO -> [CODIGO] " + codigoLimpio
                            + " [NOMBRE] " + nombre
                            + " [PRECIO] " + precio
                            + " [STOCK] " + stock + ColoresConsola.RESET);
                } else {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No hay ningun producto asociado a ese codigo" + ColoresConsola.RESET);
                    return;
                }
            }

            // compruebo si tiene movimientos despues de mostrar sus datos
            if (tieneMovimientosAsociados(codigoLimpio)) {
                return;
            }

            if (!LectorConsola.confirmar("PERMISO PARA ELIMINAR EL PRODUCTO S/N")) {
                System.out.println(ColoresConsola.VERDE + "RECIBIDO, SALIENDO..." + ColoresConsola.RESET);
                return;
            }

            psEliminar.setString(1, codigoLimpio);
            int filasAfectadas = psEliminar.executeUpdate();

            System.out.println(ColoresConsola.VERDE + "PRODUCTO BORRADO CORRECTAMENTE FILAS AFECTADAS -> " + filasAfectadas + ColoresConsola.RESET);

        } catch (SQLException e) {
            // si aun asi salta la restriccion de clave foranea de MySQL, informamos en rojo sin borrar movimientos
            System.out.println(ColoresConsola.ROJO + "[ERROR] No se puede eliminar el producto porque tiene movimientos historicos asociados (" + e.getMessage() + ")" + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para listar todos los productos con los nombres de su categoria y proveedor
     * ordenados por codigo
     */
    public void listarProductos() {
        String listar = "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
                      + "c.nombre AS nombre_categoria, pr.nombre AS nombre_proveedor "
                      + "FROM producto p "
                      + "INNER JOIN categoria c ON p.categoria_id = c.id "
                      + "INNER JOIN proveedor pr ON p.proveedor_id = pr.id "
                      + "ORDER BY p.codigo"; // no pongo ASC ya que por defecto es ascendente

        try (Connection con = ConexionBD.obtenerConexion(); 
             PreparedStatement psListar = con.prepareStatement(listar);
             ResultSet rs = psListar.executeQuery()) {

            boolean vacio = true; // si entra en el bucle es que hay por lo menos un producto por lo que no estaria vacio

            while (rs.next()) {
                vacio = false;
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                BigDecimal precio = rs.getBigDecimal("precio");
                int stock = rs.getInt("stock");
                int idCategoria = rs.getInt("categoria_id");
                String nombreCategoria = rs.getString("nombre_categoria");
                int idProveedor = rs.getInt("proveedor_id");
                String nombreProveedor = rs.getString("nombre_proveedor");

                System.out.println("[CODIGO] " + codigo 
                                 + " [NOMBRE] " + nombre 
                                 + " [PRECIO] " + precio 
                                 + " [STOCK] " + stock 
                                 + " [CATEGORIA] " + idCategoria + " (" + nombreCategoria + ")"
                                 + " [PROVEEDOR] " + idProveedor + " (" + nombreProveedor + ")");
            }

            if (vacio) {
                System.out.println(ColoresConsola.AZUL + "[NO HAY PRODUCTOS REGISTRADOS]" + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para buscar productos cuyo nombre contenga un fragmento de texto
     */
    public void buscarProductosPorNombre() {
        String textoIntroducido = LectorConsola.leerTexto("Introduce el fragmento del nombre a buscar");

        if (textoIntroducido == null || textoIntroducido.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El fragmento de busqueda no puede estar vacio" + ColoresConsola.RESET);
            return;
        }

        String textoLimpio = textoIntroducido.trim();

        String buscarNombre = "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
                            + "c.nombre AS nombre_categoria, pr.nombre AS nombre_proveedor "
                            + "FROM producto p "
                            + "INNER JOIN categoria c ON p.categoria_id = c.id "
                            + "INNER JOIN proveedor pr ON p.proveedor_id = pr.id "
                            + "WHERE p.nombre LIKE ? "
                            + "ORDER BY p.codigo";

        try (Connection con = ConexionBD.obtenerConexion(); 
             PreparedStatement psBuscar = con.prepareStatement(buscarNombre)) {

            // concateno los % para buscar coincidencias parciales en cualquier posicion del nombre
            psBuscar.setString(1, "%" + textoLimpio + "%");

            try (ResultSet rs = psBuscar.executeQuery()) {
                boolean vacio = true;

                while (rs.next()) {
                    vacio = false;
                    String codigo = rs.getString("codigo");
                    String nombre = rs.getString("nombre");
                    BigDecimal precio = rs.getBigDecimal("precio");
                    int stock = rs.getInt("stock");
                    int idCategoria = rs.getInt("categoria_id");
                    String nombreCategoria = rs.getString("nombre_categoria");
                    int idProveedor = rs.getInt("proveedor_id");
                    String nombreProveedor = rs.getString("nombre_proveedor");

                    System.out.println("[CODIGO] " + codigo + " [NOMBRE] " + nombre + " [PRECIO] " + precio + " [STOCK] " + stock + " [CATEGORIA] " + idCategoria + " (" + nombreCategoria + ")" + " [PROVEEDOR] " + idProveedor + " (" + nombreProveedor + ")");
                }

                if (vacio) {
                    System.out.println(ColoresConsola.AZUL + "[NO SE HAN ENCONTRADO PRODUCTOS QUE COINCIDAN CON: " + textoLimpio + "]" + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para consultar productos con un stock inferior al umbral indicado
     */
    public void consultarProductosStockInferior() {
        int umbral = LectorConsola.leerEntero("Introduce el umbral de stock");

        if (umbral < 0) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El umbral no puede ser negativo" + ColoresConsola.RESET);
            return;
        }

        String buscarStock = "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
                           + "c.nombre AS nombre_categoria, pr.nombre AS nombre_proveedor "
                           + "FROM producto p "
                           + "INNER JOIN categoria c ON p.categoria_id = c.id "
                           + "INNER JOIN proveedor pr ON p.proveedor_id = pr.id "
                           + "WHERE p.stock < ? "
                           + "ORDER BY p.stock, p.codigo"; // ordenamos primero por menor stock y luego por codigo

        try (Connection con = ConexionBD.obtenerConexion(); 
             PreparedStatement psStock = con.prepareStatement(buscarStock)) {

            // establezco el umbral en la posicion de la interrogacion
            psStock.setInt(1, umbral);

            try (ResultSet rs = psStock.executeQuery()) {
                boolean vacio = true;

                while (rs.next()) {
                    vacio = false;
                    String codigo = rs.getString("codigo");
                    String nombre = rs.getString("nombre");
                    BigDecimal precio = rs.getBigDecimal("precio");
                    int stock = rs.getInt("stock");
                    int idCategoria = rs.getInt("categoria_id");
                    String nombreCategoria = rs.getString("nombre_categoria");
                    int idProveedor = rs.getInt("proveedor_id");
                    String nombreProveedor = rs.getString("nombre_proveedor");

                   System.out.println("[CODIGO] " + codigo + " [NOMBRE] " + nombre + " [PRECIO] " + precio + " [STOCK] " + stock + " [CATEGORIA] " + idCategoria + " (" + nombreCategoria + ")" + " [PROVEEDOR] " + idProveedor + " (" + nombreProveedor + ")");
                }

                if (vacio) {
                    System.out.println(ColoresConsola.AZUL + "[NO HAY PRODUCTOS CON STOCK INFERIOR A " + umbral + "]" + ColoresConsola.RESET);
                }
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para copiar los productos del inventario en memoria (UT1) a MySQL
     * dentro de una unica transaccion
     *
     * @param listaMemoria la lista de productos de la UT1
     */
    public void copiarInventarioAMySQL(List<Producto> listaMemoria) {
        // 1. Comprobacion inicial
        if (listaMemoria == null || listaMemoria.isEmpty()) {
            System.out.println(ColoresConsola.AZUL + "[AVISO] No hay productos para importar" + ColoresConsola.RESET);
            return;
        }

        System.out.println("NUMERO DE PRODUCTOS EN MEMORIA PARA IMPORTAR: " + listaMemoria.size());

        if (!LectorConsola.confirmar("CONFIRMA LA IMPORTACION A MYSQL S/N")) {
            System.out.println(ColoresConsola.VERDE + "RECIBIDO, SALIENDO..." + ColoresConsola.RESET);
            return;
        }

        String buscarCodigo = "SELECT * FROM producto WHERE codigo = ?";
        String referenciaCategoria = "SELECT * FROM categoria WHERE id = ?";
        String referenciaProveedor = "SELECT * FROM proveedor WHERE id = ?";
        String insertarProducto = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";

        int insertados = 0;
        int omitidos = 0;
        int rechazados = 0;

        try (Connection con = ConexionBD.obtenerConexion()) {
            // desactivamos el autocommit para que todo el lote vaya en una unica transaccion
            con.setAutoCommit(false);

            try (PreparedStatement psCodigo = con.prepareStatement(buscarCodigo);
                 PreparedStatement psCategoria = con.prepareStatement(referenciaCategoria);
                 PreparedStatement psProveedor = con.prepareStatement(referenciaProveedor);
                 PreparedStatement psInsertar = con.prepareStatement(insertarProducto)) {

                for (Producto p : listaMemoria) {

                    // compruebo si el codigo ya existe en MySQL (los duplicados se omiten)
                    if (p.getCodigo() != null && !p.getCodigo().trim().isEmpty()) {
                        psCodigo.setString(1, p.getCodigo().trim().toUpperCase());
                        try (ResultSet rs = psCodigo.executeQuery()) {
                            if (rs.next()) { // si ya existe en la BD lo omitimos y pasamos al siguiente
                                System.out.println(ColoresConsola.AZUL + "[OMITIDO] El producto con codigo " + p.getCodigo() + " ya existe en MySQL" + ColoresConsola.RESET);
                                omitidos++;
                                continue;
                            }
                        }
                    }

                    // compruebo si cumple las reglas de negocio reutilizando validarProducto(p)
                    List<String> errores = validarProducto(p);
                    if (!errores.isEmpty()) { // caso malo: se rechaza e indicamos el motivo
                        System.out.println(ColoresConsola.ROJO + "== PRODUCTO " + p.getCodigo() + " RECHAZADO POR ERRORES ==" + ColoresConsola.RESET);
                        for (String error : errores) {
                            System.out.println(ColoresConsola.ROJO + "- " + error + " -" + ColoresConsola.RESET);
                        }
                        rechazados++;
                        continue;
                    }

                    // compruebo si la categoria existe en MySQL
                    psCategoria.setInt(1, p.getCategoriaId());
                    try (ResultSet rs = psCategoria.executeQuery()) {
                        if (!rs.next()) { // si no hay nada
                            System.out.println(ColoresConsola.ROJO + "[RECHAZADO] Producto " + p.getCodigo() + ": el ID de la categoria no existe" + ColoresConsola.RESET);
                            rechazados++;
                            continue;
                        }
                    }

                    // compruebo si el proveedor existe en MySQL
                    psProveedor.setInt(1, p.getProveedorId());
                    try (ResultSet rs = psProveedor.executeQuery()) {
                        if (!rs.next()) { // si no hay nada
                            System.out.println(ColoresConsola.ROJO + "[RECHAZADO] Producto " + p.getCodigo() + ": el ID del proveedor no existe" + ColoresConsola.RESET);
                            rechazados++;
                            continue;
                        }
                    }

                    // si llego hasta aqui hemos pasado todas las validaciones, introducimos el producto
                    psInsertar.setString(1, p.getCodigo().trim().toUpperCase());
                    psInsertar.setString(2, p.getNombre().trim());
                    psInsertar.setBigDecimal(3, p.getPrecio());
                    psInsertar.setInt(4, p.getStock());
                    psInsertar.setInt(5, p.getCategoriaId());
                    psInsertar.setInt(6, p.getProveedorId());

                    psInsertar.executeUpdate();
                    insertados++;
                    System.out.println(ColoresConsola.VERDE + "[OK] Producto " + p.getCodigo() + " preparado para importar" + ColoresConsola.RESET);
                }

                // confirmamos todas las inserciones del lote al terminar el bucle
                con.commit();

                // resumen final
                System.out.println(ColoresConsola.VERDE + "[OK] IMPORTACION FINALIZADA -> Insertados: " + insertados + ColoresConsola.RESET);
                System.out.println(ColoresConsola.AZUL + "Omitidos por duplicado: " + omitidos + ColoresConsola.RESET);
                System.out.println(ColoresConsola.ROJO + "Rechazados por otras causas: " + rechazados + ColoresConsola.RESET);

            } catch (SQLException e) {
                // si ocurre un error SQL inesperado deshacemos todo el lote con rollback
                con.rollback();
                System.out.println(ColoresConsola.ROJO + "[ERROR] Error SQL inesperado. Se ha realizado ROLLBACK y no se ha confirmado ninguna insercion del lote: " + e.getMessage() + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }






}
