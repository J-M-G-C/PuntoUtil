package pe.edu.cibertec.puntoutil.dto.pedido;

import pe.edu.cibertec.puntoutil.entity.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String codigoSeguimiento,
        String nombreCliente,
        String dni,
        String telefono,
        String correo,
        String clienteUsuario,
        EstadoPedido estado,
        BigDecimal total,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaEntrega,
        String observaciones,
        List<PedidoDetalleResponse> detalles
) {
}
