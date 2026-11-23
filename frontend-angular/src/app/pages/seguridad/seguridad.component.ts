import { Component, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { Usuario } from '../../models/inventario.models';

@Component({
  selector: 'app-seguridad',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-stack">
      <div class="card">
        <div class="section-head">
          <h2>Registrar Nuevo Usuario Corporativo</h2>
        </div>
        <form (ngSubmit)="guardarUsuario()" class="form-grid">
          <div class="form-field">
            <label for="usr-username">Username</label>
            <input
              id="usr-username"
              name="username"
              type="text"
              class="form-control mono"
              [(ngModel)]="form.username"
              placeholder="ej. supervisor.planta"
              required
            />
          </div>
          <div class="form-field">
            <label for="usr-pass">Contraseña</label>
            <input
              id="usr-pass"
              name="password"
              type="password"
              minlength="8"
              class="form-control"
              [(ngModel)]="form.password"
              placeholder="Mínimo 8 caracteres"
              required
            />
          </div>
          <div class="form-field">
            <label for="usr-nombre">Nombre Completo</label>
            <input
              id="usr-nombre"
              name="nombreCompleto"
              type="text"
              class="form-control"
              [(ngModel)]="form.nombreCompleto"
              placeholder="Nombre y Apellidos"
              required
            />
          </div>
          <div class="form-field">
            <label for="usr-email">Correo Electrónico</label>
            <input
              id="usr-email"
              name="email"
              type="email"
              class="form-control"
              [(ngModel)]="form.email"
              placeholder="usuario&#64;electromecanicaandina.com"
              required
            />
          </div>
          <div class="form-field">
            <label for="usr-rol">Rol Asignado</label>
            <select id="usr-rol" name="rolId" class="form-control" [(ngModel)]="form.rolId">
              @for (r of store.roles(); track r.id) {
                <option [ngValue]="r.id">{{ r.nombre }}</option>
              }
            </select>
          </div>
          <div class="form-field form-actions-end">
            <button type="submit" class="btn btn-primary">Crear Usuario</button>
          </div>
        </form>
      </div>

      <div class="two-col-layout">
        <div class="card">
          <div class="section-head">
            <h2>Usuarios Registrados ({{ store.usuarios().length }})</h2>
          </div>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Usuario</th>
                  <th>Nombre Completo</th>
                  <th>Email</th>
                  <th>Roles</th>
                  <th>Estado</th>
                  <th>Acción</th>
                </tr>
              </thead>
              <tbody>
                @for (u of store.usuarios(); track u.id) {
                  <tr>
                    <td class="mono">#{{ u.id }}</td>
                    <td class="mono"><strong>{{ u.username }}</strong></td>
                    <td>{{ u.nombreCompleto }}</td>
                    <td>{{ u.email }}</td>
                    <td>
                      @for (r of u.roles ?? []; track $index) {
                        <span class="badge badge-info">{{ $any(r).nombre || r }}</span>
                      }
                    </td>
                    <td>
                      @if (u.activo) {
                        <span class="badge badge-success">ACTIVO</span>
                      } @else {
                        <span class="badge badge-danger">INACTIVO</span>
                      }
                    </td>
                    <td>
                      <button type="button" class="btn btn-sm btn-secondary" (click)="alternarEstado(u)">
                        {{ u.activo ? 'Desactivar' : 'Activar' }}
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>

        <div class="card">
          <div class="section-head">
            <h2>Roles y Permisos del Sistema ({{ store.roles().length }})</h2>
          </div>
          <div class="roles-stack">
            @for (r of store.roles(); track r.id) {
              <div class="role-card">
                <div class="role-card-head">
                  <strong>{{ r.nombre }}</strong>
                  <span class="badge badge-info">{{ r.permisos?.length || 0 }} permisos</span>
                </div>
                <p class="role-desc">{{ r.descripcion }}</p>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `
})
export class SeguridadComponent {
  form = {
    username: '',
    password: '',
    nombreCompleto: '',
    email: '',
    rolId: 0
  };

  constructor(
    public store: StoreService,
    private api: ApiService
  ) {
    effect(() => {
      const roles = this.store.roles();
      if (!this.form.rolId && roles.length > 0) {
        this.form.rolId = (roles[0].id ?? roles[0].idRol)!;
      }
    });
  }

  guardarUsuario(): void {
    this.store.limpiarMensajes();
    const payload = {
      username: this.form.username.trim(),
      password: this.form.password,
      nombreCompleto: this.form.nombreCompleto.trim(),
      email: this.form.email.trim(),
      rolesIds: [Number(this.form.rolId)]
    };

    this.api.crearUsuario(payload).subscribe({
      next: (u) => {
        this.store.notificarExito(`Usuario "${u.username}" creado exitosamente.`);
        this.form = {
          username: '',
          password: '',
          nombreCompleto: '',
          email: '',
          rolId: this.store.roles()[0]?.id ?? 0
        };
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al registrar usuario'))
    });
  }

  alternarEstado(u: Usuario): void {
    this.store.limpiarMensajes();
    this.api.cambiarEstadoUsuario((u.id ?? u.idUsuario)!, !u.activo).subscribe({
      next: (actualizado) => {
        this.store.notificarExito(
          `Estado de ${actualizado.username} actualizado a ${actualizado.activo ? 'ACTIVO' : 'INACTIVO'}.`
        );
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'No se pudo cambiar el estado'))
    });
  }
}
