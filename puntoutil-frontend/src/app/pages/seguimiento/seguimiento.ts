import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { descargarArchivo, mensajeError } from '../../core/errores';
import { EstadoPedido, Pedido } from '../../core/modelos';

@Component({
  selector: 'app-seguimiento',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './seguimiento.html',
  styleUrl: './seguimiento.css',
})
export class SeguimientoPage implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly ruta = inject(ActivatedRoute);

  codigo = '';
  readonly pedido = signal<Pedido | null>(null);
  readonly buscando = signal(false);
  readonly error = signal('');
  readonly estados: EstadoPedido[] = ['PENDIENTE', 'LISTO', 'ENTREGADO'];

  ngOnInit(): void {
    this.ruta.queryParamMap.subscribe((parametros) => {
      const codigo = parametros.get('codigo');
      if (codigo) {
        this.codigo = codigo;
        this.consultar();
      }
    });
  }

  consultar(): void {
    const codigo = this.codigo.trim().toUpperCase();
    if (!codigo) {
      this.error.set('Ingresa un código de seguimiento.');
      this.pedido.set(null);
      return;
    }
    this.buscando.set(true);
    this.error.set('');
    this.pedido.set(null);
    this.http.get<Pedido>(`/api/pedidos/seguimiento/${encodeURIComponent(codigo)}`).subscribe({
      next: (pedido) => {
        this.pedido.set(pedido);
        this.buscando.set(false);
      },
      error: (error) => {
        this.error.set(mensajeError(error, 'No se encontró un pedido con ese código.'));
        this.buscando.set(false);
      },
    });
  }

  completado(estado: EstadoPedido, actual: EstadoPedido): boolean {
    if (actual === 'CANCELADO') {
      return false;
    }
    return this.estados.indexOf(estado) <= this.estados.indexOf(actual);
  }

  descargarPdf(): void {
    const pedido = this.pedido();
    if (!pedido) {
      return;
    }
    this.error.set('');
    this.http
      .get(`/api/pedidos/seguimiento/${encodeURIComponent(pedido.codigoSeguimiento)}/pdf`, {
        responseType: 'blob',
      })
      .subscribe({
        next: (archivo) =>
          descargarArchivo(archivo, `PuntoUtil_Pedido_${pedido.codigoSeguimiento}.pdf`),
        error: (error) => this.error.set(mensajeError(error, 'No se pudo descargar el PDF.')),
      });
  }
}
