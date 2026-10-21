package pe.edu.cibertec.puntoutil.dto;

import pe.edu.cibertec.puntoutil.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(

        Long id,
        Long productoId,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockNuevo,
        String motivo,
        LocalDateTime fecha

) {}
