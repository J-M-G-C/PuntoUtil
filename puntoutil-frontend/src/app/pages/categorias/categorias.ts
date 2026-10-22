
import { HttpClient } from '@angular/common/http';
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

interface Categoria {
  id: number;
  nombre: string;
  activo?: boolean;
}

@Component({
  selector: 'app-categorias',
  standalone: true,
  templateUrl: './categorias.html',
  styleUrl: './categorias.css'
})
export class CategoriasPage implements OnInit {

  private readonly http = inject(HttpClient);

  categorias = signal<Categoria[]>([]);
  cargando = signal(true);
  error = signal('');
  busqueda = signal('');

  categoriasFiltradas = computed(() => {
    const texto = this.busqueda().toLowerCase().trim();

    return this.categorias().filter(categoria =>
      categoria.nombre.toLowerCase().includes(texto)
    );
  });

categoriasActivas = computed(() =>
  this.categorias().filter(
    categoria => categoria.activo !== false
  ).length
);

  ngOnInit(): void {
    this.cargarCategorias();
  }

  cargarCategorias(): void {
    this.cargando.set(true);
    this.error.set('');

    this.http.get<Categoria[]>('/api/categorias')
      .subscribe({
        next: (datos) => {
          this.categorias.set(datos);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set(
            'No se pudieron cargar las categorías.'
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
