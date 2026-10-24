import { Routes } from '@angular/router';

import { ProductosPage } from './pages/productos/productos';
import { CategoriasPage } from './pages/categorias/categorias';
import { InventarioPage } from './pages/inventario/inventario';
import { Login } from './pages/login/login';
import { Usuarios } from './pages/usuarios/usuarios';

export const routes: Routes = [

  // Pantalla inicial
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  // Inicio de sesión
  {
    path: 'login',
    component: Login,
    title: 'Iniciar sesión | PuntoÚtil'
  },

  // Productos
  {
    path: 'productos',
    component: ProductosPage,
    title: 'Productos | PuntoÚtil'
  },

  // Categorías
  {
    path: 'categorias',
    component: CategoriasPage,
    title: 'Categorías | PuntoÚtil'
  },

  // Inventario
  {
    path: 'inventario',
    component: InventarioPage,
    title: 'Inventario | PuntoÚtil'
  },

  // Usuarios
  {
    path: 'usuarios',
    component: Usuarios,
    title: 'Usuarios | PuntoÚtil'
  },

  // Ruta desconocida
  {
    path: '**',
    redirectTo: 'login'
  }

];