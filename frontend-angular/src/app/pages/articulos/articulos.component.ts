import { Component, computed, effect, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { StoreService } from '../../services/store.service';
import { Articulo } from '../../models/inventario.models';

@Component({
  selector: 'app-articulos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-stack">
      @if (auth.hasPermiso('ARTICULO_GESTIONAR')) {
        <div class="card">
          <div class="section-head">
            <h2>{{ editandoId ? 'Editar Artículo #' + editandoId : 'Registrar Nuevo Artículo' }}</h2>
            @if (editandoId) {
              <button type="button" class="btn btn-sm btn-secondary" (click)="cancelarEdicion()">
                Cancelar Edición
              </button>
            }
          </div>

          <form (ngSubmit)="guardar()" class="form-grid">
            <div class="form-field">
              <label for="art-codigo">Código Interno (Único)</label>
              <input
                id="art-codigo"
                name="codigoInterno"
                type="text"
                class="form-control mono"
                [(ngModel)]="form.codigoInterno"
                placeholder="Ej. EL-00105"
                required
              />
            </div>

            <div class="form-field">
              <label for="art-desc">Descripción Técnica</label>
              <input
                id="art-desc"
                name="descripcion"
                type="text"
                class="form-control"
                [(ngModel)]="form.descripcion"
                placeholder="Ej. Contactor Trifásico 50A 220V"
                required
              />
            </div>

            <div class="form-field">
              <label for="art-cat">Categoría</label>
              <select id="art-cat" name="categoriaId" class="form-control" [(ngModel)]="form.categoriaId" required>
                @for (c of store.categorias(); track c.idCategoria) {
                  <option [ngValue]="c.idCategoria">{{ c.nombre }}</option>
                }
              </select>
            </div>

            <div class="form-field">
              <label for="art-um">Unidad de Medida</label>
              <select id="art-um" name="unidadId" class="form-control" [(ngModel)]="form.unidadId" required>
                @for (u of store.unidades(); track u.idUnidad) {
                  <option [ngValue]="u.idUnidad">{{ u.nombre }} ({{ u.abreviatura }})</option>
                }
              </select>
            </div>

            <div class="form-field">
              <label for="art-ubic">Ubicación en Almacén</label>
              <select id="art-ubic" name="ubicacionId" class="form-control" [(ngModel)]="form.ubicacionId">
                @for (ub of store.ubicaciones(); track ub.id) {
                  <option [ngValue]="ub.id">{{ ub.codigo }} — {{ ub.nombre }}</option>
                }
              </select>
            </div>

            <div class="form-field">
              <label for="art-min">Stock Mínimo</label>
              <input
                id="art-min"
                name="stockMinimo"
                type="number"
                step="0.01"
                class="form-control mono"
                [(ngModel)]="form.stockMinimo"
                required
              />
            </div>

            <div class="form-field">
              <label for="art-actual">Stock Inicial / Actual</label>
              <input
                id="art-actual"
                name="stockActual"
                type="number"
                step="0.01"
                class="form-control mono"
                [(ngModel)]="form.stockActual"
                required
              />
            </div>

            <div class="form-field form-actions-end">
              <button type="submit" class="btn btn-primary">
                {{ editandoId ? 'Guardar Cambios' : 'Registrar Artículo' }}
              </button>
            </div>
          </form>
        </div>
      }

      <div class="card">
        <div class="section-head">
          <h2>Listado Maestro de Artículos ({{ filtrados().length }})</h2>
          <div class="filter-bar">
            <input
              type="text"
              class="form-control"
              placeholder="Buscar por código, descripción o categoría..."
              [ngModel]="busqueda()"
              (ngModelChange)="busqueda.set($event)"
            />
            <label class="checkbox-inline">
              <input
                type="checkbox"
                [ngModel]="soloBajoStock()"
                (ngModelChange)="soloBajoStock.set($event)"
              />
              <span>Solo bajo stock</span>
            </label>
          </div>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Código Interno</th>
                <th>Descripción</th>
                <th>Categoría</th>
                <th>Unidad</th>
                <th>Ubicación</th>
                <th>Stock Actual</th>
                <th>Stock Mín.</th>
                <th>Estado</th>
                @if (auth.hasPermiso('ARTICULO_GESTIONAR')) {
                  <th>Acciones</th>
                }
              </tr>
            </thead>
            <tbody>
              @for (art of filtrados(); track art.idArticulo) {
                <tr>
                  <td class="mono">#{{ art.idArticulo }}</td>
                  <td class="mono"><strong>{{ art.codigoInterno }}</strong></td>
                  <td>{{ art.descripcion }}</td>
                  <td>{{ art.categoria?.nombre }}</td>
                  <td class="mono">{{ art.unidadMedida?.abreviatura }}</td>
                  <td class="mono">{{ art.ubicacion?.codigo || '-' }}</td>
                  <td class="mono"><strong>{{ art.stockActual }}</strong></td>
                  <td class="mono">{{ art.stockMinimo }}</td>
                  <td>
                    @if (art.stockActual <= art.stockMinimo) {
                      <span class="badge badge-danger">BAJO STOCK</span>
                    } @else {
                      <span class="badge badge-success">NORMAL</span>
                    }
                  </td>
                  @if (auth.hasPermiso('ARTICULO_GESTIONAR')) {
                    <td class="actions-cell">
                      <button type="button" class="btn btn-sm btn-secondary" (click)="prepararEdicion(art)">
                        Editar
                      </button>
                      <button type="button" class="btn btn-sm btn-danger" (click)="eliminar(art)">
                        Eliminar
                      </button>
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class ArticulosComponent {
  readonly busqueda = signal<string>('');
  readonly soloBajoStock = signal<boolean>(false);

  readonly filtrados = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const bajo = this.soloBajoStock();
    return this.store.articulos().filter((a) => {
      const cumpleTexto =
        !q ||
        a.codigoInterno.toLowerCase().includes(q) ||
        a.descripcion.toLowerCase().includes(q) ||
        (a.categoria?.nombre?.toLowerCase().includes(q) ?? false);
      const cumpleStock = !bajo || a.stockActual <= a.stockMinimo;
      return cumpleTexto && cumpleStock;
    });
  });

  editandoId: number | null = null;
  form = {
    codigoInterno: '',
    descripcion: '',
    categoriaId: 0,
    unidadId: 0,
    ubicacionId: 0,
    stockMinimo: 10,
    stockActual: 0
  };

  constructor(
    public auth: AuthService,
    public store: StoreService,
    private api: ApiService
  ) {
    effect(() => {
      const cats = this.store.categorias();
      const ums = this.store.unidades();
      const ubs = this.store.ubicaciones();
      if (!this.form.categoriaId && cats.length > 0) this.form.categoriaId = cats[0].idCategoria!;
      if (!this.form.unidadId && ums.length > 0) this.form.unidadId = (ums[0].idUnidad ?? ums[0].idUnidadMedida)!;
      if (!this.form.ubicacionId && ubs.length > 0) this.form.ubicacionId = ubs[0].id!;
    });
  }

  guardar(): void {
    this.store.limpiarMensajes();
    const payload: Partial<Articulo> = {
      codigoInterno: this.form.codigoInterno.trim(),
      descripcion: this.form.descripcion.trim(),
      categoria: { idCategoria: Number(this.form.categoriaId), nombre: '' },
      unidadMedida: { idUnidad: Number(this.form.unidadId), nombre: '', abreviatura: '' },
      ubicacion: this.form.ubicacionId
        ? { id: Number(this.form.ubicacionId), nombre: '', codigo: '', tipo: '' }
        : null,
      stockMinimo: Number(this.form.stockMinimo),
      stockActual: Number(this.form.stockActual)
    };

    const obs = this.editandoId
      ? this.api.actualizarArticulo(this.editandoId, payload)
      : this.api.crearArticulo(payload);

    obs.subscribe({
      next: () => {
        this.store.notificarExito(
          this.editandoId ? 'Artículo actualizado correctamente.' : 'Nuevo artículo registrado exitosamente.'
        );
        this.cancelarEdicion();
        this.store.cargarDatos();
      },
      error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al guardar el artículo'))
    });
  }

  prepararEdicion(art: Articulo): void {
    this.editandoId = art.idArticulo!;
    this.form = {
      codigoInterno: art.codigoInterno,
      descripcion: art.descripcion,
      categoriaId: art.categoria?.idCategoria ?? 0,
      unidadId: art.unidadMedida?.idUnidad ?? art.unidadMedida?.idUnidadMedida ?? 0,
      ubicacionId: art.ubicacion?.id ?? 0,
      stockMinimo: art.stockMinimo,
      stockActual: art.stockActual
    };
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.form = {
      codigoInterno: '',
      descripcion: '',
      categoriaId: this.store.categorias()[0]?.idCategoria ?? 0,
      unidadId: this.store.unidades()[0]?.idUnidad ?? this.store.unidades()[0]?.idUnidadMedida ?? 0,
      ubicacionId: this.store.ubicaciones()[0]?.id ?? 0,
      stockMinimo: 10,
      stockActual: 0
    };
  }

  eliminar(art: Articulo): void {
    if (!this.store.confirmarEliminacion(`el artículo "${art.codigoInterno} — ${art.descripcion}"`)) return;
    this.store.limpiarMensajes();
    this.api.eliminarArticulo(art.idArticulo!).subscribe({
      next: () => {
        this.store.notificarExito('Artículo eliminado correctamente.');
        this.store.cargarDatos();
      },
      error: (err) =>
        this.store.notificarError(
          this.store.extraerError(err, 'No se puede eliminar un artículo con lotes o historial de Kardex asociado.')
        )
    });
  }
}
