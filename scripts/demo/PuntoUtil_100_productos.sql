-- PuntoUtil - catálogo de DEMOSTRACIÓN: 100 productos nuevos en 12 categorías
-- Ejecutar MANUALMENTE en MySQL Workbench, NO agregar a db/migration de Flyway.
-- Los productos PU de prueba ya se eliminaron previamente en MySQL; esta carga no elimina ni cambia productos existentes.
-- Idempotente para códigos LIB001 a LIB100; si ya existen, los omite.
-- Precios en S/ referenciales ficticios para pruebas; no representan cotizaciones actuales.
-- Stock inicial representa saldo de apertura para esta DEMO; no genera movimiento de inventario.
-- Compatible con tabla categorias(nombre, activa) y productos(codigo, nombre, precio, stock, stock_minimo, categoria_id, activo).
-- Ejecutar SOLO después de comprobar SHOW COLUMNS FROM productos.
-- NO ejecutar si hay ventas o pedidos reales ligados a estos productos: esta carga está pensada para datos DEMO.
SET NAMES utf8mb4;
USE puntoutil_db;

-- Comprobar la estructura antes de la primera ejecución:
-- SHOW COLUMNS FROM categorias;
-- SHOW COLUMNS FROM productos;

START TRANSACTION;

-- Insertar solo las categorías que aún no existen:
INSERT INTO categorias (nombre, activa)
SELECT lista.nombre, TRUE
FROM (
  SELECT 'Cuadernos y libretas' AS nombre
  UNION ALL SELECT 'Lapiceros y bolígrafos'
  UNION ALL SELECT 'Lápices y portaminas'
  UNION ALL SELECT 'Borradores y tajadores'
  UNION ALL SELECT 'Colores y plumones'
  UNION ALL SELECT 'Resaltadores y correctores'
  UNION ALL SELECT 'Papeles y cartulinas'
  UNION ALL SELECT 'Fólderes y archivadores'
  UNION ALL SELECT 'Reglas y geometría'
  UNION ALL SELECT 'Gomas y adhesivos'
  UNION ALL SELECT 'Tijeras y materiales escolares'
  UNION ALL SELECT 'Otros útiles de oficina'
) AS lista
WHERE NOT EXISTS (
  SELECT 1 FROM categorias c WHERE c.nombre = lista.nombre
);

-- Insertar solo códigos nuevos. Los códigos actuales PU... se conservan.
INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria_id, activo)
SELECT semillas.codigo, semillas.nombre, semillas.precio, semillas.stock,
       semillas.stock_minimo, c.id, TRUE
