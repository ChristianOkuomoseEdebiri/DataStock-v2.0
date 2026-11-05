package es.iesisidra.datastock.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import es.iesisidra.datastock.util.ColoresConsola;

public class ConexionBD {
    // Ruta donde has guardado tu archivo de credenciales
    private static final String PROPERTIES_FILE = "src/main/java/resources/db.properties";
    private static Properties props = new Properties();
    private static boolean propiedadesCargadas = false;

    /**
     * Método privado que lee el archivo db.properties solo la primera vez que se necesita.
     */
    private static void cargarPropiedades() {
        if (!propiedadesCargadas) {
            try (InputStream is = Files.newInputStream(Paths.get(PROPERTIES_FILE))) {
                props.load(is);
                propiedadesCargadas = true;
            } catch (Exception e) {
                System.out.println( ColoresConsola.ROJO + "[ERROR] No se pudo encontrar o leer el archivo: " + PROPERTIES_FILE + ColoresConsola.RESET);
            }
        }
    }

    /**
     * Establece y devuelve una conexión a MySQL usando los datos del properties.
     * @return conexion a la base de datos
     * @throws SQLException Si hay un problema conectando a la BD o faltan datos
     */
    public static Connection obtenerConexion() throws SQLException {
        // Asegurarnos de que las propiedades están cargadas
        cargarPropiedades();
        
        // Extraer los valores del archivo
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");
        
        // Pequeña validación de seguridad por si el archivo está vacío
        if (url == null || user == null || pass == null) {
            throw new SQLException("Faltan credenciales en el archivo db.properties");
        }

        // 4. devuelvo la conexion
        return DriverManager.getConnection(url, user, pass);
    }


    
    
}
