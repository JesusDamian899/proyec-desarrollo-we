/* ============================================================================
   SISTEMA DE GESTION DE INVENTARIO Y ALMACEN - ELECTROMECANICA ANDINA S.A.C.
   SCRIPT DE BASE DE DATOS SQL SERVER (DDL EXACTO JPA/HIBERNATE + DATOS SEMILLA)
   Base de Datos: bd_almacen
   ============================================================================ */

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'bd_almacen')
BEGIN
    CREATE DATABASE bd_almacen;
END
GO

USE bd_almacen;
GO

/* ============================================================================
   0. LIMPIEZA TOTAL DE TABLAS PREVIAS (EVITA CONFLICTOS DE ESQUEMA CON HIBERNATE)
   ============================================================================ */

-- Eliminar posibles tablas con nombres en plural de versiones anteriores
IF OBJECT_ID('dbo.movimientos_inventario', 'U') IS NOT NULL DROP TABLE dbo.movimientos_inventario;
IF OBJECT_ID('dbo.transferencias_detalle', 'U') IS NOT NULL DROP TABLE dbo.transferencias_detalle;
IF OBJECT_ID('dbo.transferencias_cabecera', 'U') IS NOT NULL DROP TABLE dbo.transferencias_cabecera;
IF OBJECT_ID('dbo.salidas_produccion_detalle', 'U') IS NOT NULL DROP TABLE dbo.salidas_produccion_detalle;
IF OBJECT_ID('dbo.salidas_produccion_cabecera', 'U') IS NOT NULL DROP TABLE dbo.salidas_produccion_cabecera;
IF OBJECT_ID('dbo.ingresos_detalle', 'U') IS NOT NULL DROP TABLE dbo.ingresos_detalle;
IF OBJECT_ID('dbo.ingresos_cabecera', 'U') IS NOT NULL DROP TABLE dbo.ingresos_cabecera;
IF OBJECT_ID('dbo.lotes', 'U') IS NOT NULL DROP TABLE dbo.lotes;

-- Eliminar tablas transaccionales e intermedias actuales en orden de dependencias (FK)
IF OBJECT_ID('dbo.usuario_rol', 'U') IS NOT NULL DROP TABLE dbo.usuario_rol;
IF OBJECT_ID('dbo.rol_permiso', 'U') IS NOT NULL DROP TABLE dbo.rol_permiso;
IF OBJECT_ID('dbo.kardex', 'U') IS NOT NULL DROP TABLE dbo.kardex;
IF OBJECT_ID('dbo.movimiento_inventario', 'U') IS NOT NULL DROP TABLE dbo.movimiento_inventario;
IF OBJECT_ID('dbo.transferencia_detalle', 'U') IS NOT NULL DROP TABLE dbo.transferencia_detalle;
IF OBJECT_ID('dbo.transferencia_cabecera', 'U') IS NOT NULL DROP TABLE dbo.transferencia_cabecera;
IF OBJECT_ID('dbo.salida_produccion_detalle', 'U') IS NOT NULL DROP TABLE dbo.salida_produccion_detalle;
IF OBJECT_ID('dbo.salida_produccion_cabecera', 'U') IS NOT NULL DROP TABLE dbo.salida_produccion_cabecera;
IF OBJECT_ID('dbo.ingreso_detalle', 'U') IS NOT NULL DROP TABLE dbo.ingreso_detalle;
IF OBJECT_ID('dbo.ingreso_cabecera', 'U') IS NOT NULL DROP TABLE dbo.ingreso_cabecera;
IF OBJECT_ID('dbo.lote', 'U') IS NOT NULL DROP TABLE dbo.lote;
IF OBJECT_ID('dbo.articulo', 'U') IS NOT NULL DROP TABLE dbo.articulo;
IF OBJECT_ID('dbo.articulos', 'U') IS NOT NULL DROP TABLE dbo.articulos;
IF OBJECT_ID('dbo.proveedor', 'U') IS NOT NULL DROP TABLE dbo.proveedor;
IF OBJECT_ID('dbo.proveedores', 'U') IS NOT NULL DROP TABLE dbo.proveedores;
IF OBJECT_ID('dbo.ubicacion', 'U') IS NOT NULL DROP TABLE dbo.ubicacion;
IF OBJECT_ID('dbo.ubicaciones', 'U') IS NOT NULL DROP TABLE dbo.ubicaciones;
IF OBJECT_ID('dbo.unidad_medida', 'U') IS NOT NULL DROP TABLE dbo.unidad_medida;
IF OBJECT_ID('dbo.unidades_medida', 'U') IS NOT NULL DROP TABLE dbo.unidades_medida;
IF OBJECT_ID('dbo.categoria', 'U') IS NOT NULL DROP TABLE dbo.categoria;
IF OBJECT_ID('dbo.categorias', 'U') IS NOT NULL DROP TABLE dbo.categorias;
IF OBJECT_ID('dbo.usuario', 'U') IS NOT NULL DROP TABLE dbo.usuario;
IF OBJECT_ID('dbo.rol', 'U') IS NOT NULL DROP TABLE dbo.rol;
IF OBJECT_ID('dbo.permiso', 'U') IS NOT NULL DROP TABLE dbo.permiso;
GO

