
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import { HttpClient } from '@angular/common/http';

interface ProductoInventario {
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
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

  // Productos activos
  productosActivos = computed(() =>
    this.productos().filter(producto => producto.activo)
  );

  // Total de unidades disponibles
  totalUnidades = computed(() =>
    this.productosActivos().reduce(
      (total, producto) => total + producto.stock,
      0
    )
  );

  // Productos con stock bajo
  productosBajoMinimo = computed(() =>
    this.productosActivos().filter(
      producto => producto.stock <= producto.stockMinimo
    ).length
  );

  // Productos agotados
  productosAgotados = computed(() =>
    this.productosActivos().filter(
      producto => producto.stock === 0
    ).length
  );

  // Buscar y filtrar productos
  productosFiltrados = computed(() => {

    const texto = this.busqueda().toLowerCase().trim();

    return this.productosActivos().filter(producto => {

      const coincideBusqueda =
        producto.codigo.toLowerCase().includes(texto) ||
        producto.nombre.toLowerCase().includes(texto) ||
        (producto.categoriaNombre ?? '')
          .toLowerCase()
          .includes(texto);

      const cumpleFiltroStock =
        !this.soloStockBajo() ||
        producto.stock <= producto.stockMinimo;

      return coincideBusqueda && cumpleFiltroStock;
    });
  });

  // Cargar inventario al abrir la pantalla
  ngOnInit(): void {
    this.cargarInventario();
  }

  // Consultar productos desde Spring Boot
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

  // Buscar por nombre, codigo o categoria
  buscar(evento: Event): void {

    const input = evento.target as HTMLInputElement;

    this.busqueda.set(input.value);
  }

  // Mostrar solamente productos con stock bajo
  filtrarStockBajo(evento: Event): void {

    const input = evento.target as HTMLInputElement;

    this.soloStockBajo.set(input.checked);
  }

  // Descargar reporte de inventario en CSV
  descargarInventarioCsv(): void {

    const productos = this.productosFiltrados();

    if (productos.length === 0) {
      return;
    }

    // Preparar los valores para el archivo CSV
    const celda = (dato: string | number): string => {

      let valor = String(dato ?? '');

      // Evitar que Excel interprete texto como formula
      if (/^\s*[=+\-@]/.test(valor)) {
        valor = "'" + valor;
      }

      // Escapar comillas dobles
      return '"' + valor.replace(/"/g, '""') + '"';
    };

    // Encabezados y registros del reporte
    const filas: (string | number)[][] = [

      [
        'Código',
        'Producto',
        'Categoría',
        'Precio (S/)',
        'Stock actual',
        'Stock mínimo',
        'Estado'
      ],

      ...productos.map(producto => [

        producto.codigo,

        producto.nombre,

        producto.categoriaNombre ?? 'Sin categoría',

        producto.precio.toFixed(2),

        producto.stock,

        producto.stockMinimo,

        producto.stock === 0
          ? 'Agotado'
          : producto.stock <= producto.stockMinimo
            ? 'Stock bajo'
            : 'Disponible'

      ])
    ];

    // Generar contenido CSV con codificacion UTF-8
    const contenido =
      '\uFEFF' +
      filas
        .map(fila => fila.map(celda).join(';'))
        .join('\r\n');

    // Crear archivo descargable
    const archivo = new Blob(
      [contenido],
      {
        type: 'text/csv;charset=utf-8;'
      }
    );

    const url = URL.createObjectURL(archivo);

    const enlace = document.createElement('a');

    enlace.href = url;

    enlace.download = 'PuntoUtil_Inventario.csv';

    document.body.appendChild(enlace);

    enlace.click();

    enlace.remove();

    // Liberar el recurso tras iniciar la descarga
    setTimeout(() => {
      URL.revokeObjectURL(url);
    }, 1000);
  }
}
