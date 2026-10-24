package pe.edu.cibertec.puntoutil.dto.pedido;

import jakarta.validation.constraints.NotNull;
import pe.edu.cibertec.puntoutil.entity.EstadoPedido;

public record ActualizarEstadoPedidoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado
) {
}
