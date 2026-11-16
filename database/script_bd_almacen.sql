/* ============================================================================
   SISTEMA DE GESTION DE INVENTARIO Y ALMACEN - ELECTROMECANICA ANDINA S.A.C.
   SCRIPT DE BASE DE DATOS SQL SERVER (DDL + REGISTROS INICIALES)
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
   1. TABLAS DE SEGURIDAD Y CONTROL DE ACCESO (SPRINT 01: RF-01, RF-02, RF-03)
   ============================================================================ */

IF OBJECT_ID('dbo.usuario_rol', 'U') IS NOT NULL DROP TABLE dbo.usuario_rol;
IF OBJECT_ID('dbo.rol_permiso', 'U') IS NOT NULL DROP TABLE dbo.rol_permiso;
IF OBJECT_ID('dbo.kardex', 'U') IS NOT NULL DROP TABLE dbo.kardex;
IF OBJECT_ID('dbo.movimientos_inventario', 'U') IS NOT NULL DROP TABLE dbo.movimientos_inventario;
IF OBJECT_ID('dbo.transferencias_detalle', 'U') IS NOT NULL DROP TABLE dbo.transferencias_detalle;
IF OBJECT_ID('dbo.transferencias_cabecera', 'U') IS NOT NULL DROP TABLE dbo.transferencias_cabecera;
IF OBJECT_ID('dbo.salidas_produccion_detalle', 'U') IS NOT NULL DROP TABLE dbo.salidas_produccion_detalle;
IF OBJECT_ID('dbo.salidas_produccion_cabecera', 'U') IS NOT NULL DROP TABLE dbo.salidas_produccion_cabecera;
IF OBJECT_ID('dbo.ingresos_detalle', 'U') IS NOT NULL DROP TABLE dbo.ingresos_detalle;
IF OBJECT_ID('dbo.ingresos_cabecera', 'U') IS NOT NULL DROP TABLE dbo.ingresos_cabecera;
IF OBJECT_ID('dbo.lotes', 'U') IS NOT NULL DROP TABLE dbo.lotes;
IF OBJECT_ID('dbo.articulos', 'U') IS NOT NULL DROP TABLE dbo.articulos;
IF OBJECT_ID('dbo.proveedores', 'U') IS NOT NULL DROP TABLE dbo.proveedores;
IF OBJECT_ID('dbo.ubicaciones', 'U') IS NOT NULL DROP TABLE dbo.ubicaciones;
IF OBJECT_ID('dbo.unidades_medida', 'U') IS NOT NULL DROP TABLE dbo.unidades_medida;
IF OBJECT_ID('dbo.categorias', 'U') IS NOT NULL DROP TABLE dbo.categorias;
IF OBJECT_ID('dbo.usuario', 'U') IS NOT NULL DROP TABLE dbo.usuario;
IF OBJECT_ID('dbo.rol', 'U') IS NOT NULL DROP TABLE dbo.rol;
IF OBJECT_ID('dbo.permiso', 'U') IS NOT NULL DROP TABLE dbo.permiso;
GO

