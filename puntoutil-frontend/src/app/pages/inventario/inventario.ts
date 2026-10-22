import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface ProductoInventario {
  id: number;
  codigo: string;
  nombre: string;
  stock: number;
  stockMinimo: number;
  categoriaNombre: string | null;
  activo: boolean;
}

@Component({
  selector: 'app-inventario',
  standalone: true,
  templateUrl: './inventario.html',
  styleUrl: './inventario.css'
})
export class InventarioPage implements OnInit {

  private readonly http = inject(HttpClient);

  productos = signal<ProductoInventario[]>([]);
  cargando = signal(true);
  error = signal('');
  busqueda = signal('');
  soloStockBajo = signal(false);

  productosActivos = computed(() =>
    this.productos().filter(p => p.activo)
  );

  totalUnidades = computed(() =>
    this.productosActivos().reduce(
      (suma, producto) => suma + producto.stock,
      0
    )
  );

  productosBajoMinimo = computed(() =>
    this.productosActivos().filter(
      p => p.stock <= p.stockMinimo
    ).length
  );

  productosAgotados = computed(() =>
    this.productosActivos().filter(
      p => p.stock === 0
    ).length
  );

  productosFiltrados = computed(() => {
    const texto = this.busqueda().toLowerCase().trim();

    return this.productosActivos().filter(p => {
      const coincide =
        p.codigo.toLowerCase().includes(texto) ||
        p.nombre.toLowerCase().includes(texto) ||
        (p.categoriaNombre ?? '')
          .toLowerCase().includes(texto);

      const cumpleStock =
        !this.soloStockBajo() ||
        p.stock <= p.stockMinimo;

      return coincide && cumpleStock;
    });
  });

  ngOnInit(): void {
    this.cargarInventario();
  }

  cargarInventario(): void {
    this.cargando.set(true);
    this.error.set('');

    this.http.get<ProductoInventario[]>('/api/productos')
      .subscribe({
        next: (datos) => {
          this.productos.set(datos);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set(
            'No se pudo consultar el inventario.'
          );
          this.cargando.set(false);
        }
      });
  }

  buscar(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.busqueda.set(input.value);
  }

  filtrarStockBajo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.soloStockBajo.set(input.checked);
  }
}
