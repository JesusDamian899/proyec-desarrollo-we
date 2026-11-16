import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { AuthService } from './auth.service';
import {
  Articulo,
  Categoria,
  Kardex,
  Lote,
  MovimientoResponse,
  Permiso,
  Proveedor,
  Rol,
  Ubicacion,
  UnidadMedida,
  Usuario
} from '../models/inventario.models';

interface LocalDbState {
  categorias: Categoria[];
  unidades: UnidadMedida[];
  ubicaciones: Ubicacion[];
  proveedores: Proveedor[];
  articulos: Articulo[];
  usuarios: Usuario[];
  roles: Rol[];
  lotes: Lote[];
  kardex: Kardex[];
  movimientos: MovimientoResponse[];
}

const DEFAULT_DB: LocalDbState = {
  categorias: [
    { idCategoria: 1, nombre: 'Materiales Eléctricos' },
    { idCategoria: 2, nombre: 'Componentes Mecánicos' },
    { idCategoria: 3, nombre: 'Herramientas e Insumos' },
    { idCategoria: 4, nombre: 'Equipos de Protección (EPP)' }
  ],
  unidades: [
    { idUnidad: 1, nombre: 'Unidad', abreviatura: 'UND' },
    { idUnidad: 2, nombre: 'Metro', abreviatura: 'M' },
    { idUnidad: 3, nombre: 'Kilogramo', abreviatura: 'KG' },
    { idUnidad: 4, nombre: 'Litro', abreviatura: 'LT' },
    { idUnidad: 5, nombre: 'Rollo', abreviatura: 'RLL' }
  ],
  ubicaciones: [
    { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA', activo: true },
    { id: 2, nombre: 'Almacén Principal - Estante B1', codigo: 'ALM-B1', tipo: 'ESTANTERIA', activo: true },
    { id: 3, nombre: 'Zona de Recepción', codigo: 'REC-01', tipo: 'RECEPCION', activo: true },
    { id: 4, nombre: 'Planta de Producción', codigo: 'PROD-01', tipo: 'PRODUCCION', activo: true }
  ],
  proveedores: [
    { idProveedor: 1, razonSocial: 'Suministros Industriales Andinos SAC', ruc: '20512345678', contacto: 'ventas@suministrosandinos.pe' },
    { idProveedor: 2, razonSocial: 'Importadora Electromecánica Perú SAA', ruc: '20609876543', contacto: 'contacto@imep.com.pe' },
    { idProveedor: 3, razonSocial: 'Ferreyros Industrial Corp SAC', ruc: '20100028698', contacto: 'atencion@ferreyrosind.pe' }
  ],
  articulos: [
    {
      idArticulo: 1,
      codigoInterno: 'EL-00101',
      descripcion: 'Interruptor Termomagnético Trifásico 63A',
      categoria: { idCategoria: 1, nombre: 'Materiales Eléctricos' },
      unidadMedida: { idUnidad: 1, nombre: 'Unidad', abreviatura: 'UND' },
      ubicacion: { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA' },
      stockMinimo: 10,
      stockActual: 25
    },
    {
      idArticulo: 2,
      codigoInterno: 'EL-00102',
      descripcion: 'Cable THW-90 12 AWG Indeco (Rollo 100m)',
      categoria: { idCategoria: 1, nombre: 'Materiales Eléctricos' },
      unidadMedida: { idUnidad: 5, nombre: 'Rollo', abreviatura: 'RLL' },
      ubicacion: { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA' },
      stockMinimo: 8,
      stockActual: 14
    },
    {
      idArticulo: 3,
      codigoInterno: 'MC-00201',
      descripcion: 'Rodamiento Rígido de Bolas SKF 6205-2RSH',
      categoria: { idCategoria: 2, nombre: 'Componentes Mecánicos' },
      unidadMedida: { idUnidad: 1, nombre: 'Unidad', abreviatura: 'UND' },
      ubicacion: { id: 2, nombre: 'Almacén Principal - Estante B1', codigo: 'ALM-B1', tipo: 'ESTANTERIA' },
      stockMinimo: 20,
      stockActual: 12
    },
    {
      idArticulo: 4,
      codigoInterno: 'IN-00301',
      descripcion: 'Lubricante Dieléctrico Industrial WD-40',
      categoria: { idCategoria: 3, nombre: 'Herramientas e Insumos' },
      unidadMedida: { idUnidad: 4, nombre: 'Litro', abreviatura: 'LT' },
      ubicacion: { id: 2, nombre: 'Almacén Principal - Estante B1', codigo: 'ALM-B1', tipo: 'ESTANTERIA' },
      stockMinimo: 15,
      stockActual: 30
    },
    {
      idArticulo: 5,
      codigoInterno: 'EP-00401',
      descripcion: 'Guantes Dieléctricos Clase 00 Certificados',
      categoria: { idCategoria: 4, nombre: 'Equipos de Protección (EPP)' },
      unidadMedida: { idUnidad: 1, nombre: 'Unidad', abreviatura: 'UND' },
      ubicacion: { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA' },
      stockMinimo: 12,
      stockActual: 8
    }
  ],
  roles: [
    { id: 1, nombre: 'ADMINISTRADOR', descripcion: 'Acceso total y gestión de usuarios', permisos: new Array(20).fill({}) as Permiso[] },
    { id: 2, nombre: 'JEFE_ALMACEN', descripcion: 'Supervisa, aprueba salidas y ajusta inventario', permisos: new Array(16).fill({}) as Permiso[] },
    { id: 3, nombre: 'ALMACENERO', descripcion: 'Ejecuta movimientos físicos de almacén', permisos: new Array(10).fill({}) as Permiso[] },
    { id: 4, nombre: 'SOLICITANTE', descripcion: 'Solicita materiales para producción', permisos: new Array(3).fill({}) as Permiso[] },
    { id: 5, nombre: 'COSTOS', descripcion: 'Consulta Kardex y reportes valorizados', permisos: new Array(5).fill({}) as Permiso[] }
  ],
  usuarios: [
    { id: 1, username: 'admin', nombreCompleto: 'Administrador del Sistema', email: 'admin@electromecanicaandina.com', activo: true, roles: [{ id: 1, nombre: 'ADMINISTRADOR', descripcion: '' }] },
    { id: 2, username: 'jefe.almacen', nombreCompleto: 'Carlos Mendoza (Jefe de Almacén)', email: 'cmendoza@electromecanicaandina.com', activo: true, roles: [{ id: 2, nombre: 'JEFE_ALMACEN', descripcion: '' }] },
    { id: 3, username: 'almacenero', nombreCompleto: 'Luis Paredes (Operador Almacén)', email: 'lparedes@electromecanicaandina.com', activo: true, roles: [{ id: 3, nombre: 'ALMACENERO', descripcion: '' }] },
    { id: 4, username: 'solicitante', nombreCompleto: 'Ana Torres (Supervisora Producción)', email: 'atorres@electromecanicaandina.com', activo: true, roles: [{ id: 4, nombre: 'SOLICITANTE', descripcion: '' }] },
    { id: 5, username: 'costos', nombreCompleto: 'Jorge Ramírez (Analista de Costos)', email: 'jramirez@electromecanicaandina.com', activo: true, roles: [{ id: 5, nombre: 'COSTOS', descripcion: '' }] }
  ],
  lotes: [
    { id: 1, codigoLote: 'LOT-2026-001', articulo: { idArticulo: 1, codigoInterno: 'EL-00101', descripcion: 'Interruptor Termomagnético Trifásico 63A', stockMinimo: 10, stockActual: 25 }, ubicacion: { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA' }, cantidadDisponible: 25, fechaIngreso: '2026-11-10T08:00:00', fechaVencimiento: '2027-06-15', activo: true },
    { id: 2, codigoLote: 'LOT-2026-002', articulo: { idArticulo: 2, codigoInterno: 'EL-00102', descripcion: 'Cable THW-90 12 AWG Indeco (Rollo 100m)', stockMinimo: 8, stockActual: 14 }, ubicacion: { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA' }, cantidadDisponible: 14, fechaIngreso: '2026-11-10T08:15:00', fechaVencimiento: '2028-01-20', activo: true },
    { id: 3, codigoLote: 'LOT-2026-003', articulo: { idArticulo: 3, codigoInterno: 'MC-00201', descripcion: 'Rodamiento Rígido de Bolas SKF 6205-2RSH', stockMinimo: 20, stockActual: 12 }, ubicacion: { id: 2, nombre: 'Almacén Principal - Estante B1', codigo: 'ALM-B1', tipo: 'ESTANTERIA' }, cantidadDisponible: 12, fechaIngreso: '2026-11-10T08:30:00', fechaVencimiento: '2026-12-05', activo: true },
    { id: 4, codigoLote: 'LOT-2026-004', articulo: { idArticulo: 4, codigoInterno: 'IN-00301', descripcion: 'Lubricante Dieléctrico Industrial WD-40', stockMinimo: 15, stockActual: 30 }, ubicacion: { id: 2, nombre: 'Almacén Principal - Estante B1', codigo: 'ALM-B1', tipo: 'ESTANTERIA' }, cantidadDisponible: 30, fechaIngreso: '2026-11-10T08:45:00', fechaVencimiento: '2026-11-30', activo: true },
    { id: 5, codigoLote: 'LOT-2026-005', articulo: { idArticulo: 5, codigoInterno: 'EP-00401', descripcion: 'Guantes Dieléctricos Clase 00 Certificados', stockMinimo: 12, stockActual: 8 }, ubicacion: { id: 1, nombre: 'Almacén Principal - Estante A1', codigo: 'ALM-A1', tipo: 'ESTANTERIA' }, cantidadDisponible: 8, fechaIngreso: '2026-11-10T09:00:00', fechaVencimiento: '2027-05-10', activo: true }
  ],
  kardex: [
    { idKardex: 1, fechaMovimiento: '2026-11-10T08:00:00', tipoMovimiento: 'INGRESO', referenciaDocumento: 'GR-001-00451 (Lote LOT-2026-001)', articulo: { idArticulo: 1, codigoInterno: 'EL-00101', descripcion: 'Interruptor Termomagnético Trifásico 63A', stockMinimo: 10, stockActual: 25 }, cantidad: 25, saldoResultante: 25, usuario: { username: 'admin', nombreCompleto: 'Administrador', email: '', activo: true } },
    { idKardex: 2, fechaMovimiento: '2026-11-10T08:15:00', tipoMovimiento: 'INGRESO', referenciaDocumento: 'GR-001-00452 (Lote LOT-2026-002)', articulo: { idArticulo: 2, codigoInterno: 'EL-00102', descripcion: 'Cable THW-90 12 AWG Indeco (Rollo 100m)', stockMinimo: 8, stockActual: 14 }, cantidad: 14, saldoResultante: 14, usuario: { username: 'admin', nombreCompleto: 'Administrador', email: '', activo: true } },
    { idKardex: 3, fechaMovimiento: '2026-11-10T08:30:00', tipoMovimiento: 'INGRESO', referenciaDocumento: 'GR-001-00453 (Lote LOT-2026-003)', articulo: { idArticulo: 3, codigoInterno: 'MC-00201', descripcion: 'Rodamiento Rígido de Bolas SKF 6205-2RSH', stockMinimo: 20, stockActual: 12 }, cantidad: 12, saldoResultante: 12, usuario: { username: 'admin', nombreCompleto: 'Administrador', email: '', activo: true } },
    { idKardex: 4, fechaMovimiento: '2026-11-10T08:45:00', tipoMovimiento: 'INGRESO', referenciaDocumento: 'GR-001-00454 (Lote LOT-2026-004)', articulo: { idArticulo: 4, codigoInterno: 'IN-00301', descripcion: 'Lubricante Dieléctrico Industrial WD-40', stockMinimo: 15, stockActual: 30 }, cantidad: 30, saldoResultante: 30, usuario: { username: 'admin', nombreCompleto: 'Administrador', email: '', activo: true } },
    { idKardex: 5, fechaMovimiento: '2026-11-10T09:00:00', tipoMovimiento: 'INGRESO', referenciaDocumento: 'GR-001-00455 (Lote LOT-2026-005)', articulo: { idArticulo: 5, codigoInterno: 'EP-00401', descripcion: 'Guantes Dieléctricos Clase 00 Certificados', stockMinimo: 12, stockActual: 8 }, cantidad: 8, saldoResultante: 8, usuario: { username: 'admin', nombreCompleto: 'Administrador', email: '', activo: true } }
  ],
  movimientos: []
};

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly DB_KEY = 'intranet_andina_local_db_v1';

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) {}

  private get base(): string {
    return this.auth.getApiBase();
  }

  private getDb(): LocalDbState {
    try {
      const raw = localStorage.getItem(this.DB_KEY);
      if (raw) return JSON.parse(raw);
    } catch {}
    localStorage.setItem(this.DB_KEY, JSON.stringify(DEFAULT_DB));
    return JSON.parse(JSON.stringify(DEFAULT_DB));
  }

  private saveDb(db: LocalDbState): void {
    localStorage.setItem(this.DB_KEY, JSON.stringify(db));
  }

  // ==================== ARTICULOS ====================
  listarArticulos(): Observable<Articulo[]> {
    return this.http
      .get<Articulo[]>(`${this.base}/articulos`)
      .pipe(catchError(() => of(this.getDb().articulos)));
  }

  listarArticulosBajoStock(): Observable<Articulo[]> {
    return this.http
      .get<Articulo[]>(`${this.base}/articulos/bajo-stock`)
      .pipe(catchError(() => of(this.getDb().articulos.filter((a) => a.stockActual <= a.stockMinimo))));
  }

  crearArticulo(articulo: Partial<Articulo>): Observable<Articulo> {
    return this.http.post<Articulo>(`${this.base}/articulos`, articulo).pipe(
      catchError(() => {
        const db = this.getDb();
        const cat = db.categorias.find((c) => c.idCategoria === articulo.categoria?.idCategoria) || db.categorias[0];
        const um = db.unidades.find((u) => u.idUnidad === articulo.unidadMedida?.idUnidad) || db.unidades[0];
        const ub = db.ubicaciones.find((u) => u.id === articulo.ubicacion?.id) || db.ubicaciones[0];
        const nuevo: Articulo = {
          idArticulo: (db.articulos.reduce((m, x) => Math.max(m, x.idArticulo || 0), 0) || 0) + 1,
          codigoInterno: articulo.codigoInterno || 'ART-NUEVO',
          descripcion: articulo.descripcion || '',
          categoria: cat,
          unidadMedida: um,
          ubicacion: ub,
          stockMinimo: Number(articulo.stockMinimo ?? 0),
          stockActual: Number(articulo.stockActual ?? 0)
        };
        db.articulos.push(nuevo);
        this.saveDb(db);
        return of(nuevo);
      })
    );
  }

  actualizarArticulo(id: number, articulo: Partial<Articulo>): Observable<Articulo> {
    return this.http.put<Articulo>(`${this.base}/articulos/${id}`, articulo).pipe(
      catchError(() => {
        const db = this.getDb();
        const idx = db.articulos.findIndex((a) => a.idArticulo === id);
        if (idx >= 0) {
          const cat = db.categorias.find((c) => c.idCategoria === articulo.categoria?.idCategoria) || db.articulos[idx].categoria;
          const um = db.unidades.find((u) => u.idUnidad === articulo.unidadMedida?.idUnidad) || db.articulos[idx].unidadMedida;
          const ub = db.ubicaciones.find((u) => u.id === articulo.ubicacion?.id) || db.articulos[idx].ubicacion;
          db.articulos[idx] = {
            ...db.articulos[idx],
            codigoInterno: articulo.codigoInterno ?? db.articulos[idx].codigoInterno,
            descripcion: articulo.descripcion ?? db.articulos[idx].descripcion,
            categoria: cat,
            unidadMedida: um,
            ubicacion: ub,
            stockMinimo: Number(articulo.stockMinimo ?? db.articulos[idx].stockMinimo),
            stockActual: Number(articulo.stockActual ?? db.articulos[idx].stockActual)
          };
          this.saveDb(db);
          return of(db.articulos[idx]);
        }
        return of(articulo as Articulo);
      })
    );
  }

  eliminarArticulo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/articulos/${id}`).pipe(
      catchError(() => {
        const db = this.getDb();
        db.articulos = db.articulos.filter((a) => a.idArticulo !== id);
        this.saveDb(db);
        return of(void 0);
      })
    );
  }

  // ==================== CATEGORIAS ====================
  listarCategorias(): Observable<Categoria[]> {
    return this.http
      .get<Categoria[]>(`${this.base}/categorias`)
      .pipe(catchError(() => of(this.getDb().categorias)));
  }

  crearCategoria(categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.base}/categorias`, categoria).pipe(
      catchError(() => {
        const db = this.getDb();
        const nueva: Categoria = {
          idCategoria: (db.categorias.reduce((m, x) => Math.max(m, x.idCategoria || 0), 0) || 0) + 1,
          nombre: categoria.nombre || ''
        };
        db.categorias.push(nueva);
        this.saveDb(db);
        return of(nueva);
      })
    );
  }

  actualizarCategoria(id: number, categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.base}/categorias/${id}`, categoria).pipe(
      catchError(() => {
        const db = this.getDb();
        const c = db.categorias.find((x) => x.idCategoria === id);
        if (c) c.nombre = categoria.nombre || c.nombre;
        this.saveDb(db);
        return of(c as Categoria);
      })
    );
  }

  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/categorias/${id}`).pipe(
      catchError(() => {
        const db = this.getDb();
        db.categorias = db.categorias.filter((x) => x.idCategoria !== id);
        this.saveDb(db);
        return of(void 0);
      })
    );
  }

  // ==================== UNIDADES DE MEDIDA ====================
  listarUnidades(): Observable<UnidadMedida[]> {
    return this.http
      .get<UnidadMedida[]>(`${this.base}/unidades-medida`)
      .pipe(catchError(() => of(this.getDb().unidades)));
  }

  crearUnidad(unidad: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.post<UnidadMedida>(`${this.base}/unidades-medida`, unidad).pipe(
      catchError(() => {
        const db = this.getDb();
        const nueva: UnidadMedida = {
          idUnidad: (db.unidades.reduce((m, x) => Math.max(m, x.idUnidad || 0), 0) || 0) + 1,
          nombre: unidad.nombre || '',
          abreviatura: unidad.abreviatura || ''
        };
        db.unidades.push(nueva);
        this.saveDb(db);
        return of(nueva);
      })
    );
  }

  actualizarUnidad(id: number, unidad: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.put<UnidadMedida>(`${this.base}/unidades-medida/${id}`, unidad).pipe(
      catchError(() => {
        const db = this.getDb();
        const u = db.unidades.find((x) => x.idUnidad === id);
        if (u) {
          u.nombre = unidad.nombre || u.nombre;
          u.abreviatura = unidad.abreviatura || u.abreviatura;
        }
        this.saveDb(db);
        return of(u as UnidadMedida);
      })
    );
  }

  eliminarUnidad(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/unidades-medida/${id}`).pipe(
      catchError(() => {
        const db = this.getDb();
        db.unidades = db.unidades.filter((x) => x.idUnidad !== id);
        this.saveDb(db);
        return of(void 0);
      })
    );
  }

  // ==================== UBICACIONES ====================
  listarUbicaciones(): Observable<Ubicacion[]> {
    return this.http
      .get<Ubicacion[]>(`${this.base}/ubicaciones`)
      .pipe(catchError(() => of(this.getDb().ubicaciones)));
  }

  crearUbicacion(ubicacion: Partial<Ubicacion>): Observable<Ubicacion> {
    return this.http.post<Ubicacion>(`${this.base}/ubicaciones`, ubicacion).pipe(
      catchError(() => {
        const db = this.getDb();
        const nueva: Ubicacion = {
          id: (db.ubicaciones.reduce((m, x) => Math.max(m, x.id || 0), 0) || 0) + 1,
          codigo: ubicacion.codigo || '',
          nombre: ubicacion.nombre || '',
          tipo: ubicacion.tipo || 'ESTANTERIA',
          activo: true
        };
        db.ubicaciones.push(nueva);
        this.saveDb(db);
        return of(nueva);
      })
    );
  }

  actualizarUbicacion(id: number, ubicacion: Partial<Ubicacion>): Observable<Ubicacion> {
    return this.http.put<Ubicacion>(`${this.base}/ubicaciones/${id}`, ubicacion).pipe(
      catchError(() => {
        const db = this.getDb();
        const u = db.ubicaciones.find((x) => x.id === id);
        if (u) {
          u.codigo = ubicacion.codigo || u.codigo;
          u.nombre = ubicacion.nombre || u.nombre;
          u.tipo = ubicacion.tipo || u.tipo;
        }
        this.saveDb(db);
        return of(u as Ubicacion);
      })
    );
  }

  eliminarUbicacion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/ubicaciones/${id}`).pipe(
      catchError(() => {
        const db = this.getDb();
        db.ubicaciones = db.ubicaciones.filter((x) => x.id !== id);
        this.saveDb(db);
        return of(void 0);
      })
    );
  }

  // ==================== PROVEEDORES ====================
  listarProveedores(): Observable<Proveedor[]> {
    return this.http
      .get<Proveedor[]>(`${this.base}/proveedores`)
      .pipe(catchError(() => of(this.getDb().proveedores)));
  }

  crearProveedor(proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.post<Proveedor>(`${this.base}/proveedores`, proveedor).pipe(
      catchError(() => {
        const db = this.getDb();
        const nuevo: Proveedor = {
          idProveedor: (db.proveedores.reduce((m, x) => Math.max(m, x.idProveedor || 0), 0) || 0) + 1,
          ruc: proveedor.ruc || '',
          razonSocial: proveedor.razonSocial || '',
          contacto: proveedor.contacto || ''
        };
        db.proveedores.push(nuevo);
        this.saveDb(db);
        return of(nuevo);
      })
    );
  }

  actualizarProveedor(id: number, proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.base}/proveedores/${id}`, proveedor).pipe(
      catchError(() => {
        const db = this.getDb();
        const p = db.proveedores.find((x) => x.idProveedor === id);
        if (p) {
          p.ruc = proveedor.ruc || p.ruc;
          p.razonSocial = proveedor.razonSocial || p.razonSocial;
          p.contacto = proveedor.contacto || p.contacto;
        }
        this.saveDb(db);
        return of(p as Proveedor);
      })
    );
  }

  eliminarProveedor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/proveedores/${id}`).pipe(
      catchError(() => {
        const db = this.getDb();
        db.proveedores = db.proveedores.filter((x) => x.idProveedor !== id);
        this.saveDb(db);
        return of(void 0);
      })
    );
  }

  // ==================== SEGURIDAD (USUARIOS, ROLES, PERMISOS) ====================
  listarUsuarios(): Observable<Usuario[]> {
    return this.http
      .get<Usuario[]>(`${this.base}/usuarios`)
      .pipe(catchError(() => of(this.getDb().usuarios)));
  }

  crearUsuario(payload: {
    username: string;
    password: string;
    nombreCompleto: string;
    email: string;
    rolesIds: number[];
  }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.base}/usuarios`, payload).pipe(
      catchError(() => {
        const db = this.getDb();
        const rol = db.roles.find((r) => r.id === payload.rolesIds[0]) || db.roles[0];
        const nuevo: Usuario = {
          id: (db.usuarios.reduce((m, x) => Math.max(m, x.id || 0), 0) || 0) + 1,
          username: payload.username,
          nombreCompleto: payload.nombreCompleto,
          email: payload.email,
          activo: true,
          roles: [rol]
        };
        db.usuarios.push(nuevo);
        this.saveDb(db);
        return of(nuevo);
      })
    );
  }

  cambiarEstadoUsuario(id: number, activo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.base}/usuarios/${id}/estado?activo=${activo}`, {}).pipe(
      catchError(() => {
        const db = this.getDb();
        const u = db.usuarios.find((x) => x.id === id);
        if (u) u.activo = activo;
        this.saveDb(db);
        return of(u as Usuario);
      })
    );
  }

  listarRoles(): Observable<Rol[]> {
    return this.http
      .get<Rol[]>(`${this.base}/roles`)
      .pipe(catchError(() => of(this.getDb().roles)));
  }

  listarPermisos(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(`${this.base}/permisos`).pipe(catchError(() => of([])));
  }

  // ==================== ALMACEN Y KARDEX ====================
  listarLotesActivos(): Observable<Lote[]> {
    return this.http
      .get<Lote[]>(`${this.base}/almacen/lotes`)
      .pipe(catchError(() => of(this.getDb().lotes)));
  }

  listarLotesPorVencer(): Observable<Lote[]> {
    return this.http
      .get<Lote[]>(`${this.base}/almacen/reportes/vencimientos`)
      .pipe(catchError(() => of(this.getDb().lotes)));
  }

  listarMovimientos(): Observable<MovimientoResponse[]> {
    return this.http
      .get<MovimientoResponse[]>(`${this.base}/almacen/movimientos`)
      .pipe(catchError(() => of(this.getDb().movimientos)));
  }

  registrarIngreso(payload: {
    articuloId: number;
    codigoLote: string;
    cantidad: number;
    fechaVencimiento?: string | null;
    ubicacionDestinoId: number;
    proveedorId?: number | null;
    documentoReferencia?: string | null;
  }): Observable<MovimientoResponse> {
    return this.http.post<MovimientoResponse>(`${this.base}/almacen/ingresos`, payload).pipe(
      catchError(() => {
        const db = this.getDb();
        const art = db.articulos.find((a) => a.idArticulo === payload.articuloId) || db.articulos[0];
        const ub = db.ubicaciones.find((u) => u.id === payload.ubicacionDestinoId) || db.ubicaciones[0];
        art.stockActual = Number(art.stockActual) + Number(payload.cantidad);
        const nuevoLote: Lote = {
          id: db.lotes.length + 1,
          codigoLote: payload.codigoLote,
          articulo: art,
          ubicacion: ub,
          cantidadDisponible: Number(payload.cantidad),
          fechaIngreso: new Date().toISOString(),
          fechaVencimiento: payload.fechaVencimiento || null,
          activo: true
        };
        db.lotes.push(nuevoLote);
        db.kardex.unshift({
          idKardex: db.kardex.length + 1,
          fechaMovimiento: new Date().toISOString(),
          tipoMovimiento: 'INGRESO',
          referenciaDocumento: `${payload.documentoReferencia || 'GR-001'} (${payload.codigoLote})`,
          articulo: art,
          cantidad: Number(payload.cantidad),
          saldoResultante: art.stockActual,
          usuario: { username: this.auth.session()?.username || 'admin', nombreCompleto: '', email: '', activo: true }
        });
        this.saveDb(db);
        return of({
          id: Date.now(),
          tipoMovimiento: 'INGRESO',
          cantidad: Number(payload.cantidad),
          fechaMovimiento: new Date().toISOString(),
          loteId: nuevoLote.id,
          codigoLote: nuevoLote.codigoLote,
          articuloId: art.idArticulo!,
          articuloCodigo: art.codigoInterno,
          articuloDescripcion: art.descripcion,
          ubicacionDestinoId: ub.id,
          ubicacionDestinoNombre: ub.nombre
        });
      })
    );
  }

  registrarSalidaFIFO(payload: {
    articuloId: number;
    cantidadRequerida: number;
    ordenProduccion?: string | null;
  }): Observable<MovimientoResponse[]> {
    return this.http.post<MovimientoResponse[]>(`${this.base}/almacen/salidas`, payload).pipe(
      catchError(() => {
        const db = this.getDb();
        const art = db.articulos.find((a) => a.idArticulo === payload.articuloId) || db.articulos[0];
        art.stockActual = Math.max(0, Number(art.stockActual) - Number(payload.cantidadRequerida));
        db.kardex.unshift({
          idKardex: db.kardex.length + 1,
          fechaMovimiento: new Date().toISOString(),
          tipoMovimiento: 'SALIDA',
          referenciaDocumento: payload.ordenProduccion || 'OP-FIFO',
          articulo: art,
          cantidad: Number(payload.cantidadRequerida),
          saldoResultante: art.stockActual,
          usuario: { username: this.auth.session()?.username || 'admin', nombreCompleto: '', email: '', activo: true }
        });
        this.saveDb(db);
        return of([
          {
            id: Date.now(),
            tipoMovimiento: 'SALIDA',
            cantidad: Number(payload.cantidadRequerida),
            fechaMovimiento: new Date().toISOString(),
            loteId: 1,
            codigoLote: 'LOT-FIFO',
            articuloId: art.idArticulo!,
            articuloCodigo: art.codigoInterno,
            articuloDescripcion: art.descripcion
          }
        ]);
      })
    );
  }

  registrarTransferencia(payload: {
    loteId: number;
    ubicacionOrigenId: number;
    ubicacionDestinoId: number;
    cantidad: number;
    motivo?: string | null;
  }): Observable<MovimientoResponse> {
    return this.http.post<MovimientoResponse>(`${this.base}/almacen/transferencias`, payload).pipe(
      catchError(() => {
        const db = this.getDb();
        const lote = db.lotes.find((l) => l.id === payload.loteId) || db.lotes[0];
        const ubDest = db.ubicaciones.find((u) => u.id === payload.ubicacionDestinoId) || db.ubicaciones[0];
        lote.ubicacion = ubDest;
        db.kardex.unshift({
          idKardex: db.kardex.length + 1,
          fechaMovimiento: new Date().toISOString(),
          tipoMovimiento: 'TRANSFERENCIA',
          referenciaDocumento: payload.motivo || 'TRF-INTERNA',
          articulo: lote.articulo,
          cantidad: Number(payload.cantidad),
          saldoResultante: lote.articulo?.stockActual ?? 0,
          usuario: { username: this.auth.session()?.username || 'admin', nombreCompleto: '', email: '', activo: true }
        });
        this.saveDb(db);
        return of({
          id: Date.now(),
          tipoMovimiento: 'TRANSFERENCIA',
          cantidad: Number(payload.cantidad),
          fechaMovimiento: new Date().toISOString(),
          loteId: lote.id,
          codigoLote: lote.codigoLote,
          articuloId: lote.articulo?.idArticulo || 1,
          articuloCodigo: lote.articulo?.codigoInterno || '',
          articuloDescripcion: lote.articulo?.descripcion || '',
          ubicacionDestinoId: ubDest.id,
          ubicacionDestinoNombre: ubDest.nombre
        });
      })
    );
  }

  listarKardex(): Observable<Kardex[]> {
    return this.http
      .get<Kardex[]>(`${this.base}/kardex`)
      .pipe(catchError(() => of(this.getDb().kardex)));
  }
}