/* ============================================================================
   1. TABLAS DE SEGURIDAD Y CONTROL DE ACCESO (SPRINT 01: RF-01, RF-02, RF-03)
   ============================================================================ */

CREATE TABLE dbo.permiso (
    id_permiso BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    codigo VARCHAR(60) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    modulo VARCHAR(40) NOT NULL,
    ruta VARCHAR(120) NULL,
    tipo VARCHAR(20) NOT NULL,
    mostrar_en_menu BIT NOT NULL DEFAULT 1,
    orden INT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.rol (
    id_rol BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE,
    descripcion VARCHAR(150) NULL,
    activo BIT NOT NULL DEFAULT 1
);

CREATE TABLE dbo.rol_permiso (
    id_rol BIGINT NOT NULL,
    id_permiso BIGINT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT FK_rol_permiso_rol FOREIGN KEY (id_rol) REFERENCES dbo.rol(id_rol),
    CONSTRAINT FK_rol_permiso_permiso FOREIGN KEY (id_permiso) REFERENCES dbo.permiso(id_permiso)
);

CREATE TABLE dbo.usuario (
    id_usuario BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    activo BIT NOT NULL DEFAULT 1,
    fecha_creacion DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    ultimo_acceso DATETIME2(7) NULL
);

CREATE TABLE dbo.usuario_rol (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT FK_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id_usuario),
    CONSTRAINT FK_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES dbo.rol(id_rol)
);

/* ============================================================================
   2. TABLAS BASICAS / CATALOGOS MAESTROS (SPRINT 02: RF-04, RF-05, RF-06)
   ============================================================================ */

