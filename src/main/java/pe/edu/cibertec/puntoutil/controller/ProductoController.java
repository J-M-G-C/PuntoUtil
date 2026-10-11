package pe.edu.cibertec.puntoutil.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.cibertec.puntoutil.dto.ProductoResponse;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @GetMapping
    public List<ProductoResponse> listarProductos() {

        return List.of(
                new ProductoResponse(
                        "PU01",
                        "Cuaderno College A4 100 hojas",
                        new BigDecimal("5.20")
                ),
                new ProductoResponse(
                        "PU02",
                        "Bolígrafo Pilot BL-G2-5",
                        new BigDecimal("12.20")
                ),
                new ProductoResponse(
                        "PU03",
                        "Papel Chamex A4 500 hojas",
                        new BigDecimal("15.10")
                )
        );
    }
}