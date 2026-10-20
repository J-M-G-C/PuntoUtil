package pe.edu.cibertec.puntoutil.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequest(

        @NotBlank
        @Size(max = 30)
        String codigo,

        @NotBlank
        @Size(max = 150)
        String nombre,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal precio,

        @NotNull
        @Min(0)
        Integer stock,

        @NotNull
        @Min(0)
        Integer stockMinimo,

        Long categoriaId
) {
}