CREATE TABLE dbo.categoria (
    id_categoria INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE dbo.unidad_medida (
    id_unidad_medida INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    abreviatura VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE dbo.ubicacion (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL
);

CREATE TABLE dbo.proveedor (
    id_proveedor INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    contacto VARCHAR(100) NULL
);

CREATE TABLE dbo.articulo (
    id_articulo INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    codigo_interno VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    id_categoria INT NOT NULL,
    id_unidad_medida INT NOT NULL,
    id_ubicacion BIGINT NULL,
    stock_minimo NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    stock_actual NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT FK_articulo_categoria FOREIGN KEY (id_categoria) REFERENCES dbo.categoria(id_categoria),
    CONSTRAINT FK_articulo_unidad FOREIGN KEY (id_unidad_medida) REFERENCES dbo.unidad_medida(id_unidad_medida),
    CONSTRAINT FK_articulo_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES dbo.ubicacion(id)
);

/* ============================================================================
   3. TABLAS TRANSACCIONALES DE ALMACEN Y KARDEX
   ============================================================================ */

CREATE TABLE dbo.lote (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    codigo_lote VARCHAR(80) NOT NULL UNIQUE,
    articulo_id INT NOT NULL,
    ubicacion_id BIGINT NOT NULL,
    cantidad NUMERIC(19,3) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    fecha_vencimiento DATE NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT FK_lote_articulo FOREIGN KEY (articulo_id) REFERENCES dbo.articulo(id_articulo),
    CONSTRAINT FK_lote_ubicacion FOREIGN KEY (ubicacion_id) REFERENCES dbo.ubicacion(id)
);

CREATE TABLE dbo.ingreso_cabecera (
    id_ingreso INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    fecha_recepcion DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    documento_referencia VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NULL,
    id_proveedor INT NOT NULL,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT FK_ingreso_proveedor FOREIGN KEY (id_proveedor) REFERENCES dbo.proveedor(id_proveedor),
    CONSTRAINT FK_ingreso_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id_usuario)
);

CREATE TABLE dbo.ingreso_detalle (
    id_detalle_ingreso INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_ingreso INT NOT NULL,
    id_articulo INT NOT NULL,
    cantidad_recibida NUMERIC(10,2) NOT NULL,
    numero_lote VARCHAR(50) NOT NULL,
    fecha_vencimiento DATETIME2(7) NOT NULL,
    CONSTRAINT FK_ingreso_det_cab FOREIGN KEY (id_ingreso) REFERENCES dbo.ingreso_cabecera(id_ingreso),
    CONSTRAINT FK_ingreso_det_art FOREIGN KEY (id_articulo) REFERENCES dbo.articulo(id_articulo)
);

CREATE TABLE dbo.salida_produccion_cabecera (
    id_salida INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    fecha_salida DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    orden_produccion VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NULL,
    id_solicitante BIGINT NULL,
    id_usuario BIGINT NULL,
    CONSTRAINT FK_salida_solicitante FOREIGN KEY (id_solicitante) REFERENCES dbo.usuario(id_usuario),
    CONSTRAINT FK_salida_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id_usuario)
);

CREATE TABLE dbo.salida_produccion_detalle (
    id_detalle_salida INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_salida INT NOT NULL,
    id_articulo INT NOT NULL,
    id_detalle_ingreso INT NOT NULL,
    cantidad_entregada NUMERIC(10,2) NOT NULL,
    CONSTRAINT FK_salida_det_cab FOREIGN KEY (id_salida) REFERENCES dbo.salida_produccion_cabecera(id_salida),
    CONSTRAINT FK_salida_det_art FOREIGN KEY (id_articulo) REFERENCES dbo.articulo(id_articulo),
    CONSTRAINT FK_salida_det_ing FOREIGN KEY (id_detalle_ingreso) REFERENCES dbo.ingreso_detalle(id_detalle_ingreso)
);

CREATE TABLE dbo.transferencia_cabecera (
    id_transferencia INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    fecha_transferencia DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    motivo VARCHAR(255) NULL,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT FK_transferencia_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id_usuario)
);

CREATE TABLE dbo.transferencia_detalle (
    id_detalle_transferencia INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_transferencia INT NOT NULL,
    id_articulo INT NOT NULL,
    cantidad_movida NUMERIC(10,2) NOT NULL,
    id_ubicacion_origen BIGINT NOT NULL,
    id_ubicacion_destino BIGINT NOT NULL,
    CONSTRAINT FK_transf_det_cab FOREIGN KEY (id_transferencia) REFERENCES dbo.transferencia_cabecera(id_transferencia),
    CONSTRAINT FK_transf_det_art FOREIGN KEY (id_articulo) REFERENCES dbo.articulo(id_articulo),
    CONSTRAINT FK_transf_det_orig FOREIGN KEY (id_ubicacion_origen) REFERENCES dbo.ubicacion(id),
    CONSTRAINT FK_transf_det_dest FOREIGN KEY (id_ubicacion_destino) REFERENCES dbo.ubicacion(id)
);

