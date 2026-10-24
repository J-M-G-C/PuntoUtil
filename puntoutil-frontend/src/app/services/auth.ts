import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  usuario: string;
  contrasena: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  usuario: string;
  nombre: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  private readonly apiUrl = '/api/auth';

  login(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      datos
    ).pipe(
      tap(respuesta => {
        sessionStorage.setItem('token', respuesta.token);
        sessionStorage.setItem('usuario', respuesta.usuario);
        sessionStorage.setItem('nombre', respuesta.nombre);
        sessionStorage.setItem('rol', respuesta.rol);
      })
    );
  }

  obtenerToken(): string | null {
    return sessionStorage.getItem('token');
  }

  obtenerRol(): string | null {
    return sessionStorage.getItem('rol');
  }

  estaAutenticado(): boolean {
    return !!this.obtenerToken();
  }

  cerrarSesion(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('usuario');
    sessionStorage.removeItem('nombre');
    sessionStorage.removeItem('rol');
  }
}