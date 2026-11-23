import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { StoreService } from '../services/store.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  styleUrl: './shell.component.css',
  template: `
    <div class="intranet-shell">
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="brand-badge">EA</div>
          <div>
            <div class="sidebar-title">Electromecánica Andina</div>
            <div class="sidebar-sub">Intranet Corporativa</div>
          </div>
        </div>

        <div class="user-box">
          <div class="user-name">{{ auth.session()?.nombreCompleto }}</div>
          <div class="user-meta mono">&#64;{{ auth.session()?.username }}</div>
          <div class="user-roles">
            @for (r of auth.session()?.roles ?? []; track r) {
              <span class="role-chip">{{ r }}</span>
            }
          </div>
        </div>

        <nav class="sidebar-nav">
          <div class="nav-group-label">Panel Principal</div>
          <a routerLink="/resumen" routerLinkActive="active" class="nav-item" (click)="store.limpiarMensajes()">
            <span>Resumen e Indicadores</span>
          </a>

          <div class="nav-group-label">Catálogos Maestros</div>
          @if (auth.hasPermiso('ARTICULO_VER')) {
            <a routerLink="/articulos" routerLinkActive="active" class="nav-item" (click)="store.limpiarMensajes()">
              <span>Catálogo de Artículos</span>
              <span class="nav-count">{{ store.articulos().length }}</span>
            </a>
          }

          @if (auth.hasPermiso('MAESTRO_GESTIONAR')) {
            <a routerLink="/catalogos" routerLinkActive="active" class="nav-item" (click)="store.limpiarMensajes()">
              <span>Categorías, Unidades y Ubic.</span>
            </a>
          }

          @if (auth.hasPermiso('PROVEEDOR_GESTIONAR')) {
            <a routerLink="/proveedores" routerLinkActive="active" class="nav-item" (click)="store.limpiarMensajes()">
              <span>Proveedores</span>
              <span class="nav-count">{{ store.proveedores().length }}</span>
            </a>
          }

          @if (auth.hasAnyPermiso('USUARIO_GESTIONAR', 'ROL_GESTIONAR')) {
            <div class="nav-group-label">Administración y Acceso</div>
            <a routerLink="/seguridad" routerLinkActive="active" class="nav-item" (click)="store.limpiarMensajes()">
              <span>Usuarios y Roles</span>
              <span class="nav-count">{{ store.usuarios().length }}</span>
            </a>
          }

          @if (auth.hasAnyPermiso('INGRESO_VER', 'INGRESO_CREAR', 'SALIDA_VER', 'SALIDA_ATENDER', 'TRANSFER_VER', 'KARDEX_VER')) {
            <div class="nav-group-label">Operaciones</div>
            <a routerLink="/almacen" routerLinkActive="active" class="nav-item" (click)="store.limpiarMensajes()">
              <span>Almacén, FIFO y Kardex</span>
            </a>
          }
        </nav>

        <div class="sidebar-footer">
          <button type="button" class="btn btn-secondary btn-full" (click)="cerrarSesion()">
            Cerrar Sesión
          </button>
        </div>
      </aside>

      <main class="main-area">
        <header class="topbar">
          <div>
            <h1 class="page-heading">{{ tituloActual() }}</h1>
            <p class="page-subheading">
              Sistema de Gestión de Inventario y Almacén — Electromecánica Andina S.A.C.
            </p>
          </div>
          <div class="topbar-actions">
            <button type="button" class="btn btn-secondary" (click)="store.cargarDatos()">
              Actualizar Datos
            </button>
          </div>
        </header>

        <div class="content-body">
          @if (store.mensajeExito()) {
            <div class="alert alert-success">
              <span>{{ store.mensajeExito() }}</span>
              <button type="button" class="btn btn-sm btn-secondary" (click)="store.limpiarMensajes()">OK</button>
            </div>
          }

          @if (store.mensajeError()) {
            <div class="alert alert-error">
              <span>{{ store.mensajeError() }}</span>
              <button type="button" class="btn btn-sm btn-secondary" (click)="store.limpiarMensajes()">Cerrar</button>
            </div>
          }

          <router-outlet />
        </div>
      </main>
    </div>
  `
})
export class ShellComponent implements OnInit {
  readonly tituloActual = signal<string>('Panel General de Inventario');

  constructor(
    public auth: AuthService,
    public store: StoreService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.store.cargarDatos();
    this.actualizarTitulo();
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => this.actualizarTitulo());
  }

  private actualizarTitulo(): void {
    let child = this.route.firstChild;
    while (child?.firstChild) {
      child = child.firstChild;
    }
    this.tituloActual.set(child?.snapshot.data['titulo'] || 'Panel General de Inventario');
  }

  cerrarSesion(): void {
    this.auth.logout();
    this.store.limpiarMensajes();
    this.router.navigate(['/login']);
  }
}