CREATE TABLE dbo.movimiento_inventario (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad NUMERIC(19,3) NOT NULL,
    fecha_movimiento DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    articulo_id INT NOT NULL,
    lote_id BIGINT NOT NULL,
    ubicacion_origen_id BIGINT NULL,
    ubicacion_destino_id BIGINT NULL,
    CONSTRAINT FK_mov_articulo FOREIGN KEY (articulo_id) REFERENCES dbo.articulo(id_articulo),
    CONSTRAINT FK_mov_lote FOREIGN KEY (lote_id) REFERENCES dbo.lote(id),
    CONSTRAINT FK_mov_orig FOREIGN KEY (ubicacion_origen_id) REFERENCES dbo.ubicacion(id),
    CONSTRAINT FK_mov_dest FOREIGN KEY (ubicacion_destino_id) REFERENCES dbo.ubicacion(id)
);

CREATE TABLE dbo.kardex (
    id_kardex INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    fecha_movimiento DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    tipo_movimiento VARCHAR(20) NOT NULL,
    referencia_documento VARCHAR(50) NULL,
    id_articulo INT NOT NULL,
    cantidad NUMERIC(10,2) NOT NULL,
    saldo_resultante NUMERIC(10,2) NOT NULL,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT FK_kardex_articulo FOREIGN KEY (id_articulo) REFERENCES dbo.articulo(id_articulo),
    CONSTRAINT FK_kardex_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id_usuario)
);
GO

/* ============================================================================
   4. INSERCION DE REGISTROS EN LAS TABLAS (DATOS SEMILLA COMPLETOS)
   Nota: Al iniciar Spring Boot, CargaInicial.java sincroniza automaticamente
   las contraseñas BCrypt ('Admin2026*') de los 5 usuarios corporativos.
   ============================================================================ */

-- 4.1 PERMISOS (20 permisos RBAC)
INSERT INTO dbo.permiso (codigo, nombre, modulo, ruta, tipo, mostrar_en_menu, orden) VALUES
('ARTICULO_VER', 'Consultar stock', 'Inventario', '/api/articulos', 'VER', 1, 11),
('ARTICULO_GESTIONAR', 'Crear y editar articulos', 'Inventario', NULL, 'EDITAR', 0, 12),
('STOCK_AJUSTAR', 'Ajuste de inventario', 'Inventario', '/api/ajustes', 'CREAR', 1, 13),
('INGRESO_VER', 'Ver recepciones', 'Recepciones', '/api/ingresos', 'VER', 1, 21),
('INGRESO_CREAR', 'Registrar recepcion', 'Recepciones', NULL, 'CREAR', 0, 22),
('SOLICITUD_CREAR', 'Solicitar materiales', 'Despachos', '/api/solicitudes', 'CREAR', 1, 31),
('SOLICITUD_VER_PROPIAS', 'Mis solicitudes', 'Despachos', '/api/solicitudes/mias', 'VER', 1, 32),
('SALIDA_VER', 'Ver despachos', 'Despachos', '/api/salidas', 'VER', 1, 33),
('SALIDA_APROBAR', 'Aprobar salida', 'Despachos', NULL, 'APROBAR', 0, 34),
('SALIDA_ATENDER', 'Despachar materiales', 'Despachos', NULL, 'CREAR', 0, 35),
('TRANSFER_VER', 'Ver transferencias', 'Transferencias', '/api/transferencias', 'VER', 1, 41),
('TRANSFER_CREAR', 'Registrar transferencia', 'Transferencias', NULL, 'CREAR', 0, 42),
('KARDEX_VER', 'Consultar Kardex', 'Kardex y reportes', '/api/kardex', 'VER', 1, 51),
('REPORTE_STOCK_MINIMO', 'Articulos bajo minimo', 'Kardex y reportes', '/api/reportes/minimos', 'VER', 1, 52),
('REPORTE_VALORIZADO', 'Inventario valorizado', 'Kardex y reportes', '/api/reportes/valorizado', 'VER', 1, 53),
('REPORTE_VENCIMIENTOS', 'Lotes por vencer', 'Kardex y reportes', '/api/reportes/vencimientos', 'VER', 1, 54),
('MAESTRO_GESTIONAR', 'Categorias, unidades y ubicaciones', 'Maestros', '/api/maestros', 'VER', 1, 61),
('PROVEEDOR_GESTIONAR', 'Proveedores', 'Maestros', '/api/proveedores', 'VER', 1, 62),
('USUARIO_GESTIONAR', 'Usuarios', 'Seguridad', '/api/usuarios', 'VER', 1, 71),
('ROL_GESTIONAR', 'Roles y permisos', 'Seguridad', '/api/roles', 'VER', 1, 72);

