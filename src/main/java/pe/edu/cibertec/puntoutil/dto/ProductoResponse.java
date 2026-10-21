package pe.edu.cibertec.puntoutil.dto;

import java.math.BigDecimal;

public record ProductoResponse(

        Long id,
        String codigo,
        String nombre,
        BigDecimal precio,
        Integer stock,
        Integer stockMinimo,
        Long categoriaId,
        String categoriaNombre,
        Boolean activo

) {}
