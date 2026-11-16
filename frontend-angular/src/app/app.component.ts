import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from './services/auth.service';
import { ApiService } from './services/api.service';
import {
  Articulo,
  Categoria,
  Kardex,
  Lote,
  MovimientoResponse,
  Proveedor,
  Rol,
  Ubicacion,
  UnidadMedida,
  Usuario
} from './models/inventario.models';

type VistaActiva =
  | 'resumen'
  | 'articulos'
  | 'tablas-basicas'
  | 'proveedores'
  | 'seguridad'
  | 'almacen';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  // Credenciales login
  loginUser = 'admin';
  loginPass = 'Admin2026*';
  cargandoLogin = false;

  // Navegacion y mensajes
  vista = signal<VistaActiva>('resumen');
  mensajeExito = signal<string | null>(null);
  mensajeError = signal<string | null>(null);

  // Datos cargados desde la Base de Datos
  articulos = signal<Articulo[]>([]);
  categorias = signal<Categoria[]>([]);
  unidades = signal<UnidadMedida[]>([]);
  ubicaciones = signal<Ubicacion[]>([]);
  proveedores = signal<Proveedor[]>([]);
  usuarios = signal<Usuario[]>([]);
  roles = signal<Rol[]>([]);
  lotes = signal<Lote[]>([]);
  kardex = signal<Kardex[]>([]);
  movimientos = signal<MovimientoResponse[]>([]);

  // Filtros de Articulos
  busquedaArticulo = signal<string>('');
  soloBajoStock = signal<boolean>(false);

  articulosFiltrados = computed(() => {
    const q = this.busquedaArticulo().trim().toLowerCase();
    const bajo = this.soloBajoStock();
    return this.articulos().filter((a) => {
      const cumpleTexto =
        !q ||
        a.codigoInterno.toLowerCase().includes(q) ||
        a.descripcion.toLowerCase().includes(q) ||
        (a.categoria?.nombre?.toLowerCase().includes(q) ?? false);
      const cumpleStock = !bajo || a.stockActual <= a.stockMinimo;
      return cumpleTexto && cumpleStock;
    });
  });

  totalBajoStock = computed(
    () => this.articulos().filter((a) => a.stockActual <= a.stockMinimo).length
  );

  // Formulario Articulo (Crear / Editar)
  editandoArticuloId: number | null = null;
  formArticulo = {
    codigoInterno: '',
    descripcion: '',
    categoriaId: 0,
    unidadId: 0,
    ubicacionId: 0,
    stockMinimo: 10,
    stockActual: 0
  };

  // Formulario Categoria
  editandoCategoriaId: number | null = null;
  formCategoriaNombre = '';

  // Formulario Unidad de Medida
  editandoUnidadId: number | null = null;
  formUnidad = { nombre: '', abreviatura: '' };

  // Formulario Ubicacion
  editandoUbicacionId: number | null = null;
  formUbicacion = { codigo: '', nombre: '', tipo: 'ESTANTERIA' };

  // Formulario Proveedor
  editandoProveedorId: number | null = null;
  formProveedor = { ruc: '', razonSocial: '', contacto: '' };

  // Formulario Usuario (Sprint 01)
  formUsuario = {
    username: '',
    password: '',
    nombreCompleto: '',
    email: '',
    rolId: 0
  };

  // Formularios de Almacen (Ingresos, Salidas FIFO, Transferencias)
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
    private api: ApiService
  ) {}

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.cargarDatosIntranet();
    }
  }

  seleccionarDemoUser(username: string, pass: string): void {
    this.loginUser = username;
    this.loginPass = pass;
  }

  iniciarSesion(): void {
    this.limpiarMensajes();
    this.cargandoLogin = true;
    this.auth.login(this.loginUser.trim(), this.loginPass).subscribe({
      next: (res) => {
        this.cargandoLogin = false;
        this.notificarExito(`Bienvenido a la Intranet, ${res.nombreCompleto}`);
        this.cargarDatosIntranet();
      },
      error: (err) => {
        this.cargandoLogin = false;
        this.notificarError(err?.error?.mensaje || 'Credenciales incorrectas o servidor no disponible');
      }
    });
  }

  cerrarSesion(): void {
    this.auth.logout();
    this.limpiarMensajes();
  }

  cambiarVista(nuevaVista: VistaActiva): void {
    this.limpiarMensajes();
    this.vista.set(nuevaVista);
  }

  cargarDatosIntranet(): void {
    this.api.listarCategorias().subscribe({
      next: (data) => {
        this.categorias.set(data);
        if (data.length > 0 && !this.formArticulo.categoriaId) {
          this.formArticulo.categoriaId = data[0].idCategoria!;
        }
      }
    });

    this.api.listarUnidades().subscribe({
      next: (data) => {
        this.unidades.set(data);
        if (data.length > 0 && !this.formArticulo.unidadId) {
          this.formArticulo.unidadId = data[0].idUnidad!;
        }
      }
    });

    this.api.listarUbicaciones().subscribe({
      next: (data) => {
        this.ubicaciones.set(data);
        if (data.length > 0) {
          if (!this.formArticulo.ubicacionId) this.formArticulo.ubicacionId = data[0].id!;
          if (!this.formIngreso.ubicacionDestinoId) this.formIngreso.ubicacionDestinoId = data[0].id!;
          if (!this.formTransferencia.ubicacionOrigenId) this.formTransferencia.ubicacionOrigenId = data[0].id!;
          if (!this.formTransferencia.ubicacionDestinoId) {
            this.formTransferencia.ubicacionDestinoId = data[1 % data.length].id!;
          }
        }
      }
    });

    this.api.listarProveedores().subscribe({
      next: (data) => {
        this.proveedores.set(data);
        if (data.length > 0 && !this.formIngreso.proveedorId) {
          this.formIngreso.proveedorId = data[0].idProveedor!;
        }
      }
    });

    this.api.listarArticulos().subscribe({
      next: (data) => {
        this.articulos.set(data);
        if (data.length > 0) {
          if (!this.formIngreso.articuloId) this.formIngreso.articuloId = data[0].idArticulo!;
          if (!this.formSalida.articuloId) this.formSalida.articuloId = data[0].idArticulo!;
        }
      }
    });

    if (this.auth.hasPermiso('USUARIO_GESTIONAR')) {
      this.api.listarUsuarios().subscribe({ next: (d) => this.usuarios.set(d) });
    }

    if (this.auth.hasPermiso('ROL_GESTIONAR')) {
      this.api.listarRoles().subscribe({
        next: (d) => {
          this.roles.set(d);
          if (d.length > 0 && !this.formUsuario.rolId) {
            this.formUsuario.rolId = d[0].id!;
          }
        }
      });
    }

    this.api.listarLotesActivos().subscribe({
      next: (d) => {
        this.lotes.set(d);
        if (d.length > 0 && !this.formTransferencia.loteId) {
          this.formTransferencia.loteId = d[0].id;
          this.formTransferencia.ubicacionOrigenId = d[0].ubicacion?.id ?? 0;
        }
      }
    });

    if (this.auth.hasPermiso('KARDEX_VER')) {
      this.api.listarKardex().subscribe({ next: (d) => this.kardex.set(d) });
    }

    this.api.listarMovimientos().subscribe({ next: (d) => this.movimientos.set(d) });
  }

  // ==================== CRUD ARTICULOS (SPRINT 02) ====================
  guardarArticulo(): void {
    this.limpiarMensajes();
    const payload: Partial<Articulo> = {
      codigoInterno: this.formArticulo.codigoInterno.trim(),
      descripcion: this.formArticulo.descripcion.trim(),
      categoria: { idCategoria: Number(this.formArticulo.categoriaId), nombre: '' },
      unidadMedida: { idUnidad: Number(this.formArticulo.unidadId), nombre: '', abreviatura: '' },
      ubicacion: this.formArticulo.ubicacionId
        ? { id: Number(this.formArticulo.ubicacionId), nombre: '', codigo: '', tipo: '' }
        : null,
      stockMinimo: Number(this.formArticulo.stockMinimo),
      stockActual: Number(this.formArticulo.stockActual)
    };

    if (this.editandoArticuloId) {
      this.api.actualizarArticulo(this.editandoArticuloId, payload).subscribe({
        next: () => {
          this.notificarExito('Artículo actualizado correctamente en la Base de Datos.');
          this.cancelarEdicionArticulo();
          this.cargarDatosIntranet();
        },
        error: (err) => this.notificarError(err?.error?.mensaje || 'Error al actualizar el artículo')
      });
    } else {
      this.api.crearArticulo(payload).subscribe({
        next: () => {
          this.notificarExito('Nuevo artículo registrado exitosamente en la Base de Datos.');
          this.cancelarEdicionArticulo();
          this.cargarDatosIntranet();
        },
        error: (err) => this.notificarError(err?.error?.mensaje || 'Error al registrar el artículo')
      });
    }
  }

  prepararEdicionArticulo(art: Articulo): void {
    this.editandoArticuloId = art.idArticulo!;
    this.formArticulo = {
      codigoInterno: art.codigoInterno,
      descripcion: art.descripcion,
      categoriaId: art.categoria?.idCategoria ?? 0,
      unidadId: art.unidadMedida?.idUnidad ?? 0,
      ubicacionId: art.ubicacion?.id ?? 0,
      stockMinimo: art.stockMinimo,
      stockActual: art.stockActual
    };
  }

  cancelarEdicionArticulo(): void {
    this.editandoArticuloId = null;
    this.formArticulo = {
      codigoInterno: '',
      descripcion: '',
      categoriaId: this.categorias()[0]?.idCategoria ?? 0,
      unidadId: this.unidades()[0]?.idUnidad ?? 0,
      ubicacionId: this.ubicaciones()[0]?.id ?? 0,
      stockMinimo: 10,
      stockActual: 0
    };
  }

  eliminarArticulo(id: number): void {
    this.limpiarMensajes();
    this.api.eliminarArticulo(id).subscribe({
      next: () => {
        this.notificarExito('Artículo eliminado de la Base de Datos.');
        this.cargarDatosIntranet();
      },
      error: () =>
        this.notificarError(
          'No se puede eliminar un artículo con lotes o historial de Kardex asociado.'
        )
    });
  }

  // ==================== CRUD CATEGORIAS (SPRINT 02) ====================
  guardarCategoria(): void {
    this.limpiarMensajes();
    const nombre = this.formCategoriaNombre.trim();
    if (!nombre) return;

    const obs = this.editandoCategoriaId
      ? this.api.actualizarCategoria(this.editandoCategoriaId, { nombre })
      : this.api.crearCategoria({ nombre });

    obs.subscribe({
      next: () => {
        this.notificarExito(
          this.editandoCategoriaId ? 'Categoría actualizada.' : 'Categoría registrada en BD.'
        );
        this.editandoCategoriaId = null;
        this.formCategoriaNombre = '';
        this.cargarDatosIntranet();
      },
      error: (err) => this.notificarError(err?.error?.mensaje || 'Error al guardar categoría')
    });
  }

  editarCategoria(cat: Categoria): void {
    this.editandoCategoriaId = cat.idCategoria!;
    this.formCategoriaNombre = cat.nombre;
  }

  eliminarCategoria(id: number): void {
    this.limpiarMensajes();
    this.api.eliminarCategoria(id).subscribe({
      next: () => {
        this.notificarExito('Categoría eliminada.');
        this.cargarDatosIntranet();
      },
      error: () => this.notificarError('No se puede eliminar una categoría con artículos asociados.')
    });
  }

  // ==================== CRUD UNIDADES DE MEDIDA (SPRINT 02) ====================
  guardarUnidad(): void {
    this.limpiarMensajes();
    const payload = {
      nombre: this.formUnidad.nombre.trim(),
      abreviatura: this.formUnidad.abreviatura.trim().toUpperCase()
    };
    if (!payload.nombre || !payload.abreviatura) return;

    const obs = this.editandoUnidadId
      ? this.api.actualizarUnidad(this.editandoUnidadId, payload)
      : this.api.crearUnidad(payload);

    obs.subscribe({
      next: () => {
        this.notificarExito(
          this.editandoUnidadId ? 'Unidad de medida actualizada.' : 'Unidad de medida creada.'
        );
        this.editandoUnidadId = null;
        this.formUnidad = { nombre: '', abreviatura: '' };
        this.cargarDatosIntranet();
      },
      error: (err) => this.notificarError(err?.error?.mensaje || 'Error al guardar unidad')
    });
  }

  editarUnidad(u: UnidadMedida): void {
    this.editandoUnidadId = u.idUnidad!;
    this.formUnidad = { nombre: u.nombre, abreviatura: u.abreviatura };
  }

  eliminarUnidad(id: number): void {
    this.limpiarMensajes();
    this.api.eliminarUnidad(id).subscribe({
      next: () => {
        this.notificarExito('Unidad de medida eliminada.');
        this.cargarDatosIntranet();
      },
      error: () => this.notificarError('No se puede eliminar una unidad en uso por artículos.')
    });
  }

  // ==================== CRUD UBICACIONES (SPRINT 02) ====================
  guardarUbicacion(): void {
    this.limpiarMensajes();
    const payload = {
      codigo: this.formUbicacion.codigo.trim().toUpperCase(),
      nombre: this.formUbicacion.nombre.trim(),
      tipo: this.formUbicacion.tipo.trim().toUpperCase(),
      activo: true
    };
    if (!payload.codigo || !payload.nombre) return;

    const obs = this.editandoUbicacionId
      ? this.api.actualizarUbicacion(this.editandoUbicacionId, payload)
      : this.api.crearUbicacion(payload);

    obs.subscribe({
      next: () => {
        this.notificarExito(
          this.editandoUbicacionId ? 'Ubicación actualizada.' : 'Ubicación registrada en BD.'
        );
        this.editandoUbicacionId = null;
        this.formUbicacion = { codigo: '', nombre: '', tipo: 'ESTANTERIA' };
        this.cargarDatosIntranet();
      },
      error: (err) => this.notificarError(err?.error?.mensaje || 'Error al guardar ubicación')
    });
  }

  editarUbicacion(ub: Ubicacion): void {
    this.editandoUbicacionId = ub.id!;
    this.formUbicacion = { codigo: ub.codigo, nombre: ub.nombre, tipo: ub.tipo };
  }

  eliminarUbicacion(id: number): void {
    this.limpiarMensajes();
    this.api.eliminarUbicacion(id).subscribe({
      next: () => {
        this.notificarExito('Ubicación eliminada.');
        this.cargarDatosIntranet();
      },
      error: () => this.notificarError('No se puede eliminar una ubicación con stock o lotes.')
    });
  }

  // ==================== CRUD PROVEEDORES (SPRINT 02) ====================
  guardarProveedor(): void {
    this.limpiarMensajes();
    const payload = {
      ruc: this.formProveedor.ruc.trim(),
      razonSocial: this.formProveedor.razonSocial.trim(),
      contacto: this.formProveedor.contacto.trim()
    };

    const obs = this.editandoProveedorId
      ? this.api.actualizarProveedor(this.editandoProveedorId, payload)
      : this.api.crearProveedor(payload);

    obs.subscribe({
      next: () => {
        this.notificarExito(
          this.editandoProveedorId ? 'Proveedor actualizado.' : 'Proveedor registrado en BD.'
        );
        this.editandoProveedorId = null;
        this.formProveedor = { ruc: '', razonSocial: '', contacto: '' };
        this.cargarDatosIntranet();
      },
      error: (err) => this.notificarError(err?.error?.mensaje || 'Error al guardar proveedor')
    });
  }

  editarProveedor(p: Proveedor): void {
    this.editandoProveedorId = p.idProveedor!;
    this.formProveedor = {
      ruc: p.ruc,
      razonSocial: p.razonSocial,
      contacto: p.contacto
    };
  }

  eliminarProveedor(id: number): void {
    this.limpiarMensajes();
    this.api.eliminarProveedor(id).subscribe({
      next: () => {
        this.notificarExito('Proveedor eliminado.');
        this.cargarDatosIntranet();
      },
      error: () => this.notificarError('No se puede eliminar un proveedor con ingresos registrados.')
    });
  }

  // ==================== SEGURIDAD: USUARIOS Y ROLES (SPRINT 01) ====================
  guardarUsuario(): void {
    this.limpiarMensajes();
    const payload = {
      username: this.formUsuario.username.trim(),
      password: this.formUsuario.password,
      nombreCompleto: this.formUsuario.nombreCompleto.trim(),
      email: this.formUsuario.email.trim(),
      rolesIds: [Number(this.formUsuario.rolId)]
    };

    this.api.crearUsuario(payload).subscribe({
      next: (u) => {
        this.notificarExito(`Usuario "${u.username}" creado exitosamente.`);
        this.formUsuario = {
          username: '',
          password: '',
          nombreCompleto: '',
          email: '',
          rolId: this.roles()[0]?.id ?? 0
        };
        this.cargarDatosIntranet();
      },
      error: (err) => this.notificarError(err?.error?.mensaje || 'Error al registrar usuario')
    });
  }

  alternarEstadoUsuario(u: Usuario): void {
    this.limpiarMensajes();
    this.api.cambiarEstadoUsuario(u.id!, !u.activo).subscribe({
      next: (actualizado) => {
        this.notificarExito(
          `Estado de ${actualizado.username} actualizado a ${actualizado.activo ? 'ACTIVO' : 'INACTIVO'}.`
        );
        this.cargarDatosIntranet();
      },
      error: (err) => this.notificarError(err?.error?.mensaje || 'No se pudo cambiar el estado')
    });
  }

  // ==================== OPERACIONES DE ALMACEN (SPRINTS 03, 04, 05) ====================
  ejecutarIngreso(): void {
    this.limpiarMensajes();
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
          this.notificarExito(
            `Ingreso registrado: +${mov.cantidad} en lote ${mov.codigoLote} (${mov.articuloDescripcion}).`
          );
          this.formIngreso.codigoLote = '';
          this.cargarDatosIntranet();
        },
        error: (err) => this.notificarError(err?.error?.mensaje || 'Error al registrar ingreso')
      });
  }

  ejecutarSalidaFIFO(): void {
    this.limpiarMensajes();
    this.api
      .registrarSalidaFIFO({
        articuloId: Number(this.formSalida.articuloId),
        cantidadRequerida: Number(this.formSalida.cantidadRequerida),
        ordenProduccion: this.formSalida.ordenProduccion.trim()
      })
      .subscribe({
        next: (movs) => {
          this.notificarExito(
            `Salida FIFO atendida exitosamente desde ${movs.length} lote(s) hacia producción.`
          );
          this.cargarDatosIntranet();
        },
        error: (err) => this.notificarError(err?.error?.mensaje || 'Error al procesar salida FIFO')
      });
  }

  alCambiarLoteTransferencia(): void {
    const lote = this.lotes().find((l) => l.id === Number(this.formTransferencia.loteId));
    if (lote?.ubicacion?.id) {
      this.formTransferencia.ubicacionOrigenId = lote.ubicacion.id;
    }
  }

  ejecutarTransferencia(): void {
    this.limpiarMensajes();
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
          this.notificarExito(
            `Transferencia registrada: ${mov.cantidad} unidades hacia ${mov.ubicacionDestinoNombre}.`
          );
          this.cargarDatosIntranet();
        },
        error: (err) =>
          this.notificarError(err?.error?.mensaje || 'Error al registrar transferencia')
      });
  }

  private notificarExito(msg: string): void {
    this.mensajeExito.set(msg);
    this.mensajeError.set(null);
  }

  private notificarError(msg: string): void {
    this.mensajeError.set(msg);
    this.mensajeExito.set(null);
  }

  limpiarMensajes(): void {
    this.mensajeExito.set(null);
    this.mensajeError.set(null);
  }
}
