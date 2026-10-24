export type Rol = 'ADMINISTRADOR' | 'VENDEDOR' | 'CLIENTE';
export type EstadoPedido = 'PENDIENTE' | 'LISTO' | 'ENTREGADO' | 'CANCELADO';

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
  stock: number;
  stockMinimo: number;
  categoriaNombre: string | null;
  activo: boolean;
}

export interface Usuario {
  id: number;
  usuario: string;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
}

export interface InicioSesion {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  usuario: string;
  nombre: string;
  rol: Rol;
}

export interface DetalleVenta {
  id: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  producto: Producto;
}

export interface Venta {
  id: number;
  fecha: string;
  total: number;
  detalles: DetalleVenta[];
}

export interface PedidoDetalle {
  id: number;
  productoId: number;
  codigoProducto: string;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  codigoSeguimiento: string;
  nombreCliente: string;
  dni: string | null;
  telefono: string | null;
  correo: string | null;
  clienteUsuario: string | null;
  estado: EstadoPedido;
  total: number;
  fechaCreacion: string;
  fechaActualizacion: string | null;
  fechaEntrega: string | null;
  observaciones: string | null;
  detalles: PedidoDetalle[];
}

export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}
