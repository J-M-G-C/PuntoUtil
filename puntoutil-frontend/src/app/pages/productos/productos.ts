import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
  stock: number;
  stockMinimo: number;
  categoriaId: number | null;
  categoriaNombre: string | null;
  activo: boolean;
}

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './productos.html',
  styleUrl: './productos.css'
})
export class ProductosPage implements OnInit {

  private readonly http = inject(HttpClient);

  productos = signal<Producto[]>([]);
  cargando = signal(true);
  error = signal('');
  busqueda = signal('');

  productosFiltrados = computed(() => {
    const texto = this.busqueda().toLowerCase().trim();

    return this.productos().filter(producto =>
      producto.codigo.toLowerCase().includes(texto) ||
      producto.nombre.toLowerCase().includes(texto) ||
      (producto.categoriaNombre ?? '')
        .toLowerCase().includes(texto)
    );
  });

  totalProductos = computed(() =>
    this.productos().length
  );

  totalUnidades = computed(() =>
    this.productos().reduce(
      (total, producto) => total + producto.stock,
      0
    )
  );

  productosStockBajo = computed(() =>
    this.productos().filter(
      producto => producto.stock <= producto.stockMinimo
    ).length
  );

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando.set(true);
    this.error.set('');

    this.http.get<Producto[]>('/api/productos')
      .subscribe({
        next: (datos) => {
          this.productos.set(datos);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set(
            'No se pudo conectar con el backend de PuntoÚtil.'
          );
          this.cargando.set(false);
        }
      });
  }

  buscar(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.busqueda.set(input.value);
  }
}
