import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Usuario {
  id: number;
  usuario: string;
  nombre: string;
  email: string;
  rol: string;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UsuariosService {

  private http = inject(HttpClient);

  private apiUrl = '/api/usuarios';

  listarUsuarios(): Observable<Usuario[]> {

    const token = sessionStorage.getItem('token');

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token ?? ''}`
    });

    return this.http.get<Usuario[]>(this.apiUrl, { headers });
  }
}