package pe.edu.cibertec.puntoutil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import pe.edu.cibertec.puntoutil.entity.TipoMovimiento;

public record MovimientoInventarioRequest(

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El ID debe ser positivo")
        Long productoId,

        @NotNull(message = "El tipo es obligatorio")
        TipoMovimiento tipo,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255)
        String motivo

) {}
