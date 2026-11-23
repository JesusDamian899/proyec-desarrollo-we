import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { StoreService } from '../../services/store.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styles: [`
    .login-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1.5rem;
      background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
    }
    .login-card {
      width: 100%;
      max-width: 480px;
      padding: 2rem;
    }
    .login-brand {
      display: flex;
      align-items: center;
      gap: 0.9rem;
      margin-bottom: 1.5rem;
    }
    .login-brand h1 {
      font-size: 1.2rem;
      font-weight: 700;
      color: var(--text-main);
    }
    .subtitle {
      font-size: 0.825rem;
      color: var(--text-secondary);
    }
    .login-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .demo-credentials {
      margin-top: 1.5rem;
      padding-top: 1.15rem;
      border-top: 1px solid var(--border-color);
    }
    .demo-title {
      display: block;
      font-size: 0.775rem;
      font-weight: 600;
      color: var(--text-secondary);
      margin-bottom: 0.6rem;
    }
    .demo-buttons {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.45rem;
    }
  `],
  template: `
    <div class="login-wrapper">
      <div class="login-card card">
        <div class="login-brand">
          <div class="brand-badge">EA</div>
          <div>
            <h1>Electromecánica Andina S.A.C.</h1>
            <p class="subtitle">Intranet de Gestión de Almacén, Catálogos Maestros y Kardex</p>
          </div>
        </div>

        @if (errorMsg) {
          <div class="alert alert-error">
            <span>{{ errorMsg }}</span>
            <button type="button" class="btn btn-sm btn-secondary" (click)="errorMsg = null">Cerrar</button>
          </div>
        }

        <form (ngSubmit)="iniciarSesion()" class="login-form">
          <div class="form-field">
            <label for="login-username">Usuario corporativo</label>
            <input
              id="login-username"
              name="username"
              type="text"
              class="form-control"
              [(ngModel)]="username"
              placeholder="Ej. admin"
              required
            />
          </div>

          <div class="form-field">
            <label for="login-password">Contraseña</label>
            <input
              id="login-password"
              name="password"
              type="password"
              class="form-control"
              [(ngModel)]="password"
              placeholder="••••••••"
              required
            />
          </div>

          <button
            id="btn-login-submit"
            type="submit"
            class="btn btn-primary btn-full"
            [disabled]="cargando"
          >
            {{ cargando ? 'Verificando credenciales...' : 'Ingresar a la Intranet' }}
          </button>
        </form>

        <div class="demo-credentials">
          <span class="demo-title">Acceso rápido por perfil corporativo:</span>
          <div class="demo-buttons">
            <button type="button" class="btn btn-sm btn-secondary" (click)="seleccionar('admin', 'Admin2026*')">
              Administrador (admin)
            </button>
            <button type="button" class="btn btn-sm btn-secondary" (click)="seleccionar('jefe.almacen', 'Jefe2026*')">
              Jefe Almacén (jefe.almacen)
            </button>
            <button type="button" class="btn btn-sm btn-secondary" (click)="seleccionar('almacenero', 'Almacen2026*')">
              Almacenero (almacenero)
            </button>
            <button type="button" class="btn btn-sm btn-secondary" (click)="seleccionar('costos', 'Costos2026*')">
              Costos (costos)
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  username = 'admin';
  password = 'Admin2026*';
  cargando = false;
  errorMsg: string | null = null;

  constructor(
    private auth: AuthService,
    private store: StoreService,
    private router: Router
  ) {}

  seleccionar(user: string, pass: string): void {
    this.username = user;
    this.password = pass;
    this.errorMsg = null;
  }

  iniciarSesion(): void {
    this.errorMsg = null;
    this.cargando = true;
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: (res) => {
        this.cargando = false;
        this.store.notificarExito(`Bienvenido a la Intranet, ${res.nombreCompleto}`);
        this.store.cargarDatos();
        this.router.navigate(['/resumen']);
      },
      error: (err) => {
        this.cargando = false;
        this.errorMsg = this.store.extraerError(
          err,
          'Credenciales incorrectas o servidor no disponible'
        );
      }
    });
  }
}
