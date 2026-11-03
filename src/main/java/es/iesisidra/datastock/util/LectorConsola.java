package es.iesisidra.datastock.util;

import java.util.Scanner;

/**
 * Utilidad compartida para solicitar datos por consola.
 * <p>Utiliza un único {@link Scanner} para toda la aplicación. Se leen líneas
 * completas y se convierten cuando procede, evitando conflictos entre
 * {@code nextInt()} y {@code nextLine()}.</p>
 */
public final class LectorConsola {
    private static final Scanner TECLADO = new Scanner(System.in);
    private LectorConsola() { }

    /** Lee una línea; permite una cadena vacía. */
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!TECLADO.hasNextLine()) {
            // Entrada cerrada (por ejemplo, ejecución no interactiva).
            System.out.println();
            System.exit(0);
        }
        return TECLADO.nextLine();
    }

    /** Lee un entero; repite la pregunta ante una entrada no válida. */
    public static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).trim();
            try { return Integer.parseInt(texto); }
            catch (NumberFormatException ex) {
                System.out.println(ColoresConsola.pintar("Introduce un número entero válido.", ColoresConsola.AMARILLO));
            }
        }
    }

    /** Lee una opción comprendida entre los límites indicados. */
    public static int leerOpcion(String mensaje, int minimo, int maximo) {
        while (true) {
            int opcion = leerEntero(mensaje);
            if (opcion >= minimo && opcion <= maximo) return opcion;
            System.out.println(ColoresConsola.pintar("Opción fuera de rango.", ColoresConsola.AMARILLO));
        }
    }

    /** Solicita confirmación con S/N y devuelve true únicamente si se elige S. */
    public static boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje + " (S/N): ").trim();
            if (respuesta.equalsIgnoreCase("S")) return true;
            if (respuesta.equalsIgnoreCase("N")) return false;
            System.out.println("Responde S o N.");
        }
    }

    /** Detiene el menú hasta que el usuario pulse Intro. */
    public static void pausar() { leerTexto("Pulsa Intro para continuar..."); }


    /**
 * Lee un número decimal (double) desde la consola controlando excepciones de formato.
 * 
 * @param mensaje Texto a mostrar al usuario.
 * @return El valor double introducido por el usuario.
 */
public static double leerDouble(String mensaje) {
    double valor = 0.0;
    boolean valido = false;

    while (!valido) {
        try {
            System.out.print(mensaje);
            String entrada = TECLADO.nextLine().trim().replace(',', '.'); // Permite coma o punto
            valor = Double.parseDouble(entrada);
            valido = true;
        } catch (NumberFormatException e) {
            System.out.println(ColoresConsola.ROJO + "ERROR: Debe introducir un número decimal valido" + ColoresConsola.RESET);
        }
    }

    return valor;
}
}
