package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.servicio.Inventario;

/**
 * Punto de entrada reservado para RF-FIC-07: consultas XPath.
 *
 */
public final class XPathActividad {
    private XPathActividad() { }
    
    public static void main(String[] args) {
        Inventario inventario = new Inventario();
        inventario.consultaXPath("datos/ejemplos/catalogo_valido.xml");
    }
}
