# DataStock – Proyecto inicial (UT1, UT2)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola

## 1. ¿Qué es este proyecto?

DataStock es una práctica **individual e integradora** que evolucionará durante tres unidades:

- **UT1 – Ficheros:** importar JSON/XML, consultar en memoria, exportar, serializar y desarrollar dos actividades independientes (XPath y `RandomAccessFile`) -> Ya realizada
- **UT2 – JDBC:** incorporar MySQL, operaciones SQL y transacciones. -> Se implementa en este proyecto
- **UT3 – Componentes:** reorganizar el acceso a datos en componentes reutilizables.

**Este programa incluye los requisitos de la UT1 y desarrolla los de la UT2.**

## 2. Requisitos previos

- JDK **21** instalado y seleccionado en el IDE (`java -version`).
- Maven instalado, o un IDE con integración Maven.
- Consola con UTF-8 para ver correctamente tildes y símbolos.
- MySQL Server: 8.0.44 Community Server (Win64).
- SQLite: Motor embedido local.
- IMPORTANTE -> Poner la dependencia de slf4j para evitar avisos por consola a la hora de ejecutar la app

### Drivers y Dependencias (Maven)
* **MySQL Connector/J:** `8.0.33` (`com.mysql:mysql-connector-j`)
* **SQLite JDBC:** `3.45.1.0` (`org.xerial:sqlite-jdbc`)
* **slf4j-simple** `1.7.36`

## 3. Cómo abrir y ejecutar

1. Descomprime el ZIP y abre **la carpeta que contiene `pom.xml`** como proyecto Maven en IntelliJ IDEA, Eclipse o VS Code.
2. Espera a que Maven descargue la dependencia Gson (requiere conexión a Internet la primera vez).
3. Ejecuta `es.iesisidra.datastock.App` desde el IDE, o en una terminal situada en la raíz:

```bash
mvn clean compile
mvn exec:java
```

Para ejecutar las actividades complementarias:

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
```

> Si el IDE muestra caracteres de control ANSI, desactiva el color al ejecutar Java con `-Ddatastock.color=false`, o configura la variable de entorno `NO_COLOR=1`.

## 4. Organización inicial del proyecto

```text
DataStock_V2/
├── datos/
│   ├── ejemplos/              # Archivos de prueba (JSON/XML)
│   └── salidas/               # Exportaciones y ficheros generados
├── material_referencia/       # Scripts SQL y esquemas ER
└── src/
    └── main/
        ├── java/es/iesisidra/datastock/
        │   ├── complementarias/   # Prácticas de acceso aleatorio y XPath
        │   ├── config/            # Configuración de conexiones (ConexionBD.java)
        │   ├── modelo/            # Clases del modelo (Categoria, Producto, Proveedor, etc.)
        │   ├── repository/        # Repositorios y persistencia en XML
        │   ├── servicio/          # Lógica de negocio, inventario y GestorBaseDatos.java
        │   ├── ui/                # Interfaz de consola (MenuPrincipal.java)
        │   ├── util/              # Utilidades (Colores ANSI, LectorConsola)
        │   └── App.java           # Punto de entrada principal
        └── resources/
            └── db.properties      # Credenciales de conexión (Excluido de Git)
```

## 5. Opciones que han de aparecer

**DATASTOCK - GESTION DE INVENTARIO:**

```text
1. Explorar ficheros y directorios     UT1
2. Importar productos desde JSON       UT1
3. Importar productos desde XML        UT1
4. Consultar productos                 UT1
5. Exportar productos                  UT1
6. Guardar y recuperar inventario      UT1
7. Gestionar base de datos MySQL       UT2
0. Salir
```

`RF-FIC-07` (XPath) y `RF-FIC-08` (`RandomAccessFile`) se ejecutan en clases independientes.

**GESTION MYSQL - UT2:**

```text
1. Comprobar conexión y mostrar información de la BD
2. Gestionar categorías
3. Gestionar proovedores
4. Gestionar productos (CRUD)
5. Consulta y buscar productos
6. Registrar entrada o salida de stock
7. Consultar historial de movimientos
8. Importar productos de memoria a MySQL
9. Demostracion de transaccion con commit/rollback
0. Volver al menu principal
```

## 6. Reglas clave del programa

- JSON raíz `productos` y XML raíz `catalogo` con elementos `producto`.
- Los campos del producto son `codigo`, `nombre`, `precio`, `stock`, `categoriaId` y `proveedorId`.
- Los códigos no se duplican, incluso si cambia el uso de mayúsculas/minúsculas.
- Un documento mal formado o con estructura general inválida **no modifica** el inventario.
- Si el documento es válido pero contiene registros incorrectos, se incorporan solo los válidos.
- Importar JSON/XML **añade** productos; recuperar un `.dat` **sustituye** el inventario, tras confirmar cuando corresponda.
- En UT1 no hay base de datos: los ID de categoría y proveedor se comprueban como enteros positivos, pero no se buscan en MySQL.
- El fichero `stock.bin` de RF-FIC-08 es una **copia independiente**: los cambios hechos allí no actualizan la colección Java.
- Se debe ejecutar en MySQL el script que contiene la BD (incluido en material_referencia)
- Para ejecutar la aplicación, debes crear un archivo **src/main/resources/db.properties** con las variables **db.url, db.username y db.password.** Este archivo está ignorado en Git por seguridad.
