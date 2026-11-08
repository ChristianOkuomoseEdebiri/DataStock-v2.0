package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;

public class GestorBaseDatos {
    private GestorCategorias gestorCategorias = new GestorCategorias();
    private GestorProveedores gestorProveedores = new GestorProveedores();
    private GestorProductos gestorProductos = new GestorProductos();

    public void comprobarConexion() {
        System.out.println("Comprobando conexión...");

        String sql = "SELECT COUNT(*) FROM producto";

        // Bloque try-with-resources. para cerrar automaticamente al acabar y no exponernos a perdidas innecesarias de recursos
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // getMetaData() extrae información interna del servidor MySQL al que nos hemos conectado
            DatabaseMetaData meta = conn.getMetaData();
            
            System.out.println(ColoresConsola.VERDE + "[OK] Conexión establecida correctamente." + ColoresConsola.RESET);
            
            // getCatalog() le pregunta a la conexión en que base de datos estamos trabajando
            String nombreBD = conn.getCatalog();
            if (nombreBD != null) {
                System.out.println("Base de datos: " + nombreBD);
            } else {
                // Si el driver no devuelve el nombre, ponemos el nuestro por defecto
                System.out.println("Base de datos: datastock");
            }
            
            // Mostramos el nombre del motor (MySQL) y su versión exacta
            System.out.println("Gestor: " + meta.getDatabaseProductName());
            System.out.println("Versión: " + meta.getDatabaseProductVersion());

            // rs es el resultado de la consulta. Al ser un count sabemos que solo hay una fila
            if (rs.next()) {
                // rs.getInt(1) extrae el valor de la primera (y única) columna de esa fila
                System.out.println("Productos almacenados: " + rs.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] No se ha podido conectar a MySQL o acceder al esquema." + ColoresConsola.RESET);
            System.out.println(ColoresConsola.ROJO + "Motivo: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    // metodos correspondientes a categoria

    public void altaCategoria (int id, String nombre) {
        gestorCategorias.altaCategoria(id, nombre);
    }

    public void listarCategorias() {
        gestorCategorias.listarCategorias();
    }

    public void modificarNombreCategoria (int id) {
        gestorCategorias.modificarNombre(id);
    }

    public void eliminarCategoria (int id) {
        gestorCategorias.eliminarCategoria(id);
    }

    // metodos correspondientes a proveedor
    public void altaProveedor (int id, String nombre) {
        gestorProveedores.altaProveedor(id, nombre);
    }

    public void listarProveedor() {
        gestorProveedores.listarProveedores();
    }

    public void modificarNombreProveedor (int id) {
        gestorProveedores.modificarNombre(id);
    }

    public void eliminarProveedor (int id) {
        gestorProveedores.eliminarProveedor(id);
    }

    // correspondientes a producto

    public void altaProducto () {
        gestorProductos.altaProducto();
    }

    public void consultaProductoPorCodigo () {
        gestorProductos.consultarProductoPorCodigo();
    }

    public void modificarProducto() {
        gestorProductos.modificarProducto();
    }

    public void eliminarProducto () {
        gestorProductos.eliminarProducto();
    }

    

    
    


}
