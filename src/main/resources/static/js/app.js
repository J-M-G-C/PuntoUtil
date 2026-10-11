'use strict';

const $ = id => document.getElementById(id);
const moneda = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });
const vistas = new Set(['inicio','productos','categorias','clientes','ventas','inventario','usuarios']);
const titulos = {
  inicio: ['PANEL DE CONTROL','Bienvenido a PuntoÚtil 👋'],
  productos: ['CATÁLOGO','Productos'], categorias: ['ORGANIZACIÓN','Categorías'],
  clientes: ['ATENCIÓN COMERCIAL','Clientes'], ventas: ['OPERACIONES','Ventas'],
  inventario: ['EXISTENCIAS','Inventario'], usuarios: ['SEGURIDAD','Usuarios y permisos']
};
let productos = [];
let categorias = [
  { nombre: 'Cuadernos', base: true },
  { nombre: 'Escritura', base: true },
  { nombre: 'Papel', base: true }
];
let clientes = [];
let itemsVenta = [];

function navegar() {
  const fragmento = window.location.hash.slice(1);
  const actual = vistas.has(fragmento) ? fragmento : 'inicio';
  for (const vista of vistas) $("view-" + vista).hidden = vista !== actual;
  document.querySelectorAll('[data-view]').forEach(enlace => {
    const activo = enlace.dataset.view === actual;
    enlace.classList.toggle('selected', activo);
    if (activo) enlace.setAttribute('aria-current','page');
    else enlace.removeAttribute('aria-current');
  });
  $('encabezadoSeccion').textContent = titulos[actual][0];
  $('tituloSeccion').textContent = titulos[actual][1];
  window.scrollTo(0,0);
}

async function obtenerJson(ruta) {
  const respuesta = await fetch(ruta, {credentials:'same-origin'});
  if (respuesta.redirected && respuesta.url.includes('/login')) {
    window.location.assign('/login');
    throw new Error('Se requiere iniciar sesión');
  }
  if (!respuesta.ok) throw new Error(`Error HTTP ${respuesta.status} en ${ruta}`);
  const tipo = respuesta.headers.get('content-type') || '';
  if (!tipo.includes('json')) throw new Error(`La respuesta de ${ruta} no es JSON`);
  return respuesta.json();
}

function celda(fila, texto, clase = '') {
  const td = document.createElement('td');
  if (clase) { const span = document.createElement('span'); span.className = clase; span.textContent = String(texto); td.appendChild(span); }
  else td.textContent = String(texto);
  fila.appendChild(td);
  return td;
}
function accion(fila, texto, onClick) {
  const td = document.createElement('td');
  const boton = document.createElement('button');
  boton.type = 'button'; boton.className = 'text-button'; boton.textContent = texto;
  boton.addEventListener('click', onClick); td.appendChild(boton); fila.appendChild(td);
}
function vacio(tbody, columnas, mensaje) {
  tbody.replaceChildren(); const tr = document.createElement('tr');
  const td = celda(tr, mensaje, 'message'); td.colSpan = columnas; tbody.appendChild(tr);
}