FROM (
  SELECT 'LIB001' AS codigo, 'Cuaderno A4 cuadriculado 100 hojas' AS nombre, 6.90 AS precio, 60 AS stock, 10 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB002' AS codigo, 'Cuaderno A4 rayado 100 hojas' AS nombre, 6.90 AS precio, 45 AS stock, 10 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB003' AS codigo, 'Cuaderno A4 triple raya 100 hojas' AS nombre, 7.20 AS precio, 35 AS stock, 8 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB004' AS codigo, 'Cuaderno A5 cuadriculado 80 hojas' AS nombre, 4.50 AS precio, 50 AS stock, 10 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB005' AS codigo, 'Cuaderno A5 rayado 80 hojas' AS nombre, 4.50 AS precio, 55 AS stock, 10 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB006' AS codigo, 'Cuaderno espiral universitario 200 hojas' AS nombre, 14.90 AS precio, 30 AS stock, 6 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB007' AS codigo, 'Cuaderno empastado A4 100 hojas' AS nombre, 12.50 AS precio, 20 AS stock, 5 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB008' AS codigo, 'Libreta de apuntes A6 60 hojas' AS nombre, 3.00 AS precio, 80 AS stock, 12 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB009' AS codigo, 'Libreta tapa dura A5 96 hojas' AS nombre, 10.90 AS precio, 25 AS stock, 5 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB010' AS codigo, 'Cuaderno de dibujo A4 30 hojas' AS nombre, 6.50 AS precio, 18 AS stock, 5 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB011' AS codigo, 'Cuaderno de caligrafía primaria' AS nombre, 4.90 AS precio, 36 AS stock, 8 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB012' AS codigo, 'Cuaderno de música pentagramado' AS nombre, 6.00 AS precio, 14 AS stock, 5 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB013' AS codigo, 'Block de notas adhesivas 76 x 76 mm' AS nombre, 5.50 AS precio, 40 AS stock, 8 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB014' AS codigo, 'Agenda escolar semanal' AS nombre, 17.90 AS precio, 10 AS stock, 4 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB015' AS codigo, 'Bitácora de apuntes A5' AS nombre, 13.50 AS precio, 5 AS stock, 5 AS stock_minimo, 'Cuadernos y libretas' AS categoria
  UNION ALL SELECT 'LIB016' AS codigo, 'Lapicero azul punta fina 0.7 mm' AS nombre, 1.20 AS precio, 100 AS stock, 20 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB017' AS codigo, 'Lapicero negro punta fina 0.7 mm' AS nombre, 1.20 AS precio, 100 AS stock, 20 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB018' AS codigo, 'Lapicero rojo punta fina 0.7 mm' AS nombre, 1.20 AS precio, 90 AS stock, 20 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB019' AS codigo, 'Bolígrafo azul tinta gel 0.5 mm' AS nombre, 2.80 AS precio, 40 AS stock, 10 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB020' AS codigo, 'Bolígrafo negro tinta gel 0.5 mm' AS nombre, 2.80 AS precio, 40 AS stock, 10 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB021' AS codigo, 'Bolígrafo rojo tinta gel 0.5 mm' AS nombre, 2.80 AS precio, 35 AS stock, 10 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB022' AS codigo, 'Lapicero borrable azul' AS nombre, 5.90 AS precio, 24 AS stock, 6 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB023' AS codigo, 'Lapicero borrable negro' AS nombre, 5.90 AS precio, 22 AS stock, 6 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB024' AS codigo, 'Set de 4 lapiceros de colores' AS nombre, 7.50 AS precio, 14 AS stock, 5 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB025' AS codigo, 'Bolígrafo retráctil azul 1.0 mm' AS nombre, 2.40 AS precio, 36 AS stock, 8 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB026' AS codigo, 'Bolígrafo retráctil negro 1.0 mm' AS nombre, 2.40 AS precio, 30 AS stock, 8 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB027' AS codigo, 'Lapicero multicolor de 4 tintas' AS nombre, 6.50 AS precio, 4 AS stock, 5 AS stock_minimo, 'Lapiceros y bolígrafos' AS categoria
  UNION ALL SELECT 'LIB028' AS codigo, 'Lápiz grafito HB unidad' AS nombre, 0.90 AS precio, 100 AS stock, 20 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB029' AS codigo, 'Lápiz grafito 2B unidad' AS nombre, 1.20 AS precio, 50 AS stock, 10 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB030' AS codigo, 'Lápiz grafito 4B unidad' AS nombre, 1.50 AS precio, 30 AS stock, 8 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB031' AS codigo, 'Lápiz triangular escolar HB' AS nombre, 1.70 AS precio, 40 AS stock, 10 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB032' AS codigo, 'Lápiz bicolor rojo y azul' AS nombre, 1.30 AS precio, 35 AS stock, 8 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB033' AS codigo, 'Portaminas 0.5 mm' AS nombre, 4.50 AS precio, 25 AS stock, 6 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB034' AS codigo, 'Portaminas 0.7 mm' AS nombre, 4.50 AS precio, 25 AS stock, 6 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB035' AS codigo, 'Minas HB 0.5 mm tubo' AS nombre, 2.50 AS precio, 40 AS stock, 10 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB036' AS codigo, 'Minas HB 0.7 mm tubo' AS nombre, 2.50 AS precio, 32 AS stock, 8 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB037' AS codigo, 'Set de lápices de dibujo 2B a 6B' AS nombre, 14.90 AS precio, 8 AS stock, 4 AS stock_minimo, 'Lápices y portaminas' AS categoria
  UNION ALL SELECT 'LIB038' AS codigo, 'Borrador blanco pequeño' AS nombre, 1.00 AS precio, 80 AS stock, 15 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB039' AS codigo, 'Borrador blanco grande' AS nombre, 1.80 AS precio, 45 AS stock, 10 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB040' AS codigo, 'Borrador para lápiz y tinta' AS nombre, 2.50 AS precio, 24 AS stock, 6 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB041' AS codigo, 'Borrador moldeable para dibujo' AS nombre, 4.90 AS precio, 12 AS stock, 4 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB042' AS codigo, 'Tajador plástico simple' AS nombre, 1.20 AS precio, 60 AS stock, 15 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB043' AS codigo, 'Tajador metálico simple' AS nombre, 2.20 AS precio, 30 AS stock, 8 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB044' AS codigo, 'Tajador doble con depósito' AS nombre, 4.50 AS precio, 18 AS stock, 5 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB045' AS codigo, 'Tajador de escritorio con manivela' AS nombre, 24.90 AS precio, 3 AS stock, 4 AS stock_minimo, 'Borradores y tajadores' AS categoria
  UNION ALL SELECT 'LIB046' AS codigo, 'Lápices de colores caja 12 unidades' AS nombre, 9.90 AS precio, 35 AS stock, 8 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB047' AS codigo, 'Lápices de colores caja 24 unidades' AS nombre, 19.90 AS precio, 16 AS stock, 5 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB048' AS codigo, 'Lápices de colores caja 36 unidades' AS nombre, 29.90 AS precio, 9 AS stock, 4 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB049' AS codigo, 'Plumones escolares caja 12 unidades' AS nombre, 11.90 AS precio, 25 AS stock, 6 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB050' AS codigo, 'Plumones escolares caja 24 unidades' AS nombre, 21.90 AS precio, 11 AS stock, 4 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB051' AS codigo, 'Plumones punta fina caja 12 unidades' AS nombre, 13.90 AS precio, 18 AS stock, 5 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB052' AS codigo, 'Plumones punta gruesa caja 12 unidades' AS nombre, 14.90 AS precio, 20 AS stock, 5 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB053' AS codigo, 'Plumón indeleble negro' AS nombre, 3.90 AS precio, 35 AS stock, 8 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB054' AS codigo, 'Plumón indeleble azul' AS nombre, 3.90 AS precio, 35 AS stock, 8 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB055' AS codigo, 'Plumón para pizarra azul' AS nombre, 4.50 AS precio, 0 AS stock, 5 AS stock_minimo, 'Colores y plumones' AS categoria
  UNION ALL SELECT 'LIB056' AS codigo, 'Resaltador amarillo' AS nombre, 3.50 AS precio, 60 AS stock, 10 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB057' AS codigo, 'Resaltador verde' AS nombre, 3.50 AS precio, 30 AS stock, 8 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB058' AS codigo, 'Resaltador rosado' AS nombre, 3.50 AS precio, 32 AS stock, 8 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB059' AS codigo, 'Resaltador naranja' AS nombre, 3.50 AS precio, 30 AS stock, 8 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB060' AS codigo, 'Set de 4 resaltadores pastel' AS nombre, 13.90 AS precio, 12 AS stock, 4 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB061' AS codigo, 'Corrector líquido 20 ml' AS nombre, 4.90 AS precio, 25 AS stock, 6 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB062' AS codigo, 'Corrector en cinta 5 mm x 6 m' AS nombre, 6.50 AS precio, 18 AS stock, 5 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB063' AS codigo, 'Corrector tipo lapicero 7 ml' AS nombre, 5.50 AS precio, 4 AS stock, 5 AS stock_minimo, 'Resaltadores y correctores' AS categoria
  UNION ALL SELECT 'LIB064' AS codigo, 'Papel bond A4 75 g paquete 100 hojas' AS nombre, 8.90 AS precio, 45 AS stock, 10 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB065' AS codigo, 'Papel bond A4 75 g resma 500 hojas' AS nombre, 25.90 AS precio, 30 AS stock, 6 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB066' AS codigo, 'Papel bond oficio paquete 100 hojas' AS nombre, 10.90 AS precio, 18 AS stock, 5 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB067' AS codigo, 'Cartulina escolar blanca unidad' AS nombre, 1.50 AS precio, 80 AS stock, 15 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB068' AS codigo, 'Cartulina escolar de colores unidad' AS nombre, 1.80 AS precio, 70 AS stock, 15 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB069' AS codigo, 'Papel lustre paquete 10 unidades' AS nombre, 4.50 AS precio, 50 AS stock, 10 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB070' AS codigo, 'Papel crepé unidad' AS nombre, 2.00 AS precio, 45 AS stock, 10 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB071' AS codigo, 'Papelógrafo blanco unidad' AS nombre, 1.50 AS precio, 35 AS stock, 8 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB072' AS codigo, 'Papel milimetrado paquete 10 hojas' AS nombre, 4.90 AS precio, 8 AS stock, 5 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB073' AS codigo, 'Cartón dúplex tamaño A3 unidad' AS nombre, 2.50 AS precio, 22 AS stock, 6 AS stock_minimo, 'Papeles y cartulinas' AS categoria
  UNION ALL SELECT 'LIB074' AS codigo, 'Fólder manila A4 unidad' AS nombre, 1.20 AS precio, 100 AS stock, 20 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB075' AS codigo, 'Fólder manila oficio unidad' AS nombre, 1.50 AS precio, 70 AS stock, 15 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB076' AS codigo, 'Fólder plástico A4 con fastener' AS nombre, 3.90 AS precio, 35 AS stock, 8 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB077' AS codigo, 'Fólder plástico A4 con botón' AS nombre, 4.50 AS precio, 35 AS stock, 8 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB078' AS codigo, 'Archivador palanca A4 lomo ancho' AS nombre, 15.90 AS precio, 12 AS stock, 5 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB079' AS codigo, 'Archivador palanca A4 lomo delgado' AS nombre, 13.90 AS precio, 14 AS stock, 5 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB080' AS codigo, 'Separadores de colores A4 10 pestañas' AS nombre, 7.50 AS precio, 3 AS stock, 5 AS stock_minimo, 'Fólderes y archivadores' AS categoria
  UNION ALL SELECT 'LIB081' AS codigo, 'Regla plástica 20 cm' AS nombre, 1.50 AS precio, 60 AS stock, 12 AS stock_minimo, 'Reglas y geometría' AS categoria
  UNION ALL SELECT 'LIB082' AS codigo, 'Regla plástica 30 cm' AS nombre, 2.50 AS precio, 55 AS stock, 10 AS stock_minimo, 'Reglas y geometría' AS categoria
  UNION ALL SELECT 'LIB083' AS codigo, 'Regla metálica 30 cm' AS nombre, 7.50 AS precio, 20 AS stock, 5 AS stock_minimo, 'Reglas y geometría' AS categoria
  UNION ALL SELECT 'LIB084' AS codigo, 'Escuadra escolar 45 grados' AS nombre, 3.50 AS precio, 25 AS stock, 6 AS stock_minimo, 'Reglas y geometría' AS categoria
  UNION ALL SELECT 'LIB085' AS codigo, 'Transportador 180 grados' AS nombre, 2.90 AS precio, 30 AS stock, 6 AS stock_minimo, 'Reglas y geometría' AS categoria
  UNION ALL SELECT 'LIB086' AS codigo, 'Compás escolar con lápiz' AS nombre, 9.90 AS precio, 12 AS stock, 4 AS stock_minimo, 'Reglas y geometría' AS categoria
  UNION ALL SELECT 'LIB087' AS codigo, 'Goma líquida escolar 125 ml' AS nombre, 4.90 AS precio, 45 AS stock, 10 AS stock_minimo, 'Gomas y adhesivos' AS categoria
  UNION ALL SELECT 'LIB088' AS codigo, 'Goma líquida escolar 250 ml' AS nombre, 7.90 AS precio, 25 AS stock, 6 AS stock_minimo, 'Gomas y adhesivos' AS categoria
  UNION ALL SELECT 'LIB089' AS codigo, 'Pegamento en barra 8 g' AS nombre, 3.50 AS precio, 50 AS stock, 10 AS stock_minimo, 'Gomas y adhesivos' AS categoria
  UNION ALL SELECT 'LIB090' AS codigo, 'Pegamento en barra 21 g' AS nombre, 6.90 AS precio, 23 AS stock, 6 AS stock_minimo, 'Gomas y adhesivos' AS categoria
  UNION ALL SELECT 'LIB091' AS codigo, 'Silicona líquida escolar 100 ml' AS nombre, 5.90 AS precio, 30 AS stock, 8 AS stock_minimo, 'Gomas y adhesivos' AS categoria
  UNION ALL SELECT 'LIB092' AS codigo, 'Cinta adhesiva transparente 12 mm' AS nombre, 2.50 AS precio, 4 AS stock, 5 AS stock_minimo, 'Gomas y adhesivos' AS categoria
  UNION ALL SELECT 'LIB093' AS codigo, 'Tijera escolar punta roma 13 cm' AS nombre, 5.90 AS precio, 35 AS stock, 8 AS stock_minimo, 'Tijeras y materiales escolares' AS categoria
  UNION ALL SELECT 'LIB094' AS codigo, 'Tijera multiuso 20 cm' AS nombre, 12.90 AS precio, 18 AS stock, 5 AS stock_minimo, 'Tijeras y materiales escolares' AS categoria
  UNION ALL SELECT 'LIB095' AS codigo, 'Plastilina caja 12 colores' AS nombre, 7.90 AS precio, 30 AS stock, 8 AS stock_minimo, 'Tijeras y materiales escolares' AS categoria
  UNION ALL SELECT 'LIB096' AS codigo, 'Témpera escolar set 6 colores' AS nombre, 11.90 AS precio, 14 AS stock, 5 AS stock_minimo, 'Tijeras y materiales escolares' AS categoria
  UNION ALL SELECT 'LIB097' AS codigo, 'Grapadora metálica pequeña' AS nombre, 17.90 AS precio, 12 AS stock, 4 AS stock_minimo, 'Otros útiles de oficina' AS categoria
  UNION ALL SELECT 'LIB098' AS codigo, 'Caja de grapas 26/6' AS nombre, 4.50 AS precio, 45 AS stock, 10 AS stock_minimo, 'Otros útiles de oficina' AS categoria
  UNION ALL SELECT 'LIB099' AS codigo, 'Perforador de papel 2 huecos' AS nombre, 18.90 AS precio, 0 AS stock, 3 AS stock_minimo, 'Otros útiles de oficina' AS categoria
  UNION ALL SELECT 'LIB100' AS codigo, 'Clips metálicos caja 100 unidades' AS nombre, 3.90 AS precio, 60 AS stock, 12 AS stock_minimo, 'Otros útiles de oficina' AS categoria
) AS semillas
INNER JOIN categorias c ON c.nombre = semillas.categoria
WHERE NOT EXISTS (
  SELECT 1 FROM productos p WHERE p.codigo = semillas.codigo
);

COMMIT;

-- Verificación del catálogo de demostración:
SELECT COUNT(*) AS productos_demo FROM productos WHERE codigo LIKE 'LIB%';
SELECT c.nombre AS categoria, COUNT(*) AS productos_demo
FROM productos p INNER JOIN categorias c ON c.id = p.categoria_id
WHERE p.codigo LIKE 'LIB%'
GROUP BY c.id, c.nombre
ORDER BY c.nombre;
SELECT codigo, nombre, precio, stock, stock_minimo FROM productos WHERE codigo LIKE 'LIB%' ORDER BY codigo LIMIT 15;
