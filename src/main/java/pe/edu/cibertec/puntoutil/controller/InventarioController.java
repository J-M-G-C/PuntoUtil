package pe.edu.cibertec.puntoutil.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.cibertec.puntoutil.dto.MovimientoInventarioRequest;
import pe.edu.cibertec.puntoutil.dto.MovimientoInventarioResponse;
import pe.edu.cibertec.puntoutil.service.InventarioService;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(
            InventarioService inventarioService) {

        this.inventarioService = inventarioService;
    }

    // Registrar entradas y salidas
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventarioResponse>
    registrarMovimiento(
            @Valid @RequestBody
            MovimientoInventarioRequest request) {

        MovimientoInventarioResponse movimiento =
                inventarioService.registrarMovimiento(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movimiento);
    }

    // Consultar historial de movimientos
    @GetMapping("/movimientos/producto/{productoId}")
    public List<MovimientoInventarioResponse>
    listarMovimientos(
            @PathVariable Long productoId) {

        return inventarioService
                .listarMovimientosPorProducto(productoId);
    }
}