CREATE TABLE dbo.permiso (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo VARCHAR(60) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    modulo VARCHAR(50) NOT NULL,
    ruta VARCHAR(120) NULL,
    tipo VARCHAR(15) NOT NULL,
    mostrar_en_menu BIT NOT NULL DEFAULT 1,
    orden INT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.rol (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE,
    descripcion VARCHAR(150) NULL,
    activo BIT NOT NULL DEFAULT 1
);

CREATE TABLE dbo.rol_permiso (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT FK_rol_permiso_rol FOREIGN KEY (rol_id) REFERENCES dbo.rol(id),
    CONSTRAINT FK_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES dbo.permiso(id)
);

CREATE TABLE dbo.usuario (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    activo BIT NOT NULL DEFAULT 1,
    fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE dbo.usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT FK_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuario(id),
    CONSTRAINT FK_usuario_rol_rol FOREIGN KEY (rol_id) REFERENCES dbo.rol(id)
);

/* ============================================================================
   2. TABLAS BASICAS / CATALOGOS MAESTROS (SPRINT 02: RF-04, RF-05, RF-06)
   ============================================================================ */

CREATE TABLE dbo.categorias (
    id_categoria BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE dbo.unidades_medida (
    id_unidad BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    abreviatura VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE dbo.ubicaciones (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    activo BIT NOT NULL DEFAULT 1
);

CREATE TABLE dbo.proveedores (
    id_proveedor BIGINT IDENTITY(1,1) PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    contacto VARCHAR(100) NULL
);

CREATE TABLE dbo.articulos (
    id_articulo BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo_interno VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL,
    id_categoria BIGINT NOT NULL,
    id_unidad BIGINT NOT NULL,
    id_ubicacion BIGINT NULL,
    stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT FK_articulos_categoria FOREIGN KEY (id_categoria) REFERENCES dbo.categorias(id_categoria),
    CONSTRAINT FK_articulos_unidad FOREIGN KEY (id_unidad) REFERENCES dbo.unidades_medida(id_unidad),
    CONSTRAINT FK_articulos_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES dbo.ubicaciones(id)
);

/* ============================================================================
   3. TABLAS TRANSACCIONALES DE ALMACEN Y KARDEX (SPRINTS 03, 04 Y 05)
   ============================================================================ */

CREATE TABLE dbo.lotes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo_lote VARCHAR(60) NOT NULL,
    articulo_id BIGINT NOT NULL,
    ubicacion_id BIGINT NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL,
    fecha_ingreso DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    fecha_vencimiento DATE NULL,
    activo BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_lotes_articulo FOREIGN KEY (articulo_id) REFERENCES dbo.articulos(id_articulo),
    CONSTRAINT FK_lotes_ubicacion FOREIGN KEY (ubicacion_id) REFERENCES dbo.ubicaciones(id)
);

CREATE TABLE dbo.ingresos_cabecera (
    id_ingreso BIGINT IDENTITY(1,1) PRIMARY KEY,
    fecha_ingreso DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    documento_referencia VARCHAR(50) NOT NULL,
    id_proveedor BIGINT NULL,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT FK_ingresos_proveedor FOREIGN KEY (id_proveedor) REFERENCES dbo.proveedores(id_proveedor),
    CONSTRAINT FK_ingresos_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id)
);

CREATE TABLE dbo.ingresos_detalle (
    id_ingreso_detalle BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_ingreso BIGINT NOT NULL,
    id_articulo BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    costo_unitario DECIMAL(10,2) NULL,
    lote VARCHAR(50) NOT NULL,
    fecha_vencimiento DATE NULL,
    cantidad_disponible_lote DECIMAL(10,2) NOT NULL,
    CONSTRAINT FK_ingresos_det_cab FOREIGN KEY (id_ingreso) REFERENCES dbo.ingresos_cabecera(id_ingreso),
    CONSTRAINT FK_ingresos_det_art FOREIGN KEY (id_articulo) REFERENCES dbo.articulos(id_articulo)
);

CREATE TABLE dbo.salidas_produccion_cabecera (
    id_salida BIGINT IDENTITY(1,1) PRIMARY KEY,
    fecha_salida DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    orden_produccion VARCHAR(50) NOT NULL,
    id_usuario_solicitante BIGINT NOT NULL,
    id_usuario_despachador BIGINT NOT NULL,
    CONSTRAINT FK_salidas_solicitante FOREIGN KEY (id_usuario_solicitante) REFERENCES dbo.usuario(id),
    CONSTRAINT FK_salidas_despachador FOREIGN KEY (id_usuario_despachador) REFERENCES dbo.usuario(id)
);

CREATE TABLE dbo.salidas_produccion_detalle (
    id_salida_detalle BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_salida BIGINT NOT NULL,
    id_articulo BIGINT NOT NULL,
    cantidad_despachada DECIMAL(10,2) NOT NULL,
    id_ingreso_detalle_lote BIGINT NULL,
    CONSTRAINT FK_salidas_det_cab FOREIGN KEY (id_salida) REFERENCES dbo.salidas_produccion_cabecera(id_salida),
    CONSTRAINT FK_salidas_det_art FOREIGN KEY (id_articulo) REFERENCES dbo.articulos(id_articulo),
    CONSTRAINT FK_salidas_det_lote FOREIGN KEY (id_ingreso_detalle_lote) REFERENCES dbo.ingresos_detalle(id_ingreso_detalle)
);

CREATE TABLE dbo.transferencias_cabecera (
    id_transferencia BIGINT IDENTITY(1,1) PRIMARY KEY,
    fecha_transferencia DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    motivo VARCHAR(200) NOT NULL,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT FK_transferencias_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id)
);

