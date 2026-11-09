package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class GestorCategorias {

    /**
     * Metodo para dar de alta a categorias
     * @param id el id de la categoria nueva
     * @param nombre el nombre de la categoria nueva
     */

    public void altaCategoria(int id, String nombre) {
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

        if (nombreNoNulo.length() < 1 || nombreNoNulo.length() > 60) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El nombre debe de estar entre 1 a 60 caracteres" + ColoresConsola.RESET);
            return;
        }

        // llegados aqui debemos conectarnos con la base de datos y comprobar que no existe el id ni otra categoria con el mismo nombre
        String consultaID = "SELECT * FROM categoria WHERE id = ?";
        String consultaNombre = "SELECT * FROM categoria WHERE nombre = ?";
        String insertarCategoria = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psNombre = con.prepareStatement(consultaNombre); PreparedStatement psID = con.prepareStatement(consultaID); PreparedStatement psInsertar = con.prepareStatement(insertarCategoria)) {

            // establezco el id en la posicion de la interrogacion
            psID.setInt(1, id);

            try (ResultSet rs = psID.executeQuery()) {
                if (rs.next()) { // si hay algo en la consulta es que ha devuelto una categoria por lo tango no deberia de seguir
                    System.out.println(ColoresConsola.ROJO + "[ERROR] Existe una categoria con ese ID" + ColoresConsola.RESET);
                    return;
                }
            }

            // igual pero con el nombre
            psNombre.setString(1, nombreNoNulo);

            try (ResultSet rs = psNombre.executeQuery()) {
                if (rs.next()) { // si hay algo en la consulta es que ha devuelto una categoria por lo tango no deberia de seguir
                    System.out.println(ColoresConsola.ROJO + "[ERROR] Existe una categoria con ese nombre" + ColoresConsola.RESET);
                    return;
                }
            }

            psInsertar.setInt(1, id);
            psInsertar.setString(2, nombreNoNulo);

            int filasInsertadas = psInsertar.executeUpdate();

            System.out.println(ColoresConsola.VERDE + "[CATEGORIA INSERTADA CORRECTAMENTE] filas insertadas: " + filasInsertadas + ColoresConsola.RESET);

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        }

    }

    /**
     * Metodo para listar las categorias de la BD en orden ascendente (en base al id)
     */

    public void listarCategorias() {
        String listar = "SELECT id, nombre FROM categoria ORDER BY id"; // no pongo ASC ya que por defecto es ascendente
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psListar = con.prepareStatement(listar); ResultSet rs = psListar.executeQuery();) {

            boolean vacio = true; // si entra en el bucle es que hay por lo menos una categoria por lo que no estaria vacio

            while (rs.next()) {
                vacio = false;
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                System.out.println("[IDENTIFICADOR] " + id + " [NOMBRE] " + nombre);
            }

            if (vacio) {
                System.out.println(ColoresConsola.AZUL + "[NO HAY CATEGORIAS REGISTRADAS]" + ColoresConsola.RESET);
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo para modificar el nombre de la categoria
     * @param id el id de la categoria a modificar
     */

    public void modificarNombre(int id) {
        // identificador positivo
        if (id <= 0) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El identificador debe de ser positivo" + ColoresConsola.RESET);
            return;
        }

        String consultaID = "SELECT id, nombre FROM categoria WHERE ID = ?";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement psID = con.prepareStatement(consultaID);) {

            // establezco el id en la posicion de la interrogacion
            psID.setInt(1, id);

            try (ResultSet rs = psID.executeQuery()) {
                if (!rs.next()) { // si no hay algo en la consulta es que NO ha devuelto una categoria 
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No existe una categoria con ese ID" + ColoresConsola.RESET);
                    return;
                }
                String nombreActual = rs.getString("nombre");
                System.out.println("NOMBRE ACTUAL: " + nombreActual);

                String nombreIntroducido = LectorConsola.leerTexto("Introduce el nombre");

                if (nombreIntroducido == null || nombreIntroducido.trim().isEmpty()) {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] El nombre no puede ser nulo/estar vacio" + ColoresConsola.RESET);
                    return;
                }

                // si pasa quitamos espacios y lo pongo en minusculas independientemente de que sea case-insensitive
                String nombreNoNulo = nombreIntroducido.trim();

                if (nombreNoNulo.length() < 1 || nombreNoNulo.length() > 60) {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] El nombre debe de estar entre 1 a 60 caracteres" + ColoresConsola.RESET);
                    return;
                }

                String sqlDuplicado = "SELECT nombre FROM categoria WHERE LOWER(nombre) = ?";
                String actualizar = "UPDATE categoria SET nombre = ? WHERE id = ?";

                try (PreparedStatement psNombre = con.prepareStatement(sqlDuplicado);
                    PreparedStatement psActualizar = con.prepareStatement(actualizar)) {

                    psNombre.setString(1, nombreNoNulo.toLowerCase());
                    ResultSet rsDuplicado = psNombre.executeQuery();

                    if (rsDuplicado.next()) {
                        System.out.println(ColoresConsola.ROJO + "[ERROR] Ese nombre ya existe" + ColoresConsola.RESET);
                        return;
                    }

                    // pedimos confirmacion

                    if (!LectorConsola.confirmar("CONFIRMA EL CAMBIO DE NOMBRE S/N")) {
                        System.out.println(ColoresConsola.VERDE + "[RECIBIDO, SALIENDO CORRECTAMENTE...]" + ColoresConsola.RESET);
                        return;
                    }

                    // si hemos llegado hasta aqui ejecutamos el update
                    psActualizar.setString(1, nombreNoNulo);
                    psActualizar.setInt(2, id);
                    int filasAfectadas = psActualizar.executeUpdate();
                    System.out.println(ColoresConsola.VERDE + "[NOMBRE ACTUALIZADO CORRECTAMENTE]" + ColoresConsola.RESET);
                    
                }

            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        }

    }
    /**
     * Metodo auxiliar para saber si la categoria tiene productos asociados
     * @param categoria_id el id de la categoria
     * @return false si no tiene productos asociados y true si si los tiene
     */

    private boolean tieneProductosAsociados (int categoria_id) {
        boolean tieneProductos = false;
        // identificador positivo
        if (categoria_id <= 0) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El identificador debe de ser positivo" + ColoresConsola.RESET);
            tieneProductos = true;
            return tieneProductos;
        }



        // nos vale con que devuelta uno en vez de todos los registros

        String sql = "SELECT 1 FROM producto WHERE categoria_id = ? LIMIT 1";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, categoria_id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { // en caso de que haya 1
                    System.out.println(ColoresConsola.ROJO + "[ERROR] hay productos asociados a esa categoria" + ColoresConsola.RESET);
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
     * Metodo para eliminar la categoria
     * @param id el id de la categoria a eliminar
     */


    public void eliminarCategoria(int id) {
        if (tieneProductosAsociados(id)) {
            return;
        }

        String sqlCategoria = "SELECT nombre FROM categoria WHERE id = ?";
        String eliminar = "DELETE FROM categoria WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sqlCategoria);
            PreparedStatement psEliminar = con.prepareStatement(eliminar)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String nombre;

            if (rs.next()) {
                nombre = rs.getString("nombre");

            } else {
                System.out.println(ColoresConsola.ROJO + "[ERROR] no hay categoria asociada a ese id" + ColoresConsola.RESET);
                return;
            }

            System.out.println(ColoresConsola.AZUL + "CATEGORIA OBTENIDA CON NOMBRE -> " + nombre + ColoresConsola.RESET);

            if (!LectorConsola.confirmar("PERMISO PARA ELIMINAR LA CATEGORIA S/N")) {
                System.out.println(ColoresConsola.VERDE + "RECIBIDO, SALIENDO..." + ColoresConsola.RESET);
                return;
            }

            psEliminar.setInt(1, id);
            int filasAfectadas = psEliminar.executeUpdate();

            System.out.println(ColoresConsola.VERDE + "CATEGORIA BORRADA CORRECTAMENTE FILAS AFECTADAS -> " + filasAfectadas + ColoresConsola.RESET);



        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }



    }

    /**
     * Metodo: demostracion de transacciones JDBC
     * Documentacion a tener en cuenta: se utilizan los IDs 500, 501 y 502.
     * Realmente solo se llegan a introducir 500 y 501 por lo tanto si queremos eliminarlos tenemos el metodo eliminarCategoria al que le debemos de proporcionar los IDs
     * No se utilizan IDs que puedan afectar a datos reales
     */

    public void transacciones () {
        String comprobar = "SELECT id FROM categoria WHERE id IN (?, ?, ?)";// vamos a usar IDs que no esten usados
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(comprobar)
            ) {
                
                // usamos 500, 501 y 502 ya que son IDscon una probabilidad casi nula de ser usados
                ps.setInt(1, 500);
                ps.setInt(2, 501);
                ps.setInt(3, 502);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Categorias disponibles con id = 500, 501 y 502");
                    }
                    else {
                        System.out.println("No hay categorias disponibles");
                        return;
                    }
                }

                System.out.println("SE VAN A INSERTAR TRES CATEGORIAS DE PRUEBA CON LOS IDs 500, 501 y 502. (NO DEBEN ESTAR EN USO)");
                
                System.out.println("EN LA PRIMERA TRANSACCION SE INTRODUCIRAN DOS CATEGORIAS CON IDs = 500 y 501");

                System.out.println("EN LA SEGUNDA TRANSACCION SE INTRODUCIRA 502 DOS VECES PROVOCANDO DE MANERA INTENCIONADA UN FALLO Y HACIENDO UN ROLLBACK");

                // pedimos permiso
                if (!LectorConsola.confirmar("DESEAS CONTINUAR S/N")) {
                    System.out.println(ColoresConsola.VERDE + "[OK] SALIENDO..." + ColoresConsola.RESET);
                    return;
                }
                

                // desactivamos el autocommit
                con.setAutoCommit(false);

                // caso A
                String insertar = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
                String comprobarInsercion = "SELECT * FROM categoria WHERE id IN (?, ?)";
                try (PreparedStatement psInsertar = con.prepareStatement(insertar);
                PreparedStatement psComprobar = con.prepareStatement(comprobarInsercion)) {
                    // como avisamos insertamos las dos primeras categorias
                    psInsertar.setInt(1, 500);
                    psInsertar.setString(2, "PRUEBA500");
                    psInsertar.executeUpdate();

                    psInsertar.setInt(1, 501);
                    psInsertar.setString(2, "PRUEBA501");
                    psInsertar.executeUpdate();

                    // hacemos commit y comprobamos
                    con.commit();

                    psComprobar.setInt(1, 500);
                    psComprobar.setInt(2, 501);

                    try (ResultSet rs = psComprobar.executeQuery()) {
                        int encontrados = 0;
                        while (rs.next()) {
                            encontrados++;
                            System.out.println("[NOMBRE] " + rs.getString("nombre") + " [ID] " + rs.getInt("id"));
                        } if (encontrados != 2) {
                            System.out.println(ColoresConsola.ROJO + "NO SE ENCONTRARON LAS DOS CATEGORIAS INTRODUCIDAS" + ColoresConsola.RESET);
                            return;
                        } else {
                            System.out.println(ColoresConsola.VERDE + "[OK] ENCONTRADOS CORRECTAMENTE" + ColoresConsola.RESET);
                        }
                        
                    }

                    
                } catch (SQLException se) {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] no se pudo introducir, realizando rollback" + se.getMessage() + ColoresConsola.RESET);
                    con.rollback();
                }

                // caso B
                try (PreparedStatement psInsertar = con.prepareStatement(insertar)) {
                    // como avisamos insertamos la misma categoria dos veces
                    psInsertar.setInt(1, 502);
                    psInsertar.setString(2, "PRUEBA502");
                    psInsertar.executeUpdate();

                    psInsertar.setInt(1, 502);
                    psInsertar.setString(2, "PRUEBA502");
                    psInsertar.executeUpdate();

                    // no deberia de permitirnos hacer el commit
                    con.commit();

                    
                } catch (SQLException sqle) {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] CLAVE DUPLICADA " + sqle.getMessage() + ColoresConsola.RESET);
                    con.rollback();
                   
                }

                // compruebo que no se haya insertado
                String comprobarInsercionFinal = "SELECT * FROM categoria WHERE id = ?";
                try (PreparedStatement pscomprobarInsercionFinal = con.prepareStatement(comprobarInsercionFinal)) {
                    pscomprobarInsercionFinal.setInt(1, 502);
                    
                    try (ResultSet rs = pscomprobarInsercionFinal.executeQuery()) {
                        if (rs.next()) {
                            System.out.println(ColoresConsola.ROJO + "[ERROR] SE HA ENCONTRADO UNA CATEGORIA CON EL ID 502" + ColoresConsola.RESET);

                        } else {
                            System.out.println(ColoresConsola.VERDE + "[OK] EL ROLLBACK FUNCIONO CORRECTAMENTE" + ColoresConsola.RESET);
                        }
                        
                    }


                }

                con.setAutoCommit(true);


            
        } catch (SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        }
        
    }





}
