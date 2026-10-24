import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { ItemCarrito, Producto, Venta } from '../../core/modelos';

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ventas.html',
  styleUrl: './ventas.css',
})
export class VentasPage implements OnInit {
  private readonly http = inject(HttpClient);

  readonly productos = signal<Producto[]>([]);
  readonly ventas = signal<Venta[]>([]);
  readonly carrito = signal<ItemCarrito[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly exito = signal('');

  productoId: number | null = null;
  cantidad = 1;

  readonly disponibles = computed(() =>
    this.productos().filter((producto) => producto.activo && producto.stock > 0),
  );
  readonly total = computed(() =>
    this.carrito().reduce((suma, item) => suma + item.producto.precio * item.cantidad, 0),
  );
  readonly unidades = computed(() =>
    this.carrito().reduce((suma, item) => suma + item.cantidad, 0),
  );
  readonly totalVendido = computed(() =>
    this.ventas().reduce((suma, venta) => suma + venta.total, 0),
  );

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
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
    this.http.get<Venta[]>('/api/ventas').subscribe({
      next: (ventas) => this.ventas.set(ventas.sort((a, b) => b.id - a.id)),
      error: (error) =>
        this.error.set(mensajeError(error, 'No se pudo consultar el historial de ventas.')),
    });
  }

  agregar(): void {
    this.error.set('');
    this.exito.set('');
    const producto = this.disponibles().find((item) => item.id === Number(this.productoId));
    const cantidad = Number(this.cantidad);
    if (!producto || !Number.isInteger(cantidad) || cantidad <= 0) {
      this.error.set('Selecciona un producto e ingresa una cantidad válida.');
      return;
    }
    const actual = this.carrito().find((item) => item.producto.id === producto.id)?.cantidad ?? 0;
    if (actual + cantidad > producto.stock) {
      this.error.set(
        `Stock insuficiente para ${producto.nombre}. Solo hay ${producto.stock} unidades.`,
      );
      return;
    }
    this.carrito.update((carrito) => {
      const existe = carrito.some((item) => item.producto.id === producto.id);
      return existe
        ? carrito.map((item) =>
            item.producto.id === producto.id
              ? { ...item, cantidad: item.cantidad + cantidad }
              : item,
          )
        : [...carrito, { producto, cantidad }];
    });
    this.productoId = null;
    this.cantidad = 1;
  }

  quitar(id: number): void {
    this.carrito.update((carrito) => carrito.filter((item) => item.producto.id !== id));
  }

  registrar(): void {
    if (!this.carrito().length || this.guardando()) {
      return;
    }
    this.guardando.set(true);
    this.error.set('');
    this.exito.set('');
    const detalles = this.carrito().map((item) => ({
      productoId: item.producto.id,
      cantidad: item.cantidad,
    }));
    this.http.post<Venta>('/api/ventas', { detalles }).subscribe({
      next: (venta) => {
        this.guardando.set(false);
        this.carrito.set([]);
        this.exito.set(
          `Venta N.° ${venta.id} registrada por S/ ${Number(venta.total).toFixed(2)}.`,
        );
        this.cargarDatos();
      },
      error: (error) => {
        this.guardando.set(false);
        this.error.set(mensajeError(error, 'No se pudo registrar la venta.'));
      },
    });
  }
}
