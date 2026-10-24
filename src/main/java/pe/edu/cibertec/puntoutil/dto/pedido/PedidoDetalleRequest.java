package pe.edu.cibertec.puntoutil.dto.pedido;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoDetalleRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        @Max(value = 1000000, message = "La cantidad no debe superar 1000000")
        Integer cantidad
) {
}
