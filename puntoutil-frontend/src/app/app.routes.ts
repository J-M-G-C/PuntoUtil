import { Routes } from '@angular/router';
import { ProductosPage } from './pages/productos/productos';
import { CategoriasPage } from './pages/categorias/categorias';
import { InventarioPage } from './pages/inventario/inventario';

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
  path: 'inventario',
  component: InventarioPage,
  title: 'Inventario | PuntoÚtil'
},
  {
    path: '**',
    redirectTo: 'productos'
  }
];
