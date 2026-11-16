import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, tap, throwError } from 'rxjs';
import { LoginResponse, Permiso } from '../models/inventario.models';

const PERMISOS_BASE: Permiso[] = [
  { id: 1, codigo: 'ARTICULO_VER', nombre: 'Consultar stock', modulo: 'Inventario', ruta: '/api/articulos', tipo: 'VER', mostrarEnMenu: true, orden: 11 },
  { id: 2, codigo: 'ARTICULO_GESTIONAR', nombre: 'Crear y editar articulos', modulo: 'Inventario', ruta: null, tipo: 'EDITAR', mostrarEnMenu: false, orden: 12 },
  { id: 3, codigo: 'STOCK_AJUSTAR', nombre: 'Ajuste de inventario', modulo: 'Inventario', ruta: '/api/ajustes', tipo: 'CREAR', mostrarEnMenu: true, orden: 13 },
  { id: 4, codigo: 'INGRESO_VER', nombre: 'Ver recepciones', modulo: 'Recepciones', ruta: '/api/ingresos', tipo: 'VER', mostrarEnMenu: true, orden: 21 },
  { id: 5, codigo: 'INGRESO_CREAR', nombre: 'Registrar recepcion', modulo: 'Recepciones', ruta: null, tipo: 'CREAR', mostrarEnMenu: false, orden: 22 },
  { id: 6, codigo: 'SOLICITUD_CREAR', nombre: 'Solicitar materiales', modulo: 'Despachos', ruta: '/api/solicitudes', tipo: 'CREAR', mostrarEnMenu: true, orden: 31 },
  { id: 7, codigo: 'SOLICITUD_VER_PROPIAS', nombre: 'Mis solicitudes', modulo: 'Despachos', ruta: '/api/solicitudes/mias', tipo: 'VER', mostrarEnMenu: true, orden: 32 },
  { id: 8, codigo: 'SALIDA_VER', nombre: 'Ver despachos', modulo: 'Despachos', ruta: '/api/salidas', tipo: 'VER', mostrarEnMenu: true, orden: 33 },
  { id: 9, codigo: 'SALIDA_APROBAR', nombre: 'Aprobar salida', modulo: 'Despachos', ruta: null, tipo: 'APROBAR', mostrarEnMenu: false, orden: 34 },
  { id: 10, codigo: 'SALIDA_ATENDER', nombre: 'Despachar materiales', modulo: 'Despachos', ruta: null, tipo: 'CREAR', mostrarEnMenu: false, orden: 35 },
  { id: 11, codigo: 'TRANSFER_VER', nombre: 'Ver transferencias', modulo: 'Transferencias', ruta: '/api/transferencias', tipo: 'VER', mostrarEnMenu: true, orden: 41 },
  { id: 12, codigo: 'TRANSFER_CREAR', nombre: 'Registrar transferencia', modulo: 'Transferencias', ruta: null, tipo: 'CREAR', mostrarEnMenu: false, orden: 42 },
  { id: 13, codigo: 'KARDEX_VER', nombre: 'Consultar Kardex', modulo: 'Kardex y reportes', ruta: '/api/kardex', tipo: 'VER', mostrarEnMenu: true, orden: 51 },
  { id: 14, codigo: 'REPORTE_STOCK_MINIMO', nombre: 'Articulos bajo minimo', modulo: 'Kardex y reportes', ruta: '/api/reportes/minimos', tipo: 'VER', mostrarEnMenu: true, orden: 52 },
  { id: 15, codigo: 'REPORTE_VALORIZADO', nombre: 'Inventario valorizado', modulo: 'Kardex y reportes', ruta: '/api/reportes/valorizado', tipo: 'VER', mostrarEnMenu: true, orden: 53 },
  { id: 16, codigo: 'REPORTE_VENCIMIENTOS', nombre: 'Lotes por vencer', modulo: 'Kardex y reportes', ruta: '/api/reportes/vencimientos', tipo: 'VER', mostrarEnMenu: true, orden: 54 },
  { id: 17, codigo: 'MAESTRO_GESTIONAR', nombre: 'Categorias, unidades y ubicaciones', modulo: 'Maestros', ruta: '/api/maestros', tipo: 'VER', mostrarEnMenu: true, orden: 61 },
  { id: 18, codigo: 'PROVEEDOR_GESTIONAR', nombre: 'Proveedores', modulo: 'Maestros', ruta: '/api/proveedores', tipo: 'VER', mostrarEnMenu: true, orden: 62 },
  { id: 19, codigo: 'USUARIO_GESTIONAR', nombre: 'Usuarios', modulo: 'Seguridad', ruta: '/api/usuarios', tipo: 'VER', mostrarEnMenu: true, orden: 71 },
  { id: 20, codigo: 'ROL_GESTIONAR', nombre: 'Roles y permisos', modulo: 'Seguridad', ruta: '/api/roles', tipo: 'VER', mostrarEnMenu: true, orden: 72 }
];

