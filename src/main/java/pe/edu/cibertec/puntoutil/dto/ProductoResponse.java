package pe.edu.cibertec.puntoutil.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        String codigo,
        String nombre,
        BigDecimal precio
) {
}