import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  cargando = false;
  mensajeError = '';

  formulario = this.fb.nonNullable.group({
    usuario: ['', Validators.required],
    contrasena: ['', Validators.required]
  });

  iniciarSesion(): void {

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando = true;
    this.mensajeError = '';

    this.authService.login(
      this.formulario.getRawValue()
    ).subscribe({

      next: () => {
        this.cargando = false;
        this.router.navigate(['/productos']);
      },

      error: (error) => {
        this.cargando = false;

        if (error.status === 401) {
          this.mensajeError = 'Usuario o contraseña incorrectos.';
        } else if (error.status === 0) {
          this.mensajeError = 'No se pudo conectar con el servidor.';
        } else {
          this.mensajeError = 'No se pudo iniciar sesión. Inténtalo nuevamente.';
        }
      }

    });

  }
}