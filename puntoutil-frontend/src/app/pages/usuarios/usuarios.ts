import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuariosService, Usuario } from '../../services/usuarios.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {

  private usuariosService = inject(UsuariosService);
  private cdr = inject(ChangeDetectorRef);

  usuarios: Usuario[] = [];

  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
  this.cargando = true;
  this.error = '';

  this.usuariosService.listarUsuarios().subscribe({
    next: (data) => {
      this.usuarios = data;
      this.cargando = false;

      console.log('Usuarios obtenidos:', data);

      // Actualizar la interfaz de Angular
      this.cdr.detectChanges();
    },
    error: (err) => {
      console.error('Error al obtener usuarios:', err);

      this.error = 'No se pudieron cargar los usuarios.';
      this.cargando = false;

      // Actualizar la interfaz cuando ocurre un error
      this.cdr.detectChanges();
    }
  });
}

  get totalUsuarios(): number {
    return this.usuarios.length;
  }

  get usuariosActivos(): number {
    return this.usuarios.filter(usuario => usuario.activo).length;
  }

  get totalAdministradores(): number {
    return this.usuarios.filter(
      usuario => usuario.rol === 'ADMINISTRADOR'
    ).length;
  }
}