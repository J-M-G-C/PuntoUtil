CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_seguimiento VARCHAR(20) NOT NULL UNIQUE,
    cliente_id BIGINT,
    nombre_cliente VARCHAR(150) NOT NULL,
    dni VARCHAR(15),
    telefono VARCHAR(20),
    correo VARCHAR(160),
    estado VARCHAR(20) NOT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6),
    fecha_entrega DATETIME(6),
    observaciones VARCHAR(255),

    CONSTRAINT chk_pedido_estado CHECK (estado IN ('PENDIENTE', 'LISTO', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id)
);

CREATE TABLE pedido_detalles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_subtotal CHECK (subtotal >= 0)
);

CREATE INDEX idx_pedido_estado ON pedidos(estado);
CREATE INDEX idx_pedido_cliente ON pedidos(cliente_id);
CREATE INDEX idx_pedido_fecha ON pedidos(fecha_creacion);
CREATE INDEX idx_pedido_detalle_pedido ON pedido_detalles(pedido_id);

