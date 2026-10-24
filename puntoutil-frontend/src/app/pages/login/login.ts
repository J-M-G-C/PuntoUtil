import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { mensajeError } from '../../core/errores';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  usuario = '';
  contrasena = '';
  readonly enviando = signal(false);
  readonly error = signal('');

  ingresar(): void {
    if (!this.usuario.trim() || !this.contrasena || this.enviando()) {
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    this.auth.login(this.usuario.trim(), this.contrasena).subscribe({
      next: () => {
        this.enviando.set(false);
        const retorno = this.route.snapshot.queryParamMap.get('retorno');
        const destino =
          retorno?.startsWith('/') && !retorno.startsWith('//') ? retorno : '/productos';
        this.router.navigateByUrl(destino);
      },
      error: (error) => {
        this.error.set(mensajeError(error, 'No se pudo iniciar sesión.'));
        this.enviando.set(false);
      },
    });
  }
}
