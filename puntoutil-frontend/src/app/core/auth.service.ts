import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { tap } from 'rxjs';
import { InicioSesion, Rol } from './modelos';

interface Sesion extends InicioSesion {
  expiraEn: number;
}

const CLAVE_SESION = 'puntoutil.sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sesion = signal<Sesion | null>(this.recuperarSesion());

  readonly usuario = computed(() => this.sesion()?.usuario ?? '');
  readonly nombre = computed(() => this.sesion()?.nombre ?? '');
  readonly rol = computed(() => this.sesion()?.rol ?? null);
  readonly autenticado = computed(() => Boolean(this.token()));

  login(usuario: string, contrasena: string) {
    return this.http.post<InicioSesion>('/api/auth/login', { usuario, contrasena }).pipe(
      tap((respuesta) => {
        const sesion: Sesion = {
          ...respuesta,
          expiraEn: Date.now() + respuesta.expiraEnSegundos * 1000,
        };
        sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
        this.sesion.set(sesion);
      }),
    );
  }

  token(): string | null {
    const sesion = this.sesion();
    return sesion && sesion.expiraEn > Date.now() ? sesion.token : null;
  }

  puede(...roles: Rol[]): boolean {
    return Boolean(this.token()) && roles.includes(this.rol() as Rol);
  }

  cerrarSesion(): void {
    sessionStorage.removeItem(CLAVE_SESION);
    this.sesion.set(null);
  }

  private recuperarSesion(): Sesion | null {
    if (typeof sessionStorage === 'undefined') {
      return null;
    }
    try {
      const valor = sessionStorage.getItem(CLAVE_SESION);
      const sesion = valor ? (JSON.parse(valor) as Sesion) : null;
      if (sesion && sesion.expiraEn > Date.now()) {
        return sesion;
      }
    } catch {
      sessionStorage.removeItem(CLAVE_SESION);
    }
    sessionStorage.removeItem(CLAVE_SESION);
    return null;
  }
}
