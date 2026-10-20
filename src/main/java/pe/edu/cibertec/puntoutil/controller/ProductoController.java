package pe.edu.cibertec.puntoutil.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.cibertec.puntoutil.dto.ProductoRequest;
import pe.edu.cibertec.puntoutil.dto.ProductoResponse;
import pe.edu.cibertec.puntoutil.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> listarProductos() {
        return productoService.listarProductos();
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> registrarProducto(
            @Valid @RequestBody ProductoRequest request) {

        ProductoResponse producto =
                productoService.registrarProducto(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(producto);
    }
}