CREATE TABLE dbo.transferencias_detalle (
    id_transferencia_detalle BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_transferencia BIGINT NOT NULL,
    id_articulo BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    id_ubicacion_origen BIGINT NOT NULL,
    id_ubicacion_destino BIGINT NOT NULL,
    CONSTRAINT FK_transf_det_cab FOREIGN KEY (id_transferencia) REFERENCES dbo.transferencias_cabecera(id_transferencia),
    CONSTRAINT FK_transf_det_art FOREIGN KEY (id_articulo) REFERENCES dbo.articulos(id_articulo),
    CONSTRAINT FK_transf_det_orig FOREIGN KEY (id_ubicacion_origen) REFERENCES dbo.ubicaciones(id),
    CONSTRAINT FK_transf_det_dest FOREIGN KEY (id_ubicacion_destino) REFERENCES dbo.ubicaciones(id)
);

CREATE TABLE dbo.movimientos_inventario (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    fecha_movimiento DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    lote_id BIGINT NOT NULL,
    ubicacion_origen_id BIGINT NULL,
    ubicacion_destino_id BIGINT NULL,
    observacion VARCHAR(255) NULL,
    CONSTRAINT FK_mov_lote FOREIGN KEY (lote_id) REFERENCES dbo.lotes(id),
    CONSTRAINT FK_mov_orig FOREIGN KEY (ubicacion_origen_id) REFERENCES dbo.ubicaciones(id),
    CONSTRAINT FK_mov_dest FOREIGN KEY (ubicacion_destino_id) REFERENCES dbo.ubicaciones(id)
);

CREATE TABLE dbo.kardex (
    id_kardex BIGINT IDENTITY(1,1) PRIMARY KEY,
    fecha_movimiento DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    tipo_movimiento VARCHAR(30) NOT NULL,
    referencia_documento VARCHAR(50) NOT NULL,
    id_articulo BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    saldo_resultante DECIMAL(10,2) NOT NULL,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT FK_kardex_articulo FOREIGN KEY (id_articulo) REFERENCES dbo.articulos(id_articulo),
    CONSTRAINT FK_kardex_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id)
);
GO

