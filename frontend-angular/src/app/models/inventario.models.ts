export interface Permiso {
  id?: number;
  codigo: string;
  nombre: string;
  modulo: string;
  ruta: string | null;
  tipo: string;
  mostrarEnMenu: boolean;
  orden: number;
}

export interface Rol {
  id?: number;
  nombre: string;
  descripcion: string;
  activo?: boolean;
  permisos?: Permiso[];
}

export interface Usuario {
  id?: number;
  username: string;
  nombreCompleto: string;
  email: string;
  activo: boolean;
  fechaCreacion?: string;
  roles?: Rol[];
}

export interface LoginResponse {
  token: string;
  tipo: string;
  username: string;
  nombreCompleto: string;
  roles: string[];
  permisos: Permiso[];
}

export interface Categoria {
  idCategoria?: number;
  nombre: string;
}

export interface UnidadMedida {
  idUnidad?: number;
  nombre: string;
  abreviatura: string;
}

export interface Ubicacion {
  id?: number;
  nombre: string;
  codigo: string;
  tipo: string;
  activo?: boolean;
}

export interface Proveedor {
  idProveedor?: number;
  razonSocial: string;
  ruc: string;
  contacto: string;
}

export interface Articulo {
  idArticulo?: number;
  codigoInterno: string;
  descripcion: string;
  categoria?: Categoria | null;
  unidadMedida?: UnidadMedida | null;
  ubicacion?: Ubicacion | null;
  stockMinimo: number;
  stockActual: number;
}

export interface Lote {
  id: number;
  codigoLote: string;
  articulo?: Articulo | null;
  ubicacion?: Ubicacion | null;
  cantidadDisponible: number;
  fechaIngreso: string;
  fechaVencimiento?: string | null;
  activo: boolean;
}

export interface MovimientoResponse {
  id: number;
  tipoMovimiento: string;
  cantidad: number;
  fechaMovimiento: string;
  loteId: number;
  codigoLote: string;
  articuloId: number;
  articuloCodigo: string;
  articuloDescripcion: string;
  ubicacionOrigenId?: number | null;
  ubicacionOrigenNombre?: string | null;
  ubicacionDestinoId?: number | null;
  ubicacionDestinoNombre?: string | null;
  observacion?: string | null;
}

export interface Kardex {
  idKardex: number;
  fechaMovimiento: string;
  tipoMovimiento: string;
  referenciaDocumento: string;
  articulo?: Articulo | null;
  cantidad: number;
  saldoResultante: number;
  usuario?: Usuario | null;
}
