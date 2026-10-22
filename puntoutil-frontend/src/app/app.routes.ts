import { Routes } from '@angular/router';
import { ProductosPage } from './pages/productos/productos';
import { CategoriasPage } from './pages/categorias/categorias';

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
    path: 'categorias',
    component: CategoriasPage,
    title: 'Categorías | PuntoÚtil'
  },
  {
    path: '**',
    redirectTo: 'productos'
  }
];
