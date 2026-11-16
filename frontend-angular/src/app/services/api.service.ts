import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {}

  // ==================== ARTICULOS ====================
  listarArticulos(): Observable<Articulo[]> {
    return this.http.get<Articulo[]>('/api/articulos');
  }

  listarArticulosBajoStock(): Observable<Articulo[]> {
    return this.http.get<Articulo[]>('/api/articulos/bajo-stock');
  }

  crearArticulo(articulo: Partial<Articulo>): Observable<Articulo> {
    return this.http.post<Articulo>('/api/articulos', articulo);
  }

  actualizarArticulo(id: number, articulo: Partial<Articulo>): Observable<Articulo> {
    return this.http.put<Articulo>(`/api/articulos/${id}`, articulo);
  }

  eliminarArticulo(id: number): Observable<void> {
    return this.http.delete<void>(`/api/articulos/${id}`);
  }

  // ==================== CATEGORIAS ====================
  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>('/api/categorias');
  }

  crearCategoria(categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.post<Categoria>('/api/categorias', categoria);
  }

  actualizarCategoria(id: number, categoria: Partial<Categoria>): Observable<Categoria> {
    return this.http.put<Categoria>(`/api/categorias/${id}`, categoria);
  }

  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`/api/categorias/${id}`);
  }

  // ==================== UNIDADES DE MEDIDA ====================
  listarUnidades(): Observable<UnidadMedida[]> {
    return this.http.get<UnidadMedida[]>('/api/unidades-medida');
  }

  crearUnidad(unidad: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.post<UnidadMedida>('/api/unidades-medida', unidad);
  }

  actualizarUnidad(id: number, unidad: Partial<UnidadMedida>): Observable<UnidadMedida> {
    return this.http.put<UnidadMedida>(`/api/unidades-medida/${id}`, unidad);
  }

  eliminarUnidad(id: number): Observable<void> {
    return this.http.delete<void>(`/api/unidades-medida/${id}`);
  }

  // ==================== UBICACIONES ====================
  listarUbicaciones(): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>('/api/ubicaciones');
  }

  crearUbicacion(ubicacion: Partial<Ubicacion>): Observable<Ubicacion> {
    return this.http.post<Ubicacion>('/api/ubicaciones', ubicacion);
  }

  actualizarUbicacion(id: number, ubicacion: Partial<Ubicacion>): Observable<Ubicacion> {
    return this.http.put<Ubicacion>(`/api/ubicaciones/${id}`, ubicacion);
  }

  eliminarUbicacion(id: number): Observable<void> {
    return this.http.delete<void>(`/api/ubicaciones/${id}`);
  }

  // ==================== PROVEEDORES ====================
  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>('/api/proveedores');
  }

  crearProveedor(proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.post<Proveedor>('/api/proveedores', proveedor);
  }

  actualizarProveedor(id: number, proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`/api/proveedores/${id}`, proveedor);
  }

  eliminarProveedor(id: number): Observable<void> {
    return this.http.delete<void>(`/api/proveedores/${id}`);
  }

  // ==================== SEGURIDAD (USUARIOS, ROLES, PERMISOS) ====================
  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>('/api/usuarios');
  }

  crearUsuario(payload: {
    username: string;
    password: string;
    nombreCompleto: string;
    email: string;
    rolesIds: number[];
  }): Observable<Usuario> {
    return this.http.post<Usuario>('/api/usuarios', payload);
  }

  cambiarEstadoUsuario(id: number, activo: boolean): Observable<Usuario> {
    return this.http.patch<Usuario>(`/api/usuarios/${id}/estado?activo=${activo}`, {});
  }

  listarRoles(): Observable<Rol[]> {
    return this.http.get<Rol[]>('/api/roles');
  }

  listarPermisos(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>('/api/permisos');
  }

  // ==================== ALMACEN Y KARDEX ====================
  listarLotesActivos(): Observable<Lote[]> {
    return this.http.get<Lote[]>('/api/almacen/lotes');
  }

  listarLotesPorVencer(): Observable<Lote[]> {
    return this.http.get<Lote[]>('/api/almacen/reportes/vencimientos');
  }

  listarMovimientos(): Observable<MovimientoResponse[]> {
    return this.http.get<MovimientoResponse[]>('/api/almacen/movimientos');
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
    return this.http.post<MovimientoResponse>('/api/almacen/ingresos', payload);
  }

  registrarSalidaFIFO(payload: {
    articuloId: number;
    cantidadRequerida: number;
    ordenProduccion?: string | null;
  }): Observable<MovimientoResponse[]> {
    return this.http.post<MovimientoResponse[]>('/api/almacen/salidas', payload);
  }

  registrarTransferencia(payload: {
    loteId: number;
    ubicacionOrigenId: number;
    ubicacionDestinoId: number;
    cantidad: number;
    motivo?: string | null;
  }): Observable<MovimientoResponse> {
    return this.http.post<MovimientoResponse>('/api/almacen/transferencias', payload);
  }

  listarKardex(): Observable<Kardex[]> {
    return this.http.get<Kardex[]>('/api/kardex');
  }
}
