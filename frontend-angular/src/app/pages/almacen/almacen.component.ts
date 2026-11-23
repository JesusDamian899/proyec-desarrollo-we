import { Component, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { StoreService } from '../../services/store.service';

@Component({
  selector: 'app-almacen',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-stack">
      <div class="three-col-grid">
        @if (auth.hasPermiso('INGRESO_CREAR')) {
          <div class="card">
            <div class="section-head">
              <h2>1. Ingreso de Stock por Lote</h2>
            </div>
            <form (ngSubmit)="ejecutarIngreso()" class="stack-form">
              <div class="form-field">
                <label>Artículo</label>
                <select name="ingArt" class="form-control" [(ngModel)]="formIngreso.articuloId">
                  @for (a of store.articulos(); track a.idArticulo) {
                    <option [ngValue]="a.idArticulo">{{ a.codigoInterno }} — {{ a.descripcion }}</option>
                  }
                </select>
              </div>
              <div class="two-inputs">
                <div class="form-field">
                  <label>Código de Lote</label>
                  <input
                    name="ingLote"
                    type="text"
                    class="form-control mono"
                    [(ngModel)]="formIngreso.codigoLote"
                    placeholder="LOT-2026-099"
                    required
                  />
                </div>
                <div class="form-field">
                  <label>Cantidad</label>
                  <input
                    name="ingCant"
                    type="number"
                    step="0.01"
                    class="form-control mono"
                    [(ngModel)]="formIngreso.cantidad"
                    required
                  />
                </div>
              </div>
              <div class="two-inputs">
                <div class="form-field">
                  <label>Ubicación Destino</label>
                  <select name="ingUbic" class="form-control" [(ngModel)]="formIngreso.ubicacionDestinoId">
                    @for (ub of store.ubicaciones(); track ub.id) {
                      <option [ngValue]="ub.id">{{ ub.codigo }}</option>
                    }
                  </select>
                </div>
                <div class="form-field">
                  <label>Guía / Documento</label>
                  <input
                    name="ingDoc"
                    type="text"
                    class="form-control mono"
                    [(ngModel)]="formIngreso.documentoReferencia"
                    required
                  />
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Registrar Ingreso</button>
            </form>
          </div>
        }

        @if (auth.hasAnyPermiso('SALIDA_ATENDER', 'SALIDA_APROBAR', 'SOLICITUD_CREAR')) {
          <div class="card">
            <div class="section-head">
              <h2>2. Salida a Producción (FIFO)</h2>
            </div>
            <form (ngSubmit)="ejecutarSalidaFIFO()" class="stack-form">
              <div class="form-field">
                <label>Artículo a Despachar</label>
                <select name="salArt" class="form-control" [(ngModel)]="formSalida.articuloId">
                  @for (a of store.articulos(); track a.idArticulo) {
                    <option [ngValue]="a.idArticulo">
                      {{ a.codigoInterno }} (Disp: {{ a.stockActual }})
                    </option>
                  }
                </select>
              </div>
              <div class="two-inputs">
                <div class="form-field">
                  <label>Cantidad Requerida</label>
                  <input
                    name="salCant"
                    type="number"
                    step="0.01"
                    class="form-control mono"
                    [(ngModel)]="formSalida.cantidadRequerida"
                    required
                  />
                </div>
                <div class="form-field">
                  <label>Orden Producción</label>
                  <input
                    name="salOp"
                    type="text"
                    class="form-control mono"
                    [(ngModel)]="formSalida.ordenProduccion"
                    required
                  />
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Atender Salida FIFO</button>
            </form>
          </div>
        }

        @if (auth.hasPermiso('TRANSFER_CREAR')) {
          <div class="card">
            <div class="section-head">
              <h2>3. Transferencia entre Ubicaciones</h2>
            </div>
            <form (ngSubmit)="ejecutarTransferencia()" class="stack-form">
              <div class="form-field">
                <label>Lote Activo</label>
                <select
                  name="trfLote"
                  class="form-control"
                  [(ngModel)]="formTransferencia.loteId"
                  (ngModelChange)="alCambiarLote()"
                >
                  @for (l of store.lotes(); track l.id) {
                    <option [ngValue]="l.id">
                      {{ l.codigoLote }} — {{ l.articulo?.codigoInterno }} ({{ l.cantidadDisponible }} en {{ l.ubicacion?.codigo }})
                    </option>
                  }
                </select>
              </div>
              <div class="two-inputs">
                <div class="form-field">
                  <label>Ubicación Destino</label>
                  <select name="trfDest" class="form-control" [(ngModel)]="formTransferencia.ubicacionDestinoId">
                    @for (ub of store.ubicaciones(); track ub.id) {
                      <option [ngValue]="ub.id">{{ ub.codigo }} — {{ ub.nombre }}</option>
                    }
                  </select>
                </div>
                <div class="form-field">
                  <label>Cantidad a Mover</label>
                  <input
                    name="trfCant"
                    type="number"
                    step="0.01"
                    class="form-control mono"
                    [(ngModel)]="formTransferencia.cantidad"
                    required
                  />
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Transferir Stock</button>
            </form>
          </div>
        }
      </div>

      <div class="card">
        <div class="section-head">
          <h2>Historial de Trazabilidad en Kardex ({{ store.kardex().length }} registros)</h2>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Fecha / Hora</th>
                <th>Tipo</th>
                <th>Documento / Referencia</th>
                <th>Artículo</th>
                <th>Cantidad</th>
                <th>Saldo Resultante</th>
                <th>Usuario</th>
              </tr>
            </thead>
            <tbody>
              @for (k of store.kardex(); track k.idKardex) {
                <tr>
                  <td class="mono">#{{ k.idKardex }}</td>
                  <td class="mono">{{ k.fechaMovimiento | date:'yyyy-MM-dd HH:mm' }}</td>
                  <td>
                    <span
                      class="badge"
                      [class.badge-success]="k.tipoMovimiento === 'INGRESO'"
                      [class.badge-warning]="k.tipoMovimiento === 'SALIDA'"
                      [class.badge-info]="k.tipoMovimiento === 'TRANSFERENCIA'"
                    >
                      {{ k.tipoMovimiento }}
                    </span>
                  </td>
                  <td class="mono">{{ k.referenciaDocumento }}</td>
                  <td>{{ k.articulo?.codigoInterno }} — {{ k.articulo?.descripcion }}</td>
                  <td class="mono"><strong>{{ k.cantidad }}</strong></td>
                  <td class="mono"><strong>{{ k.saldoResultante }}</strong></td>
                  <td class="mono">{{ k.usuario?.username }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class AlmacenComponent {
  formIngreso = {
    articuloId: 0,
    codigoLote: '',
    cantidad: 10,
    fechaVencimiento: '2027-06-30',
    ubicacionDestinoId: 0,
    proveedorId: 0,
    documentoReferencia: 'GR-001-00500'
  };

  formSalida = {
    articuloId: 0,
    cantidadRequerida: 5,
    ordenProduccion: 'OP-2026-105'
  };

  formTransferencia = {
    loteId: 0,
    ubicacionOrigenId: 0,
    ubicacionDestinoId: 0,
    cantidad: 5,
    motivo: 'Reubicacion operativa de estanteria'
  };

  constructor(
    public auth: AuthService,
    public store: StoreService,
    private api: ApiService
  ) {
    effect(() => {
      const arts = this.store.articulos();
      const ubs = this.store.ubicaciones();
      const provs = this.store.proveedores();
      const lotes = this.store.lotes();

      if (arts.length > 0) {
        if (!this.formIngreso.articuloId) this.formIngreso.articuloId = arts[0].idArticulo!;
        if (!this.formSalida.articuloId) this.formSalida.articuloId = arts[0].idArticulo!;
      }
      if (ubs.length > 0) {
        if (!this.formIngreso.ubicacionDestinoId) this.formIngreso.ubicacionDestinoId = ubs[0].id!;
        if (!this.formTransferencia.ubicacionOrigenId) this.formTransferencia.ubicacionOrigenId = ubs[0].id!;
        if (!this.formTransferencia.ubicacionDestinoId) {
          this.formTransferencia.ubicacionDestinoId = ubs[1 % ubs.length].id!;
        }
      }
      if (provs.length > 0 && !this.formIngreso.proveedorId) {
        this.formIngreso.proveedorId = provs[0].idProveedor!;
      }
      if (lotes.length > 0 && !this.formTransferencia.loteId) {
        this.formTransferencia.loteId = lotes[0].id;
        if (lotes[0].ubicacion?.id) {
          this.formTransferencia.ubicacionOrigenId = lotes[0].ubicacion.id;
        }
      }
    });
  }

  ejecutarIngreso(): void {
    this.store.limpiarMensajes();
    this.api
      .registrarIngreso({
        articuloId: Number(this.formIngreso.articuloId),
        codigoLote: this.formIngreso.codigoLote.trim(),
        cantidad: Number(this.formIngreso.cantidad),
        fechaVencimiento: this.formIngreso.fechaVencimiento || null,
        ubicacionDestinoId: Number(this.formIngreso.ubicacionDestinoId),
        proveedorId: this.formIngreso.proveedorId ? Number(this.formIngreso.proveedorId) : null,
        documentoReferencia: this.formIngreso.documentoReferencia.trim()
      })
      .subscribe({
        next: (mov) => {
          this.store.notificarExito(
            `Ingreso registrado: +${mov.cantidad} en lote ${mov.codigoLote} (${mov.articuloDescripcion}).`
          );
          this.formIngreso.codigoLote = '';
          this.store.cargarDatos();
        },
        error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al registrar ingreso'))
      });
  }

  ejecutarSalidaFIFO(): void {
    this.store.limpiarMensajes();
    this.api
      .registrarSalidaFIFO({
        articuloId: Number(this.formSalida.articuloId),
        cantidadRequerida: Number(this.formSalida.cantidadRequerida),
        ordenProduccion: this.formSalida.ordenProduccion.trim()
      })
      .subscribe({
        next: (movs) => {
          this.store.notificarExito(
            `Salida FIFO atendida exitosamente desde ${movs.length} lote(s) hacia producción.`
          );
          this.store.cargarDatos();
        },
        error: (err) => this.store.notificarError(this.store.extraerError(err, 'Error al procesar salida FIFO'))
      });
  }

  alCambiarLote(): void {
    const lote = this.store.lotes().find((l) => l.id === Number(this.formTransferencia.loteId));
    if (lote?.ubicacion?.id) {
      this.formTransferencia.ubicacionOrigenId = lote.ubicacion.id;
    }
  }

  ejecutarTransferencia(): void {
    this.store.limpiarMensajes();
    this.api
      .registrarTransferencia({
        loteId: Number(this.formTransferencia.loteId),
        ubicacionOrigenId: Number(this.formTransferencia.ubicacionOrigenId),
        ubicacionDestinoId: Number(this.formTransferencia.ubicacionDestinoId),
        cantidad: Number(this.formTransferencia.cantidad),
        motivo: this.formTransferencia.motivo.trim()
      })
      .subscribe({
        next: (mov) => {
          this.store.notificarExito(
            `Transferencia registrada: ${mov.cantidad} unidades hacia ${mov.ubicacionDestinoNombre}.`
          );
          this.store.cargarDatos();
        },
        error: (err) =>
          this.store.notificarError(this.store.extraerError(err, 'Error al registrar transferencia'))
      });
  }
}
