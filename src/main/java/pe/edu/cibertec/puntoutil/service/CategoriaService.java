package pe.edu.cibertec.puntoutil.service;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.puntoutil.dto.CategoriaRequest;
import pe.edu.cibertec.puntoutil.dto.CategoriaResponse;
import pe.edu.cibertec.puntoutil.entity.Categoria;
import pe.edu.cibertec.puntoutil.repository.CategoriaRepository;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Listar categorias activas
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {

        return categoriaRepository.findByActivaTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Registrar una categoria
    @Transactional
    public CategoriaResponse registrarCategoria(
            CategoriaRequest request) {

        String nombre = request.nombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una categoria con ese nombre"
            );
        }

        Categoria categoria = new Categoria();

        categoria.setNombre(nombre);
        categoria.setActiva(true);

        Categoria guardada = categoriaRepository.save(categoria);

        return convertirAResponse(guardada);
    }

    // Actualizar una categoria
    @Transactional
    public CategoriaResponse actualizarCategoria(
            Long id, CategoriaRequest request) {

        Categoria categoria = categoriaRepository
                .findByIdAndActivaTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));

        String nombre = request.nombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe otra categoria con ese nombre"
                    );
                });

        categoria.setNombre(nombre);

        Categoria actualizada = categoriaRepository.save(categoria);

        return convertirAResponse(actualizada);
    }

    // Desactivar una categoria
    @Transactional
    public void desactivarCategoria(Long id) {

        Categoria categoria = categoriaRepository
                .findByIdAndActivaTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada o ya desactivada"
                ));

        categoria.setActiva(false);

        categoriaRepository.save(categoria);
    }

    // Convertir entidad a DTO
    private CategoriaResponse convertirAResponse(
            Categoria categoria) {

        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre()
        );
    }
}
