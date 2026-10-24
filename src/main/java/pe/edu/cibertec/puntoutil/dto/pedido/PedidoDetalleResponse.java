package pe.edu.cibertec.puntoutil.dto.pedido;

import java.math.BigDecimal;

public record PedidoDetalleResponse(
        Long id,
        Long productoId,
        String codigoProducto,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
