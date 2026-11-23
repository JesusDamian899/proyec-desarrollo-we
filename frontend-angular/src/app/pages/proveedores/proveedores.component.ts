import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { Proveedor } from '../../models/inventario.models';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-stack">
      <div class="card">
        <div class="section-head">
          <h2>{{ editandoId ? 'Editar Proveedor #' + editandoId : 'Registrar Nuevo Proveedor' }}</h2>
          @if (editandoId) {
            <button type="button" class="btn btn-sm btn-secondary" (click)="cancelar()">Cancelar Edición</button>
          }
        </div>
        <form (ngSubmit)="guardar()" class="form-grid">
          <div class="form-field">
            <label for="prov-ruc">RUC (11 dígitos)</label>
            <input
              id="prov-ruc"
              name="ruc"
              type="text"
              maxlength="11"
              class="form-control mono"
              [(ngModel)]="form.ruc"
              placeholder="20512345678"
              required
            />
          </div>
          <div class="form-field">
            <label for="prov-razon">Razón Social</label>
            <input
              id="prov-razon"
              name="razonSocial"
              type="text"
              class="form-control"
              [(ngModel)]="form.razonSocial"
              placeholder="Nombre de la empresa proveedora SAC / SAA"
              required
            />
          </div>
          <div class="form-field">
            <label for="prov-contacto">Correo / Teléfono de Contacto</label>
            <input
              id="prov-contacto"
              name="contacto"
              type="text"
              class="form-control"
              [(ngModel)]="form.contacto"
              placeholder="ventas@proveedor.com.pe"
              required
            />
          </div>
          <div class="form-field form-actions-end">
            <button type="submit" class="btn btn-primary">
              {{ editandoId ? 'Actualizar Proveedor' : 'Registrar Proveedor' }}
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <div class="section-head">
          <h2>Directorio de Proveedores Registrados ({{ store.proveedores().length }})</h2>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>RUC</th>
                <th>Razón Social</th>
                <th>Contacto</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (p of store.proveedores(); track p.idProveedor) {
                <tr>
                  <td class="mono">#{{ p.idProveedor }}</td>
                  <td class="mono"><strong>{{ p.ruc }}</strong></td>
                  <td>{{ p.razonSocial }}</td>
                  <td>{{ p.contacto }}</td>
                  <td class="actions-cell">
                    <button type="button" class="btn btn-sm btn-secondary" (click)="editar(p)">Editar</button>
                    <button type="button" class="btn btn-sm btn-danger" (click)="eliminar(p)">Eliminar</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class ProveedoresComponent {
  editandoId: number | null = null;
  form = { ruc: '', razonSocial: '', contacto: '' };

  constructor(
    public store: StoreService,
    private api: ApiService
  ) {}

  guardar(): void {
    this.store.limpiarMensajes();
    const payload = {
      ruc: this.form.ruc.trim(),
      razonSocial: this.form.razonSocial.trim(),
      contacto: this.form.contacto.trim()
    };

    const obs = this.editandoId
      ? this.api.actualizarProveedor(this.editandoId, payload)
      : this.api.crearProveedor(payload);

    obs.subscribe({
      next: () => {
        this.store.notificarExito(
          this.editandoId ? 'Proveedor actualizado.' : 'Proveedor registrado exitosamente.'
        );
        this.cancelar();
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al guardar proveedor'))
    });
  }

  editar(p: Proveedor): void {
    this.editandoId = p.idProveedor!;
    this.form = { ruc: p.ruc, razonSocial: p.razonSocial, contacto: p.contacto };
  }

  cancelar(): void {
    this.editandoId = null;
    this.form = { ruc: '', razonSocial: '', contacto: '' };
  }

  eliminar(p: Proveedor): void {
    if (!this.store.confirmarEliminacion(`al proveedor "${p.razonSocial}" (RUC ${p.ruc})`)) return;
    this.store.limpiarMensajes();
    this.api.eliminarProveedor(p.idProveedor!).subscribe({
      next: () => {
        this.store.notificarExito('Proveedor eliminado.');
        this.store.cargarDatos();
      },
      error: (err) =>
        this.store.notificarError(
          this.store.extraerError(err, 'No se puede eliminar un proveedor con ingresos registrados.')
        )
    });
  }
}
