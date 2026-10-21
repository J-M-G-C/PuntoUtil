package pe.edu.cibertec.puntoutil.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.cibertec.puntoutil.dto.CategoriaRequest;
import pe.edu.cibertec.puntoutil.dto.CategoriaResponse;
import pe.edu.cibertec.puntoutil.service.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(
            CategoriaService categoriaService) {

        this.categoriaService = categoriaService;
    }

    // Listar categorias
    @GetMapping
    public List<CategoriaResponse> listarCategorias() {
        return categoriaService.listarCategorias();
    }

    // Registrar categoria
    @PostMapping
    public ResponseEntity<CategoriaResponse> registrarCategoria(
            @Valid @RequestBody CategoriaRequest request) {

        CategoriaResponse categoria =
                categoriaService.registrarCategoria(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoria);
    }

    // Actualizar categoria
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {

        CategoriaResponse categoria =
                categoriaService.actualizarCategoria(id, request);

        return ResponseEntity.ok(categoria);
    }

    // Desactivar categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarCategoria(
            @PathVariable Long id) {

        categoriaService.desactivarCategoria(id);

        return ResponseEntity.noContent().build();
    }
}
