package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class GestorProveedores {

    /**
     * Metodo para dar de alta a proveedores
     *
     * @param id el id del proveedor nuevo
     * @param nombre el nombre de la categoria nueva
     */
    public void altaProveedor(int id, String nombre) {
        // identificador positivo
        if (id <= 0) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El identificador debe de ser positivo" + ColoresConsola.RESET);
            return;
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El nombre no puede ser nulo/estar vacio" + ColoresConsola.RESET);
            return;
        }

        // si pasa quitamos espacios
        String nombreNoNulo = nombre.trim();

        if (nombreNoNulo.length() < 1 || nombreNoNulo.length() > 100) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El nombre debe de estar entre 1 a 100 caracteres" + ColoresConsola.RESET);
            return;
        }

        boolean correoEsNulo = true;
        String correo = null; // solo llega a ser null si no se quiere introducir
        String correoNoNulo = "";

        if (LectorConsola.confirmar("Se va a introducir un correo electronico? -> S/N")) {
            correo = LectorConsola.leerTexto("Introduce el correo");

            if (correo == null || correo.trim().isEmpty()) { // teoricamente puede ser nulo pero no debe de serlo si accede a introducirlo
                System.out.println(ColoresConsola.ROJO + "[ERROR] El correo no puede ser nulo/estar vacio (en caso de haber pulsado S)" + ColoresConsola.RESET);
                return;
            }

            correoEsNulo = false;
            correoNoNulo = correo.trim();

            // comprobamos que coincida con el patron

            if (!correoNoNulo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                System.out.println(ColoresConsola.ROJO + "[ERROR] El correo no cumple con el formato valido" + ColoresConsola.RESET);
                return;

            }

            // cumple el tamaño

            if (correoNoNulo.length() < 1 || correoNoNulo.length() > 120) {
                System.out.println(ColoresConsola.ROJO + "[ERROR] El email debe de estar entre 1 a 120 caracteres" + ColoresConsola.RESET);
                return;
            }

            // llegados aqui debemos conectarnos con la base de datos y comprobar que no existe el id ni otra categoria con el mismo nombre
            String consultaID = "SELECT * FROM proveedor WHERE ID = ?";
            String consultaCorreo = "SELECT * FROM proveedor WHERE email = ?";
            String insertarProveedor = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";

            try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psID = con.prepareStatement(consultaID); PreparedStatement psInsertar = con.prepareStatement(insertarProveedor); PreparedStatement psCorreo = con.prepareStatement(consultaCorreo)) {

                // establezco el id en la posicion de la interrogacion
                psID.setInt(1, id);

                try (ResultSet rs = psID.executeQuery()) {
                    if (rs.next()) { // si hay algo en la consulta es que ha devuelto un proveedor por lo tango no deberia de seguir
                        System.out.println(ColoresConsola.ROJO + "[ERROR] Existe un proveedor con ese ID" + ColoresConsola.RESET);
                        return;
                    }
                }

                if (!correoEsNulo) { // si no no hace falta buscarlo
                    // igual pero con el proveedor
                    psCorreo.setString(1, correoNoNulo);
                    try (ResultSet rs = psCorreo.executeQuery()) {
                        if (rs.next()) { // si hay algo en la consulta es que ha devuelto un proveedor por lo tango no deberia de seguir
                            System.out.println(ColoresConsola.ROJO + "[ERROR] Existe un proveedor con ese email" + ColoresConsola.RESET);
                            return;
                        }
                    }
                }

                psInsertar.setInt(1, id);
                psInsertar.setString(2, nombreNoNulo);

                if (correoEsNulo) { // si es nullo introduzco null directamente
                    psInsertar.setString(3, null);
                } else {
                    psInsertar.setString(3, correoNoNulo.trim());
                }

                // aplico cambios

                int filasInsertadas = psInsertar.executeUpdate();

                System.out.println(ColoresConsola.VERDE + "[PROVEEDOR INSERTADO CORRECTAMENTE] filas insertadas: " + filasInsertadas + ColoresConsola.RESET);

            } catch (SQLException e) {
                System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

            }

        }

    }

    /**
     * Metodo para listar los proveedores de la BD en orden ascendente (en base
     * al id)
     */
    public void listarProveedores() {
        String listar = "SELECT id, nombre, email FROM proveedor ORDER BY id"; // no pongo ASC ya que por defecto es ascendente
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psListar = con.prepareStatement(listar); ResultSet rs = psListar.executeQuery();) {
            boolean vacio = true; // si entra en el bucle es que hay por lo menos un proveedor por lo que no estaria vacio

            while (rs.next()) {
                vacio = false;
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String email = "-"; // en el caso de que sea null mostraremos: - en vez de null
                if (rs.getString("email") != null) {
                    email = rs.getString("email");
                }
                System.out.println("[IDENTIFICADOR] " + id + " [NOMBRE] " + nombre + " [EMAIL] " + email);

            }

            if (vacio) { // en el caso de no encontrar nada
                System.out.println(ColoresConsola.AZUL + "[NO HAY PROVEEDORES REGISTRADOS]" + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para modificar el nombre y categoria del proveedor
     *
     * @param id el id del proveedor a modificar
     *
     */
    public void modificarNombre(int id) {
        // identificador positivo
        if (id <= 0) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El identificador debe de ser positivo" + ColoresConsola.RESET);
            return;
        }

        String consultaID = "SELECT id, nombre, email FROM proveedor WHERE ID = ?";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psID = con.prepareStatement(consultaID);) {

            // establezco el id en la posicion de la interrogacion
            psID.setInt(1, id);

            try (ResultSet rs = psID.executeQuery()) {
                if (!rs.next()) { // si no hay algo en la consulta es que NO ha devuelto una categoria 
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No existe un proveedor con ese ID" + ColoresConsola.RESET);
                    return;
                }

                // obtengo datos para mostrarlos por pantalla
                String nombreActual = rs.getString("nombre");
                String emailActual = rs.getString("email");
                System.out.println("NOMBRE ACTUAL: " + nombreActual + " EMAIL ACTUAL: " + emailActual);

                // doy los avisos
                System.out.println("SI SE INTRODUCE UNICAMENTE INTRO SE CONSERVA EL DATO ACTUAL");
                System.out.println("PARA DEJAR EL EMAIL SIN VALOR INTRODUCIR: -");

                // IMPORTANTE: mis sentencias sql dependen del usuario asi que creamos cambioDeNombre y cambioDeEmail para saber que hacer dependiendo del caso
                String nombreIntroducido = LectorConsola.leerTexto("Introduce el nombre");
                boolean cambioDeNombre = true;

                if (nombreIntroducido == null || nombreIntroducido.trim().isEmpty()) {
                    cambioDeNombre = false;
                }

                String emailIntroducido = LectorConsola.leerTexto("Introduce el email");
                boolean cambioDeEmail = true;

                if (emailIntroducido == null || emailIntroducido.trim().isEmpty()) {
                    cambioDeEmail = false;
                }

                if (!cambioDeNombre && !cambioDeEmail) {
                    System.out.println(ColoresConsola.VERDE + "No se realizan cambios" + ColoresConsola.RESET);
                    return;
                }

                String nombreNoNulo = "";
                if (cambioDeNombre) {
                    nombreNoNulo = nombreIntroducido.trim(); // no hay problema con hacer .trim() ya que cambioDeNombre se ocupa de que si es null este en false y no entra al if

                    if (nombreNoNulo.length() < 1 || nombreNoNulo.length() > 100) {
                        System.out.println(ColoresConsola.ROJO + "[ERROR] El nombre debe de estar entre 1 a 100 caracteres" + ColoresConsola.RESET);
                        return;
                    }
                }

                if (cambioDeEmail) {
                    if (emailIntroducido.trim().equalsIgnoreCase("-")) { // en caso de que sea igual a - como le aseguramos nos comprometemos a que sea null
                        emailIntroducido = null;
                    } else {
                        // ha de cumplir con las validaciones
                        if (!emailIntroducido.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                            System.out.println(ColoresConsola.ROJO + "[ERROR] El correo no cumple con el formato valido" + ColoresConsola.RESET);
                            return;

                        }

                        if (emailIntroducido.length() < 1 || emailIntroducido.length() > 120) {
                            System.out.println(ColoresConsola.ROJO + "[ERROR] El email debe de estar entre 1 a 120 caracteres" + ColoresConsola.RESET);
                            return;
                        }

                    }
                }

                // preparamos las sentencias para saber si el email es duplicado en caso de que lo tenga otro proveedor

                String sqlDuplicado = "SELECT email FROM proveedor WHERE LOWER(email) = ? AND id != ?";
                String actualizar = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";

                // IMPORTANTE: ya avise de que las sentencias cambian dependiendo de lo que quiera el usuario
                if (cambioDeEmail && !cambioDeNombre) {
                    actualizar = "UPDATE proveedor SET email = ? WHERE id = ?";

                } else if (!cambioDeEmail && cambioDeNombre) {
                    actualizar = "UPDATE proveedor SET nombre = ? WHERE id = ?";
                }

                try (PreparedStatement psEmail = con.prepareStatement(sqlDuplicado); PreparedStatement psActualizar = con.prepareStatement(actualizar)) {

                    if (cambioDeEmail && emailIntroducido != null) {
                        psEmail.setString(1, emailIntroducido.toLowerCase());
                        psEmail.setInt(2, id);
                        ResultSet rsDuplicado = psEmail.executeQuery();

                        if (rsDuplicado.next()) {
                            System.out.println(ColoresConsola.ROJO + "[ERROR] Ese email ya existe" + ColoresConsola.RESET);
                            return;
                        }
                    }

                    // pedimos confirmacion
                    if (!LectorConsola.confirmar("CONFIRMA EL CAMBIO DE DATOS S/N")) {
                        System.out.println(ColoresConsola.VERDE + "[RECIBIDO, SALIENDO CORRECTAMENTE...]" + ColoresConsola.RESET);
                        return;
                    }

                    // aplicamos dependiendo de los cambios del usuario

                    if (cambioDeEmail && cambioDeNombre) {
                        psActualizar.setString(1,  nombreNoNulo);
                        psActualizar.setString(2, emailIntroducido);
                        psActualizar.setInt(3, id);

                    }

                    else if (cambioDeEmail && !cambioDeNombre) {
                        psActualizar.setString(1, emailIntroducido);
                        psActualizar.setInt(2, id);

                    } else if (!cambioDeEmail && cambioDeNombre) {
                        psActualizar.setString(1,  nombreNoNulo);
                        psActualizar.setInt(2, id);
                        
                    }

                    int filasAfectadas = psActualizar.executeUpdate();
                    System.out.println(ColoresConsola.VERDE + "[ACTUALIZADO CORRECTAMENTE] filas afectadas: " + filasAfectadas + ColoresConsola.RESET);

                }

            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        }

    }

    /**
     * Metodo auxiliar para saber si la categoria tiene productos asociados
     *
     * @param proveedor_id el id del proveedor
     * @return false si no tiene productos asociados y true si si los tiene
     */
    private boolean tieneProductosAsociados(int proveedor_id) {
        boolean tieneProductos = false;
        // identificador positivo
        if (proveedor_id <= 0) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El identificador debe de ser positivo" + ColoresConsola.RESET);
            tieneProductos = true;
            return tieneProductos;
        }

        // nos vale con que devuelta uno en vez de todos los registros
        String sql = "SELECT 1 FROM producto WHERE proveedor_id = ? LIMIT 1";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, proveedor_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { // en caso de que haya 1
                System.out.println(ColoresConsola.ROJO + "[ERROR] hay productos asociados a ese proveedor" + ColoresConsola.RESET);
                tieneProductos = true;
                return tieneProductos;
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
            tieneProductos = true;
            return tieneProductos;
        }

        return tieneProductos;

    }

    /**
     * Metodo para eliminar el proveedor
     *
     * @param id el id del proveedor a eliminar
     */
    public void eliminarProveedor(int id) {
        // comprobamos que no tenga productos asociados
        if (tieneProductosAsociados(id)) {
            return;
        }

        // preparamos las sentencias

        String sqlCategoria = "SELECT nombre FROM proveedor WHERE id = ?";
        String eliminar = "DELETE FROM proveedor WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sqlCategoria); PreparedStatement psEliminar = con.prepareStatement(eliminar)) {
            ps.setInt(1, id);
            

            String nombre;

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                nombre = rs.getString("nombre");

            } else { // en caso de que el proveedor con ese id no exista
                System.out.println(ColoresConsola.ROJO + "[ERROR] no hay proveedor asociado a ese id" + ColoresConsola.RESET);
                return;
            }
                
            }

            System.out.println(ColoresConsola.AZUL + "PROVEEDOR OBTENIDO CON NOMBRE -> " + nombre + ColoresConsola.RESET);

            // pedimos permiso
            if (!LectorConsola.confirmar("PERMISO PARA ELIMINAR EL PROVEEDOR S/N")) {
                System.out.println(ColoresConsola.VERDE + "RECIBIDO, SALIENDO..." + ColoresConsola.RESET);
                return;
            }

            // en caso de llegar aqui eliminamos

            psEliminar.setInt(1, id);
            int filasAfectadas = psEliminar.executeUpdate();

            System.out.println(ColoresConsola.VERDE + "PROVEEDOR BORRADO CORRECTAMENTE FILAS AFECTADAS -> " + filasAfectadas + ColoresConsola.RESET);

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }

    }

}
