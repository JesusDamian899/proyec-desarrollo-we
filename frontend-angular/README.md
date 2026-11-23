# Intranet Corporativa — Frontend Angular 19

Interfaz web modular desarrollada en **Angular 19** (Standalone Components + Signals + Router Guards) para el Sistema de Gestión de Almacén e Inventario de **Electromecánica Andina S.A.C.**

## Estructura

- `src/app/guards/` — Protección de rutas autenticadas y verificación de permisos (`authGuard`, `guestGuard`, `permisoGuard`).
- `src/app/layout/` — `ShellComponent` con menú lateral dinámico según permisos y cabecera contextual.
- `src/app/pages/` — Módulos independientes: `login`, `dashboard`, `articulos`, `catalogos`, `proveedores`, `seguridad`, `almacen`.
- `src/app/services/` — `AuthService`, `authInterceptor`, `ApiService` y `StoreService`.

## Comandos

```bash
npm install
npm start        # Servidor de desarrollo en http://localhost:4200
npm run build    # Compilación de producción en dist/frontend-angular/
```
