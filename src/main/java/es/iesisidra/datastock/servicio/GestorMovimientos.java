package es.iesisidra.datastock.servicio;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class GestorMovimientos {
    /**
     * Metodo para registrar una ENTRADA de stock llamando al metodo comun
     */
    public void registrarEntrada() {
        registrarMovimiento("ENTRADA");
    }

    /**
     * Metodo para registrar una SALIDA de stock llamando al metodo comun
     */
    public void registrarSalida() {
        registrarMovimiento("SALIDA");
    }

    /**
     * Metodo comun que valida los datos e invoca el procedimiento almacenado
     * registrar_movimiento(?, ?, ?) mediante CallableStatement
     *
     * @param tipo puede ser "ENTRADA" o "SALIDA"
     */
    private void registrarMovimiento(String tipo) {
        String codigoIntroducido = LectorConsola.leerTexto("Introduce el codigo del producto");

        if (codigoIntroducido == null || codigoIntroducido.trim().isEmpty()) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] El codigo no puede estar vacio" + ColoresConsola.RESET);
            return;
        }

        // normalizo el codigo a mayusculas y sin espacios
        String codigoLimpio = codigoIntroducido.trim().toUpperCase();

        String sqlProducto = "SELECT nombre, stock FROM producto WHERE codigo = ?";
        String sqlProcedimiento = "{CALL registrar_movimiento(?, ?, ?)}";
        String sqlUltimoMovimiento = "SELECT id, fecha FROM movimiento_stock WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1"; 

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement psProducto = con.prepareStatement(sqlProducto);
             CallableStatement csMovimiento = con.prepareCall(sqlProcedimiento);
             PreparedStatement psUltimoMov = con.prepareStatement(sqlUltimoMovimiento)) {

            // 1. Compruebo que el producto existe y obtengo su nombre y stock actual
            psProducto.setString(1, codigoLimpio);
            String nombreProducto;
            int stockAnterior;

            try (ResultSet rs = psProducto.executeQuery()) {
                if (!rs.next()) { // si no hay resultado es que el producto no existe
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No existe ningun producto con el codigo: " + codigoLimpio + ColoresConsola.RESET);
                    return;
                }
                nombreProducto = rs.getString("nombre");
                stockAnterior = rs.getInt("stock");
            }

            System.out.println(ColoresConsola.AZUL + "PRODUCTO -> [NOMBRE] " + nombreProducto + " [STOCK ACTUAL] " + stockAnterior + ColoresConsola.RESET);

            // 2. Solicito la cantidad y compruebo que sea estrictamente positiva
            int cantidad = LectorConsola.leerEntero("Introduce la cantidad para la " + tipo);

            if (cantidad <= 0) {
                System.out.println(ColoresConsola.ROJO + "[ERROR] La cantidad debe ser estrictamente positiva (mayor que 0)" + ColoresConsola.RESET);
                return;
            }

            // si es una SALIDA, compruebo antes en Java si supera las existencias
            if (tipo.equals("SALIDA") && cantidad > stockAnterior) {
                System.out.println(ColoresConsola.ROJO + "[ERROR] La cantidad de SALIDA (" + cantidad + ") supera el stock disponible (" + stockAnterior + ")" + ColoresConsola.RESET);
                return;
            }

            // 3. Pido confirmacion S/N antes de ejecutar la operacion
            if (!LectorConsola.confirmar("CONFIRMA EL REGISTRO DE " + tipo + " DE " + cantidad + " UNIDADES S/N")) {
                System.out.println(ColoresConsola.VERDE + "[RECIBIDO, OPERACION CANCELADA SIN CAMBIOS...]" + ColoresConsola.RESET);
                return;
            }

            // 4. Invoco el procedimiento almacenado registrar_movimiento(?, ?, ?) con CallableStatement
            csMovimiento.setString(1, codigoLimpio);
            csMovimiento.setString(2, tipo);
            csMovimiento.setInt(3, cantidad);
            csMovimiento.execute();

            // 5. Consulto el nuevo stock actualizado en la tabla producto
            int stockPosterior = stockAnterior;
            try (ResultSet rsNuevo = psProducto.executeQuery()) {
                if (rsNuevo.next()) {
                    stockPosterior = rsNuevo.getInt("stock");
                }
            }

            // 6. Consulto los datos del movimiento recien registrado para mostrarlos
            psUltimoMov.setString(1, codigoLimpio);
            try (ResultSet rsMov = psUltimoMov.executeQuery()) {
                if (rsMov.next()) {
                    int idMov = rsMov.getInt("id");
                    Timestamp fechaMov = rsMov.getTimestamp("fecha");
                    System.out.println(ColoresConsola.VERDE + "[OK] MOVIMIENTO REGISTRADO CORRECTAMENTE (ID Mov: " + idMov + " | Fecha: " + fechaMov + ")" + ColoresConsola.RESET);
                } else {
                    System.out.println(ColoresConsola.VERDE + "[OK] MOVIMIENTO REGISTRADO CORRECTAMENTE" + ColoresConsola.RESET);
                }
            }

            // muestro el resumen final como pide el enunciado
            System.out.println(ColoresConsola.VERDE + "RESUMEN -> [TIPO] " + tipo 
                             + " [STOCK ANTERIOR] " + stockAnterior 
                             + " [CANTIDAD] " + cantidad 
                             + " [STOCK POSTERIOR] " + stockPosterior + ColoresConsola.RESET);

        } catch (SQLException e) {
            // si el procedimiento almacenado lanza SIGNAL SQLSTATE (ej. stock insuficiente), cae aqui y se muestra en rojo
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
        }
    }
    
}
