package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.util.ColoresConsola;

public class DemoSQLite {
    public static void main(String[] args) {
        String rutaDb = "datos/sqlite/productos_demo.db";
        String url = "jdbc:sqlite:" + rutaDb;

        String sqlCreate = "CREATE TABLE IF NOT EXISTS producto_demo (codigo TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL)";
        String sqlInsert = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
        String sqlSelect = "SELECT codigo, nombre FROM producto_demo ORDER BY codigo";

        String[][] productosDemo = {
            {"SQL-001", "Producto demo A"},
            {"SQL-002", "Producto demo B"}
        };

        try {
            Files.createDirectories(Paths.get("datos/sqlite"));

            try (Connection con = DriverManager.getConnection(url);
                 Statement stm = con.createStatement()) {

                System.out.println("[OK] Conexión SQLite abierta: " + rutaDb);

                stm.executeUpdate(sqlCreate);
                System.out.println("[OK] Tabla producto_demo disponible.");

                int insertados = 0;
                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    for (String[] prod : productosDemo) {
                        ps.setString(1, prod[0]);
                        ps.setString(2, prod[1]);
                        insertados += ps.executeUpdate();
                    }
                }

                int existentes = productosDemo.length - insertados;
                System.out.println("Insertados: " + insertados + " | Ya existentes: " + existentes);

                try (ResultSet rs = stm.executeQuery(sqlSelect)) {
                    while (rs.next()) {
                        System.out.println(rs.getString("codigo") + " | " + rs.getString("nombre"));
                    }
                }
            }
        } catch (IOException | SQLException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
            
        }
    }
}
