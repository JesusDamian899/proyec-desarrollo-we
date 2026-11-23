import { Injectable, computed, signal } from '@angular/core';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import {
  Articulo,
  Categoria,
  Kardex,
  Lote,
  MovimientoResponse,
  Proveedor,
  Rol,
  Ubicacion,
  UnidadMedida,
  Usuario
} from '../models/inventario.models';

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  readonly articulos = signal<Articulo[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly unidades = signal<UnidadMedida[]>([]);
  readonly ubicaciones = signal<Ubicacion[]>([]);
  readonly proveedores = signal<Proveedor[]>([]);
  readonly usuarios = signal<Usuario[]>([]);
  readonly roles = signal<Rol[]>([]);
  readonly lotes = signal<Lote[]>([]);
  readonly kardex = signal<Kardex[]>([]);
  readonly movimientos = signal<MovimientoResponse[]>([]);

  readonly mensajeExito = signal<string | null>(null);
  readonly mensajeError = signal<string | null>(null);
  readonly cargando = signal<boolean>(false);

  readonly totalBajoStock = computed(
    () => this.articulos().filter((a) => a.stockActual <= a.stockMinimo).length
  );

  constructor(
    private api: ApiService,
    private auth: AuthService
  ) {}

  cargarDatos(): void {
    if (!this.auth.isLoggedIn()) return;
    this.cargando.set(true);

    this.api.listarCategorias().subscribe({ next: (d) => this.categorias.set(d), error: () => {} });
    this.api.listarUnidades().subscribe({ next: (d) => this.unidades.set(d), error: () => {} });
    this.api.listarUbicaciones().subscribe({ next: (d) => this.ubicaciones.set(d), error: () => {} });
    this.api.listarProveedores().subscribe({ next: (d) => this.proveedores.set(d), error: () => {} });

    this.api.listarArticulos().subscribe({
      next: (d) => {
        this.articulos.set(d);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });

    if (this.auth.hasPermiso('USUARIO_GESTIONAR')) {
      this.api.listarUsuarios().subscribe({ next: (d) => this.usuarios.set(d), error: () => {} });
    } else {
      this.usuarios.set([]);
    }

    if (this.auth.hasPermiso('ROL_GESTIONAR')) {
      this.api.listarRoles().subscribe({ next: (d) => this.roles.set(d), error: () => {} });
    } else {
      this.roles.set([]);
    }

    this.api.listarLotesActivos().subscribe({ next: (d) => this.lotes.set(d), error: () => {} });

    if (this.auth.hasPermiso('KARDEX_VER')) {
      this.api.listarKardex().subscribe({ next: (d) => this.kardex.set(d), error: () => {} });
    } else {
      this.kardex.set([]);
    }

    this.api.listarMovimientos().subscribe({ next: (d) => this.movimientos.set(d), error: () => {} });
  }

  notificarExito(msg: string): void {
    this.mensajeExito.set(msg);
    this.mensajeError.set(null);
  }

  notificarError(msg: string): void {
    this.mensajeError.set(msg);
    this.mensajeExito.set(null);
  }

  limpiarMensajes(): void {
    this.mensajeExito.set(null);
    this.mensajeError.set(null);
  }

  extraerError(err: any, fallback: string): string {
    if (!err) return fallback;
    if (typeof err.error === 'string' && err.error.trim().length > 0) return err.error;
    if (err.error?.mensaje) return err.error.mensaje;
    if (err.message) return err.message;
    return fallback;
  }

  confirmarEliminacion(entidad: string): boolean {
    return typeof window === 'undefined' ? true : window.confirm(`¿Deseas eliminar ${entidad}? Esta acción no se puede deshacer.`);
  }
}