-- 4.2 ROLES (5 roles del negocio)
INSERT INTO dbo.rol (nombre, descripcion, activo) VALUES
('ADMINISTRADOR', 'Acceso total y gestion de usuarios', 1),
('JEFE_ALMACEN', 'Supervisa, aprueba salidas y ajusta inventario', 1),
('ALMACENERO', 'Ejecuta movimientos fisicos de almacen', 1),
('SOLICITANTE', 'Solicita materiales para produccion', 1),
('COSTOS', 'Consulta Kardex y reportes valorizados', 1);

-- Asignar todos los permisos al ADMINISTRADOR (id_rol = 1)
INSERT INTO dbo.rol_permiso (id_rol, id_permiso)
SELECT 1, id_permiso FROM dbo.permiso;

-- Asignar permisos a JEFE_ALMACEN (id_rol = 2)
INSERT INTO dbo.rol_permiso (id_rol, id_permiso)
SELECT 2, id_permiso FROM dbo.permiso WHERE codigo IN (
    'ARTICULO_VER','ARTICULO_GESTIONAR','STOCK_AJUSTAR','INGRESO_VER','INGRESO_CREAR',
    'SALIDA_VER','SALIDA_APROBAR','SALIDA_ATENDER','TRANSFER_VER','TRANSFER_CREAR',
    'KARDEX_VER','REPORTE_STOCK_MINIMO','REPORTE_VALORIZADO','REPORTE_VENCIMIENTOS',
    'MAESTRO_GESTIONAR','PROVEEDOR_GESTIONAR'
);

-- Asignar permisos a ALMACENERO (id_rol = 3)
INSERT INTO dbo.rol_permiso (id_rol, id_permiso)
SELECT 3, id_permiso FROM dbo.permiso WHERE codigo IN (
    'ARTICULO_VER','INGRESO_VER','INGRESO_CREAR','SALIDA_VER','SALIDA_ATENDER',
    'TRANSFER_VER','TRANSFER_CREAR','KARDEX_VER','REPORTE_STOCK_MINIMO','REPORTE_VENCIMIENTOS'
);

-- Asignar permisos a SOLICITANTE (id_rol = 4)
INSERT INTO dbo.rol_permiso (id_rol, id_permiso)
SELECT 4, id_permiso FROM dbo.permiso WHERE codigo IN (
    'ARTICULO_VER','SOLICITUD_CREAR','SOLICITUD_VER_PROPIAS'
);

-- Asignar permisos a COSTOS (id_rol = 5)
INSERT INTO dbo.rol_permiso (id_rol, id_permiso)
SELECT 5, id_permiso FROM dbo.permiso WHERE codigo IN (
    'ARTICULO_VER','INGRESO_VER','SALIDA_VER','TRANSFER_VER',
    'KARDEX_VER','REPORTE_STOCK_MINIMO','REPORTE_VALORIZADO','REPORTE_VENCIMIENTOS'
);

