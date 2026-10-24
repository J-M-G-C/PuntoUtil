import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { descargarArchivo, mensajeError } from '../../core/errores';
import { EstadoPedido, ItemCarrito, Pedido, Producto } from '../../core/modelos';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css',
})
export class PedidosPage implements OnInit {
  private readonly http = inject(HttpClient);
  readonly auth = inject(AuthService);

  readonly productos = signal<Producto[]>([]);
  readonly pedidos = signal<Pedido[]>([]);
  readonly carrito = signal<ItemCarrito[]>([]);
  readonly cargando = signal(true);
  readonly enviando = signal(false);
  readonly procesandoId = signal<number | null>(null);
  readonly error = signal('');
  readonly exito = signal('');
  readonly ultimoCodigo = signal('');

  nombreCliente = '';
  dni = '';
  telefono = '';
  correo = '';
  observaciones = '';
  productoId: number | null = null;
  cantidad = 1;
  readonly filtroEstado = signal('');

  readonly disponibles = computed(() =>
    this.productos().filter((producto) => producto.activo && producto.stock > 0),
  );
  readonly total = computed(() =>
    this.carrito().reduce((suma, item) => suma + item.producto.precio * item.cantidad, 0),
  );
  readonly pendientes = computed(
    () => this.pedidos().filter((pedido) => pedido.estado === 'PENDIENTE').length,
  );
  readonly pedidosFiltrados = computed(() =>
    this.filtroEstado()
      ? this.pedidos().filter((pedido) => pedido.estado === this.filtroEstado())
      : this.pedidos(),
  );

  ngOnInit(): void {
    this.nombreCliente = this.auth.nombre();
    this.actualizar();
  }

  actualizar(): void {
    this.cargando.set(true);
    this.http.get<Producto[]>('/api/productos').subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.cargando.set(false);
      },
      error: (error) => {
        this.error.set(mensajeError(error, 'No se pudieron cargar los productos.'));
        this.cargando.set(false);
      },
    });
    this.http.get<Pedido[]>('/api/pedidos').subscribe({
      next: (pedidos) => this.pedidos.set(pedidos.sort((a, b) => b.id - a.id)),
      error: (error) =>
        this.error.set(mensajeError(error, 'No se pudieron consultar los pedidos.')),
    });
  }

  agregar(): void {
    this.error.set('');
    const producto = this.disponibles().find((item) => item.id === Number(this.productoId));
    const cantidad = Number(this.cantidad);
    if (!producto || !Number.isInteger(cantidad) || cantidad <= 0) {
      this.error.set('Selecciona un producto e ingresa una cantidad válida.');
      return;
    }
    const actual = this.carrito().find((item) => item.producto.id === producto.id)?.cantidad ?? 0;
    if (actual + cantidad > producto.stock) {
      this.error.set(
        `No hay suficiente stock de ${producto.nombre}. Disponible: ${producto.stock}.`,
      );
      return;
    }
    this.carrito.update((items) =>
      items.some((item) => item.producto.id === producto.id)
        ? items.map((item) =>
            item.producto.id === producto.id
              ? { ...item, cantidad: item.cantidad + cantidad }
              : item,
          )
        : [...items, { producto, cantidad }],
    );
    this.productoId = null;
    this.cantidad = 1;
  }

  quitar(id: number): void {
    this.carrito.update((items) => items.filter((item) => item.producto.id !== id));
  }

  crear(): void {
    if (this.enviando() || !this.nombreCliente.trim() || !this.carrito().length) {
      this.error.set('Indica el nombre del cliente y agrega al menos un producto.');
      return;
    }
    this.enviando.set(true);
    this.error.set('');
    this.exito.set('');
    this.ultimoCodigo.set('');
    const datos = {
      nombreCliente: this.nombreCliente.trim(),
      dni: this.dni.trim() || null,
      telefono: this.telefono.trim() || null,
      correo: this.correo.trim() || null,
      observaciones: this.observaciones.trim() || null,
      detalles: this.carrito().map((item) => ({
        productoId: item.producto.id,
        cantidad: item.cantidad,
      })),
    };
    this.http.post<Pedido>('/api/pedidos', datos).subscribe({
      next: (pedido) => {
        this.enviando.set(false);
        this.ultimoCodigo.set(pedido.codigoSeguimiento);
        this.exito.set(
          `Pedido ${pedido.codigoSeguimiento} registrado. El stock se descontará al entregar.`,
        );
        this.carrito.set([]);
        this.dni = '';
        this.telefono = '';
        this.correo = '';
        this.observaciones = '';
        this.actualizar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeError(error, 'No se pudo registrar el pedido.'));
      },
    });
  }

  cambiarEstado(pedido: Pedido, estado: EstadoPedido): void {
    if (this.procesandoId() !== null) {
      return;
    }
    const descripcion =
      estado === 'ENTREGADO'
        ? 'Confirmar la entrega descontará el stock.'
        : estado === 'CANCELADO'
          ? 'Este pedido quedará cancelado.'
          : 'El pedido quedará listo para recojo.';
    if (!window.confirm(`${descripcion} ¿Deseas continuar?`)) {
      return;
    }
    this.procesandoId.set(pedido.id);
    this.error.set('');
    this.exito.set('');
    const endpoint =
      estado === 'ENTREGADO'
        ? `${pedido.id}/entregar`
        : estado === 'CANCELADO'
          ? `${pedido.id}/cancelar`
          : `${pedido.id}/estado`;
    const body = estado === 'LISTO' ? { estado } : {};
    this.http.patch<Pedido>(`/api/pedidos/${endpoint}`, body).subscribe({
      next: (actualizado) => {
        this.procesandoId.set(null);
        this.exito.set(
          `El pedido ${actualizado.codigoSeguimiento} ahora está ${actualizado.estado}.`,
        );
        this.actualizar();
      },
      error: (error) => {
        this.procesandoId.set(null);
        this.error.set(mensajeError(error, 'No se pudo actualizar el estado del pedido.'));
      },
    });
  }

  descargarPdf(pedido: Pedido): void {
    this.error.set('');
    this.http.get(`/api/pedidos/${pedido.id}/pdf`, { responseType: 'blob' }).subscribe({
      next: (archivo) =>
        descargarArchivo(archivo, `PuntoUtil_Pedido_${pedido.codigoSeguimiento}.pdf`),
      error: (error) => this.error.set(mensajeError(error, 'No se pudo descargar el PDF.')),
    });
  }
}