/* ============================================================================
   4. INSERCION DE REGISTROS EN LAS TABLAS (DATOS SEMILLA COMPLETOS)
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

-- Asignar todos los permisos al ADMINISTRADOR (rol_id = 1)
INSERT INTO dbo.rol_permiso (rol_id, permiso_id)
SELECT 1, id FROM dbo.permiso;

-- Asignar permisos a JEFE_ALMACEN (rol_id = 2)
INSERT INTO dbo.rol_permiso (rol_id, permiso_id)
SELECT 2, id FROM dbo.permiso WHERE codigo IN (
    'ARTICULO_VER','ARTICULO_GESTIONAR','STOCK_AJUSTAR','INGRESO_VER','INGRESO_CREAR',
    'SALIDA_VER','SALIDA_APROBAR','SALIDA_ATENDER','TRANSFER_VER','TRANSFER_CREAR',
    'KARDEX_VER','REPORTE_STOCK_MINIMO','REPORTE_VALORIZADO','REPORTE_VENCIMIENTOS',
    'MAESTRO_GESTIONAR','PROVEEDOR_GESTIONAR'
);

-- Asignar permisos a ALMACENERO (rol_id = 3)
INSERT INTO dbo.rol_permiso (rol_id, permiso_id)
SELECT 3, id FROM dbo.permiso WHERE codigo IN (
    'ARTICULO_VER','INGRESO_VER','INGRESO_CREAR','SALIDA_VER','SALIDA_ATENDER',
    'TRANSFER_VER','TRANSFER_CREAR','KARDEX_VER','REPORTE_STOCK_MINIMO','REPORTE_VENCIMIENTOS'
);

-- 4.3 USUARIOS (Hash BCrypt de 'Admin2026*')
INSERT INTO dbo.usuario (username, password_hash, nombre_completo, email, activo) VALUES
('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Administrador del Sistema', 'admin@electromecanicaandina.com', 1),
('jefe.almacen', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Carlos Mendoza (Jefe de Almacen)', 'cmendoza@electromecanicaandina.com', 1),
('almacenero', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Luis Paredes (Operador Almacen)', 'lparedes@electromecanicaandina.com', 1),
('solicitante', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Ana Torres (Supervisora Produccion)', 'atorres@electromecanicaandina.com', 1),
('costos', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M1T5pW3vJ8sL9mN2pQ4rS6tU8vW2', 'Jorge Ramirez (Analista de Costos)', 'jramirez@electromecanicaandina.com', 1);

INSERT INTO dbo.usuario_rol (usuario_id, rol_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5);

-- 4.4 CATEGORIAS
INSERT INTO dbo.categorias (nombre) VALUES
('Materiales Electricos'),
('Componentes Mecanicos'),
('Herramientas e Insumos'),
('Equipos de Proteccion (EPP)');

-- 4.5 UNIDADES DE MEDIDA
INSERT INTO dbo.unidades_medida (nombre, abreviatura) VALUES
('Unidad', 'UND'),
('Metro', 'M'),
('Kilogramo', 'KG'),
('Litro', 'LT'),
('Rollo', 'RLL');

-- 4.6 UBICACIONES
INSERT INTO dbo.ubicaciones (nombre, codigo, tipo, activo) VALUES
('Almacen Principal - Estante A1', 'ALM-A1', 'ESTANTERIA', 1),
('Almacen Principal - Estante B1', 'ALM-B1', 'ESTANTERIA', 1),
('Zona de Recepcion', 'REC-01', 'RECEPCION', 1),
('Planta de Produccion', 'PROD-01', 'PRODUCCION', 1);

-- 4.7 PROVEEDORES
INSERT INTO dbo.proveedores (razon_social, ruc, contacto) VALUES
('Suministros Industriales Andinos SAC', '20512345678', 'ventas@suministrosandinos.pe'),
('Importadora Electromecanica Peru SAA', '20609876543', 'contacto@imep.com.pe'),
('Ferreyros Industrial Corp SAC', '20100028698', 'atencion@ferreyrosind.pe');

-- 4.8 ARTICULOS
INSERT INTO dbo.articulos (codigo_interno, descripcion, id_categoria, id_unidad, id_ubicacion, stock_minimo, stock_actual) VALUES
('EL-00101', 'Interruptor Termomagnetico Trifasico 63A', 1, 1, 1, 10.00, 25.00),
('EL-00102', 'Cable THW-90 12 AWG Indeco (Rollo 100m)', 1, 5, 1, 8.00, 14.00),
('MC-00201', 'Rodamiento Rigido de Bolas SKF 6205-2RSH', 2, 1, 2, 20.00, 12.00),
('IN-00301', 'Lubricante Dielectrico Industrial WD-40', 3, 4, 2, 15.00, 30.00),
('EP-00401', 'Guantes Dielectricos Clase 00 Certificados', 4, 1, 1, 12.00, 8.00);

-- 4.9 LOTES Y KARDEX INICIAL
INSERT INTO dbo.lotes (codigo_lote, articulo_id, ubicacion_id, cantidad_disponible, fecha_vencimiento, activo) VALUES
('LOT-2026-001', 1, 1, 25.00, '2027-06-15', 1),
('LOT-2026-002', 2, 1, 14.00, '2028-01-20', 1),
('LOT-2026-003', 3, 2, 12.00, '2026-12-05', 1),
('LOT-2026-004', 4, 2, 30.00, '2026-11-30', 1),
('LOT-2026-005', 5, 1, 8.00,  '2027-05-10', 1);

INSERT INTO dbo.kardex (tipo_movimiento, referencia_documento, id_articulo, cantidad, saldo_resultante, id_usuario) VALUES
('INGRESO', 'GR-001-00451 (Lote LOT-2026-001)', 1, 25.00, 25.00, 1),
('INGRESO', 'GR-001-00452 (Lote LOT-2026-002)', 2, 14.00, 14.00, 1),
('INGRESO', 'GR-001-00453 (Lote LOT-2026-003)', 3, 12.00, 12.00, 1),
('INGRESO', 'GR-001-00454 (Lote LOT-2026-004)', 4, 30.00, 30.00, 1),
('INGRESO', 'GR-001-00455 (Lote LOT-2026-005)', 5,  8.00,  8.00, 1);
GO
