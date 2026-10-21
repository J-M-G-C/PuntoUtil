package pe.edu.cibertec.puntoutil.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.puntoutil.entity.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    Optional<Categoria> findByIdAndActivaTrue(Long id);

    List<Categoria> findByActivaTrue();
}
