import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('Autenticación PuntoÚtil', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('inicia sesión y conserva el rol del backend', () => {
    auth.login('admin', 'contrasena-segura').subscribe();
    const solicitud = http.expectOne('/api/auth/login');
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual({ usuario: 'admin', contrasena: 'contrasena-segura' });
    solicitud.flush({
      token: 'token-de-prueba',
      tipo: 'Bearer',
      expiraEnSegundos: 3600,
      usuario: 'admin',
      nombre: 'Administrador',
      rol: 'ADMINISTRADOR',
    });
    expect(auth.autenticado()).toBe(true);
    expect(auth.puede('ADMINISTRADOR')).toBe(true);
    expect(auth.puede('CLIENTE')).toBe(false);
  });

  it('envía el token a endpoints protegidos y lo elimina al salir', () => {
    auth.login('admin', 'contrasena-segura').subscribe();
    http.expectOne('/api/auth/login').flush({
      token: 'jwt-prueba',
      tipo: 'Bearer',
      expiraEnSegundos: 3600,
      usuario: 'admin',
      nombre: 'Administrador',
      rol: 'ADMINISTRADOR',
    });

    const cliente = TestBed.inject(HttpClient);
    cliente.get('/api/ventas').subscribe();
    const solicitud = http.expectOne('/api/ventas');
    expect(solicitud.request.headers.get('Authorization')).toBe('Bearer jwt-prueba');
    solicitud.flush([]);

    auth.cerrarSesion();
    expect(auth.token()).toBeNull();
    expect(auth.autenticado()).toBe(false);
  });
});
