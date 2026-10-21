CREATE TABLE movimientos_inventario (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    producto_id BIGINT NOT NULL,

    tipo VARCHAR(20) NOT NULL,

    cantidad INT NOT NULL,

    stock_anterior INT NOT NULL,

    stock_nuevo INT NOT NULL,

    motivo VARCHAR(255) NOT NULL,

    fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_movimiento_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos(id),

    CONSTRAINT chk_movimiento_tipo
        CHECK (tipo IN ('ENTRADA', 'SALIDA')),

    CONSTRAINT chk_movimiento_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_stock_anterior
        CHECK (stock_anterior >= 0),

    CONSTRAINT chk_stock_nuevo
        CHECK (stock_nuevo >= 0)

);

CREATE INDEX idx_movimiento_producto_fecha
ON movimientos_inventario(producto_id, fecha);