function renderProductos() {
  const termino = $('buscar').value.trim().toLocaleLowerCase('es-PE');
  const filtrados = productos.filter(p => `${p.codigo} ${p.nombre}`.toLocaleLowerCase('es-PE').includes(termino));
  $('contador').textContent = `${filtrados.length} de ${productos.length} productos`;
  const cuerpo = $('listaProductos'); cuerpo.replaceChildren();
  if (!filtrados.length) return vacio(cuerpo,4,'No hay productos que coincidan con la búsqueda.');
  filtrados.forEach(p=> {
    const tr = document.createElement('tr');
    celda(tr,p.codigo,'code-pill'); celda(tr,p.nombre,'product-name');
    celda(tr,moneda.format(Number(p.precio)),'price'); celda(tr,'Demo','state-pill'); cuerpo.appendChild(tr);
  });
}
function renderInventario() {
  const cuerpo = $('listaInventario'); cuerpo.replaceChildren();
  if (!productos.length) return vacio(cuerpo,4,'No hay productos disponibles desde la API.');
  productos.forEach(p => { const tr=document.createElement('tr');
    celda(tr,p.codigo,'code-pill');celda(tr,p.nombre);celda(tr,'No disponible');
    celda(tr,'MySQL pendiente','pending-pill');cuerpo.appendChild(tr); });
}
function renderSelectProductos() {
  const select = $('productoVenta'); select.replaceChildren();
  const inicial=document.createElement('option'); inicial.value=''; inicial.textContent='Selecciona un producto';select.appendChild(inicial);
  productos.forEach(p=> {const option=document.createElement('option');option.value=p.codigo;
    option.textContent=`${p.codigo} — ${p.nombre} (${moneda.format(Number(p.precio))})`; select.appendChild(option);});
}
async function cargarDatos() {
  $('actualizar').disabled=true; $('estadoApi').textContent='Verificando...';
  try {
    const [estado, lista] = await Promise.all([obtenerJson('/api/estado'), obtenerJson('/api/productos')]);
    if (!Array.isArray(lista)) throw new Error('El catálogo no es una lista');
    productos=lista; $('totalProductos').textContent=String(lista.length);
    $('estadoApi').textContent=estado.estado==='ACTIVO'?'Conectado':String(estado.estado || 'Desconocido');
    $('ultimaCarga').textContent=`Actualizado: ${new Date().toLocaleTimeString('es-PE',{hour:'2-digit',minute:'2-digit'})}`;
  } catch(err) {
    console.error('PuntoÚtil:', err); productos=[];
    $('totalProductos').textContent='—';$('estadoApi').textContent='Sin conexión';
    $('ultimaCarga').textContent='No se pudo contactar con Spring Boot';
  } finally {
    $('actualizar').disabled=false;renderProductos();renderInventario();renderSelectProductos();
  }
}
function renderCategorias() {
  const cuerpo=$('listaCategorias');cuerpo.replaceChildren();
  categorias.forEach((c,i)=>{const tr=document.createElement('tr');celda(tr,c.nombre,'product-name');
    celda(tr,c.base?'Ejemplo inicial':'Añadida en navegador');
    if(c.base) celda(tr,'Base de demo');
    else accion(tr,'Eliminar',()=>{categorias.splice(i,1);renderCategorias();});
    cuerpo.appendChild(tr);});
}
function renderClientes() {
  const cuerpo=$('listaClientes'); cuerpo.replaceChildren();
  if(!clientes.length) return vacio(cuerpo,3,'Aún no agregaste clientes ficticios en esta sesión.');
  clientes.forEach((nombre,i)=>{const tr=document.createElement('tr');celda(tr,nombre,'product-name');
    celda(tr,'Solo en navegador','pending-pill');accion(tr,'Eliminar',()=>{clientes.splice(i,1);renderClientes();});cuerpo.appendChild(tr);});
}
function renderVenta() {
  const cuerpo=$('detalleVenta');cuerpo.replaceChildren();
  if(!itemsVenta.length) vacio(cuerpo,4,'Agrega un producto para comenzar.');
  let centavos=0;
  itemsVenta.forEach((item,i)=>{const tr=document.createElement('tr');
    const precioCents=Math.round(Number(item.precio)*100);const sub=precioCents*item.cantidad;
    centavos+=sub;celda(tr,item.nombre,'product-name');celda(tr,item.cantidad);
    celda(tr,moneda.format(sub/100),'price');accion(tr,'Quitar',()=>{itemsVenta.splice(i,1);renderVenta();});cuerpo.appendChild(tr);});
  $('totalVenta').textContent=moneda.format(centavos/100);
}

$('formCategoria').addEventListener('submit', e=>{
  e.preventDefault();const input=$('nombreCategoria');const nombre=input.value.trim();
  if(!nombre)return;
  if(categorias.some(c=>c.nombre.toLocaleLowerCase('es-PE')===nombre.toLocaleLowerCase('es-PE'))) {
    input.setCustomValidity('Ya existe una categoría con ese nombre.'); input.reportValidity();return;
  }
  categorias.push({nombre,base:false});input.value='';renderCategorias();
});
$('nombreCategoria').addEventListener('input',e=>e.target.setCustomValidity(''));
$('formCliente').addEventListener('submit',e=>{e.preventDefault();const input=$('nombreCliente');const valor=input.value.trim();
  if(!valor)return;clientes.push(valor);input.value='';renderClientes();});
$('formVenta').addEventListener('submit',e=>{
  e.preventDefault();const producto=productos.find(p=>p.codigo===$('productoVenta').value);
  const cantidad=Number($('cantidadVenta').value);
  if(!producto || !Number.isInteger(cantidad) || cantidad < 1 || cantidad > 999)return;
  itemsVenta.push({nombre:producto.nombre,precio:producto.precio,cantidad});renderVenta();
});
$('buscar').addEventListener('input',renderProductos);
$('actualizar').addEventListener('click',cargarDatos);
window.addEventListener('hashchange',navegar);
renderCategorias();renderClientes();renderVenta();navegar();cargarDatos();
