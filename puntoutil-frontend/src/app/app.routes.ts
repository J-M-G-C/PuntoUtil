import { Routes } from '@angular/router';
import { ProductosPage } from './pages/productos/productos';
import { CategoriasPage } from './pages/categorias/categorias';
import { InventarioPage } from './pages/inventario/inventario';
import { VentasPage } from './pages/ventas/ventas';
import { PedidosPage } from './pages/pedidos/pedidos';
import { SeguimientoPage } from './pages/seguimiento/seguimiento';
import { UsuariosPage } from './pages/usuarios/usuarios';
import { LoginPage } from './pages/login/login';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'productos',
    pathMatch: 'full',
  },
  {
    path: 'productos',
    component: ProductosPage,
    title: 'Productos | PuntoÚtil',
  },
  {
    path: 'categorias',
    component: CategoriasPage,
    title: 'Categorías | PuntoÚtil',
  },
  {
    path: 'inventario',
    component: InventarioPage,
    title: 'Inventario | PuntoÚtil',
  },
  {
    path: 'login',
    component: LoginPage,
    title: 'Ingresar | PuntoÚtil',
  },
  {
    path: 'ventas',
    component: VentasPage,
    canActivate: [authGuard],
    data: { roles: ['ADMINISTRADOR', 'VENDEDOR'] },
    title: 'Ventas | PuntoÚtil',
  },
  {
    path: 'pedidos',
    component: PedidosPage,
    canActivate: [authGuard],
    data: { roles: ['ADMINISTRADOR', 'VENDEDOR', 'CLIENTE'] },
    title: 'Pedidos | PuntoÚtil',
  },
  {
    path: 'seguimiento',
    component: SeguimientoPage,
    canActivate: [authGuard],
    data: { roles: ['ADMINISTRADOR', 'VENDEDOR', 'CLIENTE'] },
    title: 'Seguimiento | PuntoÚtil',
  },
  {
    path: 'usuarios',
    component: UsuariosPage,
    canActivate: [authGuard],
    data: { roles: ['ADMINISTRADOR'] },
    title: 'Usuarios | PuntoÚtil',
  },
  {
    path: '**',
    redirectTo: 'productos',
  },
];
