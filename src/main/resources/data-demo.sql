INSERT INTO categorias (id, nombre, activa) VALUES (1, 'Cuadernos', TRUE);
INSERT INTO categorias (id, nombre, activa) VALUES (2, 'Escritura', TRUE);

INSERT INTO productos (id, codigo, nombre, precio, stock, stock_minimo, categoria_id, activo)
VALUES (1, 'CUA-100', 'Cuaderno universitario', 12.50, 80, 10, 1, TRUE);
INSERT INTO productos (id, codigo, nombre, precio, stock, stock_minimo, categoria_id, activo)
VALUES (2, 'ESC-200', 'Lapicero azul', 2.50, 150, 20, 2, TRUE);