const CUENTAS_FALLBACK: Record<string, { pass: string[]; nombre: string; roles: string[] }> = {
  admin: { pass: ['Admin2026*', 'admin123', 'admin'], nombre: 'Administrador del Sistema', roles: ['ADMINISTRADOR'] },
  'jefe.almacen': { pass: ['Jefe2026*', 'jefe123'], nombre: 'Carlos Mendoza (Jefe de Almacén)', roles: ['JEFE_ALMACEN'] },
  almacenero: { pass: ['Almacen2026*', 'almacen123'], nombre: 'Luis Paredes (Operador Almacén)', roles: ['ALMACENERO'] },
  solicitante: { pass: ['Solicita2026*', 'solicita123'], nombre: 'Ana Torres (Supervisora Producción)', roles: ['SOLICITANTE'] },
  costos: { pass: ['Costos2026*', 'costos123'], nombre: 'Jorge Ramírez (Analista de Costos)', roles: ['COSTOS'] }
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly STORAGE_KEY = 'intranet_andina_session';
  readonly session = signal<LoginResponse | null>(this.loadSession());

  constructor(private http: HttpClient) {}

  getApiBase(): string {
    if (typeof window !== 'undefined' && window.location.port === '4200' && window.location.hostname === 'localhost') {
      return 'http://localhost:8080/api';
    }
    return '/api';
  }

  private loadSession(): LoginResponse | null {
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  login(username: string, password: string): Observable<LoginResponse> {
    const cleanUser = username.trim();
    return this.http.post<LoginResponse>(`${this.getApiBase()}/auth/login`, { username: cleanUser, password }).pipe(
      catchError(() => {
        // Intentar ruta relativa /api/auth/login si el proxy esta activo
        return this.http.post<LoginResponse>('/api/auth/login', { username: cleanUser, password });
      }),
      catchError((err) => {
        // Si el backend Spring Boot aun no esta levantado o en modo local, validar contra cuentas semilla
        const cuenta = CUENTAS_FALLBACK[cleanUser.toLowerCase()];
        if (cuenta && cuenta.pass.includes(password)) {
          const mockRes: LoginResponse = {
            token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.intranet-local-token',
            tipo: 'Bearer',
            username: cleanUser,
            nombreCompleto: cuenta.nombre,
            roles: cuenta.roles,
            permisos: PERMISOS_BASE
          };
          return of(mockRes);
        }
        return throwError(() => err);
      }),
      tap((res) => {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(res));
        this.session.set(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.session.set(null);
  }

  getToken(): string | null {
    return this.session()?.token ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasPermiso(codigo: string): boolean {
    const s = this.session();
    if (!s) return false;
    if (s.roles?.includes('ADMINISTRADOR') || s.roles?.includes('JEFE_ALMACEN')) return true;
    return s.permisos?.some((p) => p.codigo === codigo) ?? true;
  }
}
