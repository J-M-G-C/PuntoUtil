import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { Rol, Usuario } from '../../core/modelos';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class UsuariosPage implements OnInit {
  private readonly http = inject(HttpClient);

  readonly usuarios = signal<Usuario[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mostrandoFormulario = signal(false);
  readonly editando = signal<number | null>(null);
  readonly error = signal('');
  readonly exito = signal('');
  readonly busqueda = signal('');

  readonly usuariosFiltrados = computed(() =>
    this.usuarios().filter((item) =>
      `${item.usuario} ${item.nombre} ${item.email}`
        .toLowerCase()
        .includes(this.busqueda().toLowerCase().trim()),
    ),
  );
  readonly activos = computed(() => this.usuarios().filter((item) => item.activo).length);

  usuario = '';
  nombre = '';
  email = '';
  contrasena = '';
  rol: Rol = 'VENDEDOR';
  activo = true;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.http.get<Usuario[]>('/api/usuarios').subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: (error) => {
        this.error.set(mensajeError(error, 'No se pudieron consultar los usuarios.'));
        this.cargando.set(false);
      },
    });
  }

  nuevo(): void {
    this.editando.set(null);
    this.usuario = '';
    this.nombre = '';
    this.email = '';
    this.contrasena = '';
    this.rol = 'VENDEDOR';
    this.activo = true;
    this.error.set('');
    this.mostrandoFormulario.set(true);
  }

  editar(usuario: Usuario): void {
    this.editando.set(usuario.id);
    this.usuario = usuario.usuario;
    this.nombre = usuario.nombre;
    this.email = usuario.email;
    this.contrasena = '';
    this.rol = usuario.rol;
    this.activo = usuario.activo;
    this.error.set('');
    this.mostrandoFormulario.set(true);
  }

  cancelar(): void {
    this.mostrandoFormulario.set(false);
    this.editando.set(null);
  }

  guardar(): void {
    if (this.guardando()) {
      return;
    }
    if (
      !this.nombre.trim() ||
      !this.email.trim() ||
      (!this.editando() && (!this.usuario.trim() || this.contrasena.length < 8))
    ) {
      this.error.set('Completa los campos obligatorios y una contraseña de al menos 8 caracteres.');
      return;
    }
    if (this.contrasena && this.contrasena.length < 8) {
      this.error.set('La nueva contraseña debe tener al menos 8 caracteres.');
      return;
    }
    this.guardando.set(true);
    this.error.set('');
    this.exito.set('');
    const id = this.editando();
    const datos =
      id === null
        ? {
            usuario: this.usuario.trim(),
            nombre: this.nombre.trim(),
            email: this.email.trim(),
            contrasena: this.contrasena,
            rol: this.rol,
          }
        : {
            nombre: this.nombre.trim(),
            email: this.email.trim(),
            rol: this.rol,
            activo: this.activo,
            contrasena: this.contrasena || null,
          };
    const solicitud =
      id === null
        ? this.http.post<Usuario>('/api/usuarios', datos)
        : this.http.put<Usuario>(`/api/usuarios/${id}`, datos);
    solicitud.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cancelar();
        this.exito.set(
          id === null ? 'Usuario creado correctamente.' : 'Usuario actualizado correctamente.',
        );
        this.cargar();
      },
      error: (error) => {
        this.guardando.set(false);
        this.error.set(mensajeError(error, 'No se pudieron guardar los datos del usuario.'));
      },
    });
  }

  desactivar(usuario: Usuario): void {
    if (!usuario.activo || !window.confirm(`¿Desactivar al usuario ${usuario.usuario}?`)) {
      return;
    }
    this.error.set('');
    this.exito.set('');
    this.http.delete<void>(`/api/usuarios/${usuario.id}`).subscribe({
      next: () => {
        this.exito.set(`El usuario ${usuario.usuario} fue desactivado.`);
        this.cargar();
      },
      error: (error) => this.error.set(mensajeError(error, 'No se pudo desactivar el usuario.')),
    });
  }
}
