package pe.edu.cibertec.puntoutil.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.cibertec.puntoutil.dto.EstadoResponse;

@RestController
@RequestMapping("/api")
public class EstadoController {

    @GetMapping("/estado")
    public EstadoResponse obtenerEstado() {
        return new EstadoResponse(
                "PuntoÚtil",
                "ACTIVO",
                "Backend de PuntoÚtil funcionando correctamente"
        );
    }
}