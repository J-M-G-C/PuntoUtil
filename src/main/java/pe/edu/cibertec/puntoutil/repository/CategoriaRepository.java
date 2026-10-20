package pe.edu.cibertec.puntoutil.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.puntoutil.entity.Categoria;
public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {
}