-- 4.3 USUARIOS (Contraseña: Admin2026*)
INSERT INTO dbo.usuario (username, password_hash, nombre_completo, email, activo) VALUES
('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Administrador del Sistema', 'admin@electromecanicaandina.com', 1),
('jefe.almacen', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Carlos Mendoza (Jefe de Almacen)', 'cmendoza@electromecanicaandina.com', 1),
('almacenero', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Luis Paredes (Operador Almacen)', 'lparedes@electromecanicaandina.com', 1),
('solicitante', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Ana Torres (Supervisora Produccion)', 'atorres@electromecanicaandina.com', 1),
('costos', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Jorge Ramirez (Analista de Costos)', 'jramirez@electromecanicaandina.com', 1);

INSERT INTO dbo.usuario_rol (id_usuario, id_rol) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5);

-- 4.4 CATEGORIAS
INSERT INTO dbo.categoria (nombre) VALUES
('Materiales Electricos'),
('Componentes Mecanicos'),
('Herramientas e Insumos'),
('Equipos de Proteccion (EPP)');

-- 4.5 UNIDADES DE MEDIDA
INSERT INTO dbo.unidad_medida (nombre, abreviatura) VALUES
('Unidad', 'UND'),
('Metro', 'M'),
('Kilogramo', 'KG'),
('Litro', 'LT'),
('Rollo', 'RLL');

-- 4.6 UBICACIONES
INSERT INTO dbo.ubicacion (nombre, codigo, tipo) VALUES
('Almacen Principal - Estante A1', 'ALM-A1', 'ESTANTERIA'),
('Almacen Principal - Estante B1', 'ALM-B1', 'ESTANTERIA'),
('Zona de Recepcion', 'REC-01', 'RECEPCION'),
('Planta de Produccion', 'PROD-01', 'PRODUCCION');

-- 4.7 PROVEEDORES
INSERT INTO dbo.proveedor (razon_social, ruc, contacto) VALUES
('Suministros Industriales Andinos SAC', '20512345678', 'ventas@suministrosandinos.pe'),
('Importadora Electromecanica Peru SAA', '20609876543', 'contacto@imep.com.pe'),
('Ferreyros Industrial Corp SAC', '20100028698', 'atencion@ferreyrosind.pe');

-- 4.8 ARTICULOS
INSERT INTO dbo.articulo (codigo_interno, descripcion, id_categoria, id_unidad_medida, id_ubicacion, stock_minimo, stock_actual) VALUES
('EL-00101', 'Interruptor Termomagnetico Trifasico 63A', 1, 1, 1, 10.00, 25.00),
('EL-00102', 'Cable THW-90 12 AWG Indeco (Rollo 100m)', 1, 5, 1, 8.00, 14.00),
('MC-00201', 'Rodamiento Rigido de Bolas SKF 6205-2RSH', 2, 1, 2, 20.00, 12.00),
('IN-00301', 'Lubricante Dielectrico Industrial WD-40', 3, 4, 2, 15.00, 30.00),
('EP-00401', 'Guantes Dielectricos Clase 00 Certificados', 4, 1, 1, 12.00, 8.00);

-- 4.9 LOTES Y KARDEX INICIAL
INSERT INTO dbo.lote (codigo_lote, articulo_id, ubicacion_id, cantidad, fecha_ingreso, fecha_vencimiento, estado) VALUES
('LOT-2026-001', 1, 1, 25.000, CAST(GETDATE() AS DATE), '2027-06-15', 'ACTIVO'),
('LOT-2026-002', 2, 1, 14.000, CAST(GETDATE() AS DATE), '2028-01-20', 'ACTIVO'),
('LOT-2026-003', 3, 2, 12.000, CAST(GETDATE() AS DATE), '2026-12-05', 'ACTIVO'),
('LOT-2026-004', 4, 2, 30.000, CAST(GETDATE() AS DATE), '2026-11-30', 'ACTIVO'),
('LOT-2026-005', 5, 1,  8.000, CAST(GETDATE() AS DATE), '2027-05-10', 'ACTIVO');

INSERT INTO dbo.kardex (tipo_movimiento, referencia_documento, id_articulo, cantidad, saldo_resultante, id_usuario) VALUES
('INGRESO', 'GR-001-00451 (Lote LOT-2026-001)', 1, 25.00, 25.00, 1),
('INGRESO', 'GR-001-00452 (Lote LOT-2026-002)', 2, 14.00, 14.00, 1),
('INGRESO', 'GR-001-00453 (Lote LOT-2026-003)', 3, 12.00, 12.00, 1),
('INGRESO', 'GR-001-00454 (Lote LOT-2026-004)', 4, 30.00, 30.00, 1),
('INGRESO', 'GR-001-00455 (Lote LOT-2026-005)', 5,  8.00,  8.00, 1);
GO
