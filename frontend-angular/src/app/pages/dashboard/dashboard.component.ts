import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { StoreService } from '../../services/store.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-stack">
      <section class="kpi-grid">
        <div class="card kpi-card">
          <span class="kpi-label">Artículos en Catálogo</span>
          <span class="kpi-value mono">{{ store.articulos().length }}</span>
          <span class="kpi-foot">{{ store.categorias().length }} categorías y {{ store.unidades().length }} unidades</span>
        </div>
        <div class="card kpi-card">
          <span class="kpi-label">Alertas Stock Mínimo</span>
          <span class="kpi-value mono" [class.text-danger]="store.totalBajoStock() > 0">{{ store.totalBajoStock() }}</span>
          <span class="kpi-foot">Artículos con stock &le; mínimo</span>
        </div>
        <div class="card kpi-card">
          <span class="kpi-label">Ubicaciones y Proveedores</span>
          <span class="kpi-value mono">{{ store.ubicaciones().length }} / {{ store.proveedores().length }}</span>
          <span class="kpi-foot">Zonas activas / Proveedores</span>
        </div>
        <div class="card kpi-card">
          <span class="kpi-label">Usuarios y Roles</span>
          <span class="kpi-value mono">{{ store.usuarios().length || '—' }} / {{ store.roles().length || 5 }}</span>
          <span class="kpi-foot">{{ auth.session()?.permisos?.length || 0 }} permisos activos</span>
        </div>
      </section>

      <div class="two-col-layout">
        <div class="card">
          <div class="section-head">
            <h2>Estado Actual del Inventario</h2>
            <div class="section-actions">
              <button type="button" class="btn btn-sm btn-secondary" (click)="exportarStockExcel()">
                Exportar Excel
              </button>
              <button type="button" class="btn btn-sm btn-danger" (click)="exportarStockPDF()">
                Exportar PDF
              </button>
              @if (auth.hasPermiso('ARTICULO_VER')) {
                <a routerLink="/articulos" class="btn btn-sm btn-primary">
                  Ir a Mantenimiento de Artículos
                </a>
              }
            </div>
          </div>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Descripción</th>
                  <th>Categoría</th>
                  <th>Ubicación</th>
                  <th>Stock Actual</th>
                  <th>Mínimo</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                @for (art of store.articulos(); track art.idArticulo) {
                  <tr>
                    <td class="mono"><strong>{{ art.codigoInterno }}</strong></td>
                    <td>{{ art.descripcion }}</td>
                    <td>{{ art.categoria?.nombre }}</td>
                    <td class="mono">{{ art.ubicacion?.codigo || 'SIN UBIC.' }}</td>
                    <td class="mono"><strong>{{ art.stockActual }}</strong> {{ art.unidadMedida?.abreviatura }}</td>
                    <td class="mono">{{ art.stockMinimo }}</td>
                    <td>
                      @if (art.stockActual <= art.stockMinimo) {
                        <span class="badge badge-danger">REPOSICIÓN URGENTE</span>
                      } @else {
                        <span class="badge badge-success">ÓPTIMO</span>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>

        <div class="card">
          <div class="section-head">
            <h2>Resumen de Catálogos del Sistema</h2>
            @if (auth.hasPermiso('MAESTRO_GESTIONAR')) {
              <a routerLink="/catalogos" class="btn btn-sm btn-secondary">
                Gestionar Catálogos
              </a>
            }
          </div>
          <ul class="summary-list">
            <li>
              <span>Categorías de Artículos</span>
              <span class="badge badge-info">{{ store.categorias().length }} registros</span>
            </li>
            <li>
              <span>Unidades de Medida</span>
              <span class="badge badge-info">{{ store.unidades().length }} registros</span>
            </li>
            <li>
              <span>Ubicaciones de Almacén</span>
              <span class="badge badge-info">{{ store.ubicaciones().length }} registros</span>
            </li>
            <li>
              <span>Proveedores Registrados</span>
              <span class="badge badge-info">{{ store.proveedores().length }} registros</span>
            </li>
            <li>
              <span>Artículos en Catálogo</span>
              <span class="badge badge-info">{{ store.articulos().length }} registros</span>
            </li>
            <li>
              <span>Lotes Activos</span>
              <span class="badge badge-info">{{ store.lotes().length }} lotes activos</span>
            </li>
            <li>
              <span>Movimientos en Kardex</span>
              <span class="badge badge-info">{{ store.kardex().length }} movimientos</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent {
  constructor(
    public auth: AuthService,
    public store: StoreService
  ) {}

  exportarStockExcel(): void {
    const filas = this.store.articulos();
    const cabeceras = ['Codigo', 'Descripcion', 'Categoria', 'Ubicacion', 'Stock Actual', 'Stock Minimo', 'Estado'];
    const lineas = filas.map((art) => {
      const estado = art.stockActual <= art.stockMinimo ? 'REPOSICION URGENTE' : 'OPTIMO';
      return [
        art.codigoInterno,
        `"${art.descripcion.replace(/"/g, '""')}"`,
        art.categoria?.nombre || '-',
        art.ubicacion?.codigo || 'SIN UBIC.',
        art.stockActual,
        art.stockMinimo,
        estado
      ].join(';');
    });

    const csv = '\ufeff' + [cabeceras.join(';'), ...lineas].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `reporte_stock_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  exportarStockPDF(): void {
    const filas = this.store
      .articulos()
      .map((art) => {
        const estado = art.stockActual <= art.stockMinimo ? 'REPOSICIÓN URGENTE' : 'ÓPTIMO';
        return `
          <tr>
            <td>${art.codigoInterno}</td>
            <td>${art.descripcion}</td>
            <td>${art.categoria?.nombre || '-'}</td>
            <td>${art.ubicacion?.codigo || 'SIN UBIC.'}</td>
            <td>${art.stockActual} ${art.unidadMedida?.abreviatura || ''}</td>
            <td>${art.stockMinimo}</td>
            <td>${estado}</td>
          </tr>`;
      })
      .join('');

    const ventana = window.open('', '_blank', 'width=900,height=650');
    if (!ventana) return;

    ventana.document.write(`
      <html>
        <head>
          <title>Reporte de Stock Actual - Electromecánica Andina S.A.C.</title>
          <style>
            body { font-family: Arial, sans-serif; padding: 24px; color: #1e293b; }
            h1 { font-size: 18px; margin-bottom: 4px; }
            p { font-size: 12px; color: #64748b; margin-top: 0; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 12px; }
            th, td { border: 1px solid #cbd5e1; padding: 8px; text-align: left; }
            th { background: #f1f5f9; }
          </style>
        </head>
        <body>
          <h1>Electromecánica Andina S.A.C. — Reporte de Stock Actual</h1>
          <p>Fecha de emisión: ${new Date().toLocaleString('es-PE')}</p>
          <table>
            <thead>
              <tr>
                <th>Código</th><th>Descripción</th><th>Categoría</th>
                <th>Ubicación</th><th>Stock Actual</th><th>Mínimo</th><th>Estado</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
          </table>
        </body>
      </html>
    `);
    ventana.document.close();
    ventana.focus();
    ventana.print();
  }
}
