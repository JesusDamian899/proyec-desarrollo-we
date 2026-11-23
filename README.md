# Sistema de Gestión de Almacén e Inventario — Electromecánica Andina S.A.C.

Aplicación web corporativa compuesta por **Backend RESTful en Spring Boot 4 (Java 17/21)**, **Frontend modular en Angular 19** y **Base de Datos en Microsoft SQL Server (`bd_almacen`)**.

---

## 1. Arquitectura y Estructura del Proyecto

```text
proyec-desarrollo-we/
├── src/main/java/com/example/inventario/
│   ├── Config/           # Carga inicial de datos y enrutamiento SPA
│   ├── Controladores/    # Endpoints REST (/api/auth, /api/usuarios, /api/articulos, /api/almacen, etc.)
│   ├── DTOs/             # Objetos de transferencia de entrada y salida (Request/Response)
│   ├── Entidades/        # Entidades JPA mapeadas a SQL Server (bd_almacen)
│   ├── Excepciones/      # Excepciones de negocio y ManejadorErrores global (@RestControllerAdvice)
│   ├── Repositorios/     # Interfaces Spring Data JPA
│   ├── Seguridad/        # SecurityConfig, JwtFiltro, JwtService, DetalleUsuarioService, UsuarioActualHelper
│   └── Servicios/        # Lógica de negocio (Almacén FIFO, Artículos, Catálogos, Proveedores, Seguridad, Kardex)
├── src/main/resources/
│   ├── application.properties        # Conexión a SQL Server (bd_almacen) y configuración JWT
│   ├── application-dev.properties    # Perfil opcional en memoria (H2 en modo SQL Server)
│   └── static/                       # Build de producción de Angular servido directamente por Spring Boot
├── frontend-angular/src/app/
│   ├── guards/           # authGuard, guestGuard, permisoGuard
│   ├── layout/           # ShellComponent (barra lateral filtrada por permisos, cabecera y alertas)
│   ├── models/           # Interfaces TypeScript sincronizadas con los DTOs de Spring Boot
│   ├── pages/            # Módulos independientes: login, dashboard, articulos, catalogos, proveedores, seguridad, almacen
│   └── services/         # AuthService, authInterceptor, ApiService y StoreService (Angular Signals)
├── database/
│   └── script_bd_almacen.sql         # Script SQL Server (DDL + datos iniciales)
├── mvnw / mvnw.cmd / pom.xml         # Wrapper y configuración de Maven
└── README.md
```

---

## 2. Cómo Ejecutar el Proyecto desde CMD (Windows)

### Paso 0: Actualizar los últimos cambios desde GitHub
```cmd
git pull origin main
```

### Paso 1: Preparar la Base de Datos (2 opciones)

#### Opción A — Usando SQL Server Local (`bd_almacen`)
1. Abre **SQL Server Management Studio (SSMS)**.
2. Abre y ejecuta el archivo [`database/script_bd_almacen.sql`](./database/script_bd_almacen.sql) para crear la base de datos `bd_almacen`, sus 17 tablas y los registros semilla.
   - *Nota:* Si usas autenticación SQL (`sa` y contraseña) en lugar de Windows Authentication (`integratedSecurity=true`), edita [`src/main/resources/application.properties`](./src/main/resources/application.properties):
     ```properties
     spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=bd_almacen;encrypt=true;trustServerCertificate=true;
     spring.datasource.username=sa
     spring.datasource.password=TuPasswordSQL
     ```

#### Opción B — Modo rápido sin instalar SQL Server (`dev`)
Ejecuta el backend con el perfil `dev`, que levanta una base de datos H2 compatible con SQL Server en memoria y carga automáticamente todos los registros mediante [`CargaInicial.java`](./src/main/java/com/example/inventario/Config/CargaInicial.java).

---

### Paso 2: Iniciar el Backend (Spring Boot) en CMD #1

En la raíz del proyecto (`proyec-desarrollo-we`):

- **Con SQL Server (`bd_almacen`):**
  ```cmd
  mvnw.cmd spring-boot:run
  ```
- **Con base de datos en memoria (`dev`):**
  ```cmd
  mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  ```

Una vez iniciado verás `Tomcat started on port 8080 (http)` y podrás abrir la aplicación directamente en **`http://localhost:8080`**.

---

### Paso 3: Iniciar el Servidor de Desarrollo de Angular en CMD #2 (opcional)

```cmd
cd frontend-angular
npm install
npm start
```

Abre tu navegador en **`http://localhost:4200`** (se conecta automáticamente al backend en `http://localhost:8080/api`).

---

## 3. Credenciales Iniciales de Acceso

| Usuario (`username`) | Contraseña (`password`) | Rol en el Sistema | Alcance |
|---|---|---|---|
| `admin` | `Admin2026*` | `ADMINISTRADOR` | Acceso total (20 permisos), administración de usuarios, roles, catálogos y almacén |
| `jefe.almacen` | `Jefe2026*` | `JEFE_ALMACEN` | Catálogos maestros, artículos, proveedores, ingresos, salidas, transferencias y reportes |
| `almacenero` | `Almacen2026*` | `ALMACENERO` | Ingresos por lote, salidas FIFO, transferencias y consulta de Kardex |
| `solicitante` | `Solicita2026*` | `SOLICITANTE` | Consulta de stock de artículos y solicitud de materiales |
| `costos` | `Costos2026*` | `COSTOS` | Consulta de inventario, Kardex y reportes |

---

## 4. Pruebas Automatizadas (JUnit 5 + MockMvc)

Ejecuta las pruebas de integración, validaciones de negocio y reglas de autorización RBAC con:

```cmd
mvnw.cmd test
```
