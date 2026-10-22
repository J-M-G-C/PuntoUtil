import { Routes } from '@angular/router';
import { ProductosPage } from './pages/productos/productos';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'productos',
    pathMatch: 'full'
  },
  {
    path: 'productos',
    component: ProductosPage,
    title: 'Productos | PuntoÚtil'
  },
  {
    path: '**',
    redirectTo: 'productos'
  }
];
