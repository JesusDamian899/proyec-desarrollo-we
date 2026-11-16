# Sistema de Gestión de Almacén e Inventario — Electromecánica Andina S.A.C.

Proyecto integrador compuesto por **Backend RESTful en Spring Boot 4 (Java)**, **Frontend de Intranet en Angular 19** y **Base de Datos en Microsoft SQL Server (`bd_almacen`)**.

---

## 1. Estructura del Proyecto

```text
proyec-desarrollo-we/
├── src/main/java/com/example/inventario/   # Backend Spring Boot (Controladores, Servicios, Entidades, Seguridad JWT)
├── src/main/resources/
│   ├── application.properties              # Conexión por defecto a SQL Server (bd_almacen)
│   └── application-dev.properties          # Perfil opcional en memoria (H2 modo SQL Server)
├── frontend-angular/                       # Aplicación de la Intranet en Angular 19
├── database/
│   └── script_bd_almacen.sql               # Script DDL + INSERTs iniciales para SQL Server
├── mvnw.cmd / pom.xml                      # Wrapper y configuración de Maven
└── README.md                               # Guía de ejecución paso a paso por CMD
```

---

## 2. Cómo Ejecutar el Proyecto desde CMD (Windows)

### Paso 0: Actualizar los últimos cambios desde GitHub
Abre una ventana de **CMD** en la carpeta de tu proyecto y ejecuta:

```cmd
git pull origin main
```

---

### Paso 1: Preparar la Base de Datos (2 opciones)

#### Opción A — Usando tu SQL Server Local (`bd_almacen`)
1. Abre **SQL Server Management Studio (SSMS)**.
2. Abre y ejecuta el archivo [`database/script_bd_almacen.sql`](file:///home/antigravity/JesusDamian899/proyec-desarrollo-we/database/script_bd_almacen.sql) (esto creará la base de datos `bd_almacen`, las 17 tablas y todos los registros iniciales).
   - *Nota:* Si usas autenticación por usuario y contraseña de SQL Server en lugar de Windows Authentication (`integratedSecurity=true`), edita [`src/main/resources/application.properties`](file:///home/antigravity/JesusDamian899/proyec-desarrollo-we/src/main/resources/application.properties) así:
     ```properties
     spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=bd_almacen;encrypt=true;trustServerCertificate=true;
     spring.datasource.username=sa
     spring.datasource.password=TuPasswordSQL
     ```

#### Opción B — Sin instalar ni configurar SQL Server (Modo `dev` automático)
Si deseas levantar y probar todo de inmediato sin depender de SQL Server, el proyecto incluye el perfil `dev` que levanta una base de datos compatible con SQL Server en memoria y carga automáticamente todos los registros iniciales mediante [`CargaInicial.java`](file:///home/antigravity/JesusDamian899/proyec-desarrollo-we/src/main/java/com/example/inventario/Controladores/CargaInicial.java).

---

### Paso 2: Iniciar el Backend (Spring Boot) en la Ventana CMD #1

En la raíz del proyecto (`proyec-desarrollo-we`), ejecuta uno de los siguientes comandos:

- **Si tienes SQL Server activo (`bd_almacen`):**
  ```cmd
  mvnw.cmd spring-boot:run
  ```

- **Si quieres ejecutarlo rápido sin SQL Server (perfil `dev`):**
  ```cmd
  mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  ```

Sabrás que el backend está listo cuando veas en consola:
```text
Tomcat started on port 8080 (http) with context path '/'
>>> CARGA INICIAL DE PERMISOS Y ROLES COMPLETADA
```

---

### Paso 3: Iniciar el Frontend de la Intranet (Angular) en la Ventana CMD #2

Abre una **segunda ventana de CMD** (dejando la primera abierta con Spring Boot), entra a la carpeta `frontend-angular` y ejecuta:

```cmd
cd frontend-angular
npm install
npm start
```

- Cuando termine de compilar verás: `Local: http://localhost:4200/`
- Abre tu navegador en **`http://localhost:4200`** (o también directamente en **`http://localhost:8080`**).

---

## 3. Credenciales de Prueba para la Intranet (Sprint 01 — RBAC)

En la pantalla de Login puedes hacer clic en los botones de acceso rápido o escribir cualquiera de estas cuentas:

| Usuario (`username`) | Contraseña (`password`) | Rol en el Sistema | Alcance |
|---|---|---|---|
| `admin` | `Admin2026*` | `ADMINISTRADOR` | Acceso total (20 permisos), gestión de usuarios, roles, maestros y almacén |
| `jefe.almacen` | `Jefe2026*` | `JEFE_ALMACEN` | Gestión de catálogos maestros, artículos, ingresos, salidas, transferencias y reportes |
| `almacenero` | `Almacen2026*` | `ALMACENERO` | Ejecución de ingresos por lote, salidas FIFO, transferencias y consulta de Kardex |
| `solicitante` | `Solicita2026*` | `SOLICITANTE` | Consulta de stock de artículos y solicitud de materiales |
| `costos` | `Costos2026*` | `COSTOS` | Consulta de Kardex y reportes de inventario |

---

## 4. Qué Probar dentro de la Intranet Angular

1. **Sprint 01 (Seguridad JWT y Usuarios/Roles):**
   - Inicia sesión con `admin` / `Admin2026*`.
   - Entra en el menú lateral a **"Usuarios y Roles"**: crea un nuevo usuario, asígnale un rol y prueba el botón **Activar / Desactivar**.
2. **Sprint 02 (Registro y Mantenimiento de Tablas Básicas):**
   - **Catálogo de Artículos:** Crea un nuevo artículo (ej. `EL-00999`), edita uno existente, filtra por *"Solo bajo stock"* y verifica que no permita códigos internos duplicados.
   - **Categorías, Unidades y Ubicaciones:** Registra, edita o elimina registros en cualquiera de las 3 tablas maestras.
   - **Proveedores:** Registra un proveedor con su RUC de 11 dígitos, razón social y contacto.
3. **Sprints 03, 04 y 05 (Almacén, FIFO y Kardex):**
   - Entra a **"Almacén, FIFO y Kardex"**, registra un **Ingreso por Lote**, una **Salida a Producción (FIFO)** o una **Transferencia entre Ubicaciones** y verifica cómo se actualiza el saldo en el historial del **Kardex**.

---

## 5. Pruebas por CMD o Postman (API REST)

### 5.1. Ejecutar pruebas automatizadas de integración (JUnit + MockMvc)
```cmd
mvnw.cmd test
```

### 5.2. Probar Login JWT desde CMD (`curl`)
```cmd
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"Admin2026*\"}"
```

### 5.3. Consultar Artículos en Postman
- **Método:** `GET`
- **URL:** `http://localhost:8080/api/articulos`
- **Header:** `Authorization: Bearer <PEGA_AQUI_EL_TOKEN_DEL_LOGIN>`
