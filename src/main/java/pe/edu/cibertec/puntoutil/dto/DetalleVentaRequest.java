package pe.edu.cibertec.puntoutil.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DetalleVentaRequest {

    @NotNull(message = "El ID del producto no puede ser nulo")
    private Long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
}
