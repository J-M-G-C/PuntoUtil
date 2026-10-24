import { HttpErrorResponse } from '@angular/common/http';

export function mensajeError(error: unknown, predeterminado: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return predeterminado;
  }
  if (error.status === 0) {
    return 'No se pudo conectar con Spring Boot. Comprueba que el backend esté encendido.';
  }
  if (error.status === 401) {
    return 'La sesión terminó o las credenciales no son correctas. Inicia sesión nuevamente.';
  }
  if (error.status === 403) {
    return 'Tu usuario no tiene permisos para realizar esta operación.';
  }
  if (typeof error.error === 'string' && error.error.trim()) {
    return error.error;
  }
  if (typeof error.error?.detail === 'string') {
    return error.error.detail;
  }
  if (typeof error.error?.message === 'string') {
    return error.error.message;
  }
  return predeterminado;
}

export function descargarArchivo(contenido: Blob, nombre: string): void {
  const url = URL.createObjectURL(contenido);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  enlace.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
