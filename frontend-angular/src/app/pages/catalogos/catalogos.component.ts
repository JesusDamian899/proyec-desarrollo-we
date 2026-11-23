import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { StoreService } from '../../services/store.service';
import { Categoria, Ubicacion, UnidadMedida } from '../../models/inventario.models';

@Component({
  selector: 'app-catalogos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="three-col-grid">
      <!-- CATEGORIAS -->
      <div class="card">
        <div class="section-head">
          <h2>Categorías ({{ store.categorias().length }})</h2>
          @if (editandoCatId) {
            <button type="button" class="btn btn-sm btn-secondary" (click)="cancelarCat()">Cancelar</button>
          }
        </div>
        <form (ngSubmit)="guardarCategoria()" class="inline-form">
          <input
            name="catNombre"
            type="text"
            class="form-control"
            [(ngModel)]="formCatNombre"
            placeholder="Nombre de categoría..."
            required
          />
          <button type="submit" class="btn btn-primary">
            {{ editandoCatId ? 'Actualizar' : 'Agregar' }}
          </button>
        </form>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (c of store.categorias(); track c.idCategoria) {
                <tr>
                  <td class="mono">#{{ c.idCategoria }}</td>
                  <td><strong>{{ c.nombre }}</strong></td>
                  <td class="actions-cell">
                    <button type="button" class="btn btn-sm btn-secondary" (click)="editarCat(c)">Editar</button>
                    <button type="button" class="btn btn-sm btn-danger" (click)="eliminarCat(c)">Borrar</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      <!-- UNIDADES DE MEDIDA -->
      <div class="card">
        <div class="section-head">
          <h2>Unidades de Medida ({{ store.unidades().length }})</h2>
          @if (editandoUniId) {
            <button type="button" class="btn btn-sm btn-secondary" (click)="cancelarUni()">Cancelar</button>
          }
        </div>
        <form (ngSubmit)="guardarUnidad()" class="stack-form">
          <div class="two-inputs">
            <input
              name="uniNombre"
              type="text"
              class="form-control"
              [(ngModel)]="formUni.nombre"
              placeholder="Nombre (Ej. Caja)"
              required
            />
            <input
              name="uniAbrev"
              type="text"
              class="form-control mono"
              [(ngModel)]="formUni.abreviatura"
              placeholder="Abrev. (CJ)"
              required
            />
          </div>
          <button type="submit" class="btn btn-primary">
            {{ editandoUniId ? 'Actualizar Unidad' : 'Registrar Unidad' }}
          </button>
        </form>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Unidad</th>
                <th>Abrev.</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (u of store.unidades(); track u.idUnidad) {
                <tr>
                  <td class="mono">#{{ u.idUnidad }}</td>
                  <td>{{ u.nombre }}</td>
                  <td class="mono"><span class="badge badge-info">{{ u.abreviatura }}</span></td>
                  <td class="actions-cell">
                    <button type="button" class="btn btn-sm btn-secondary" (click)="editarUni(u)">Editar</button>
                    <button type="button" class="btn btn-sm btn-danger" (click)="eliminarUni(u)">Borrar</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      <!-- UBICACIONES -->
      <div class="card">
        <div class="section-head">
          <h2>Ubicaciones Físicas ({{ store.ubicaciones().length }})</h2>
          @if (editandoUbicId) {
            <button type="button" class="btn btn-sm btn-secondary" (click)="cancelarUbic()">Cancelar</button>
          }
        </div>
        <form (ngSubmit)="guardarUbicacion()" class="stack-form">
          <div class="two-inputs">
            <input
              name="ubicCodigo"
              type="text"
              class="form-control mono"
              [(ngModel)]="formUbic.codigo"
              placeholder="Código (ALM-C1)"
              required
            />
            <select name="ubicTipo" class="form-control" [(ngModel)]="formUbic.tipo">
              <option value="ESTANTERIA">ESTANTERIA</option>
              <option value="RECEPCION">RECEPCION</option>
              <option value="PRODUCCION">PRODUCCION</option>
              <option value="DESPACHO">DESPACHO</option>
            </select>
          </div>
          <input
            name="ubicNombre"
            type="text"
            class="form-control"
            [(ngModel)]="formUbic.nombre"
            placeholder="Descripción de la ubicación..."
            required
          />
          <button type="submit" class="btn btn-primary">
            {{ editandoUbicId ? 'Actualizar Ubicación' : 'Registrar Ubicación' }}
          </button>
        </form>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nombre</th>
                <th>Tipo</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (ub of store.ubicaciones(); track ub.id) {
                <tr>
                  <td class="mono"><strong>{{ ub.codigo }}</strong></td>
                  <td>{{ ub.nombre }}</td>
                  <td><span class="badge badge-info">{{ ub.tipo }}</span></td>
                  <td class="actions-cell">
                    <button type="button" class="btn btn-sm btn-secondary" (click)="editarUbic(ub)">Editar</button>
                    <button type="button" class="btn btn-sm btn-danger" (click)="eliminarUbic(ub)">Borrar</button>
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
export class CatalogosComponent {
  editandoCatId: number | null = null;
  formCatNombre = '';

  editandoUniId: number | null = null;
  formUni = { nombre: '', abreviatura: '' };

  editandoUbicId: number | null = null;
  formUbic = { codigo: '', nombre: '', tipo: 'ESTANTERIA' };

  constructor(
    public store: StoreService,
    private api: ApiService
  ) {}

  // Categorias
  guardarCategoria(): void {
    this.store.limpiarMensajes();
    const nombre = this.formCatNombre.trim();
    if (!nombre) return;

    const obs = this.editandoCatId
      ? this.api.actualizarCategoria(this.editandoCatId, { nombre })
      : this.api.crearCategoria({ nombre });

    obs.subscribe({
      next: () => {
        this.store.notificarExito(
          this.editandoCatId ? 'Categoría actualizada.' : 'Categoría registrada exitosamente.'
        );
        this.cancelarCat();
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al guardar categoría'))
    });
  }

  editarCat(c: Categoria): void {
    this.editandoCatId = c.idCategoria!;
    this.formCatNombre = c.nombre;
  }

  cancelarCat(): void {
    this.editandoCatId = null;
    this.formCatNombre = '';
  }

  eliminarCat(c: Categoria): void {
    if (!this.store.confirmarEliminacion(`la categoría "${c.nombre}"`)) return;
    this.store.limpiarMensajes();
    this.api.eliminarCategoria(c.idCategoria!).subscribe({
      next: () => {
        this.store.notificarExito('Categoría eliminada.');
        this.store.cargarDatos();
      },
      error: (err) =>
        this.store.notificarError(
          this.store.extraerError(err, 'No se puede eliminar una categoría con artículos asociados.')
        )
    });
  }

  // Unidades
  guardarUnidad(): void {
    this.store.limpiarMensajes();
    const payload = {
      nombre: this.formUni.nombre.trim(),
      abreviatura: this.formUni.abreviatura.trim().toUpperCase()
    };
    if (!payload.nombre || !payload.abreviatura) return;

    const obs = this.editandoUniId
      ? this.api.actualizarUnidad(this.editandoUniId, payload)
      : this.api.crearUnidad(payload);

    obs.subscribe({
      next: () => {
        this.store.notificarExito(
          this.editandoUniId ? 'Unidad de medida actualizada.' : 'Unidad de medida creada.'
        );
        this.cancelarUni();
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al guardar unidad'))
    });
  }

  editarUni(u: UnidadMedida): void {
    this.editandoUniId = (u.idUnidad ?? u.idUnidadMedida)!;
    this.formUni = { nombre: u.nombre, abreviatura: u.abreviatura };
  }

  cancelarUni(): void {
    this.editandoUniId = null;
    this.formUni = { nombre: '', abreviatura: '' };
  }

  eliminarUni(u: UnidadMedida): void {
    if (!this.store.confirmarEliminacion(`la unidad "${u.nombre} (${u.abreviatura})"`)) return;
    this.store.limpiarMensajes();
    this.api.eliminarUnidad((u.idUnidad ?? u.idUnidadMedida)!).subscribe({
      next: () => {
        this.store.notificarExito('Unidad de medida eliminada.');
        this.store.cargarDatos();
      },
      error: (err) =>
        this.store.notificarError(
          this.store.extraerError(err, 'No se puede eliminar una unidad en uso por artículos.')
        )
    });
  }

  // Ubicaciones
  guardarUbicacion(): void {
    this.store.limpiarMensajes();
    const payload = {
      codigo: this.formUbic.codigo.trim().toUpperCase(),
      nombre: this.formUbic.nombre.trim(),
      tipo: this.formUbic.tipo.trim().toUpperCase(),
      activo: true
    };
    if (!payload.codigo || !payload.nombre) return;

    const obs = this.editandoUbicId
      ? this.api.actualizarUbicacion(this.editandoUbicId, payload)
      : this.api.crearUbicacion(payload);

    obs.subscribe({
      next: () => {
        this.store.notificarExito(
          this.editandoUbicId ? 'Ubicación actualizada.' : 'Ubicación registrada exitosamente.'
        );
        this.cancelarUbic();
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al guardar ubicación'))
    });
  }

  editarUbic(ub: Ubicacion): void {
    this.editandoUbicId = ub.id!;
    this.formUbic = { codigo: ub.codigo, nombre: ub.nombre, tipo: ub.tipo };
  }

  cancelarUbic(): void {
    this.editandoUbicId = null;
    this.formUbic = { codigo: '', nombre: '', tipo: 'ESTANTERIA' };
  }

  eliminarUbic(ub: Ubicacion): void {
    if (!this.store.confirmarEliminacion(`la ubicación "${ub.codigo} — ${ub.nombre}"`)) return;
    this.store.limpiarMensajes();
    this.api.eliminarUbicacion(ub.id!).subscribe({
      next: () => {
        this.store.notificarExito('Ubicación eliminada.');
        this.store.cargarDatos();
      },
      error: (err) =>
        this.store.notificarError(
          this.store.extraerError(err, 'No se puede eliminar una ubicación con stock o lotes.')
        )
    });
  }
}
