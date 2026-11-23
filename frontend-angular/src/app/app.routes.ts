import { Routes } from '@angular/router';
import { anyPermisoGuard, authGuard, guestGuard, permisoGuard } from './guards/auth.guard';
import { ShellComponent } from './layout/shell.component';
import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ArticulosComponent } from './pages/articulos/articulos.component';
import { CatalogosComponent } from './pages/catalogos/catalogos.component';
import { ProveedoresComponent } from './pages/proveedores/proveedores.component';
import { SeguridadComponent } from './pages/seguridad/seguridad.component';
import { AlmacenComponent } from './pages/almacen/almacen.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard]
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'resumen', pathMatch: 'full' },
      {
        path: 'resumen',
        component: DashboardComponent,
        data: { titulo: 'Panel General de Inventario' }
      },
      {
        path: 'articulos',
        component: ArticulosComponent,
        canActivate: [permisoGuard('ARTICULO_VER')],
        data: { titulo: 'Catálogo y Mantenimiento de Artículos' }
      },
      {
        path: 'catalogos',
        component: CatalogosComponent,
        canActivate: [permisoGuard('MAESTRO_GESTIONAR')],
        data: { titulo: 'Categorías, Unidades de Medida y Ubicaciones' }
      },
      {
        path: 'proveedores',
        component: ProveedoresComponent,
        canActivate: [permisoGuard('PROVEEDOR_GESTIONAR')],
        data: { titulo: 'Directorio y Gestión de Proveedores' }
      },
      {
        path: 'seguridad',
        component: SeguridadComponent,
        canActivate: [permisoGuard('USUARIO_GESTIONAR')],
        data: { titulo: 'Administración de Usuarios, Roles y Permisos' }
      },
      {
        path: 'almacen',
        component: AlmacenComponent,
        canActivate: [
          anyPermisoGuard(
            'INGRESO_VER',
            'INGRESO_CREAR',
            'SALIDA_VER',
            'SALIDA_ATENDER',
            'TRANSFER_VER',
            'KARDEX_VER'
          )
        ],
        data: { titulo: 'Operaciones de Almacén, Salidas FIFO y Kardex' }
      }
    ]
  },
  { path: '**', redirectTo: 'resumen' }
];
