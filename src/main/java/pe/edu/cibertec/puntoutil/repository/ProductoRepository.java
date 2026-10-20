package pe.edu.cibertec.puntoutil.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.puntoutil.entity.Producto;
import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);
}
